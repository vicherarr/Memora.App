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
     * Insert a new attachment locally
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
        remoteUrl: String? = null
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
            remote_url = remoteUrl
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
}