package com.vicherarr.memora.data.mappers

import com.vicherarr.memora.data.database.NotesDao
import com.vicherarr.memora.data.database.AttachmentsDao
import com.vicherarr.memora.domain.models.Note
import com.vicherarr.memora.database.Notes

/**
 * Domain Mapper for Notes following Clean Architecture principles
 * Single Responsibility: Only handles Notes entity <-> Domain model conversion
 * Dependency Inversion: Depends on abstractions (DAOs) not concrete implementations
 */
class NoteDomainMapper(
    private val attachmentsDao: AttachmentsDao,
    private val attachmentMapper: AttachmentDomainMapper
) {
    
    /**
     * Convert SQLDelight Notes entity to Domain model (without attachments)
     * Use when attachments are not needed for performance optimization
     */
    fun toDomainModel(entity: Notes): Note {
        return Note(
            id = entity.id,
            titulo = entity.titulo,
            contenido = entity.contenido,
            fechaCreacion = entity.fecha_creacion.toLongOrNull() ?: getCurrentTimestamp(),
            fechaModificacion = entity.fecha_modificacion.toLongOrNull() ?: getCurrentTimestamp(),
            usuarioId = entity.usuario_id,
            archivosAdjuntos = emptyList() // No attachments in this conversion
        )
    }
    
    /**
     * Convert SQLDelight Notes entity to Domain model WITH attachments
     * Use when full note data with attachments is needed
     */
    suspend fun toDomainModelWithAttachments(entity: Notes): Note {
        val attachments = attachmentsDao.getAttachmentsByNoteId(entity.id)
        
        return Note(
            id = entity.id,
            titulo = entity.titulo,
            contenido = entity.contenido,
            fechaCreacion = entity.fecha_creacion.toLongOrNull() ?: getCurrentTimestamp(),
            fechaModificacion = entity.fecha_modificacion.toLongOrNull() ?: getCurrentTimestamp(),
            usuarioId = entity.usuario_id,
            archivosAdjuntos = attachments.map { attachmentMapper.toDomainModel(it) }
        )
    }
    
    /**
     * Convert list of Notes entities to Domain models with attachments
     * Optimized for bulk operations
     */
    suspend fun toDomainModelListWithAttachments(entities: List<Notes>): List<Note> {
        return entities.map { entity ->
            toDomainModelWithAttachments(entity)
        }
    }
    
    /**
     * Convert list of Notes entities to Domain models without attachments
     * Optimized for performance when attachments are not needed
     */
    fun toDomainModelList(entities: List<Notes>): List<Note> {
        return entities.map { entity ->
            toDomainModel(entity)
        }
    }
    
    /**
     * Helper method to get current timestamp
     * Centralized for consistency across mapping operations
     */
    private fun getCurrentTimestamp(): Long {
        return System.currentTimeMillis()
    }
}