package com.vicherarr.memora.domain.usecases

import com.vicherarr.memora.domain.models.Note
import com.vicherarr.memora.domain.repository.NotesRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use Case for searching and retrieving notes following Clean Architecture principles
 * Single Responsibility: Only handles note search and retrieval business logic
 * Dependency Inversion: Depends on repository abstraction, not implementation
 */
class SearchNotesUseCase(
    private val notesRepository: NotesRepository
) {
    
    /**
     * Search notes by query string
     * Validates input and delegates to repository
     */
    suspend fun execute(query: String): Result<List<Note>> {
        // Business logic: Trim whitespace and validate query
        val trimmedQuery = query.trim()
        
        // Business logic: Handle empty query - return all notes
        if (trimmedQuery.isEmpty()) {
            return notesRepository.getNotes()
        }
        
        // Business logic: Validate query length (prevent performance issues)
        if (trimmedQuery.length < 2) {
            return Result.failure(
                IllegalArgumentException("Search query must be at least 2 characters long")
            )
        }
        
        // Delegate to repository for search
        return notesRepository.searchNotes(trimmedQuery)
    }
    
    /**
     * Get all notes for the current user
     * No validation needed - direct delegation to repository
     */
    suspend fun executeGetAll(): Result<List<Note>> {
        return notesRepository.getNotes()
    }
    
    /**
     * Get a specific note by ID
     * Validates input and delegates to repository
     */
    suspend fun executeGetById(noteId: String): Result<Note> {
        // Business logic: Validate note ID
        if (noteId.isBlank()) {
            return Result.failure(IllegalArgumentException("Note ID cannot be blank"))
        }
        
        // Delegate to repository for retrieval
        return notesRepository.getNoteById(noteId)
    }
    
    /**
     * Get notes as reactive Flow for UI updates
     * No validation needed - direct delegation to repository
     */
    fun executeGetNotesFlow(): Flow<List<Note>> {
        return notesRepository.getNotesFlow()
    }
}