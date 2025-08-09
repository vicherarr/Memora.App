# Plan de Persistencia de Archivos - MemoraApp

## 🎯 Problemática Identificada

**Situación Actual**: La app almacena rutas de archivos originales (`filePath`) en la base de datos, lo que genera:
- ❌ Enlaces rotos si el usuario mueve/borra archivos originales
- ❌ Dependencia de permisos de storage externo
- ❌ Archivos inaccesibles tras actualizaciones del sistema
- ❌ Inconsistencias entre plataformas (Android/iOS)

## 🎯 Diseño de Solución: App-Owned File Storage

### **Arquitectura Propuesta**

#### 1. **FileStorageManager** (Expect/Actual Pattern)
```kotlin
expect class FileStorageManager {
    suspend fun copyFileToAppStorage(
        sourceData: Any, // ByteArray o String (path)
        originalFileName: String,
        fileType: MediaType
    ): Result<String> // Returns new app-owned file path
    
    suspend fun deleteFileFromAppStorage(filePath: String): Result<Unit>
    fun getAttachmentsDirectory(): String
    fun generateUniqueFileName(originalName: String, extension: String): String
    fun getFileSize(filePath: String): Long
    fun getAvailableStorageSpace(): Long
    fun isExternalStorageAvailable(): Boolean
}
```

#### 2. **Estrategia de Almacenamiento (Prioridad)**

##### **Android**:
```kotlin
1. **PRIORIDAD 1**: External App-Specific Directory (No requiere permisos)
   /Android/data/com.vicherarr.memora/files/attachments/
   
2. **PRIORIDAD 2**: Internal App-Specific Directory (Solo si no hay externo)
   /data/data/com.vicherarr.memora/files/attachments/

Ventajas:
✅ No requiere permisos WRITE_EXTERNAL_STORAGE
✅ Se limpia automáticamente al desinstalar app
✅ Acceso exclusivo de la app
✅ Compatible con Android 10+ scoped storage
```

##### **iOS**:
```kotlin
1. **PRIORIDAD 1**: Documents Directory (Respaldado por iCloud)
   Documents/attachments/
   
2. **PRIORIDAD 2**: Library/Application Support (Local only)
   Library/Application Support/attachments/

Ventajas:
✅ Documents se respalda automáticamente
✅ Acceso exclusivo de la app
✅ Gestión automática de espacio por iOS
```

#### 3. **Estructura de Directorios**
```
Android External: /Android/data/com.vicherarr.memora/files/attachments/
Android Internal: /data/data/com.vicherarr.memora/files/attachments/
├── images/
│   ├── IMG_20250109_001_abc123.jpg
│   └── IMG_20250109_002_def456.png
└── videos/
    ├── VID_20250109_001_ghi789.mp4
    └── VID_20250109_002_jkl012.mov

iOS Documents: Documents/attachments/
iOS App Support: Library/Application Support/attachments/
├── images/
└── videos/
```

#### 4. **Flujo de Trabajo**

##### **Al Agregar Media:**
1. Usuario selecciona imagen/video desde galería/cámara
2. **MediaViewModel** → `FileStorageManager.copyFileToAppStorage()`
3. **FileStorageManager** determina mejor ubicación de storage disponible
4. Archivo se copia a carpeta de la app con nombre único
5. `MediaFile.data` almacena la **nueva ruta (app-owned)**
6. Referencia original se descarta

##### **Al Guardar Nota:**
1. `NotesRepository` recibe MediaFiles con rutas app-owned
2. Crea `ArchivoAdjunto` con `filePath` apuntando a carpeta de la app
3. Archivos ya están seguros en nuestra carpeta

##### **Al Eliminar Attachment Individual:**
1. `NoteDetailViewModel.removeAttachment()` 
2. `FileStorageManager.deleteFileFromAppStorage(attachment.filePath)`
3. Eliminar registro de BD

##### **Al Eliminar Nota Completa:**
1. `NotesRepository.deleteNote()`
2. Para cada `attachment` en `note.archivosAdjuntos`:
   - `FileStorageManager.deleteFileFromAppStorage(attachment.filePath)`
3. Eliminar nota de BD

### **5. Implementaciones Platform-Specific**

