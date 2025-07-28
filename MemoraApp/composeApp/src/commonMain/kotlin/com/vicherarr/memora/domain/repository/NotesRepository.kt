package com.vicherarr.memora.domain.repository

import com.vicherarr.memora.domain.models.Note
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz del repositorio de notas
 * Define las operaciones CRUD para la gestión de notas
 */
interface NotesRepository {
    
    /**
     * Obtiene todas las notas del usuario
     * @param page Número de página para paginación
     * @param pageSize Tamaño de página
     * @return Flow con la lista de notas
     */
    fun getAllNotes(page: Int = 0, pageSize: Int = 20): Flow<List<Note>>
    
    /**
     * Obtiene una nota específica por su ID
     * @param noteId ID de la nota
     * @return Nota encontrada o null si no existe
     */
    suspend fun getNoteById(noteId: String): Note?
    
    /**
     * Crea una nueva nota
     * @param title Título de la nota (opcional)
     * @param content Contenido de la nota
     * @return Nota creada
     */
    suspend fun createNote(title: String?, content: String): Note
    
    /**
     * Actualiza una nota existente
     * @param note Nota con los datos actualizados
     * @return Nota actualizada
     */
    suspend fun updateNote(note: Note): Note
    
    /**
     * Actualiza una nota existente por ID
     * @param noteId ID de la nota a actualizar
     * @param title Nuevo título de la nota
     * @param content Nuevo contenido de la nota
     * @return Nota actualizada
     */
    suspend fun updateNote(noteId: String, title: String?, content: String): Note
    
    /**
     * Elimina una nota
     * @param noteId ID de la nota a eliminar
     */
    suspend fun deleteNote(noteId: String)
    
    /**
     * Busca notas por texto
     * @param query Texto a buscar
     * @return Flow con las notas que coinciden con la búsqueda
     */
    fun searchNotes(query: String): Flow<List<Note>>
    
    /**
     * Sincroniza las notas locales con el servidor
     */
    suspend fun syncNotes()
    
    /**
     * Obtiene las notas almacenadas localmente
     * @return Flow con las notas locales
     */
    fun getLocalNotes(): Flow<List<Note>>
}