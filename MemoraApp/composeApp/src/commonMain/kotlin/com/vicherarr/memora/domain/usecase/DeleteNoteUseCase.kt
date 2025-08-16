package com.vicherarr.memora.domain.usecase

import com.vicherarr.memora.domain.repository.NotesRepository

/**
 * Use Case for deleting notes following Clean Architecture principles
 * Single Responsibility: Only handles note deletion business logic
 * Dependency Inversion: Depends on repository abstraction, not implementation
 */
class DeleteNoteUseCase(
    private val notesRepository: NotesRepository
) {
    
    /**
     * Delete a note by ID
     * Validates input and delegates to repository
     */
    suspend fun execute(noteId: String): Result<Unit> {
        // Business logic: Validate note ID
        if (noteId.isBlank()) {
            return Result.failure(IllegalArgumentException("Note ID cannot be blank"))
        }
        
        // Business logic: Check if note exists before deletion
        val noteExists = notesRepository.getNoteById(noteId)
        if (noteExists.isFailure) {
            return Result.failure(
                IllegalArgumentException("Note with ID '$noteId' does not exist")
            )
        }
        
        // Delegate to repository for deletion
        return notesRepository.deleteNote(noteId)
    }
    
    /**
     * Delete all notes for a specific user
     * Used for cleanup operations (e.g., account deletion)
     */
    suspend fun executeDeleteAllForUser(userId: String): Result<Unit> {
        // Business logic: Validate user ID
        if (userId.isBlank()) {
            return Result.failure(IllegalArgumentException("User ID cannot be blank"))
        }
        
        // Delegate to repository for bulk deletion
        return notesRepository.deleteAllNotesForUser(userId)
    }
}