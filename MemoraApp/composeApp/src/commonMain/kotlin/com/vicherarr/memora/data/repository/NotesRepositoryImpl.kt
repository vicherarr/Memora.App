package com.vicherarr.memora.data.repository

import com.vicherarr.memora.data.api.MemoraApiService
import com.vicherarr.memora.data.api.safeApiCall
import com.vicherarr.memora.data.database.LocalDatabaseManager
import com.vicherarr.memora.domain.models.Note
import com.vicherarr.memora.domain.repository.NotesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock

/**
 * Implementación del repositorio de notas
 * Combina API remota con almacenamiento local
 */
class NotesRepositoryImpl(
    private val apiService: MemoraApiService,
    private val localDatabase: LocalDatabaseManager
) : NotesRepository {
    
    override fun getAllNotes(page: Int, pageSize: Int): Flow<List<Note>> {
        // TODO: Implementar sincronización entre local y remoto
        return localDatabase.getAllNotes().map { localNotes ->
            localNotes.map { localNote ->
                Note(
                    id = localNote.id,
                    title = localNote.title,
                    content = localNote.content,
                    createdAt = Clock.System.now(), // TODO: convertir desde timestamp
                    modifiedAt = Clock.System.now(), // TODO: convertir desde timestamp
                    userId = localNote.userId,
                    attachments = emptyList(), // TODO: cargar attachments
                    isLocalOnly = localNote.isLocalOnly == 1L
                )
            }
        }
    }
    
    override suspend fun getNoteById(noteId: String): Note? {
        // Primero intentar desde cache local
        val localNote = localDatabase.getNoteById(noteId)
        
        if (localNote != null) {
            return Note(
                id = localNote.id,
                title = localNote.title,
                content = localNote.content,
                createdAt = Clock.System.now(), // TODO: convertir desde timestamp
                modifiedAt = Clock.System.now(), // TODO: convertir desde timestamp
                userId = localNote.userId,
                attachments = emptyList(), // TODO: cargar attachments
                isLocalOnly = localNote.isLocalOnly == 1L
            )
        }
        
        // Si no está local, intentar desde API
        val result = safeApiCall {
            apiService.getNoteById(noteId)
        }
        
        return when (result) {
            is com.vicherarr.memora.data.api.ApiResult.Success -> {
                val dto = result.data
                Note(
                    id = dto.id,
                    title = dto.titulo,
                    content = dto.contenido,
                    createdAt = Clock.System.now(), // TODO: parsear fechaCreacion
                    modifiedAt = Clock.System.now(), // TODO: parsear fechaModificacion
                    userId = dto.usuarioId,
                    attachments = emptyList(), // TODO: cargar attachments
                    isLocalOnly = false
                )
            }
            is com.vicherarr.memora.data.api.ApiResult.Error -> {
                null // No se pudo obtener la nota
            }
        }
    }
    
    override suspend fun createNote(title: String?, content: String): Note {
        val noteId = generateNoteId()
        val now = Clock.System.now()
        
        // Crear nota localmente primero
        localDatabase.insertNote(
            id = noteId,
            title = title,
            content = content,
            createdAt = now.toEpochMilliseconds(),
            modifiedAt = now.toEpochMilliseconds(),
            userId = getCurrentUserId(), // TODO: obtener user ID actual
            isLocalOnly = true
        )
        
        // TODO: Intentar sincronizar con API en background
        
        return Note(
            id = noteId,
            title = title,
            content = content,
            createdAt = now,
            modifiedAt = now,
            userId = getCurrentUserId(),
            attachments = emptyList(),
            isLocalOnly = true
        )
    }
    
    override suspend fun updateNote(note: Note): Note {
        val now = Clock.System.now()
        
        // Actualizar en base de datos local
        localDatabase.updateNote(
            id = note.id,
            title = note.title,
            content = note.content,
            modifiedAt = now.toEpochMilliseconds()
        )
        
        // TODO: Intentar sincronizar con API en background
        
        return note.copy(modifiedAt = now)
    }
    
    override suspend fun updateNote(noteId: String, title: String?, content: String): Note {
        val now = Clock.System.now()
        
        // Actualizar en base de datos local
        localDatabase.updateNote(
            id = noteId,
            title = title,
            content = content,
            modifiedAt = now.toEpochMilliseconds()
        )
        
        // TODO: Intentar sincronizar con API en background
        
        // Obtener la nota actualizada
        val updatedNote = getNoteById(noteId)
        return updatedNote ?: throw Exception("No se pudo actualizar la nota")
    }
    
    override suspend fun deleteNote(noteId: String) {
        // Eliminar de base de datos local
        localDatabase.deleteNote(noteId)
        
        // TODO: Marcar para eliminación en API en background
    }
    
    override fun searchNotes(query: String): Flow<List<Note>> {
        return localDatabase.searchNotes(getCurrentUserId(), query).map { localNotes ->
            localNotes.map { localNote ->
                Note(
                    id = localNote.id,
                    title = localNote.title,
                    content = localNote.content,
                    createdAt = Clock.System.now(), // TODO: convertir desde timestamp
                    modifiedAt = Clock.System.now(), // TODO: convertir desde timestamp
                    userId = localNote.userId,
                    attachments = emptyList(), // TODO: cargar attachments
                    isLocalOnly = localNote.isLocalOnly == 1L
                )
            }
        }
    }
    
    override suspend fun syncNotes() {
        // TODO: Implementar sincronización completa con API
    }
    
    override fun getLocalNotes(): Flow<List<Note>> {
        return getAllNotes() // Por ahora es lo mismo
    }
    
    // Funciones auxiliares privadas
    private fun generateNoteId(): String {
        return "note_${Clock.System.now().toEpochMilliseconds()}"
    }
    
    private fun getCurrentUserId(): String {
        // TODO: Obtener el ID del usuario actual desde AuthRepository
        return "temp_user_id"
    }
}