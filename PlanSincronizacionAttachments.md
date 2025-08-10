# Plan Detallado: Sincronización de Attachments con Google Drive

## 📅 Fecha: 9 Agosto 2025

---

## 🎯 **OBJETIVO**

Implementar sincronización completa de archivos multimedia (imágenes/videos) entre dispositivos usando Google Drive AppDataFolder, con consistencia de rutas y nombres únicos universales.

---

## 🏗️ **ARQUITECTURA PROPUESTA**

### **1. ESTRUCTURA DE NAMING - IDENTIFICADORES ÚNICOS UNIVERSALES**

#### **🔑 Attachment ID Format**
```
{noteId}_{attachmentIndex}_{timestamp}_{hash}
```

**Ejemplo**:
```
note_1725897234567_0_1725897234891_a7f8d2c4
note_1725897234567_1_1725897234892_b8e9c3d5
```

**Componentes**:
- `noteId`: ID de la nota padre (garantiza relación)
- `attachmentIndex`: Índice secuencial dentro de la nota (0, 1, 2...)
- `timestamp`: Momento de creación (garantiza unicidad temporal)
- `hash`: Hash corto de primeros 8 chars del contenido del archivo (garantiza unicidad de contenido)

#### **📁 File Naming Convention**
```
{attachmentId}.{extension}
```

**Ejemplos**:
```
note_1725897234567_0_1725897234891_a7f8d2c4.jpg
note_1725897234567_1_1725897234892_b8e9c3d5.mp4
note_1725897234567_2_1725897234893_c9f0e4f6.png
```

### **2. ESTRUCTURA DE PATHS - CONSISTENCIA MULTIPLATAFORMA**

#### **📱 Local Storage Structure**
```
{APP_CACHE_DIR}/memora_attachments/{noteId}/
├── note_1725897234567_0_1725897234891_a7f8d2c4.jpg
├── note_1725897234567_1_1725897234892_b8e9c3d5.mp4
└── note_1725897234567_2_1725897234893_c9f0e4f6.png
```

**Por Plataforma**:
- **Android**: `/data/data/com.vicherarr.memora/cache/memora_attachments/{noteId}/`
- **iOS**: `{Documents}/memora_attachments/{noteId}/`

#### **☁️ Google Drive Structure (AppDataFolder)**
```
/appDataFolder/attachments/{noteId}/
├── note_1725897234567_0_1725897234891_a7f8d2c4.jpg
├── note_1725897234567_1_1725897234892_b8e9c3d5.mp4
└── note_1725897234567_2_1725897234893_c9f0e4f6.png
```

### **3. METADATA TRACKING - SYNC STATUS**

#### **📋 Enhanced Attachments Schema**
```sql
CREATE TABLE attachments (
    -- Existing fields
    id TEXT NOT NULL PRIMARY KEY,
    file_path TEXT NOT NULL,
    remote_url TEXT,
    nombre_original TEXT NOT NULL,
    tipo_archivo INTEGER NOT NULL,
    tipo_mime TEXT NOT NULL,
    tamano_bytes INTEGER NOT NULL,
    fecha_subida TEXT NOT NULL,
    nota_id TEXT NOT NULL,
    
    -- Enhanced sync management
    sync_status TEXT NOT NULL DEFAULT 'PENDING',     -- PENDING, UPLOADING, SYNCED, FAILED, DELETED
    needs_upload INTEGER NOT NULL DEFAULT 1,         -- 0=no, 1=yes
    local_created_at INTEGER NOT NULL,
    last_sync_attempt INTEGER,
    
    -- New sync fields
    remote_path TEXT,                                 -- Path in Google Drive
    remote_file_id TEXT,                             -- Google Drive file ID
    content_hash TEXT,                               -- Hash for integrity verification
    download_status TEXT DEFAULT 'NONE',            -- NONE, DOWNLOADING, DOWNLOADED, FAILED
    is_cached_locally INTEGER NOT NULL DEFAULT 1,   -- 0=not cached, 1=cached
    
    FOREIGN KEY (nota_id) REFERENCES notes(id) ON DELETE CASCADE
);
```

