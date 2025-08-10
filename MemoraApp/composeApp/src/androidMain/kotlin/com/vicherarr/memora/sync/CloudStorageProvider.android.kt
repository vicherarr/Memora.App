package com.vicherarr.memora.sync

import android.content.Context
import android.util.Log
import android.accounts.Account
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import com.vicherarr.memora.data.auth.CloudAuthProvider
import com.vicherarr.memora.domain.model.AuthState

/**
 * Implementación de CloudStorageProvider para Android usando Google Drive
 */
actual interface CloudStorageProvider {
    actual suspend fun autenticar()
    actual suspend fun descargarDB(): ByteArray?
    actual suspend fun subirDB(data: ByteArray)
    actual suspend fun obtenerMetadatosRemotos(): Long?
    actual suspend fun forceDeleteRemoteDatabase(): Result<Boolean>
    actual suspend fun forceDeleteAllRemoteFiles(): Result<Boolean>
    
    // NUEVOS MÉTODOS: Metadata management para sincronización incremental
    actual suspend fun saveMetadata(userId: String, metadataContent: String): Result<String>
    actual suspend fun loadMetadata(userId: String): Result<String?>
    actual suspend fun deleteMetadata(userId: String): Result<Boolean>
    actual suspend fun metadataExists(userId: String): Result<Boolean>
}

/**
 * Implementación real para Google Drive AppDataFolder
 * Integrado con CloudAuthProvider para reutilizar autenticación
 */
