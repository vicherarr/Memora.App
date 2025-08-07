package com.vicherarr.memora.domain.repository

import com.vicherarr.memora.domain.models.Note
import com.vicherarr.memora.domain.models.MediaFile
import com.vicherarr.memora.domain.models.ArchivoAdjunto

/**
 * Repository interface for notes operations
 */
interface NotesRepository {
    suspend fun getNotes(): Result<List<Note>>
    suspend fun getNoteById(id: String): Result<Note>
    suspend fun createNote(titulo: String?, contenido: String): Result<Note>
    suspend fun createNoteWithAttachments(titulo: String?, contenido: String, attachments: List<MediaFile>): Result<Note>
    suspend fun updateNote(id: String, titulo: String?, contenido: String): Result<Note>
    suspend fun updateNoteWithAttachments(id: String, titulo: String?, contenido: String, newAttachments: List<ArchivoAdjunto>): Result<Note>
    suspend fun deleteNote(id: String): Result<Unit>
}