---

## 🔄 **ESTRATEGIA DE SINCRONIZACIÓN**

### **FASE 1: UPLOAD (Local → Google Drive)**

#### **🚀 Upload Process Flow - CON TRANSICIÓN DE PATHS**
```
1. Detectar attachments con sync_status='PENDING'
2. Para cada attachment:
   a. Leer archivo desde path ORIGINAL (ej: /cache/images/photo_12345.jpg)
   b. Calcular content_hash del contenido
   c. Generar nuevo attachment_id con format estándar
   d. Verificar si ya existe en Drive (por remote_path)
   e. Crear estructura de carpetas en Drive si no existe
   f. Subir archivo con nombre único estructurado
   g. PASO CRÍTICO: Descargar archivo desde Drive a path estructurado local
   h. Actualizar attachment en BD local:
      - file_path: NUEVO path estructurado
      - remote_file_id: ID de Google Drive  
      - remote_path: Path remoto estructurado
      - content_hash: Hash calculado
      - sync_status: 'SYNCED'
   i. Eliminar archivo original (opcional para ahorrar espacio)
```

#### **📤 Upload Implementation Strategy - CON TRANSICIÓN DE PATHS**
```kotlin
suspend fun uploadAttachment(attachment: Attachments): Result<String> {
    // 1. Read file data from ORIGINAL path
    val originalFileData = fileManager.getFile(attachment.file_path) ?: return Result.failure(...)
    
    // 2. Calculate content hash for integrity
    val contentHash = calculateSHA256Hash(originalFileData)
    
    // 3. Generate structured attachment ID (if not already structured)
    val structuredAttachmentId = if (isOriginalPath(attachment.file_path)) {
        generateStructuredAttachmentId(attachment.nota_id, attachment.id, contentHash)
    } else {
        attachment.id // Already structured
    }
    
    // 4. Build structured remote path
    val extension = getExtension(attachment.nombre_original)
    val remotePath = "attachments/${attachment.nota_id}/${structuredAttachmentId}.${extension}"
    
    // 5. Check if file already exists in Drive
    val existingFile = driveService.findFileByPath(remotePath)
    if (existingFile != null) {
        // File already exists, just transition to structured path locally
        return transitionToStructuredPath(attachment, existingFile.id, remotePath, contentHash)
    }
    
    // 6. Create folder structure if needed
    val noteFolder = createNoteFolderIfNeeded(attachment.nota_id)
    
    // 7. Upload file with structured name
    val fileMetadata = File()
        .setName("${structuredAttachmentId}.${extension}")
        .setParents(listOf(noteFolder.id))
    
    val mediaContent = ByteArrayContent(attachment.tipo_mime, originalFileData)
    val uploadedFile = driveService.files().create(fileMetadata, mediaContent).execute()
    
    // 8. CRITICAL: Download file back to structured local path
    val structuredLocalPath = buildStructuredLocalPath(attachment.nota_id, structuredAttachmentId, extension)
    val downloadedData = driveService.files().get(uploadedFile.id).executeMediaAsInputStream().readBytes()
    
    // 9. Save to structured path (overwrite if exists)
    fileManager.createDirectoriesIfNeeded(structuredLocalPath)
    fileManager.saveFileToPath(downloadedData, structuredLocalPath, overwrite = true)
    
    // 10. Update local database with NEW structured path
    attachmentsDao.updateToStructuredPath(
        attachmentId = attachment.id,
        newAttachmentId = structuredAttachmentId,
        newFilePath = structuredLocalPath,
        remoteFileId = uploadedFile.id,
        remotePath = remotePath,
        contentHash = contentHash,
        syncStatus = "SYNCED"
    )
    
    // 11. Clean up original file (optional - save storage)
    fileManager.deleteFile(attachment.file_path)
    
    return Result.success(uploadedFile.id)
}

private suspend fun transitionToStructuredPath(
    attachment: Attachments, 
    remoteFileId: String, 
    remotePath: String, 
    contentHash: String
): Result<String> {
    // File exists in Drive, just download to structured path locally
    val structuredLocalPath = buildStructuredLocalPath(...)
    val fileData = driveService.files().get(remoteFileId).executeMediaAsInputStream().readBytes()
    
    fileManager.createDirectoriesIfNeeded(structuredLocalPath)
    fileManager.saveFileToPath(fileData, structuredLocalPath, overwrite = true)
    
    attachmentsDao.updateToStructuredPath(
        attachmentId = attachment.id,
        newFilePath = structuredLocalPath,
        remoteFileId = remoteFileId,
        remotePath = remotePath,
        contentHash = contentHash,
        syncStatus = "SYNCED"
    )
    
    // Clean up original
    fileManager.deleteFile(attachment.file_path)
    
    return Result.success(remoteFileId)
}
```