class GoogleDriveStorageProvider(
    private val context: Context,
    private val cloudAuthProvider: CloudAuthProvider
) : CloudStorageProvider {
    
    companion object {
        private const val TAG = "GoogleDriveStorage"
        private const val DB_FILE_NAME = "memora_sync.db"
        private const val MEMORA_FOLDER_NAME = "Memora"
    }
    
    private var driveService: Drive? = null
    private var memoraFolderId: String? = null
    
    /**
     * Get Drive service for AttachmentSyncRepository
     * Should only be called after autenticar() has been successful
     */
    fun getDriveService(): Drive? = driveService
    
    override suspend fun autenticar() {
        try {
            Log.d(TAG, "Iniciando autenticación para Google Drive...")
            
            // Verificar si CloudAuthProvider ya está autenticado
            val authState = cloudAuthProvider.authState.value
            Log.d(TAG, "Estado actual de CloudAuthProvider: $authState")
            
            if (authState !is AuthState.Authenticated) {
                Log.d(TAG, "CloudAuthProvider no autenticado. Intentando autenticación...")
                // Intentar autenticar usando CloudAuthProvider
                val result = cloudAuthProvider.signIn()
                if (result.isFailure) {
                    throw Exception("Error autenticando con CloudAuthProvider: ${result.exceptionOrNull()?.message}")
                }
                Log.d(TAG, "CloudAuthProvider autenticado exitosamente")
            }
            
            // Obtener cuenta desde Google Sign-In (ya autenticada por CloudAuthProvider)
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account == null) {
                throw Exception("No se pudo obtener cuenta de Google después de la autenticación")
            }
            
            Log.d(TAG, "Cuenta de Google encontrada: ${account.email}")
            
            // Crear el servicio Drive
            createDriveService(account)
            Log.d(TAG, "Servicio Google Drive inicializado exitosamente")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error configurando Google Drive: ${e.message}", e)
            throw Exception("Error configurando Google Drive: ${e.message}")
        }
    }
    
    private fun createDriveService(account: GoogleSignInAccount) {
        val credential = GoogleAccountCredential.usingOAuth2(context, listOf(DriveScopes.DRIVE_APPDATA))
        credential.selectedAccount = account.account
        
        driveService = Drive.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("Memora")
            .build()
    }
    
    /**
     * Configuración para usar AppDataFolder correctamente
     * AppDataFolder es un espacio privado para la aplicación, no visible para el usuario
     */
    private fun logAppDataInfo() {
        Log.d(TAG, "Usando Google Drive AppDataFolder (espacio privado de la app)")
        Log.d(TAG, "Scope: DRIVE_APPDATA, Espacio: appDataFolder")
    }
    
    override suspend fun descargarDB(): ByteArray? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "====== DESCARGA DESDE GOOGLE DRIVE ======")
            Log.d(TAG, "📥 Descargando DB desde Google Drive AppDataFolder...")
            Log.d(TAG, "📥 Cuenta actual: vicherarr@gmail.com (esperada)")
            logAppDataInfo()
            
            val service = driveService ?: throw Exception("Google Drive service no inicializado")
            
            // SOLUCIÓN PROFESIONAL: Usar spaces='appDataFolder' para búsquedas en AppData
            val fileList = service.files().list()
                .setSpaces("appDataFolder")
                .setQ("name='$DB_FILE_NAME' and trashed=false")
                .setFields("files(id, name, size)")
                .execute()
            
            val files = fileList.files
            if (files.isNullOrEmpty()) {
                Log.d(TAG, "📥 ❌ No se encontró archivo de DB remota en AppDataFolder")
                Log.d(TAG, "📥 Esto significa:")
                Log.d(TAG, "📥   - Primera sincronización desde este dispositivo, O")
                Log.d(TAG, "📥   - El otro dispositivo no ha sincronizado aún")
                Log.d(TAG, "====== DESCARGA: ARCHIVO NO ENCONTRADO ======")
                return@withContext null
            }
            
            val file = files[0]
            Log.d(TAG, "📥 ✅ Archivo encontrado en AppDataFolder:")
            Log.d(TAG, "📥   - Nombre: ${file.name}")
            Log.d(TAG, "📥   - ID: ${file.id}")
            Log.d(TAG, "📥   - Tamaño: ${file.size} bytes")
            
            // Descargar contenido del archivo
            val outputStream = ByteArrayOutputStream()
            service.files().get(file.id).executeMediaAndDownloadTo(outputStream)
            
            val data = outputStream.toByteArray()
            Log.d(TAG, "📥 ✅ Archivo descargado exitosamente:")
            Log.d(TAG, "📥   - Bytes descargados: ${data.size}")
            Log.d(TAG, "📥   - Contenido (primeros 200 chars): ${data.decodeToString().take(200)}")
            Log.d(TAG, "====== DESCARGA COMPLETADA ======")
            
            data
            
        } catch (e: Exception) {
            Log.e(TAG, "Error descargando DB: ${e.message}", e)
            throw Exception("Error descargando base de datos: ${e.message}")
        }
    }
    
    override suspend fun subirDB(data: ByteArray): Unit = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "====== SUBIDA A GOOGLE DRIVE ======")
            Log.d(TAG, "📤 Subiendo DB a Google Drive AppDataFolder")
            Log.d(TAG, "📤 Tamaño de datos: ${data.size} bytes")
            Log.d(TAG, "📤 Cuenta actual: vicherarr@gmail.com (esperada)")
            Log.d(TAG, "📤 Contenido (primeros 200 chars): ${data.decodeToString().take(200)}")
            logAppDataInfo()
            
            val service = driveService ?: throw Exception("Google Drive service no inicializado")
            
            // SOLUCIÓN PROFESIONAL: Usar spaces='appDataFolder' para búsquedas en AppData
            val fileList = service.files().list()
                .setSpaces("appDataFolder")
                .setQ("name='$DB_FILE_NAME' and trashed=false")
                .setFields("files(id, name)")
                .execute()
            
            val files = fileList.files
            val fileContent = ByteArrayContent("application/octet-stream", data)
            
            if (files.isNullOrEmpty()) {
                // PRIMERA VEZ: Crear archivo nuevo en AppDataFolder
                Log.d(TAG, "📤 PRIMERA VEZ - Creando archivo '$DB_FILE_NAME' en AppDataFolder...")
                
                val fileMetadata = File()
                    .setName(DB_FILE_NAME)
                    .setParents(listOf("appDataFolder"))
                
                val file = service.files().create(fileMetadata, fileContent)
                    .setFields("id, name")
                    .execute()
                
                Log.d(TAG, "📤 ✅ Archivo creado exitosamente:")
                Log.d(TAG, "📤   - Nombre: ${file.name}")
                Log.d(TAG, "📤   - ID: ${file.id}")
                Log.d(TAG, "📤   - Ubicación: AppDataFolder (privado para vicherarr@gmail.com)")
                
            } else {
                // ACTUALIZAR: Archivo ya existe
                val existingFileId = files[0].id
                val existingFileName = files[0].name
                Log.d(TAG, "📤 ACTUALIZANDO archivo existente:")
                Log.d(TAG, "📤   - ID: $existingFileId")
                Log.d(TAG, "📤   - Nombre: $existingFileName")
                
                val updatedFile = service.files().update(existingFileId, null, fileContent)
                    .setFields("id, name, modifiedTime")
                    .execute()
                
                Log.d(TAG, "📤 ✅ Archivo actualizado exitosamente:")
                Log.d(TAG, "📤   - Nombre: ${updatedFile.name}")
                Log.d(TAG, "📤   - ID: ${updatedFile.id}")
                Log.d(TAG, "📤   - Timestamp: ${updatedFile.modifiedTime}")
            }
            
            Log.d(TAG, "====== SUBIDA COMPLETADA ======")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error subiendo DB: ${e.message}", e)
            throw Exception("Error subiendo base de datos: ${e.message}")
        }
    }
    
    override suspend fun obtenerMetadatosRemotos(): Long? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Obteniendo metadatos del archivo remoto desde AppDataFolder...")
            logAppDataInfo()
            
            val service = driveService ?: throw Exception("Google Drive service no inicializado")
            
            // SOLUCIÓN PROFESIONAL: Usar spaces='appDataFolder' para búsquedas en AppData
            val fileList = service.files().list()
                .setSpaces("appDataFolder")
                .setQ("name='$DB_FILE_NAME' and trashed=false")
                .setFields("files(id, name, modifiedTime)")
                .execute()
            
            val files = fileList.files
            if (files.isNullOrEmpty()) {
                Log.d(TAG, "No se encontró archivo remoto")
                return@withContext null
            }
            
            val file = files[0]
            val timestamp = file.modifiedTime?.value
            Log.d(TAG, "Archivo remoto encontrado: ${file.name}, timestamp: $timestamp")
            
            timestamp
            
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo metadatos remotos: ${e.message}", e)
            throw Exception("Error obteniendo metadatos remotos: ${e.message}")
        }
    }
    
    /**
     * TEMPORARY: Force delete remote database for fresh start
     * TODO: Remove this method after testing
     */
    override suspend fun forceDeleteRemoteDatabase(): Result<Boolean> = withContext(Dispatchers.Default) {
        try {
            Log.d(TAG, "🚨 FORCE DELETE: Eliminando base de datos remota...")
            
            val service = driveService ?: throw Exception("Google Drive service no inicializado")
            
            // Buscar el archivo de base de datos principal
            val fileList = service.files().list()
                .setSpaces("appDataFolder")
                .setQ("name='$DB_FILE_NAME' and trashed=false")
                .setFields("files(id, name)")
                .execute()
            
            val files = fileList.files
            if (files.isNullOrEmpty()) {
                Log.d(TAG, "🚨 FORCE DELETE: No se encontró DB remota para eliminar")
                return@withContext Result.success(true)
            }
            
            val dbFile = files[0]
            Log.d(TAG, "🚨 FORCE DELETE: Eliminando archivo: ${dbFile.name} (ID: ${dbFile.id})")
            
            // Eliminar el archivo
            service.files().delete(dbFile.id).execute()
            
            Log.d(TAG, "🚨 FORCE DELETE: ✅ Base de datos remota eliminada exitosamente")
            Log.d(TAG, "🚨 FORCE DELETE: La próxima sincronización creará una nueva DB desde cero")
            
            return@withContext Result.success(true)
            
        } catch (e: Exception) {
            Log.e(TAG, "🚨 FORCE DELETE: Error eliminando DB remota: ${e.message}", e)
            return@withContext Result.failure(Exception("Error eliminando DB remota: ${e.message}"))
        }
    }
    
    /**
     * TEMPORARY: Force delete ALL files in AppDataFolder for complete reset
     * TODO: Remove this method after testing
     */
    override suspend fun forceDeleteAllRemoteFiles(): Result<Boolean> = withContext(Dispatchers.Default) {
        try {
            Log.d(TAG, "🚨🚨 NUCLEAR DELETE: Eliminando TODOS los archivos en AppDataFolder...")
            
            val service = driveService ?: throw Exception("Google Drive service no inicializado")
            
            // Listar TODOS los archivos en AppDataFolder
            val fileList = service.files().list()
                .setSpaces("appDataFolder")
                .setFields("files(id, name)")
                .execute()
            
            val files = fileList.files
            if (files.isNullOrEmpty()) {
                Log.d(TAG, "🚨🚨 NUCLEAR DELETE: No hay archivos para eliminar")
                return@withContext Result.success(true)
            }
            
            Log.d(TAG, "🚨🚨 NUCLEAR DELETE: Encontrados ${files.size} archivos para eliminar:")
            files.forEach { file ->
                Log.d(TAG, "  - ${file.name} (${file.id})")
            }
            
            // Eliminar cada archivo
            var deletedCount = 0
            files.forEach { file ->
                try {
                    service.files().delete(file.id).execute()
                    Log.d(TAG, "🚨🚨 NUCLEAR DELETE: ✅ Eliminado: ${file.name}")
                    deletedCount++
                } catch (e: Exception) {
                    Log.e(TAG, "🚨🚨 NUCLEAR DELETE: ❌ Error eliminando ${file.name}: ${e.message}")
                }
            }
            
            Log.d(TAG, "🚨🚨 NUCLEAR DELETE: ✅ Eliminados $deletedCount de ${files.size} archivos")
            Log.d(TAG, "🚨🚨 NUCLEAR DELETE: AppDataFolder ahora está limpio")
            
            return@withContext Result.success(true)
            
        } catch (e: Exception) {
            Log.e(TAG, "🚨🚨 NUCLEAR DELETE: Error en eliminación masiva: ${e.message}", e)
            return@withContext Result.failure(Exception("Error en eliminación masiva: ${e.message}"))
        }
    }
    
    // ========== NUEVOS MÉTODOS: METADATA MANAGEMENT ==========
    
    override suspend fun saveMetadata(userId: String, metadataContent: String): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val service = driveService ?: throw Exception("Google Drive service not initialized")
            val fileName = getMetadataFileName(userId)
            
            Log.d(TAG, "💾 Guardando metadata para usuario $userId como archivo: $fileName")
            
            // Buscar archivo existente
            val existingFile = findFileByName(fileName)
            
            val fileMetadata = File().apply {
                if (existingFile == null) {
                    name = fileName
                    parents = listOf("appDataFolder") // Guardar en AppDataFolder para privacidad
                }
            }
            
            val mediaContent = ByteArrayContent("application/json", metadataContent.toByteArray())
            
            val savedFile = if (existingFile != null) {
                // Actualizar archivo existente
                Log.d(TAG, "📝 Actualizando archivo de metadata existente: ${existingFile.id}")
                service.files().update(existingFile.id, fileMetadata, mediaContent).execute()
            } else {
                // Crear nuevo archivo
                Log.d(TAG, "📄 Creando nuevo archivo de metadata")
                service.files().create(fileMetadata, mediaContent).execute()
            }
            
            Log.d(TAG, "✅ Metadata guardado exitosamente. File ID: ${savedFile.id}")
            Result.success(savedFile.id)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error guardando metadata: ${e.message}", e)
            Result.failure(Exception("Error guardando metadata: ${e.message}"))
        }
    }
    
    override suspend fun loadMetadata(userId: String): Result<String?> = withContext(Dispatchers.IO) {
        return@withContext try {
            val service = driveService ?: throw Exception("Google Drive service not initialized")
            val fileName = getMetadataFileName(userId)
            
            Log.d(TAG, "📖 Cargando metadata para usuario $userId desde archivo: $fileName")
            
            val file = findFileByName(fileName)
            if (file == null) {
                Log.d(TAG, "📄 No se encontró archivo de metadata para usuario $userId")
                return@withContext Result.success(null)
            }
            
            val content = service.files().get(file.id).executeMediaAsInputStream()
            val metadataContent = content.readBytes().toString(Charsets.UTF_8)
            
            Log.d(TAG, "✅ Metadata cargado exitosamente. Tamaño: ${metadataContent.length} chars")
            Result.success(metadataContent)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error cargando metadata: ${e.message}", e)
            Result.failure(Exception("Error cargando metadata: ${e.message}"))
        }
    }
    
    override suspend fun deleteMetadata(userId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        return@withContext try {
            val service = driveService ?: throw Exception("Google Drive service not initialized")
            val fileName = getMetadataFileName(userId)
            
            Log.d(TAG, "🗑️ Eliminando metadata para usuario $userId archivo: $fileName")
            
            val file = findFileByName(fileName)
            if (file == null) {
                Log.d(TAG, "📄 No se encontró archivo de metadata para eliminar")
                return@withContext Result.success(true) // No existe, consideramos éxito
            }
            
            service.files().delete(file.id).execute()
            Log.d(TAG, "✅ Metadata eliminado exitosamente")
            Result.success(true)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error eliminando metadata: ${e.message}", e)
            Result.failure(Exception("Error eliminando metadata: ${e.message}"))
        }
    }
    
    override suspend fun metadataExists(userId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        return@withContext try {
            val service = driveService ?: throw Exception("Google Drive service not initialized")
            val fileName = getMetadataFileName(userId)
            
            Log.d(TAG, "🔍 Verificando si existe metadata para usuario $userId archivo: $fileName")
            
            val file = findFileByName(fileName)
            val exists = file != null
            
            Log.d(TAG, "✅ Resultado verificación metadata: $exists")
            Result.success(exists)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error verificando metadata: ${e.message}", e)
            Result.failure(Exception("Error verificando metadata: ${e.message}"))
        }
    }
    
    // ========== MÉTODOS AUXILIARES PARA METADATA ==========
    
    private fun getMetadataFileName(userId: String): String {
        return "memora_sync_metadata_${userId.hashCode().toString(16)}.json"
    }
    
    private suspend fun findFileByName(fileName: String): File? = withContext(Dispatchers.IO) {
        return@withContext try {
            val service = driveService ?: return@withContext null
            
            val result = service.files()
                .list()
                .setSpaces("appDataFolder")
                .setQ("name='$fileName' and trashed=false")
                .setFields("files(id, name, modifiedTime)")
                .execute()
            
            result.files?.firstOrNull()
        } catch (e: Exception) {
            Log.e(TAG, "Error buscando archivo por nombre '$fileName': ${e.message}")
            null
        }
    }
}

