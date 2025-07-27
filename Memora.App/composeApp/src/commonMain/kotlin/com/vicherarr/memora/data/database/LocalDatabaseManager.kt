package com.vicherarr.memora.data.database

import com.vicherarr.memora.database.MemoraDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Manager para la base de datos local usando SQLDelight
 * Proporciona acceso centralizado a todas las operaciones de base de datos
 */
class LocalDatabaseManager(
    driverFactory: DatabaseDriverFactory
) {
    private val driver = driverFactory.createDriver()
    private val database = MemoraDatabase(driver)
    
    // Queries de la base de datos
    private val userQueries = database.memoraDatabaseQueries
    
    // Operaciones de Usuario
    fun getAllUsers() = userQueries.selectAllUsers().executeAsList()
    
    fun getUserById(id: String) = userQueries.selectUserById(id).executeAsOneOrNull()
    
    fun getUserByEmail(email: String) = userQueries.selectUserByEmail(email).executeAsOneOrNull()
    
    fun insertUser(id: String, username: String, email: String, createdAt: Long) {
        userQueries.insertUser(id, username, email, createdAt)
    }
    
    fun updateUser(id: String, username: String, email: String) {
        userQueries.updateUser(username, email, id)
    }
    
    fun deleteUser(id: String) {
        userQueries.deleteUser(id)
    }
    
    // Operaciones de Nota
    fun getAllNotes(): Flow<List<com.vicherarr.memora.database.Note>> = flow {
        emit(userQueries.selectAllNotes().executeAsList())
    }
    
    fun getNotesByUserId(userId: String): Flow<List<com.vicherarr.memora.database.Note>> = flow {
        emit(userQueries.selectNotesByUserId(userId).executeAsList())
    }
    
    fun getNoteById(id: String) = userQueries.selectNoteById(id).executeAsOneOrNull()
    
    fun searchNotes(userId: String, query: String): Flow<List<com.vicherarr.memora.database.Note>> = flow {
        emit(userQueries.selectNotesByQuery(userId, query, query).executeAsList())
    }
    
    fun getNotesWithPagination(userId: String, limit: Long, offset: Long): Flow<List<com.vicherarr.memora.database.Note>> = flow {
        emit(userQueries.selectNotesWithPagination(userId, limit, offset).executeAsList())
    }
    
    fun insertNote(
        id: String,
        title: String?,
        content: String,
        createdAt: Long,
        modifiedAt: Long,
        userId: String,
        isLocalOnly: Boolean = true
    ) {
        userQueries.insertNote(id, title, content, createdAt, modifiedAt, userId, if (isLocalOnly) 1L else 0L)
    }
    
    fun updateNote(id: String, title: String?, content: String, modifiedAt: Long) {
        userQueries.updateNote(title, content, modifiedAt, id)
    }
    
    fun deleteNote(id: String) {
        userQueries.deleteNote(id)
    }
    
    fun deleteNotesByUserId(userId: String) {
        userQueries.deleteNotesByUserId(userId)
    }
    
    // Operaciones de Attachment
    fun getAttachmentsByNoteId(noteId: String) = userQueries.selectAttachmentsByNoteId(noteId).executeAsList()
    
    fun getAttachmentById(id: String) = userQueries.selectAttachmentById(id).executeAsOneOrNull()
    
    fun insertAttachment(
        id: String,
        originalName: String,
        type: Long, // 1=IMAGE, 2=VIDEO
        mimeType: String,
        sizeBytes: Long,
        uploadedAt: Long,
        noteId: String,
        localPath: String?,
        isUploaded: Boolean = false
    ) {
        userQueries.insertAttachment(
            id, originalName, type, mimeType, sizeBytes, 
            uploadedAt, noteId, localPath, if (isUploaded) 1L else 0L
        )
    }
    
    fun updateAttachment(id: String, originalName: String, localPath: String?, isUploaded: Boolean) {
        userQueries.updateAttachment(originalName, localPath, if (isUploaded) 1L else 0L, id)
    }
    
    fun deleteAttachment(id: String) {
        userQueries.deleteAttachment(id)
    }
    
    fun deleteAttachmentsByNoteId(noteId: String) {
        userQueries.deleteAttachmentsByNoteId(noteId)
    }
    
    /**
     * Cierra la conexión a la base de datos
     */
    fun close() {
        driver.close()
    }
}