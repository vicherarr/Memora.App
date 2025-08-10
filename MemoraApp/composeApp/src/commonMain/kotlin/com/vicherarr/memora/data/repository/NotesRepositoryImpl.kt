package com.vicherarr.memora.data.repository

import com.vicherarr.memora.data.api.NotesApi
import com.vicherarr.memora.data.auth.CloudAuthProvider
import com.vicherarr.memora.data.database.AttachmentsDao
import com.vicherarr.memora.data.database.NotesDao
import com.vicherarr.memora.data.database.getCurrentTimestamp
import com.vicherarr.memora.data.dto.CreateNotaDto
import com.vicherarr.memora.data.dto.UpdateNotaDto
import com.vicherarr.memora.database.Notes
import com.vicherarr.memora.database.Attachments
import com.vicherarr.memora.domain.model.AuthState
import com.vicherarr.memora.domain.models.Note
import com.vicherarr.memora.domain.models.ArchivoAdjunto
import com.vicherarr.memora.domain.models.TipoDeArchivo
import com.vicherarr.memora.domain.models.MediaFile
import com.vicherarr.memora.domain.models.MediaType
import com.vicherarr.memora.domain.platform.FileManager
import com.vicherarr.memora.domain.repository.NotesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine

/**
 * Local-first implementation of NotesRepository
 * - All operations go to local database first (immediate UI response)
 * - Background sync with remote API when available
 * - Offline-first approach for better UX
 */