### **FASE 2: DOWNLOAD (Google Drive → Local)**

#### **📥 Download Process Flow**
```
1. Al recibir metadata de notas sincronizadas
2. Para cada attachment en metadata remota:
   a. Verificar si existe localmente (por attachment.id)
   b. Si no existe o hash difiere:
      - Descargar archivo desde Drive
      - Guardar en path local estructurado
      - Crear/actualizar registro en attachments table
      - Marcar como download_status='DOWNLOADED'
```

#### **📥 Download Implementation Strategy**
```kotlin
suspend fun downloadAttachment(remoteAttachmentMetadata: RemoteAttachment): Result<String> {
    // 1. Check if already downloaded and up-to-date
    val localAttachment = attachmentsDao.getAttachmentById(remoteAttachmentMetadata.id)
    if (localAttachment?.content_hash == remoteAttachmentMetadata.hash && 
        localAttachment.is_cached_locally == 1L) {
        return Result.success(localAttachment.file_path) // Already up-to-date
    }
    
    // 2. Download file from Drive
    val fileData = driveService.files().get(remoteAttachmentMetadata.driveFileId)
        .executeMediaAsInputStream()
        .readBytes()
    
    // 3. Verify integrity
    val downloadedHash = calculateSHA256Hash(fileData)
    if (downloadedHash != remoteAttachmentMetadata.hash) {
        return Result.failure(Exception("Hash verification failed"))
    }
    
    // 4. Save to local structured path
    val localPath = buildLocalAttachmentPath(
        noteId = remoteAttachmentMetadata.noteId,
        attachmentId = remoteAttachmentMetadata.id,
        originalName = remoteAttachmentMetadata.originalName
    )
    
    fileManager.saveFileToPath(fileData, localPath)
    
    // 5. Update/create local database record
    if (localAttachment != null) {
        attachmentsDao.updateDownloadedMetadata(...)
    } else {
        attachmentsDao.insertDownloadedAttachment(...)
    }
    
    return Result.success(localPath)
}
```

### **FASE 3: CONFLICT RESOLUTION**

#### **⚖️ Conflict Detection & Resolution**
```
Conflictos Posibles:
1. Archivo modificado localmente vs remotamente
2. Archivo eliminado localmente pero existe remotamente
3. Archivo eliminado remotamente pero existe localmente

Estrategia de Resolución:
- KEEP_NEWEST: Basado en fecha de modificación
- KEEP_REMOTE: Priorizar versión remota
- KEEP_LOCAL: Priorizar versión local
- MANUAL: Solicitar decisión del usuario
```

---

## 🛠️ **IMPLEMENTACIÓN TÉCNICA**

### **COMPONENTE 1: Enhanced CloudStorageProvider**

