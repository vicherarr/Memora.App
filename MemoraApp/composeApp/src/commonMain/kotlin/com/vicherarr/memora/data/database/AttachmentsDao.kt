package com.vicherarr.memora.data.database

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.vicherarr.memora.database.MemoraDatabase
import com.vicherarr.memora.database.Attachments
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Attachments table
 * Handles all local CRUD operations for file attachments using SQLDelight
 */
class AttachmentsDao(private val database: MemoraDatabase) {
    
    private val queries = database.attachmentsQueries
    
    /**
     * Get all attachments for a specific note as Flow (reactive)
     */
    fun getAttachmentsByNoteIdFlow(noteId: String): Flow<List<Attachments>> {
        return queries.getAttachmentsByNoteId(noteId)
            .asFlow()
            .mapToList(Dispatchers.IO)
    }
    
    /**
     * Get all attachments for a specific note (one-time query)
     */
    suspend fun getAttachmentsByNoteId(noteId: String): List<Attachments> {
        return queries.getAttachmentsByNoteId(noteId).executeAsList()
    }
    
    /**
     * Get all attachments for a specific note (synchronous version for Flow usage)
     */
    fun getAttachmentsByNoteIdSync(noteId: String): List<Attachments> {
        return queries.getAttachmentsByNoteId(noteId).executeAsList()
    }
    
    /**
     * Get a specific attachment by ID
     */
    suspend fun getAttachmentById(attachmentId: String): Attachments? {
        return queries.getAttachmentById(attachmentId).executeAsOneOrNull()
    }
    
    /**
     * Get attachments that need to be synchronized with the server
     */
    suspend fun getAttachmentsNeedingSync(): List<Attachments> {
        return queries.getAttachmentsNeedingSync().executeAsList()
    }
    
    /**
     * Insert a new attachment locally with enhanced sync fields
     */
    fun insertAttachment(
        id: String,
        filePath: String,
        nombreOriginal: String,
        tipoArchivo: Long, // 1=Imagen, 2=Video
        tipoMime: String,
        tamanoBytes: Long,
        fechaSubida: String,
        notaId: String,
        syncStatus: String = "PENDING",
        needsUpload: Long = 1,
        remoteUrl: String? = null,
        remotePath: String? = null,
        remoteFileId: String? = null,
        contentHash: String? = null,
        downloadStatus: String = "NONE",
        isCachedLocally: Long = 1,
        isStructuredPath: Long = 0
    ) {
        val now = getCurrentTimestamp()
        
        queries.insertAttachment(
            id = id,
            file_path = filePath,
            nombre_original = nombreOriginal,
            tipo_archivo = tipoArchivo,
            tipo_mime = tipoMime,
            tamano_bytes = tamanoBytes,
            fecha_subida = fechaSubida,
            nota_id = notaId,
            sync_status = syncStatus,
            needs_upload = needsUpload,
            local_created_at = now,
            remote_url = remoteUrl,
            remote_path = remotePath,
            remote_file_id = remoteFileId,
            content_hash = contentHash,
            download_status = downloadStatus,
            is_cached_locally = isCachedLocally,
            is_structured_path = isStructuredPath
        )
    }
    
    /**
     * Update an existing attachment
     */
    suspend fun updateAttachment(
        attachmentId: String,
        filePath: String,
        nombreOriginal: String,
        tipoArchivo: Long,
        tipoMime: String,
        tamanoBytes: Long,
        fechaSubida: String
    ) {
        queries.updateAttachment(
            file_path = filePath,
            nombre_original = nombreOriginal,
            tipo_archivo = tipoArchivo,
            tipo_mime = tipoMime,
            tamano_bytes = tamanoBytes,
            fecha_subida = fechaSubida,
            id = attachmentId
        )
    }

    /**
     * Updates the remote URL of an attachment after a successful upload.
     */
    fun updateRemoteUrl(attachmentId: String, url: String) {
        queries.updateRemoteUrl(remote_url = url, id = attachmentId)
    }
    
    /**
     * Delete an attachment by ID
     */
    suspend fun deleteAttachment(attachmentId: String) {
        queries.deleteAttachment(attachmentId)
    }
    
    /**
     * Delete all attachments for a note
     */
    suspend fun deleteAttachmentsByNoteId(noteId: String) {
        queries.deleteAttachmentsByNoteId(noteId)
    }
    