#### **Android (FileStorageManager.android.kt)**
```kotlin
actual class FileStorageManager(private val context: Context) {
    
    private fun getOptimalStorageDirectory(): File {
        return when {
            // Prioridad 1: External app-specific directory (Android/data/package/files)
            isExternalStorageAvailable() -> {
                context.getExternalFilesDir(null)?.let { externalDir ->
                    File(externalDir, "attachments").also {
                        it.mkdirs()
                        println("Using external storage: ${it.absolutePath}")
                    }
                }
            }
            // Prioridad 2: Internal app-specific directory (fallback)
            else -> {
                File(context.filesDir, "attachments").also {
                    it.mkdirs()
                    println("Using internal storage: ${it.absolutePath}")
                }
            }
        } ?: File(context.filesDir, "attachments").also { it.mkdirs() }
    }
    
    actual fun isExternalStorageAvailable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED &&
                context.getExternalFilesDir(null) != null
    }
    
    actual fun getAvailableStorageSpace(): Long {
        val storageDir = getOptimalStorageDirectory()
        return storageDir.freeSpace
    }
    
    actual suspend fun copyFileToAppStorage(
        sourceData: Any,
        originalFileName: String, 
        fileType: MediaType
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val attachmentsDir = getOptimalStorageDirectory()
            val subDir = if (fileType == MediaType.IMAGE) "images" else "videos"
            val targetDir = File(attachmentsDir, subDir).apply { mkdirs() }
            
            // Check available space before copying
            val estimatedSize = when (sourceData) {
                is ByteArray -> sourceData.size.toLong()
                is String -> File(sourceData).length()
                else -> 0L
            }
            
            if (getAvailableStorageSpace() < estimatedSize + 50_000_000) { // 50MB buffer
                return@withContext Result.failure(
                    Exception("Espacio de almacenamiento insuficiente")
                )
            }
            
            val uniqueName = generateUniqueFileName(originalFileName, getExtension(originalFileName))
            val targetFile = File(targetDir, uniqueName)
            
            when (sourceData) {
                is ByteArray -> targetFile.writeBytes(sourceData)
                is String -> File(sourceData).copyTo(targetFile, overwrite = false)
                else -> throw IllegalArgumentException("Unsupported source data type")
            }
            
            println("File copied to: ${targetFile.absolutePath}")
            Result.success(targetFile.absolutePath)
        } catch (e: Exception) {
            println("Error copying file: ${e.message}")
            Result.failure(e)
        }
    }
    
    actual suspend fun deleteFileFromAppStorage(filePath: String): Result<Unit> = 
        withContext(Dispatchers.IO) {
            try {
                val file = File(filePath)
                if (file.exists() && file.delete()) {
                    println("File deleted: $filePath")
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("No se pudo eliminar el archivo: $filePath"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
```

