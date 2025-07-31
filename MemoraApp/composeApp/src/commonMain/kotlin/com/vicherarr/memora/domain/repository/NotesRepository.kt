package com.vicherarr.memora.domain.repository

import com.vicherarr.memora.domain.models.Note

/**
 * Repository interface for notes operations
 */
interface NotesRepository {
    suspend fun getNotes(): Result<List<Note>>
    suspend fun getNoteById(id: String): Result<Note>
    suspend fun createNote(titulo: String?, contenido: String): Result<Note>
    suspend fun updateNote(id: String, titulo: String?, contenido: String): Result<Note>
    suspend fun deleteNote(id: String): Result<Unit>
}