```kotlin
// Extensiones necesarias para CloudStorageProvider
interface CloudStorageProvider {
    // ... métodos existentes ...
    
    // Nuevos métodos para attachments
    suspend fun uploadAttachment(attachmentId: String, data: ByteArray, mimeType: String, notePath: String): String
    suspend fun downloadAttachment(remoteFileId: String): ByteArray?
    suspend fun deleteAttachment(remoteFileId: String): Boolean
    suspend fun listAttachments(noteId: String): List<RemoteAttachmentInfo>
    suspend fun createAttachmentFolder(noteId: String): String
}

data class RemoteAttachmentInfo(
    val fileId: String,
    val fileName: String,
    val size: Long,
    val modifiedTime: Long,
    val path: String
)
```

### **COMPONENTE 2: AttachmentSyncService**

```kotlin
class AttachmentSyncService(
    private val attachmentsDao: AttachmentsDao,
    private val cloudStorageProvider: CloudStorageProvider,
    private val fileManager: FileManager,
    private val hashCalculator: HashCalculator
) {
    
    suspend fun syncAttachmentsForNote(noteId: String): SyncResult
    suspend fun uploadPendingAttachments(): SyncResult
    suspend fun downloadMissingAttachments(noteId: String): SyncResult
    suspend fun resolveAttachmentConflicts(conflicts: List<AttachmentConflict>): SyncResult
    
    private suspend fun buildUniqueAttachmentId(noteId: String, index: Int, fileData: ByteArray): String
    private suspend fun calculateFileHash(data: ByteArray): String
    private fun buildLocalAttachmentPath(noteId: String, attachmentId: String, originalName: String): String
    private fun buildRemoteAttachmentPath(noteId: String, attachmentId: String, originalName: String): String
}
```

### **COMPONENTE 3: Enhanced AttachmentsDao**

```kotlin
class AttachmentsDao {
    // ... métodos existentes ...
    
    // Nuevos métodos para sync de attachments
    suspend fun getAttachmentsNeedingUpload(): List<Attachments>
    suspend fun getAttachmentsNeedingDownload(): List<Attachments>
    suspend fun updateSyncMetadata(attachmentId: String, remoteFileId: String, remotePath: String, contentHash: String, syncStatus: String)
    suspend fun markAsUploaded(attachmentId: String, remoteFileId: String, remotePath: String)
    suspend fun markAsDownloaded(attachmentId: String, localPath: String, contentHash: String)
    suspend fun getAttachmentsByContentHash(hash: String): List<Attachments>
    suspend fun updateCacheStatus(attachmentId: String, isCached: Boolean)
}
```

### **COMPONENTE 4: Path Management Utilities**

```kotlin
object AttachmentPathManager {
    
    fun generateAttachmentId(noteId: String, index: Int, timestamp: Long, contentHash: String): String {
        return "${noteId}_${index}_${timestamp}_${contentHash.take(8)}"
    }
    
    fun buildLocalAttachmentPath(noteId: String, attachmentId: String, extension: String): String {
        return "${getLocalAttachmentsDir()}/memora_attachments/${noteId}/${attachmentId}.${extension}"
    }
    
    fun buildRemoteAttachmentPath(noteId: String, attachmentId: String, extension: String): String {
        return "attachments/${noteId}/${attachmentId}.${extension}"
    }
    
    fun extractNoteIdFromPath(path: String): String? {
        // Extract noteId from path structure
    }
    
    fun extractAttachmentIdFromFileName(fileName: String): String? {
        // Extract attachmentId from filename
    }
    
    private expect fun getLocalAttachmentsDir(): String // Platform-specific
}
```

---

## 📊 **FLUJO COMPLETO DE SINCRONIZACIÓN**

