package com.vicherarr.memora.data.database

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.vicherarr.memora.database.MemoraDatabase
import com.vicherarr.memora.database.Notes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Notes table
 * Handles all local CRUD operations using SQLDelight
 */
class NotesDao(private val database: MemoraDatabase) {
    
    private val queries = database.notesQueries
    
    /**
     * Get all notes for a specific user as Flow (reactive)
     */
    fun getNotesByUserIdFlow(userId: String): Flow<List<Notes>> {
        return queries.getNotesByUserId(userId)
            .asFlow()
            .mapToList(Dispatchers.IO)
    }
    
    /**
     * Get all notes for a specific user (one-time query)
     */
    suspend fun getNotesByUserId(userId: String): List<Notes> {
        return queries.getNotesByUserId(userId).executeAsList()
    }
    
    /**
     * Get a specific note by ID
     */
    suspend fun getNoteById(noteId: String): Notes? {
        return queries.getNoteById(noteId).executeAsOneOrNull()
    }
    
    /**
     * Get notes that need to be synchronized with the server
     */
    suspend fun getNotesNeedingSync(): List<Notes> {
        return queries.getNotesNeedingSync().executeAsList()
    }
    
    /**
     * Insert a new note locally
     */
    fun insertNote(
        id: String,
        titulo: String?,
        contenido: String,
        fechaCreacion: String,
        fechaModificacion: String,
        usuarioId: String,
        syncStatus: String = "PENDING",
        needsUpload: Long = 1
    ) {
        val now = getCurrentTimestamp()
        
        queries.insertNote(
            id = id,
            titulo = titulo,
            contenido = contenido,
            fecha_creacion = fechaCreacion,
            fecha_modificacion = fechaModificacion,
            usuario_id = usuarioId,
            sync_status = syncStatus,
            needs_upload = needsUpload,
            local_created_at = now
        )
    }
    
    /**
     * Update an existing note
     */
    suspend fun updateNote(
        noteId: String,
        titulo: String?,
        contenido: String,
        fechaModificacion: String
    ) {
        queries.updateNote(
            titulo = titulo,
            contenido = contenido,
            fecha_modificacion = fechaModificacion,
            id = noteId
        )
    }
    
    /**
     * Delete a note by ID
     */
    suspend fun deleteNote(noteId: String) {
        queries.deleteNote(noteId)
    }
    
    /**
     * Delete all notes for a user (for logout/cleanup)
     */
    suspend fun deleteNotesByUserId(userId: String) {
        queries.deleteNotesByUserId(userId)
    }
    
    /**
     * Mark a note as successfully synced
     */
    fun markAsSynced(noteId: String) {
        val now = getCurrentTimestamp()
        queries.markAsSynced(now, noteId)
    }
    
    /**
     * Mark a note as failed to sync
     */
    fun markSyncFailed(noteId: String) {
        val now = getCurrentTimestamp()
        queries.markSyncFailed(now, noteId)
    }
    
    /**
     * Update sync status for a specific note
     */
    fun updateSyncStatus(
        noteId: String,
        syncStatus: String,
        needsUpload: Long = if (syncStatus == "SYNCED") 0 else 1
    ) {
        val now = getCurrentTimestamp()
        queries.updateSyncStatus(
            sync_status = syncStatus,
            needs_upload = needsUpload,
            last_sync_attempt = now,
            id = noteId
        )
    }
}