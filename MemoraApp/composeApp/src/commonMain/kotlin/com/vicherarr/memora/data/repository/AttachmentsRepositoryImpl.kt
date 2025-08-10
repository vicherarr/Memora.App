package com.vicherarr.memora.data.repository

import com.vicherarr.memora.data.auth.CloudAuthProvider
import com.vicherarr.memora.data.database.AttachmentsDao
import com.vicherarr.memora.data.database.getCurrentTimestamp
import com.vicherarr.memora.database.Attachments
import com.vicherarr.memora.domain.model.AuthState
import com.vicherarr.memora.domain.models.ArchivoAdjunto
import com.vicherarr.memora.domain.models.MediaFile
import com.vicherarr.memora.domain.models.MediaType
import com.vicherarr.memora.domain.models.TipoDeArchivo
import com.vicherarr.memora.domain.platform.FileManager
import com.vicherarr.memora.domain.repository.AttachmentsRepository
import com.vicherarr.memora.domain.repository.SyncMetadataRepository

/**
 * Local-first implementation of AttachmentsRepository
 * - All operations go to local database first (immediate UI response)
 * - Automatic metadata update after each operation for smart sync
 * - Offline-first approach for better UX
 */
class AttachmentsRepositoryImpl(
    private val attachmentsDao: AttachmentsDao,
    private val fileManager: FileManager,
    private val cloudAuthProvider: CloudAuthProvider,
    private val syncMetadataRepository: SyncMetadataRepository
) : AttachmentsRepository {

    private fun getCurrentUserId(): String {
        val authState = cloudAuthProvider.authState.value
        return when (authState) {
            is AuthState.Authenticated -> authState.user.email
            else -> "unauthenticated_user" // Fallback
        }
    }
    
    /**
     * Actualiza metadatos locales después de operaciones de attachment
     * Esto garantiza que el sistema de sincronización inteligente detecte cambios
     */
    private suspend fun updateSyncMetadataAfterAttachmentOperation() {
        try {
            val userId = getCurrentUserId()
            println("AttachmentsRepository: 📊 Actualizando metadata tras operación de attachment...")
            
            val metadataResult = syncMetadataRepository.generateCurrentSyncMetadata(userId)
            if (metadataResult.isSuccess) {
                val newMetadata = metadataResult.getOrNull()!!
                syncMetadataRepository.saveLocalSyncMetadata(newMetadata)
                println("AttachmentsRepository: ✅ Metadata local actualizado - Attachments: ${newMetadata.attachmentsCount}")
            } else {
                println("AttachmentsRepository: ⚠️ Error generando metadata: ${metadataResult.exceptionOrNull()?.message}")
            }
        } catch (e: Exception) {
            println("AttachmentsRepository: ⚠️ Error actualizando metadata: ${e.message}")
        }
    }
    
    override suspend fun getAttachmentsByNoteId(noteId: String): Result<List<ArchivoAdjunto>> {
        return try {
            val attachments = attachmentsDao.getAttachmentsByNoteId(noteId)
            val domainAttachments = attachments.map { it.toDomainModel() }
            Result.success(domainAttachments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getAttachmentById(attachmentId: String): Result<ArchivoAdjunto?> {
        return try {
            val attachment = attachmentsDao.getAttachmentById(attachmentId)
            val domainAttachment = attachment?.toDomainModel()
            Result.success(domainAttachment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun createAttachment(
        noteId: String,
        mediaFile: MediaFile
    ): Result<ArchivoAdjunto> {
        return try {
            // Save file to local storage
            val savedFile = fileManager.saveFile(mediaFile.data, mediaFile.fileName, mediaFile.type)
            if (savedFile == null) {
                return Result.failure(Exception("Failed to save media file"))
            }
            
            val attachmentId = "attachment_${getCurrentTimestamp()}_${mediaFile.fileName.hashCode()}"
            val now = getCurrentTimestamp().toString()
            
            // Convert MediaType to TipoDeArchivo
            val tipoArchivo = when (mediaFile.type) {
                MediaType.IMAGE -> TipoDeArchivo.Imagen
                MediaType.VIDEO -> TipoDeArchivo.Video
            }
            
            // Insert attachment to database
            attachmentsDao.insertAttachment(
                id = attachmentId,
                filePath = savedFile.path,
                nombreOriginal = mediaFile.fileName,
                tipoArchivo = tipoArchivo.ordinal.toLong() + 1, // 1=Imagen, 2=Video
                tipoMime = mediaFile.mimeType ?: "application/octet-stream",
                tamanoBytes = savedFile.size,
                fechaSubida = now,
                notaId = noteId
            )
            
            // Create domain model for return value
            val createdAttachment = ArchivoAdjunto(
                id = attachmentId,
                filePath = savedFile.path,
                nombreOriginal = mediaFile.fileName,
                tipoArchivo = tipoArchivo,
                tipoMime = mediaFile.mimeType ?: "application/octet-stream",
                tamanoBytes = savedFile.size,
                fechaSubida = getCurrentTimestamp(),
                notaId = noteId
            )
            
            // ✅ NUEVO: Actualizar metadata para que sync inteligente detecte el cambio
            updateSyncMetadataAfterAttachmentOperation()
            
            Result.success(createdAttachment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateAttachment(
        attachmentId: String,
        nombreOriginal: String,
        tipoMime: String
    ): Result<ArchivoAdjunto> {
        return try {
            // Get current attachment data
            val currentAttachment = attachmentsDao.getAttachmentById(attachmentId)
                ?: return Result.failure(Exception("Attachment not found"))
            
            // Update attachment in database
            attachmentsDao.updateAttachment(
                attachmentId = attachmentId,
                filePath = currentAttachment.file_path,
                nombreOriginal = nombreOriginal,
                tipoArchivo = currentAttachment.tipo_archivo,
                tipoMime = tipoMime,
                tamanoBytes = currentAttachment.tamano_bytes,
                fechaSubida = getCurrentTimestamp().toString()
            )
            
            // Get updated attachment
            val updatedAttachment = attachmentsDao.getAttachmentById(attachmentId)
                ?: return Result.failure(Exception("Attachment not found after update"))
            
            // ✅ NUEVO: Actualizar metadata para que sync inteligente detecte el cambio
            updateSyncMetadataAfterAttachmentOperation()
            
            Result.success(updatedAttachment.toDomainModel())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteAttachment(attachmentId: String): Result<Unit> {
        return try {
            // Get attachment info before deleting
            val attachment = attachmentsDao.getAttachmentById(attachmentId)
            if (attachment != null) {
                // Delete file from disk
                fileManager.deleteFile(attachment.file_path)
                
                // Delete from database
                attachmentsDao.deleteAttachment(attachmentId)
                
                // ✅ NUEVO: Actualizar metadata para que sync inteligente detecte el cambio
                updateSyncMetadataAfterAttachmentOperation()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteAttachmentsByNoteId(noteId: String): Result<Unit> {
        return try {
            // Get attachments before deleting for file cleanup
            val attachments = attachmentsDao.getAttachmentsByNoteId(noteId)
            
            // Delete files from disk
            attachments.forEach { attachment ->
                fileManager.deleteFile(attachment.file_path)
            }
            
            // Delete from database
            attachmentsDao.deleteAttachmentsByNoteId(noteId)
            
            // ✅ NUEVO: Actualizar metadata para que sync inteligente detecte el cambio
            if (attachments.isNotEmpty()) {
                updateSyncMetadataAfterAttachmentOperation()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun countAttachmentsByNoteId(noteId: String): Result<Long> {
        return try {
            val count = attachmentsDao.countAttachmentsByNoteId(noteId)
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Extension function to convert SQLDelight Attachments to domain ArchivoAdjunto
 */
private fun Attachments.toDomainModel(): ArchivoAdjunto {
    return ArchivoAdjunto(
        id = this.id,
        filePath = this.file_path,
        nombreOriginal = this.nombre_original,
        tipoArchivo = if (this.tipo_archivo == 1L) TipoDeArchivo.Imagen else TipoDeArchivo.Video,
        tipoMime = this.tipo_mime,
        tamanoBytes = this.tamano_bytes,
        fechaSubida = this.fecha_subida.toLongOrNull() ?: 0L,
        notaId = this.nota_id
    )
}