### **🔄 Sync Flow Diagram - CON TRANSICIÓN DE PATHS**
```
CREAR NOTA CON ATTACHMENTS (PRIMERA VEZ):
1. Usuario adjunta archivo → FileManager.saveFile() con path ORIGINAL
   Ej: /cache/images/IMG_20250809_143022.jpg
2. AttachmentsDao.insertAttachment() con:
   - file_path: path ORIGINAL
   - sync_status: 'PENDING'
3. UI funciona INMEDIATAMENTE con archivo original
4. Background: AttachmentSyncService detecta PENDING
5. PROCESO DE TRANSICIÓN:
   a. Leer archivo desde path original
   b. Generar attachment_id estructurado
   c. Upload a Google Drive con nombre estructurado
   d. Download desde Drive a path estructurado local
   e. Actualizar BD local:
      - file_path: NUEVO path estructurado  
      - remote_file_id: ID de Drive
      - sync_status: 'SYNCED'
   f. Eliminar archivo original
6. UI sigue funcionando (path actualizado transparentemente)

SINCRONIZAR DESDE OTRO DISPOSITIVO:
1. DatabaseSyncService descarga metadata de notas
2. Para cada nota con attachments:
   a. Verificar attachments locales vs remotos
   b. Descargar attachments faltantes con path estructurado
   c. Insertar en BD local con path estructurado directamente
   d. NO HAY TRANSICIÓN (ya viene estructurado)
3. UI se actualiza automáticamente (Flow)

MANEJO OFFLINE:
- Attachments se guardan con path original (respuesta instantánea)
- UI funciona inmediatamente con archivos locales
- Sync en background transiciona a paths estructurados
- Queue de operaciones pendientes persiste entre sesiones
```

### **🔄 PATH TRANSITION STATES**
```
Estado 1: ORIGINAL PATH (Pre-Sync)
- file_path: /cache/images/IMG_20250809_143022.jpg
- sync_status: PENDING
- remote_file_id: null

Estado 2: TRANSITIONING (Durante Sync)
- file_path: /cache/images/IMG_20250809_143022.jpg (original)
- sync_status: UPLOADING
- remote_file_id: upload_in_progress

Estado 3: STRUCTURED PATH (Post-Sync)
- file_path: /memora_attachments/note_12345/note_12345_0_1725897234_a7f8d2c4.jpg
- sync_status: SYNCED  
- remote_file_id: 1BxY...GoogleDriveFileId
```

---

## 🚀 **FASES DE IMPLEMENTACIÓN**

### **FASE 1: Infraestructura Base (1-2 días)**
1. Actualizar schema de attachments con nuevos campos
2. Implementar AttachmentPathManager utilities
3. Agregar métodos de sync a AttachmentsDao
4. Crear HashCalculator para verificación de integridad

### **FASE 2: Upload Implementation (2-3 días)**  
1. Extender GoogleDriveStorageProvider con métodos de attachments
2. Implementar AttachmentSyncService.uploadPendingAttachments()
3. Crear estructura de carpetas en Google Drive
4. Testing de upload con archivos reales

### **FASE 3: Download Implementation (2-3 días)**
1. Implementar AttachmentSyncService.downloadMissingAttachments() 
2. Integrar con DatabaseSyncService para descargar attachments al sincronizar notas
3. Manejo de verificación de integridad con hashes
4. Testing de download y regeneración de cache local

### **FASE 4: Conflict Resolution & Edge Cases (1-2 días)**
1. Implementar detectores de conflictos
2. Estrategias de resolución automática
3. Manejo de archivos eliminados
4. Recovery de errores de sync

### **FASE 5: Integration & Testing (1-2 días)**
1. Integrar AttachmentSyncService con SyncEngine existente
2. Testing end-to-end en múltiples dispositivos
3. Performance testing con archivos grandes
4. Error handling y logging

---

## 🎯 **VENTAJAS DE ESTA ARQUITECTURA**

### **✅ Consistency & Reliability**
- **Paths únicos universales**: Misma estructura en todos los dispositivos
- **IDs únicos**: noteId + index + timestamp + hash garantiza unicidad
- **Verificación de integridad**: Hashes para detectar corrupción
- **Foreign keys**: Cascading deletes mantienen consistencia

### **✅ Performance & UX**  
- **Local-first**: UI instantánea, sync en background
- **Lazy download**: Solo descargar attachments cuando sea necesario
- **Caching inteligente**: Track de qué está cached localmente
- **Batch operations**: Upload/download múltiples archivos eficientemente

