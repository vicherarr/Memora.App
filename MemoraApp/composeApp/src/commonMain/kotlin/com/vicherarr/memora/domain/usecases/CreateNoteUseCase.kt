package com.vicherarr.memora.domain.usecases

import com.vicherarr.memora.domain.models.Note
import com.vicherarr.memora.domain.models.MediaFile
import com.vicherarr.memora.domain.repository.NotesRepository
import com.vicherarr.memora.domain.validation.ValidationService

/**
 * Use Case for creating notes following Clean Architecture principles
 * Single Responsibility: Only handles note creation business logic
 * Dependency Inversion: Depends on repository abstraction, not implementation
 */
class CreateNoteUseCase(
    private val notesRepository: NotesRepository,
    private val validationService: ValidationService,
    private val manageNoteCategoriesUseCase: ManageNoteCategoriesUseCase
) {
    
    /**
     * Create a note with text content only
     * Validates input and delegates to repository
     */
    suspend fun execute(titulo: String?, contenido: String, categoryIds: List<String> = emptyList()): Result<Note> {
        // Business logic: Validate input
        val validationResult = validationService.validateNoteContent(contenido)
        if (!validationResult.isValid) {
            return Result.failure(
                IllegalArgumentException(validationResult.errorMessage ?: "Invalid note content")
            )
        }
        
        // Optional title validation
        titulo?.let { title ->
            if (title.isNotBlank()) {
                val titleValidation = validationService.validateNoteTitle(title)
                if (!titleValidation.isValid) {
                    return Result.failure(
                        IllegalArgumentException(titleValidation.errorMessage ?: "Invalid note title")
                    )
                }
            }
        }
        
        // Delegate to repository for persistence
        return notesRepository.createNote(titulo, contenido).also { result ->
            // Assign categories if note creation was successful
            result.onSuccess { note ->
                if (categoryIds.isNotEmpty()) {
                    manageNoteCategoriesUseCase.assignCategoriesToNote(note.id, categoryIds)
                }
            }
        }
    }
    
    /**
     * Create a note with text content and media attachments
     * Validates input, attachments and delegates to repository
     */
    suspend fun executeWithAttachments(
        titulo: String?, 
        contenido: String, 
        attachments: List<MediaFile>,
        categoryIds: List<String> = emptyList()
    ): Result<Note> {
        // Business logic: Validate input
        val validationResult = validationService.validateNoteContent(contenido)
        if (!validationResult.isValid) {
            return Result.failure(
                IllegalArgumentException(validationResult.errorMessage ?: "Invalid note content")
            )
        }
        
        // Optional title validation
        titulo?.let { title ->
            if (title.isNotBlank()) {
                val titleValidation = validationService.validateNoteTitle(title)
                if (!titleValidation.isValid) {
                    return Result.failure(
                        IllegalArgumentException(titleValidation.errorMessage ?: "Invalid note title")
                    )
                }
            }
        }
        
        // Business logic: Basic attachment validation
        if (attachments.isNotEmpty()) {
            for (attachment in attachments) {
                // Basic validation: check file name and data
                if (attachment.fileName.isBlank()) {
                    return Result.failure(
                        IllegalArgumentException("Attachment file name cannot be blank")
                    )
                }
                if (attachment.data.isEmpty()) {
                    return Result.failure(
                        IllegalArgumentException("Attachment data cannot be empty")
                    )
                }
            }
        }
        
        // Delegate to repository for persistence
        return notesRepository.createNoteWithAttachments(titulo, contenido, attachments).also { result ->
            // Assign categories if note creation was successful
            result.onSuccess { note ->
                if (categoryIds.isNotEmpty()) {
                    manageNoteCategoriesUseCase.assignCategoriesToNote(note.id, categoryIds)
                }
            }
        }
    }
}