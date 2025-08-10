package com.vicherarr.memora.sync

import com.vicherarr.memora.data.database.Attachment

/**
 * Repository interface para operaciones de sincronización de attachments
 * Maneja la comunicación con el almacenamiento remoto (Google Drive/iCloud)
 */
expect interface AttachmentSyncRepository {
    
    /**
     * Sube un attachment al almacenamiento remoto
     * @param attachment El attachment a subir
     * @param fileData Los datos del archivo
     * @param userId ID del usuario propietario
     * @return Result con el ID remoto del archivo subido
     */
    suspend fun uploadAttachment(
        attachment: Attachment,
        fileData: ByteArray,
        userId: String
    ): Result<String>
    
    /**
     * Descarga un attachment desde el almacenamiento remoto
     * @param remoteId ID del archivo en el almacenamiento remoto
     * @return Result con los datos del archivo descargado
     */
    suspend fun downloadAttachment(remoteId: String): Result<ByteArray>
    
    /**
     * Lista todos los attachments remotos para un usuario
     * @param userId ID del usuario
     * @return Lista de información de attachments remotos
     */
    suspend fun listRemoteAttachments(userId: String): List<RemoteAttachmentInfo>
    
    /**
     * Elimina un attachment del almacenamiento remoto
     * @param remoteId ID del archivo en el almacenamiento remoto
     * @return Result indicando si la eliminación fue exitosa
     */
    suspend fun deleteRemoteAttachment(remoteId: String): Result<Boolean>
    
    /**
     * Verifica si un attachment existe en el almacenamiento remoto
     * @param remoteId ID del archivo en el almacenamiento remoto
     * @return Result indicando si el archivo existe
     */
    suspend fun remoteAttachmentExists(remoteId: String): Result<Boolean>
    
    /**
     * Obtiene metadatos de un attachment remoto
     * @param remoteId ID del archivo en el almacenamiento remoto
     * @return Result con los metadatos del archivo
     */
    suspend fun getRemoteAttachmentMetadata(remoteId: String): Result<RemoteAttachmentMetadata>
    
    /**
     * Actualiza los metadatos de un attachment remoto
     * @param remoteId ID del archivo en el almacenamiento remoto
     * @param metadata Nuevos metadatos
     * @return Result indicando si la actualización fue exitosa
     */
    suspend fun updateRemoteAttachmentMetadata(
        remoteId: String, 
        metadata: RemoteAttachmentMetadata
    ): Result<Boolean>
}

/**
 * Metadatos de un attachment remoto
 */
data class RemoteAttachmentMetadata(
    val remoteId: String,
    val fileName: String,
    val size: Long,
    val mimeType: String,
    val contentHash: String?,
    val createdAt: Long,
    val modifiedAt: Long,
    val userId: String,
    val noteId: String,
    val customProperties: Map<String, String> = emptyMap()
)

/**
 * Configuración para operaciones de attachment sync
 */
data class AttachmentSyncOptions(
    val enableCompression: Boolean = true,
    val compressionQuality: Int = 85,
    val maxRetries: Int = 3,
    val retryDelayMs: Long = 1000,
    val enableIntegrityCheck: Boolean = true,
    val generateThumbnails: Boolean = true,
    val thumbnailSize: Int = 256
)

/**
 * Factory para crear instancias de AttachmentSyncRepository
 */
expect object AttachmentSyncRepositoryFactory {
    fun create(): AttachmentSyncRepository
}