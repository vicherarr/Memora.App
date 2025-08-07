package com.vicherarr.memora.data.repository

import com.vicherarr.memora.data.api.NotesApi
import com.vicherarr.memora.data.database.AttachmentsDao
import com.vicherarr.memora.data.database.NotesDao
import com.vicherarr.memora.data.database.getCurrentTimestamp
import com.vicherarr.memora.data.dto.CreateNotaDto
import com.vicherarr.memora.data.dto.UpdateNotaDto
import com.vicherarr.memora.database.Notes
import com.vicherarr.memora.database.Attachments
import com.vicherarr.memora.domain.models.Note
import com.vicherarr.memora.domain.models.ArchivoAdjunto
import com.vicherarr.memora.domain.models.TipoDeArchivo
import com.vicherarr.memora.domain.models.MediaFile
import com.vicherarr.memora.domain.models.MediaType
import com.vicherarr.memora.domain.repository.NotesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Local-first implementation of NotesRepository
 * - All operations go to local database first (immediate UI response)
 * - Background sync with remote API when available
 * - Offline-first approach for better UX
 */
class NotesRepositoryImpl(
    private val notesDao: NotesDao,
    private val attachmentsDao: AttachmentsDao,
    private val notesApi: NotesApi
) : NotesRepository {
    
    // TODO: Get current user ID from AuthRepository
    private val currentUserId = "user_mock" // Temporary hardcoded value
    
    override suspend fun getNotes(): Result<List<Note>> {
        return try {
            // LOCAL-FIRST: Get notes from local database immediately
            val localNotes = notesDao.getNotesByUserId(currentUserId)
            val domainNotes = localNotes.map { note ->
                note.toDomainModelWithAttachments()
            }
            
            // TODO: Background sync with API will be handled by SyncRepository
            Result.success(domainNotes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getNoteById(id: String): Result<Note> {
        return try {
            // LOCAL-FIRST: Get note from local database WITH attachments
            val localNote = notesDao.getNoteById(id)
            if (localNote != null) {
                Result.success(localNote.toDomainModelWithAttachments())
            } else {
                Result.failure(Exception("Note not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun createNote(titulo: String?, contenido: String): Result<Note> {
        return try {
            // Generate simple ID without experimental APIs
            val noteId = "note_${getCurrentTimestamp()}"
            val now = getCurrentTimestamp().toString()
            
            // LOCAL-FIRST: Insert to local database immediately  
            notesDao.insertNote(
                id = noteId,
                titulo = titulo?.takeIf { it.isNotBlank() },
                contenido = contenido,
                fechaCreacion = now,
                fechaModificacion = now,
                usuarioId = currentUserId,
                syncStatus = "PENDING", // Mark for sync
                needsUpload = 1
            )
            
            // Return the created note
            val createdNote = Note(
                id = noteId,
                titulo = titulo?.takeIf { it.isNotBlank() },
                contenido = contenido,
                fechaCreacion = getCurrentTimestamp(),
                fechaModificacion = getCurrentTimestamp(),
                usuarioId = currentUserId
            )
            
            // TODO: Background sync will be handled by SyncRepository
            Result.success(createdNote)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun createNoteWithAttachments(titulo: String?, contenido: String, attachments: List<MediaFile>): Result<Note> {
        return try {
            // Generate simple ID without experimental APIs
            val noteId = "note_${getCurrentTimestamp()}"
            val now = getCurrentTimestamp().toString()
            
            // LOCAL-FIRST: Insert note to local database immediately  
            notesDao.insertNote(
                id = noteId,
                titulo = titulo?.takeIf { it.isNotBlank() },
                contenido = contenido,
                fechaCreacion = now,
                fechaModificacion = now,
                usuarioId = currentUserId,
                syncStatus = "PENDING", // Mark for sync
                needsUpload = 1
            )
            
            // Insert attachments
            val attachmentsList = mutableListOf<ArchivoAdjunto>()
            attachments.forEachIndexed { index, mediaFile ->
                val attachmentId = "attachment_${getCurrentTimestamp()}_$index"
                
                // Convert MediaType to TipoDeArchivo
                val tipoArchivo = when (mediaFile.type) {
                    MediaType.IMAGE -> TipoDeArchivo.Imagen
                    MediaType.VIDEO -> TipoDeArchivo.Video
                }
                
                // Insert attachment to database
                attachmentsDao.insertAttachment(
                    id = attachmentId,
                    datosArchivo = mediaFile.data,
                    nombreOriginal = mediaFile.fileName,
                    tipoArchivo = tipoArchivo.ordinal.toLong() + 1, // 1=Imagen, 2=Video
                    tipoMime = mediaFile.mimeType ?: "application/octet-stream",
                    tamanoBytes = mediaFile.data.size.toLong(),
                    fechaSubida = now,
                    notaId = noteId
                )
                
                // Create domain model for return value
                attachmentsList.add(
                    ArchivoAdjunto(
                        id = attachmentId,
                        datosArchivo = mediaFile.data,
                        nombreOriginal = mediaFile.fileName,
                        tipoArchivo = tipoArchivo,
                        tipoMime = mediaFile.mimeType ?: "application/octet-stream",
                        tamanoBytes = mediaFile.data.size.toLong(),
                        fechaSubida = getCurrentTimestamp(),
                        notaId = noteId
                    )
                )
            }
            
            // Return the created note with attachments
            val createdNote = Note(
                id = noteId,
                titulo = titulo?.takeIf { it.isNotBlank() },
                contenido = contenido,
                fechaCreacion = getCurrentTimestamp(),
                fechaModificacion = getCurrentTimestamp(),
                usuarioId = currentUserId,
                archivosAdjuntos = attachmentsList
            )
            
            // TODO: Background sync will be handled by SyncRepository
            Result.success(createdNote)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateNote(id: String, titulo: String?, contenido: String): Result<Note> {
        return try {
            val now = getCurrentTimestamp().toString()
            
            // LOCAL-FIRST: Update in local database immediately
            notesDao.updateNote(
                noteId = id,
                titulo = titulo?.takeIf { it.isNotBlank() },
                contenido = contenido,
                fechaModificacion = now
            )
            
            // Get updated note to return
            val updatedNote = notesDao.getNoteById(id)
            if (updatedNote != null) {
                // TODO: Background sync will be handled by SyncRepository
                Result.success(updatedNote.toDomainModel())
            } else {
                Result.failure(Exception("Note not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateNoteWithAttachments(id: String, titulo: String?, contenido: String, newAttachments: List<ArchivoAdjunto>): Result<Note> {
        return try {
            val now = getCurrentTimestamp().toString()
            
            // LOCAL-FIRST: Update note in local database immediately
            notesDao.updateNote(
                noteId = id,
                titulo = titulo?.takeIf { it.isNotBlank() },
                contenido = contenido,
                fechaModificacion = now
            )
            
            // Delete all existing attachments for this note
            attachmentsDao.deleteAttachmentsByNoteId(id)
            
            // Insert new attachments
            for (attachment in newAttachments) {
                attachmentsDao.insertAttachment(
                    id = attachment.id,
                    datosArchivo = attachment.datosArchivo,
                    nombreOriginal = attachment.nombreOriginal,
                    tipoArchivo = attachment.tipoArchivo.ordinal.toLong() + 1, // 1=Imagen, 2=Video
                    tipoMime = attachment.tipoMime,
                    tamanoBytes = attachment.tamanoBytes,
                    fechaSubida = now,
                    notaId = id
                )
            }
            
            // Get updated note with attachments to return
            val updatedNote = notesDao.getNoteById(id)
            if (updatedNote != null) {
                // TODO: Background sync will be handled by SyncRepository
                Result.success(updatedNote.toDomainModelWithAttachments())
            } else {
                Result.failure(Exception("Note not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteNote(id: String): Result<Unit> {
        return try {
            // LOCAL-FIRST: Delete from local database immediately
            notesDao.deleteNote(id)
            
            // TODO: Background sync will be handled by SyncRepository
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get notes as Flow for reactive UI updates
     */
    fun getNotesFlow(): Flow<List<Note>> {
        return notesDao.getNotesByUserIdFlow(currentUserId)
            .map { notesList -> notesList.map { it.toDomainModel() } }
    }
    
    // MAPPING FUNCTIONS
    
    /**
     * Convert SQLDelight Notes entity to Domain model
     */
    private fun Notes.toDomainModel(): Note {
        return Note(
            id = this.id,
            titulo = this.titulo,
            contenido = this.contenido,
            fechaCreacion = this.fecha_creacion.toLongOrNull() ?: getCurrentTimestamp(),
            fechaModificacion = this.fecha_modificacion.toLongOrNull() ?: getCurrentTimestamp(),
            usuarioId = this.usuario_id
        )
    }
    
    /**
     * Convert SQLDelight Notes entity to Domain model WITH attachments
     */
    private suspend fun Notes.toDomainModelWithAttachments(): Note {
        val attachments = attachmentsDao.getAttachmentsByNoteId(this.id)
        
        return Note(
            id = this.id,
            titulo = this.titulo,
            contenido = this.contenido,
            fechaCreacion = this.fecha_creacion.toLongOrNull() ?: getCurrentTimestamp(),
            fechaModificacion = this.fecha_modificacion.toLongOrNull() ?: getCurrentTimestamp(),
            usuarioId = this.usuario_id,
            archivosAdjuntos = attachments.map { it.toDomainModel() }
        )
    }
    
    /**
     * Convert SQLDelight Attachments entity to Domain model
     */
    private fun Attachments.toDomainModel(): ArchivoAdjunto {
        return ArchivoAdjunto(
            id = this.id,
            datosArchivo = this.datos_archivo,
            nombreOriginal = this.nombre_original,
            tipoArchivo = when (this.tipo_archivo.toInt()) {
                1 -> TipoDeArchivo.Imagen
                2 -> TipoDeArchivo.Video
                else -> TipoDeArchivo.Imagen
            },
            tipoMime = this.tipo_mime,
            tamanoBytes = this.tamano_bytes,
            fechaSubida = this.fecha_subida.toLongOrNull() ?: getCurrentTimestamp(),
            notaId = this.nota_id
        )
    }
}