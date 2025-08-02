package com.vicherarr.memora.data.repository

import com.vicherarr.memora.domain.models.Note
import com.vicherarr.memora.domain.repository.NotesRepository
import kotlinx.coroutines.delay
import kotlin.random.Random

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
                    fechaCreacion = 1700000000L, // Mock timestamp
                    fechaModificacion = 1700000000L,
                    usuarioId = "user_mock"
                ),
                Note(
                    id = "note_2", 
                    titulo = "Lista de Tareas",
                    contenido = "• Completar arquitectura MVVM\n• Implementar UI\n• Conectar con API",
                    fechaCreacion = 1700001000L, // Mock timestamp
                    fechaModificacion = 1700001000L,
                    usuarioId = "user_mock"
                ),
                Note(
                    id = "note_3",
                    titulo = null,
                    contenido = "Nota sin título para probar el comportamiento",
                    fechaCreacion = 1700002000L, // Mock timestamp
                    fechaModificacion = 1700002000L,
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
                id = "note_${Random.nextInt(1000, 9999)}",
                titulo = titulo?.takeIf { it.isNotBlank() },
                contenido = contenido,
                fechaCreacion = 1700003000L, // Mock timestamp
                fechaModificacion = 1700003000L,
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
                    fechaModificacion = 1700004000L // Mock timestamp
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