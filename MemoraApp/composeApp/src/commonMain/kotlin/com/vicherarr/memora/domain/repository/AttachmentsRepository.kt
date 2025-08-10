package com.vicherarr.memora.domain.repository

import com.vicherarr.memora.domain.models.ArchivoAdjunto
import com.vicherarr.memora.domain.models.MediaFile

/**
 * Repository interface for Attachments operations
 * Clean Architecture - Domain Layer
 * 
 * Responsabilidades:
 * - Gestionar operaciones CRUD de attachments
 * - Actualizar metadatos de sync automáticamente
 * - Abstraer la capa de datos para attachments
 */
interface AttachmentsRepository {
    
    /**
     * Obtiene todos los attachments para una nota específica
     */
    suspend fun getAttachmentsByNoteId(noteId: String): Result<List<ArchivoAdjunto>>
    
    /**
     * Obtiene un attachment específico por ID
     */
    suspend fun getAttachmentById(attachmentId: String): Result<ArchivoAdjunto?>
    
    /**
     * Crea un nuevo attachment y actualiza metadatos
     */
    suspend fun createAttachment(
        noteId: String,
        mediaFile: MediaFile
    ): Result<ArchivoAdjunto>
    
    /**
     * Actualiza un attachment existente y actualiza metadatos
     */
    suspend fun updateAttachment(
        attachmentId: String,
        nombreOriginal: String,
        tipoMime: String
    ): Result<ArchivoAdjunto>
    
    /**
     * Elimina un attachment y actualiza metadatos
     */
    suspend fun deleteAttachment(attachmentId: String): Result<Unit>
    
    /**
     * Elimina todos los attachments de una nota y actualiza metadatos
     */
    suspend fun deleteAttachmentsByNoteId(noteId: String): Result<Unit>
    
    /**
     * Cuenta attachments para una nota específica
     */
    suspend fun countAttachmentsByNoteId(noteId: String): Result<Long>
}