package com.vicherarr.memora.data.repository

import com.vicherarr.memora.domain.models.Note
import com.vicherarr.memora.domain.repository.NotesRepository
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock

/**
 * Mock implementation of NotesRepository for development and testing  
 * TODO: Replace with real implementation using Ktor + SQLDelight
 */
class NotesRepositoryImpl : NotesRepository {
    
    private val mockNotes = mutableListOf<Note>()
    
    init {
        // Add some mock data
        mockNotes.addAll(
            listOf(
                Note(
                    id = "note_1",
                    titulo = "Primera Nota",
                    contenido = "Esta es mi primera nota de prueba",
                    fechaCreacion = Clock.System.now().toEpochMilliseconds() - 86400000, // 1 day ago
                    fechaModificacion = Clock.System.now().toEpochMilliseconds() - 86400000,
                    usuarioId = "user_mock"
                ),
                Note(
                    id = "note_2", 
                    titulo = "Lista de Tareas",
                    contenido = "• Completar arquitectura MVVM\n• Implementar UI\n• Conectar con API",
                    fechaCreacion = Clock.System.now().toEpochMilliseconds() - 43200000, // 12 hours ago
                    fechaModificacion = Clock.System.now().toEpochMilliseconds() - 43200000,
                    usuarioId = "user_mock"
                ),
                Note(
                    id = "note_3",
                    titulo = null,
                    contenido = "Nota sin título para probar el comportamiento",
                    fechaCreacion = Clock.System.now().toEpochMilliseconds() - 3600000, // 1 hour ago
                    fechaModificacion = Clock.System.now().toEpochMilliseconds() - 3600000,
                    usuarioId = "user_mock"
                )
            )
        )
    }
    
    override suspend fun getNotes(): Result<List<Note>> {
        return try {
            // Simulate network delay
            delay(800)
            Result.success(mockNotes.sortedByDescending { it.fechaModificacion })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getNoteById(id: String): Result<Note> {
        return try {
            delay(300)
            val note = mockNotes.find { it.id == id }
            if (note != null) {
                Result.success(note)
            } else {
                Result.failure(Exception("Note not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun createNote(titulo: String?, contenido: String): Result<Note> {
        return try {
            delay(500)
            val newNote = Note(
                id = "note_${Clock.System.now().toEpochMilliseconds()}",
                titulo = titulo?.takeIf { it.isNotBlank() },
                contenido = contenido,
                fechaCreacion = Clock.System.now().toEpochMilliseconds(),
                fechaModificacion = Clock.System.now().toEpochMilliseconds(),
                usuarioId = "user_mock"
            )
            mockNotes.add(newNote)
            Result.success(newNote)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateNote(id: String, titulo: String?, contenido: String): Result<Note> {
        return try {
            delay(500)
            val index = mockNotes.indexOfFirst { it.id == id }
            if (index != -1) {
                val updatedNote = mockNotes[index].copy(
                    titulo = titulo?.takeIf { it.isNotBlank() },
                    contenido = contenido,
                    fechaModificacion = Clock.System.now().toEpochMilliseconds()
                )
                mockNotes[index] = updatedNote
                Result.success(updatedNote)
            } else {
                Result.failure(Exception("Note not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteNote(id: String): Result<Unit> {
        return try {
            delay(300)
            val index = mockNotes.indexOfFirst { note -> note.id == id }
            if (index != -1) {
                mockNotes.removeAt(index)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Note not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}