    /**
     * Mark an attachment as successfully synced
     */
    fun markAsSynced(attachmentId: String) {
        val now = getCurrentTimestamp()
        queries.markAsSynced(now, attachmentId)
    }
    
    /**
     * Mark an attachment as failed to sync
     */
    fun markSyncFailed(attachmentId: String) {
        val now = getCurrentTimestamp()
        queries.markSyncFailed(now, attachmentId)
    }
    
    /**
     * Update sync status for a specific attachment
     */
    fun updateSyncStatus(
        attachmentId: String,
        syncStatus: String,
        needsUpload: Long = if (syncStatus == "SYNCED") 0 else 1
    ) {
        val now = getCurrentTimestamp()
        queries.updateSyncStatus(
            sync_status = syncStatus,
            needs_upload = needsUpload,
            last_sync_attempt = now,
            id = attachmentId
        )
    }
    
    /**
     * Count attachments for a specific note (useful for UI badges)
     */
    suspend fun countAttachmentsByNoteId(noteId: String): Long {
        return queries.countAttachmentsByNoteId(noteId).executeAsOne()
    }
    
    /**
     * Get only image attachments for a note
     */
    suspend fun getImageAttachmentsByNoteId(noteId: String): List<Attachments> {
        return queries.getImageAttachmentsByNoteId(noteId).executeAsList()
    }
    
    /**
     * Get only video attachments for a note
     */
    suspend fun getVideoAttachmentsByNoteId(noteId: String): List<Attachments> {
        return queries.getVideoAttachmentsByNoteId(noteId).executeAsList()
    }
    
    // ===== ENHANCED SYNC METHODS =====
    
    /**
     * Get attachments that need to be uploaded to cloud
     */
    suspend fun getAttachmentsNeedingUpload(): List<Attachments> {
        return queries.getAttachmentsNeedingUpload().executeAsList()
    }
    
    /**
     * Get attachments that need to be downloaded from cloud
     */
    suspend fun getAttachmentsNeedingDownload(): List<Attachments> {
        return queries.getAttachmentsNeedingDownload().executeAsList()
    }
    
    /**
     * Get attachments by content hash (for duplicate detection)
     */
    suspend fun getAttachmentsByContentHash(hash: String): List<Attachments> {
        return queries.getAttachmentsByContentHash(hash).executeAsList()
    }
    
    /**
     * Get attachments still using original paths (before sync)
     */
    suspend fun getAttachmentsWithOriginalPaths(): List<Attachments> {
        return queries.getAttachmentsWithOriginalPaths().executeAsList()
    }
    
    /**
     * Get attachments using structured paths (after sync)
     */
    suspend fun getAttachmentsWithStructuredPaths(): List<Attachments> {
        return queries.getAttachmentsWithStructuredPaths().executeAsList()
    }
    
    /**
     * Update attachment to structured path after successful sync
     */
    suspend fun updateToStructuredPath(
        attachmentId: String,
        newFilePath: String,
        remoteFileId: String,
        remotePath: String,
        contentHash: String
    ) {
        val now = getCurrentTimestamp()
        queries.updateToStructuredPath(
            file_path = newFilePath,
            remote_file_id = remoteFileId,
            remote_path = remotePath,
            content_hash = contentHash,
            last_sync_attempt = now,
            id = attachmentId
        )
    }
    
    /**
     * Update sync metadata for an attachment
     */
    suspend fun updateSyncMetadata(
        attachmentId: String,
        remoteFileId: String,
        remotePath: String,
        contentHash: String,
        syncStatus: String = "SYNCED",
        needsUpload: Long = 0
    ) {
        val now = getCurrentTimestamp()
        queries.updateSyncMetadata(
            remote_file_id = remoteFileId,
            remote_path = remotePath,
            content_hash = contentHash,
            sync_status = syncStatus,
            needs_upload = needsUpload,
            last_sync_attempt = now,
            id = attachmentId
        )
    }
    
    /**
     * Update download status for an attachment
     */
    suspend fun updateDownloadStatus(attachmentId: String, downloadStatus: String) {
        val now = getCurrentTimestamp()
        queries.updateDownloadStatus(
            download_status = downloadStatus,
            last_sync_attempt = now,
            id = attachmentId
        )
    }
    
    /**
     * Update cache status for an attachment
     */
    suspend fun updateCacheStatus(attachmentId: String, isCached: Boolean) {
        queries.updateCacheStatus(
            is_cached_locally = if (isCached) 1 else 0,
            id = attachmentId
        )
    }
}