class NotesRepositoryImpl(
    private val notesDao: NotesDao,
    private val attachmentsDao: AttachmentsDao,
    private val notesApi: NotesApi,
    private val fileManager: FileManager,
    private val cloudAuthProvider: CloudAuthProvider
) : NotesRepository {

    private fun getCurrentUserId(): String {
        println("====== NotesRepository: getCurrentUserId ======")
        val authState = cloudAuthProvider.authState.value
        val userId = when (authState) {
            is AuthState.Authenticated -> {
                println("✅ Usuario autenticado: ${authState.user.email}")
                authState.user.email
            }
            else -> {
                println("❌ Usuario NO autenticado. Usando ID de fallback.")
                "unauthenticated_user" // Fallback
            }
        }
        println("==============================================")
        return userId
    }
    
    override suspend fun getNotes(): Result<List<Note>> {
        return try {
            // LOCAL-FIRST: Get notes from local database immediately
            val localNotes = notesDao.getNotesByUserId(getCurrentUserId())
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
                usuarioId = getCurrentUserId(),
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
                usuarioId = getCurrentUserId()
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
                usuarioId = getCurrentUserId(),
                syncStatus = "PENDING", // Mark for sync
                needsUpload = 1
            )
            
            // Insert attachments
            val attachmentsList = mutableListOf<ArchivoAdjunto>()
            attachments.forEachIndexed { index, mediaFile ->
                val savedFile = fileManager.saveFile(mediaFile.data, mediaFile.fileName, mediaFile.type)
                if (savedFile != null) {
                    val attachmentId = "attachment_${getCurrentTimestamp()}_$index"
                    
                    // Convert MediaType to TipoDeArchivo
                    val tipoArchivo = when (mediaFile.type) {
                        MediaType.IMAGE -> TipoDeArchivo.Imagen
                        MediaType.VIDEO -> TipoDeArchivo.Video
                    }
                    
                    // Insert attachment to database
                    attachmentsDao.insertAttachment(
                        id = attachmentId,
                        filePath = savedFile.path,
                        nombreOriginal = mediaFile.fileName,
                        tipoArchivo = tipoArchivo.ordinal.toLong() + 1, // 1=Imagen, 2=Video
                        tipoMime = mediaFile.mimeType ?: "application/octet-stream",
                        tamanoBytes = savedFile.size,
                        fechaSubida = now,
                        notaId = noteId
                    )
                    
                    // Create domain model for return value
                    attachmentsList.add(
                        ArchivoAdjunto(
                            id = attachmentId,
                            filePath = savedFile.path,
                            nombreOriginal = mediaFile.fileName,
                            tipoArchivo = tipoArchivo,
                            tipoMime = mediaFile.mimeType ?: "application/octet-stream",
                            tamanoBytes = savedFile.size,
                            fechaSubida = getCurrentTimestamp(),
                            notaId = noteId
                        )
                    )
                }
            }
            
            // Return the created note with attachments
            val createdNote = Note(
                id = noteId,
                titulo = titulo?.takeIf { it.isNotBlank() },
                contenido = contenido,
                fechaCreacion = getCurrentTimestamp(),
                fechaModificacion = getCurrentTimestamp(),
                usuarioId = getCurrentUserId(),
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
    
    override suspend fun updateNoteWithAttachments(
        noteId: String,
        titulo: String?,
        contenido: String,
        existingAttachments: List<ArchivoAdjunto>,
        newMediaFiles: List<MediaFile>
    ): Result<Note> {
        return try {
            val now = getCurrentTimestamp().toString()

            // 1. Update the note's text content
            notesDao.updateNote(
                noteId = noteId,
                titulo = titulo?.takeIf { it.isNotBlank() },
                contenido = contenido,
                fechaModificacion = now
            )

            // 2. Determine which attachments to delete
            val originalAttachments = attachmentsDao.getAttachmentsByNoteId(noteId)
            val existingIds = existingAttachments.map { it.id }.toSet()
            val attachmentsToDelete = originalAttachments.filter { it.id !in existingIds }

            for (attachment in attachmentsToDelete) {
                // Delete file from disk and then from DB
                fileManager.deleteFile(attachment.file_path)
                attachmentsDao.deleteAttachment(attachment.id)
            }

            // 3. Save new media files and add them to the database
            for (mediaFile in newMediaFiles) {
                val savedFile = fileManager.saveFile(mediaFile.data, mediaFile.fileName, mediaFile.type)
                if (savedFile != null) {
                    val attachmentId = "attachment_${getCurrentTimestamp()}_${mediaFile.fileName.hashCode()}"
                    val tipoArchivo = when (mediaFile.type) {
                        MediaType.IMAGE -> TipoDeArchivo.Imagen
                        MediaType.VIDEO -> TipoDeArchivo.Video
                    }
                    attachmentsDao.insertAttachment(
                        id = attachmentId,
                        filePath = savedFile.path,
                        nombreOriginal = mediaFile.fileName,
                        tipoArchivo = tipoArchivo.ordinal.toLong() + 1,
                        tipoMime = mediaFile.mimeType,
                        tamanoBytes = savedFile.size,
                        fechaSubida = now,
                        notaId = noteId
                    )
                }
            }

            // 4. Return the fully updated note
            val updatedNote = notesDao.getNoteById(noteId)
            if (updatedNote != null) {
                Result.success(updatedNote.toDomainModelWithAttachments())
            } else {
                Result.failure(Exception("Note not found after update"))
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
    
    override suspend fun deleteAllNotesForUser(userId: String): Result<Unit> {
        return try {
            // LOCAL-FIRST: Delete all notes for user from local database
            // This also deletes attachments due to CASCADE constraint
            notesDao.deleteAllNotesForUser(userId)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get notes as Flow for reactive UI updates
     */
    override fun getNotesFlow(): Flow<List<Note>> {
        return cloudAuthProvider.authState.flatMapLatest { authState ->
            when (authState) {
                is AuthState.Authenticated -> {
                    println("NotesRepository: AuthState changed to Authenticated. Fetching notes for ${authState.user.email}")
                    // Use custom flow that combines notes with their attachments
                    getNotesWithAttachmentsFlow(authState.user.email)
                }
                else -> {
                    println("NotesRepository: AuthState changed to Unauthenticated. Returning empty list.")
                    flowOf(emptyList())
                }
            }
        }
    }
    
    /**
     * Private helper to get notes with attachments as Flow
     * Uses MVVM + Clean Architecture: combines notes and attachments flows for reactive updates
     */
    private fun getNotesWithAttachmentsFlow(userId: String): Flow<List<Note>> {
        // CLEAN ARCHITECTURE: Combine flows from different data sources
        // This ensures UI refreshes when EITHER notes OR attachments change
        return combine(
            notesDao.getNotesByUserIdFlow(userId), // Flow for notes changes
            attachmentsDao.getAllAttachmentsFlow()  // Flow for ANY attachment changes
        ) { notesList, allAttachments ->
            
            // Create a map for efficient attachment lookup by note_id
            val attachmentsByNoteId = allAttachments.groupBy { it.nota_id }
            
            // Transform notes with their corresponding attachments
            notesList.map { note ->
                val noteAttachments = attachmentsByNoteId[note.id] ?: emptyList()
                
                // Convert to domain model with attachments
                Note(
                    id = note.id,
                    titulo = note.titulo,
                    contenido = note.contenido,
                    fechaCreacion = note.fecha_creacion.toLongOrNull() ?: getCurrentTimestamp(),
                    fechaModificacion = note.fecha_modificacion.toLongOrNull() ?: getCurrentTimestamp(),
                    usuarioId = note.usuario_id,
                    archivosAdjuntos = noteAttachments.map { it.toDomainModel() }
                )
            }
        }
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
            filePath = this.file_path,
            remoteUrl = this.remote_url,
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