### **✅ Scalability & Maintenance**
- **Structured paths**: Fácil navegación y debugging
- **Metadata tracking**: Estado completo de sync en BD
- **Platform agnostic**: Same logic for Android + iOS
- **Google Drive AppDataFolder**: Private, no visible al usuario

---

## ⚠️ **MANEJO DE ARCHIVOS EXISTENTES**

### **🔄 Overwrite Strategy**
```kotlin
enum class FileOverwriteStrategy {
    OVERWRITE,           // Sobrescribir siempre (default)
    SKIP_IF_EXISTS,     // Saltar si ya existe localmente
    COMPARE_HASH,       // Solo sobrescribir si hash es diferente
    BACKUP_ORIGINAL     // Crear backup antes de sobrescribir
}
```

### **🛡️ Conflict Resolution Scenarios**

#### **Escenario 1: Archivo Local Ya Existe**
```
Situación: Al sincronizar, el path estructurado local ya tiene un archivo
Solución:
1. Calcular hash del archivo existente
2. Calcular hash del archivo remoto
3. Si hashes son iguales → SKIP (ya actualizado)
4. Si hashes difieren → OVERWRITE (versión más nueva)
5. Log de la operación para debugging
```

#### **Escenario 2: Attachment ID Collision** 
```
Situación: Dos dispositivos generan el mismo attachment_id
Solución:
1. Detectar collision por timestamp + noteId
2. Agregar suffix incremental: _collision_1, _collision_2
3. Actualizar tanto local como remoto
4. Mantener integridad referencial
```

#### **Escenario 3: Partial Upload/Download**
```
Situación: Proceso interrumpido deja archivos parciales
Solución:
1. Verificar integridad por tamaño + hash
2. Si partial → eliminar y reintentar
3. Implementar resume donde sea posible
4. Timeout y retry automático
```

### **🔧 FileManager Enhanced Methods**
```kotlin
expect class FileManager {
    suspend fun saveFileToPath(data: ByteArray, path: String, overwrite: Boolean = true): Boolean
    suspend fun createDirectoriesIfNeeded(filePath: String): Boolean
    suspend fun fileExists(path: String): Boolean
    suspend fun getFileHash(path: String): String?
    suspend fun getFileSize(path: String): Long?
    suspend fun moveFile(fromPath: String, toPath: String): Boolean
    suspend fun copyFile(fromPath: String, toPath: String): Boolean
    suspend fun createBackup(filePath: String): String? // Returns backup path
}
```

---

## 📋 **CONSIDERACIONES TÉCNICAS**

### **🔐 Security & Privacy**
- Todos los archivos en AppDataFolder (privado por app)
- No usar nombres originales en paths remotos (privacy)
- Verificación de integridad con hashes
- Manejo seguro de errores sin exponer paths

### **📱 Mobile Considerations**
- Manejo de límites de storage
- Background sync con WorkManager (Android)
- Network efficiency (batch uploads/downloads)
- Battery optimization

### **🌐 Network & Offline**
- Queue de operaciones pendientes
- Retry automático con exponential backoff
- Progress indicators para uploads/downloads grandes
- Graceful degradation en modo offline

---

## 🧪 **PLAN DE TESTING**

### **Unit Tests**
- AttachmentPathManager utilities
- Hash calculation y verificación
- AttachmentsDao CRUD operations
- Conflict resolution logic

### **Integration Tests**  
- Upload/download con Google Drive real
- Sync completo entre dos dispositivos simulados
- Error handling y recovery scenarios
- Performance con archivos grandes (>10MB)

### **Manual Testing**
- Crear nota con múltiples attachments
- Verificar sync entre Android ↔ iOS
- Test de comportamiento offline
- Verificar integridad después de sync

---

*Plan creado: 9 Agosto 2025*  
*Estado: Ready for Implementation*  
*Tiempo estimado: 8-12 días de desarrollo*