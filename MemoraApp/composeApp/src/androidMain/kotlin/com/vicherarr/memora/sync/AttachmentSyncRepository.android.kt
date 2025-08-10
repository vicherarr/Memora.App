package com.vicherarr.memora.sync

import android.content.Context
import android.util.Log
import com.google.api.client.http.ByteArrayContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.vicherarr.memora.data.database.Attachment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/**
 * Implementación para Android usando Google Drive API
 */
actual interface AttachmentSyncRepository {
    actual suspend fun uploadAttachment(
        attachment: Attachment,
        fileData: ByteArray,
        userId: String
    ): Result<String>
    
    actual suspend fun downloadAttachment(remoteId: String): Result<ByteArray>
    actual suspend fun listRemoteAttachments(userId: String): List<RemoteAttachmentInfo>
    actual suspend fun deleteRemoteAttachment(remoteId: String): Result<Boolean>
    actual suspend fun remoteAttachmentExists(remoteId: String): Result<Boolean>
    actual suspend fun getRemoteAttachmentMetadata(remoteId: String): Result<RemoteAttachmentMetadata>
    actual suspend fun updateRemoteAttachmentMetadata(
        remoteId: String, 
        metadata: RemoteAttachmentMetadata
    ): Result<Boolean>
}

/**
 * Implementación real para Google Drive
 */