#### **iOS (FileStorageManager.ios.kt)**
```kotlin
@OptIn(ExperimentalForeignApi::class)
actual class FileStorageManager {
    
    private fun getOptimalStorageDirectory(): String {
        return when {
            // Prioridad 1: Documents directory (respaldado por iCloud)
            getAvailableStorageSpace() > 100_000_000 -> { // 100MB mínimo
                val documentsPath = NSSearchPathForDirectoriesInDomains(
                    NSDocumentDirectory, NSUserDomainMask, true
                ).first() as String
                "$documentsPath/attachments"
            }
            // Prioridad 2: Application Support directory (solo local)
            else -> {
                val appSupportPath = NSSearchPathForDirectoriesInDomains(
                    NSApplicationSupportDirectory, NSUserDomainMask, true
                ).first() as String
                "$appSupportPath/attachments"
            }
        }
    }
    
    actual fun isExternalStorageAvailable(): Boolean {
        // En iOS, siempre hay "storage disponible" (Documents o App Support)
        return true
    }
    
    actual fun getAvailableStorageSpace(): Long {
        val fileManager = NSFileManager.defaultManager
        val documentsPath = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory, NSUserDomainMask, true
        ).first() as String
        
        val attributes = fileManager.attributesOfFileSystemForPath(documentsPath, error = null)
        return (attributes?.get(NSFileSystemFreeSize) as? NSNumber)?.longValue ?: 0L
    }
    
    actual suspend fun copyFileToAppStorage(
        sourceData: Any,
        originalFileName: String,
        fileType: MediaType  
    ): Result<String> = withContext(Dispatchers.Default) {
        try {
            val attachmentsPath = getOptimalStorageDirectory()
            val subDir = if (fileType == MediaType.IMAGE) "images" else "videos"
            val targetPath = "$attachmentsPath/$subDir"
            
            // Create directory if needed
            NSFileManager.defaultManager.createDirectoryAtPath(
                targetPath, 
                withIntermediateDirectories = true, 
                attributes = null, 
                error = null
            )
            
            // Check available space
            val estimatedSize = when (sourceData) {
                is ByteArray -> sourceData.size.toLong()
                is String -> {
                    val attrs = NSFileManager.defaultManager.attributesOfItemAtPath(sourceData, error = null)
                    (attrs?.get(NSFileSize) as? NSNumber)?.longValue ?: 0L
                }
                else -> 0L
            }
            
            if (getAvailableStorageSpace() < estimatedSize + 50_000_000) { // 50MB buffer
                return@withContext Result.failure(
                    Exception("Espacio de almacenamiento insuficiente")
                )
            }
            
            val uniqueName = generateUniqueFileName(originalFileName, getExtension(originalFileName))
            val finalPath = "$targetPath/$uniqueName"
            
            when (sourceData) {
                is ByteArray -> {
                    val nsData = sourceData.usePinned { pinned ->
                        NSData.create(bytes = pinned.addressOf(0), length = sourceData.size.toULong())
                    }
                    if (!nsData.writeToFile(finalPath, atomically = true)) {
                        throw Exception("Failed to write ByteArray to file")
                    }
                }
                is String -> {
                    if (!NSFileManager.defaultManager.copyItemAtPath(sourceData, toPath = finalPath, error = null)) {
                        throw Exception("Failed to copy file from source path")
                    }
                }
                else -> throw IllegalArgumentException("Unsupported source data type")
            }
            
            println("File copied to: $finalPath")
            Result.success(finalPath)
        } catch (e: Exception) {
            println("Error copying file: ${e.message}")
            Result.failure(e)
        }
    }
}
```

### **6. Modificaciones en Componentes Existentes**

#### **MediaViewModel**
```kotlin
class MediaViewModel(
    private val fileStorageManager: FileStorageManager
) : ViewModel() {
    
    suspend fun addToSelectedMedia(tempMediaFile: MediaFile) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessingMedia = true)
            
            // Check available space first
            val availableSpace = fileStorageManager.getAvailableStorageSpace()
            val estimatedFileSize = when (tempMediaFile.data) {
                is ByteArray -> (tempMediaFile.data as ByteArray).size.toLong()
                is String -> File(tempMediaFile.data as String).length()
                else -> 0L
            }
            
            if (availableSpace < estimatedFileSize + 100_000_000) { // 100MB buffer
                _uiState.value = _uiState.value.copy(
                    isProcessingMedia = false,
                    errorMessage = "Espacio de almacenamiento insuficiente. Se necesitan al menos 100MB libres."
                )
                return@launch
            }
            
            val result = fileStorageManager.copyFileToAppStorage(
                sourceData = tempMediaFile.data,
                originalFileName = tempMediaFile.fileName,
                fileType = tempMediaFile.type
            )
            
            result.onSuccess { appOwnedPath ->
                // REEMPLAZAR data con app-owned path INMEDIATAMENTE
                val finalMediaFile = tempMediaFile.copy(data = appOwnedPath)
                _uiState.value = _uiState.value.copy(
                    selectedMedia = _uiState.value.selectedMedia + finalMediaFile,
                    isProcessingMedia = false
                )
                
                // Optional: Delete original temp file
                if (tempMediaFile.data is String && (tempMediaFile.data as String).contains("temp")) {
                    try {
                        File(tempMediaFile.data as String).delete()
                    } catch (e: Exception) {
                        println("Could not delete temp file: ${e.message}")
                    }
                }
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isProcessingMedia = false,
                    errorMessage = "Error al procesar archivo: ${error.message}"
                )
            }
        }
    }
}
```

