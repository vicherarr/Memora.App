package com.vicherarr.memora.domain.usecases

import com.vicherarr.memora.domain.models.Note
import com.vicherarr.memora.domain.models.MediaFile
import com.vicherarr.memora.domain.models.ArchivoAdjunto
import com.vicherarr.memora.domain.repository.NotesRepository
import com.vicherarr.memora.domain.validation.ValidationService

/**
 * Use Case for updating notes following Clean Architecture principles
 * Single Responsibility: Only handles note update business logic
 * Dependency Inversion: Depends on repository abstraction, not implementation
 */
class UpdateNoteUseCase(
    private val notesRepository: NotesRepository,
    private val validationService: ValidationService
) {
    
    /**
     * Update a note with text content only
     * Validates input and delegates to repository
     */
    suspend fun execute(id: String, titulo: String?, contenido: String): Result<Note> {
        // Business logic: Validate note ID
        if (id.isBlank()) {
            return Result.failure(IllegalArgumentException("Note ID cannot be blank"))
        }
        
        // Business logic: Validate content
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
        return notesRepository.updateNote(id, titulo, contenido)
    }
    
    /**
     * Update a note with text content and manage attachments
     * Validates input, manages attachment changes and delegates to repository
     */
    suspend fun executeWithAttachments(
        noteId: String,
        titulo: String?,
        contenido: String,
        existingAttachments: List<ArchivoAdjunto>,
        newMediaFiles: List<MediaFile>
    ): Result<Note> {
        // Business logic: Validate note ID
        if (noteId.isBlank()) {
            return Result.failure(IllegalArgumentException("Note ID cannot be blank"))
        }
        
        // Business logic: Validate content
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
        
        // Business logic: Basic validation for new attachments
        if (newMediaFiles.isNotEmpty()) {
            for (attachment in newMediaFiles) {
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
        return notesRepository.updateNoteWithAttachments(
            noteId, titulo, contenido, existingAttachments, newMediaFiles
        )
    }
}