class GoogleDriveAttachmentSyncRepository(
    private val context: Context,
    private val driveService: Drive
) : AttachmentSyncRepository {
    
    companion object {
        private const val TAG = "GoogleDriveAttachSync"
        private const val ATTACHMENTS_FOLDER_NAME = "Memora_Attachments"
    }
    
    private var attachmentsFolderId: String? = null
    
    /**
     * Obtiene o crea la carpeta de attachments en AppDataFolder
     */
    private suspend fun getOrCreateAttachmentsFolder(): String = withContext(Dispatchers.IO) {
        if (attachmentsFolderId != null) {
            return@withContext attachmentsFolderId!!
        }
        
        try {
            // Buscar carpeta existente
            val folderList = driveService.files().list()
                .setSpaces("appDataFolder")
                .setQ("name='$ATTACHMENTS_FOLDER_NAME' and mimeType='application/vnd.google-apps.folder' and trashed=false")
                .setFields("files(id, name)")
                .execute()
            
            val existingFolder = folderList.files.firstOrNull()
            if (existingFolder != null) {
                Log.d(TAG, "Carpeta attachments encontrada: ${existingFolder.id}")
                attachmentsFolderId = existingFolder.id
                return@withContext existingFolder.id
            }
            
            // Crear nueva carpeta
            val folderMetadata = File()
                .setName(ATTACHMENTS_FOLDER_NAME)
                .setParents(listOf("appDataFolder"))
                .setMimeType("application/vnd.google-apps.folder")
            
            val createdFolder = driveService.files().create(folderMetadata)
                .setFields("id")
                .execute()
            
            Log.d(TAG, "Carpeta attachments creada: ${createdFolder.id}")
            attachmentsFolderId = createdFolder.id
            return@withContext createdFolder.id
            
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo/creando carpeta attachments: ${e.message}", e)
            throw e
        }
    }
    
    /**
     * Genera nombre de archivo remoto único
     */
    private fun generateRemoteFileName(attachment: Attachment, userId: String): String {
        // Formato: userId_noteId_attachmentId_originalName
        val sanitizedUserId = userId.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
        val sanitizedNoteId = attachment.nota_id.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
        val sanitizedAttachmentId = attachment.id.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
        val sanitizedFileName = attachment.nombre_original.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
        
        return "${sanitizedUserId}_${sanitizedNoteId}_${sanitizedAttachmentId}_${sanitizedFileName}"
    }
    
    override suspend fun uploadAttachment(
        attachment: Attachment,
        fileData: ByteArray,
        userId: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "====== SUBIENDO ATTACHMENT ======")
            Log.d(TAG, "📤 Archivo: ${attachment.nombre_original}")
            Log.d(TAG, "📤 Tamaño: ${fileData.size} bytes")
            Log.d(TAG, "📤 Usuario: $userId")
            Log.d(TAG, "📤 Note ID: ${attachment.nota_id}")
            Log.d(TAG, "📤 MIME Type: ${attachment.tipo_mime}")
            
            // Obtener carpeta de attachments
            val folderId = getOrCreateAttachmentsFolder()
            
            // Generar nombre de archivo remoto único
            val remoteFileName = generateRemoteFileName(attachment, userId)
            Log.d(TAG, "📤 Nombre remoto: $remoteFileName")
            
            // Verificar si ya existe
            val existingFileList = driveService.files().list()
                .setSpaces("appDataFolder")
                .setQ("name='$remoteFileName' and parents='$folderId' and trashed=false")
                .setFields("files(id, name)")
                .execute()
            
            val fileContent = ByteArrayContent(attachment.tipo_mime, fileData)
            
            val remoteId = if (existingFileList.files.isNullOrEmpty()) {
                // Crear archivo nuevo
                Log.d(TAG, "📤 Creando archivo nuevo en AppDataFolder...")
                
                val fileMetadata = File()
                    .setName(remoteFileName)
                    .setParents(listOf(folderId))
                    .setDescription("Memora attachment - User: $userId, Note: ${attachment.nota_id}")
                
                // Agregar propiedades personalizadas
                val properties = mapOf(
                    "userId" to userId,
                    "noteId" to attachment.nota_id,
                    "attachmentId" to attachment.id,
                    "originalName" to attachment.nombre_original,
                    "contentHash" to (attachment.content_hash ?: ""),
                    "uploadDate" to System.currentTimeMillis().toString()
                )
                fileMetadata.properties = properties
                
                val createdFile = driveService.files().create(fileMetadata, fileContent)
                    .setFields("id, name, size")
                    .execute()
                
                Log.d(TAG, "📤 ✅ Archivo creado exitosamente:")
                Log.d(TAG, "📤   - ID: ${createdFile.id}")
                Log.d(TAG, "📤   - Nombre: ${createdFile.name}")
                Log.d(TAG, "📤   - Tamaño: ${createdFile.size} bytes")
                
                createdFile.id
                
            } else {
                // Actualizar archivo existente
                val existingFileId = existingFileList.files[0].id
                Log.d(TAG, "📤 Actualizando archivo existente: $existingFileId")
                
                val updatedFile = driveService.files().update(existingFileId, null, fileContent)
                    .setFields("id, name, size, modifiedTime")
                    .execute()
                
                Log.d(TAG, "📤 ✅ Archivo actualizado exitosamente:")
                Log.d(TAG, "📤   - ID: ${updatedFile.id}")
                Log.d(TAG, "📤   - Nombre: ${updatedFile.name}")
                Log.d(TAG, "📤   - Tamaño: ${updatedFile.size} bytes")
                Log.d(TAG, "📤   - Modificado: ${updatedFile.modifiedTime}")
                
                updatedFile.id
            }
            
            Log.d(TAG, "====== SUBIDA COMPLETADA ======")
            Result.success(remoteId)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error subiendo attachment: ${e.message}", e)
            Result.failure(Exception("Error subiendo attachment: ${e.message}"))
        }
    }
    
    override suspend fun downloadAttachment(remoteId: String): Result<ByteArray> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "====== DESCARGANDO ATTACHMENT ======")
            Log.d(TAG, "📥 Remote ID: $remoteId")
            
            // Verificar que el archivo existe
            val file = driveService.files().get(remoteId)
                .setFields("id, name, size, mimeType")
                .execute()
            
            Log.d(TAG, "📥 Archivo encontrado:")
            Log.d(TAG, "📥   - Nombre: ${file.name}")
            Log.d(TAG, "📥   - Tamaño: ${file.size} bytes")
            Log.d(TAG, "📥   - MIME: ${file.mimeType}")
            
            // Descargar contenido
            val outputStream = ByteArrayOutputStream()
            driveService.files().get(remoteId).executeMediaAndDownloadTo(outputStream)
            
            val fileData = outputStream.toByteArray()
            Log.d(TAG, "📥 ✅ Descarga completada:")
            Log.d(TAG, "📥   - Bytes descargados: ${fileData.size}")
            Log.d(TAG, "====== DESCARGA COMPLETADA ======")
            
            Result.success(fileData)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error descargando attachment: ${e.message}", e)
            Result.failure(Exception("Error descargando attachment: ${e.message}"))
        }
    }
    
    override suspend fun listRemoteAttachments(userId: String): List<RemoteAttachmentInfo> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "====== LISTANDO ATTACHMENTS REMOTOS ======")
            Log.d(TAG, "📋 Usuario: $userId")
            
            // Obtener carpeta de attachments
            val folderId = getOrCreateAttachmentsFolder()
            
            // Listar archivos en la carpeta de attachments
            val fileList = driveService.files().list()
                .setSpaces("appDataFolder")
                .setQ("parents='$folderId' and trashed=false")
                .setFields("files(id, name, size, mimeType, properties, createdTime, modifiedTime)")
                .execute()
            
            val files = fileList.files ?: emptyList()
            Log.d(TAG, "📋 ${files.size} archivos encontrados en carpeta de attachments")
            
            val remoteAttachments = files.mapNotNull { file ->
                try {
                    val properties = file.properties ?: emptyMap()
                    val fileUserId = properties["userId"]
                    
                    // Filtrar por usuario
                    if (fileUserId != userId) {
                        Log.d(TAG, "📋 Omitiendo archivo de otro usuario: ${file.name} (usuario: $fileUserId)")
                        return@mapNotNull null
                    }
                    
                    val noteId = properties["noteId"] ?: ""
                    val attachmentId = properties["attachmentId"] ?: ""
                    val originalName = properties["originalName"] ?: file.name
                    val contentHash = properties["contentHash"]?.takeIf { it.isNotEmpty() }
                    val uploadDate = properties["uploadDate"]?.toLongOrNull() ?: file.createdTime?.value ?: 0L
                    
                    // Determinar MediaType desde MIME type
                    val mediaType = when {
                        file.mimeType.startsWith("image/") -> 1 // MediaType.IMAGE
                        file.mimeType.startsWith("video/") -> 2 // MediaType.VIDEO
                        else -> 1 // Default a imagen
                    }
                    
                    RemoteAttachmentInfo(
                        attachmentId = attachmentId,
                        noteId = noteId,
                        remoteId = file.id,
                        fileName = originalName,
                        mediaType = mediaType,
                        mimeType = file.mimeType,
                        contentHash = contentHash,
                        uploadDate = uploadDate,
                        remotePath = "appDataFolder/$ATTACHMENTS_FOLDER_NAME/${file.name}"
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error procesando archivo remoto ${file.name}: ${e.message}")
                    null
                }
            }
            
            Log.d(TAG, "📋 ✅ ${remoteAttachments.size} attachments procesados para usuario $userId")
            Log.d(TAG, "====== LISTADO COMPLETADO ======")
            
            remoteAttachments
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error listando attachments remotos: ${e.message}", e)
            emptyList()
        }
    }
    
    override suspend fun deleteRemoteAttachment(remoteId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🗑️ Eliminando attachment remoto: $remoteId")
            
            driveService.files().delete(remoteId).execute()
            
            Log.d(TAG, "🗑️ ✅ Attachment remoto eliminado exitosamente")
            Result.success(true)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error eliminando attachment remoto: ${e.message}", e)
            Result.failure(Exception("Error eliminando attachment: ${e.message}"))
        }
    }
    
    override suspend fun remoteAttachmentExists(remoteId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            driveService.files().get(remoteId)
                .setFields("id")
                .execute()
            
            Result.success(true)
            
        } catch (e: Exception) {
            // Si es 404, el archivo no existe
            if (e.message?.contains("404") == true) {
                Result.success(false)
            } else {
                Result.failure(Exception("Error verificando existencia: ${e.message}"))
            }
        }
    }
    
    override suspend fun getRemoteAttachmentMetadata(remoteId: String): Result<RemoteAttachmentMetadata> = withContext(Dispatchers.IO) {
        try {
            val file = driveService.files().get(remoteId)
                .setFields("id, name, size, mimeType, properties, createdTime, modifiedTime")
                .execute()
            
            val properties = file.properties ?: emptyMap()
            
            val metadata = RemoteAttachmentMetadata(
                remoteId = file.id,
                fileName = properties["originalName"] ?: file.name,
                size = file.size?.toLong() ?: 0L,
                mimeType = file.mimeType,
                contentHash = properties["contentHash"],
                createdAt = file.createdTime?.value ?: 0L,
                modifiedAt = file.modifiedTime?.value ?: 0L,
                userId = properties["userId"] ?: "",
                noteId = properties["noteId"] ?: "",
                customProperties = properties
            )
            
            Result.success(metadata)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error obteniendo metadatos: ${e.message}", e)
            Result.failure(Exception("Error obteniendo metadatos: ${e.message}"))
        }
    }
    
    override suspend fun updateRemoteAttachmentMetadata(
        remoteId: String, 
        metadata: RemoteAttachmentMetadata
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val fileUpdate = File()
            fileUpdate.properties = metadata.customProperties
            
            driveService.files().update(remoteId, fileUpdate)
                .setFields("id")
                .execute()
            
            Log.d(TAG, "✅ Metadatos actualizados para: $remoteId")
            Result.success(true)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error actualizando metadatos: ${e.message}", e)
            Result.failure(Exception("Error actualizando metadatos: ${e.message}"))
        }
    }
}

/**
 * Factory para Android
 */
actual object AttachmentSyncRepositoryFactory {
    actual fun create(): AttachmentSyncRepository {
        throw NotImplementedError("Use DI to inject GoogleDriveAttachmentSyncRepository instead")
    }
}

/**
 * Factory function con dependencias para Android
 */
fun createGoogleDriveAttachmentSyncRepository(
    context: Context, 
    driveService: Drive
): AttachmentSyncRepository {
    return GoogleDriveAttachmentSyncRepository(context, driveService)
}