#### **NotesRepository**
```kotlin
suspend fun deleteNote(noteId: String): Result<Unit> {
    return try {
        val note = notesDao.getNoteById(noteId)
        
        // Delete all associated files first
        note?.archivosAdjuntos?.forEach { attachment ->
            fileStorageManager.deleteFileFromAppStorage(attachment.filePath)
                .onFailure { error ->
                    println("Could not delete attachment file: ${attachment.filePath}, error: ${error.message}")
                }
        }
        
        // Then delete from database
        notesDao.deleteNote(noteId)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

## ✅ **Principio Fundamental: App-Owned Files Only**

### **Flujo de Datos Actualizado:**

```
1. Usuario selecciona imagen → MediaFile(data: originalPath/ByteArray)
2. MediaViewModel.addToSelectedMedia() → 
   - Check available storage space
   - FileStorageManager.copyFileToAppStorage() (external preferred)
   - MediaFile.data = newAppOwnedPath (SIEMPRE)
3. UI siempre usa MediaFile.data (que ya es app-owned path)
4. Al guardar nota → ArchivoAdjunto.filePath = appOwnedPath
5. Al mostrar imagen → AsyncImage(model: attachment.filePath) // app-owned
```

### **7. Consideraciones de Almacenamiento**

#### **Ventajas del External App-Specific Storage:**
✅ **Más Espacio**: Normalmente varios GB disponibles  
✅ **Sin Permisos**: No requiere WRITE_EXTERNAL_STORAGE  
✅ **Auto-cleanup**: Se borra al desinstalar la app  
✅ **Scoped Storage**: Compatible con Android 10+  
✅ **Performance**: Mejor I/O que storage interno  

#### **Fallback a Internal Storage:**
⚠️ **Espacio Limitado**: Normalmente < 1GB  
⚠️ **Performance**: Más lento que external  
⚠️ **Solo Emergencia**: Usar solo si no hay external  

### **8. Estrategias de Optimización de Espacio**

#### **Compresión de Imágenes**
```kotlin
// Android
fun compressImageIfNeeded(sourceFile: File, maxSizeBytes: Long = 2_000_000): ByteArray {
    val bitmap = BitmapFactory.decodeFile(sourceFile.absolutePath)
    var quality = 90
    
    do {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        quality -= 10
    } while (stream.toByteArray().size > maxSizeBytes && quality > 10)
    
    return stream.toByteArray()
}
```

#### **Limpieza de Archivos Huérfanos**
```kotlin
suspend fun cleanupOrphanedFiles() {
    val allAttachmentPaths = notesDao.getAllAttachmentPaths()
    val attachmentsDir = File(getAttachmentsDirectory())
    
    attachmentsDir.walkTopDown().forEach { file ->
        if (file.isFile && file.absolutePath !in allAttachmentPaths) {
            file.delete()
            println("Deleted orphaned file: ${file.absolutePath}")
        }
    }
}
```

### **9. Ventajas de esta Solución**

✅ **Robustez**: Archivos siempre disponibles aunque se borren originales  
✅ **Control Total**: Gestión completa del ciclo de vida de archivos  
✅ **Espacio Optimizado**: Prioriza external storage con más capacidad  
✅ **Limpieza Automática**: No archivos huérfanos al borrar notas  
✅ **Seguridad**: Files están en sandboxed app directory  
✅ **Performance**: Mejor I/O con external storage  
✅ **Sin Permisos**: No requiere permisos especiales  
✅ **Compatibilidad**: Funciona con Android 10+ scoped storage  

### **10. Consideraciones y Limitaciones**

⚠️ **Duplicación**: Archivos se duplican (trade-off necesario)  
⚠️ **Espacio**: Monitoreo constante de espacio disponible  
⚠️ **I/O Performance**: Copiar archivos grandes puede ser lento  
⚠️ **Error Handling**: Manejar fallos de copia/eliminación robustamente  

### **11. Plan de Implementación**

1. **Fase 1**: Crear `FileStorageManager` con lógica de storage selection
2. **Fase 2**: Modificar `MediaViewModel` para usar file copying con space checking
3. **Fase 3**: Actualizar `NotesRepository` para cleanup en delete  
4. **Fase 4**: Añadir utilidades de mantenimiento (cleanup, compression)

---

## 📋 Estado de Implementación

- [ ] Fase 1: FileStorageManager con storage selection
- [ ] Fase 2: MediaViewModel integration con space checking
- [ ] Fase 3: NotesRepository cleanup
- [ ] Fase 4: Maintenance utilities

**Proyecto**: En fase de desarrollo inicial - no requiere migración de datos  
**Fecha de creación**: 9 enero 2025  
**Última actualización**: 9 enero 2025