/**
 * Factory function para Android
 */
actual fun getCloudStorageProvider(): CloudStorageProvider {
    // Se manejará con DI - esto es solo para compatibilidad
    throw NotImplementedError("Use DI to inject GoogleDriveStorageProvider instead")
}

/**
 * Factory function con dependencias para Android
 */
fun getCloudStorageProvider(context: Context, cloudAuthProvider: CloudAuthProvider): CloudStorageProvider {
    return GoogleDriveStorageProvider(context, cloudAuthProvider)
}

/**
 * Lazy wrapper for AttachmentSyncRepository that waits for Google Drive service to be initialized
 */
class LazyGoogleDriveAttachmentSyncRepository(
    private val context: Context,
    private val cloudStorageProvider: GoogleDriveStorageProvider
) : AttachmentSyncRepository {
    
    private var _repository: GoogleDriveAttachmentSyncRepository? = null
    
    private suspend fun getRepository(): GoogleDriveAttachmentSyncRepository {
        if (_repository == null) {
            val driveService = cloudStorageProvider.getDriveService()
                ?: throw IllegalStateException("Google Drive service not initialized. Please authenticate first.")
            _repository = GoogleDriveAttachmentSyncRepository(context, driveService)
        }
        return _repository!!
    }
    
    override suspend fun uploadAttachment(
        attachment: com.vicherarr.memora.data.database.Attachment,
        fileData: ByteArray,
        userId: String
    ): Result<String> = getRepository().uploadAttachment(attachment, fileData, userId)
    
    override suspend fun downloadAttachment(remoteId: String): Result<ByteArray> =
        getRepository().downloadAttachment(remoteId)
    
    override suspend fun listRemoteAttachments(userId: String): List<com.vicherarr.memora.sync.RemoteAttachmentInfo> =
        getRepository().listRemoteAttachments(userId)
    
    override suspend fun deleteRemoteAttachment(remoteId: String): Result<Boolean> =
        getRepository().deleteRemoteAttachment(remoteId)
    
    override suspend fun remoteAttachmentExists(remoteId: String): Result<Boolean> =
        getRepository().remoteAttachmentExists(remoteId)
    
    override suspend fun getRemoteAttachmentMetadata(remoteId: String): Result<com.vicherarr.memora.sync.RemoteAttachmentMetadata> =
        getRepository().getRemoteAttachmentMetadata(remoteId)
    
    override suspend fun updateRemoteAttachmentMetadata(
        remoteId: String, 
        metadata: com.vicherarr.memora.sync.RemoteAttachmentMetadata
    ): Result<Boolean> = getRepository().updateRemoteAttachmentMetadata(remoteId, metadata)
}