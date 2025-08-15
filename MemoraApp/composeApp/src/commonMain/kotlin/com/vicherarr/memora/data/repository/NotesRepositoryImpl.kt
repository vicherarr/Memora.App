package com.vicherarr.memora.data.repository

import com.vicherarr.memora.data.api.NotesApi
import com.vicherarr.memora.data.auth.CloudAuthProvider
import com.vicherarr.memora.data.database.AttachmentsDao
import com.vicherarr.memora.data.database.NotesDao
import com.vicherarr.memora.data.database.CategoriesDao
import com.vicherarr.memora.data.database.NoteCategoriesDao
import com.vicherarr.memora.data.database.getCurrentTimestamp
import com.vicherarr.memora.data.dto.CreateNotaDto
import com.vicherarr.memora.data.dto.UpdateNotaDto
import com.vicherarr.memora.data.mappers.NoteDomainMapper
import com.vicherarr.memora.data.mappers.AttachmentDomainMapper
import com.vicherarr.memora.data.mappers.CategoryDomainMapper
import com.vicherarr.memora.database.Notes
import com.vicherarr.memora.database.Attachments
import com.vicherarr.memora.domain.model.AuthState
import com.vicherarr.memora.domain.models.Note
import com.vicherarr.memora.domain.models.ArchivoAdjunto
import com.vicherarr.memora.domain.models.TipoDeArchivo
import com.vicherarr.memora.domain.models.Category
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
    private val categoriesDao: CategoriesDao,
    private val noteCategoriesDao: NoteCategoriesDao,
    private val notesApi: NotesApi,
    private val fileManager: FileManager,
    private val cloudAuthProvider: CloudAuthProvider,
    private val syncMetadataRepository: com.vicherarr.memora.domain.repository.SyncMetadataRepository,
    private val deletionsDao: com.vicherarr.memora.data.database.DeletionsDao,
    private val noteDomainMapper: NoteDomainMapper,
    private val attachmentDomainMapper: AttachmentDomainMapper,
    private val categoryDomainMapper: CategoryDomainMapper
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
    
    /**
     * Actualiza metadatos locales después de operaciones de nota
     * Esto garantiza que el sistema de sincronización inteligente detecte cambios
     */
    private suspend fun updateSyncMetadataAfterNoteOperation() {
        try {
            val userId = getCurrentUserId()
            println("NotesRepository: 📊 Actualizando metadata tras operación de nota...")
            
            val metadataResult = syncMetadataRepository.generateCurrentSyncMetadata(userId)
            if (metadataResult.isSuccess) {
                val newMetadata = metadataResult.getOrNull()!!
                syncMetadataRepository.saveLocalSyncMetadata(newMetadata)
                println("NotesRepository: ✅ Metadata local actualizado - Notas: ${newMetadata.notesCount}")
            } else {
                println("NotesRepository: ⚠️ Error generando metadata: ${metadataResult.exceptionOrNull()?.message}")
            }
        } catch (e: Exception) {
            println("NotesRepository: ⚠️ Error actualizando metadata: ${e.message}")
        }
    }
    
    override suspend fun getNotes(): Result<List<Note>> {
        return try {
            // LOCAL-FIRST: Get notes from local database immediately
            val localNotes = notesDao.getNotesByUserId(getCurrentUserId())
            val domainNotes = noteDomainMapper.toDomainModelListWithAttachments(localNotes)
            
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
                Result.success(noteDomainMapper.toDomainModelWithAttachments(localNote))
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
            
            // ✅ NUEVO: Actualizar metadata para que sync inteligente detecte el cambio
            updateSyncMetadataAfterNoteOperation()
            
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
            
            // ✅ NUEVO: Actualizar metadata para que sync inteligente detecte el cambio
            updateSyncMetadataAfterNoteOperation()
            
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
                // ✅ NUEVO: Actualizar metadata para que sync inteligente detecte el cambio
                updateSyncMetadataAfterNoteOperation()
                
                // TODO: Background sync will be handled by SyncRepository
                Result.success(noteDomainMapper.toDomainModel(updatedNote))
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
                // ✅ NUEVO: Crear tombstone ANTES de eliminar attachment
                deletionsDao.insertDeletion(
                    tableName = "attachments",
                    recordId = attachment.id,
                    userId = getCurrentUserId()
                )
                
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
                // ✅ NUEVO: Actualizar metadata para que sync inteligente detecte el cambio
                updateSyncMetadataAfterNoteOperation()
                
                Result.success(noteDomainMapper.toDomainModelWithAttachments(updatedNote))
            } else {
                Result.failure(Exception("Note not found after update"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteNote(id: String): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            
            // ✅ NUEVO: Crear tombstone ANTES de eliminar físicamente
            // Esto permite que el sync sepa que esta nota fue eliminada intencionalmente
            deletionsDao.insertDeletion(
                tableName = "notes",
                recordId = id,
                userId = userId
            )
            
            // LOCAL-FIRST: Delete from local database immediately
            notesDao.deleteNote(id)
            
            // ✅ NUEVO: Actualizar metadata para que sync inteligente detecte el cambio
            updateSyncMetadataAfterNoteOperation()
            
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
    
    override suspend fun searchNotes(query: String): Result<List<Note>> {
        return try {
            if (query.isBlank()) {
                // Si query vacía, devolver todas las notas
                return getNotes()
            }
            
            // LOCAL-FIRST: Buscar en base de datos local
            val searchResults = notesDao.searchNotes(getCurrentUserId(), query)
            val domainNotes = noteDomainMapper.toDomainModelListWithAttachments(searchResults)
            
            Result.success(domainNotes)
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
     * Private helper to get notes with attachments and categories as Flow
     * Uses MVVM + Clean Architecture: combines flows from different data sources for reactive updates
     * Updated to include categories for complete note information in listings
     */
    private fun getNotesWithAttachmentsFlow(userId: String): Flow<List<Note>> {
        // CLEAN ARCHITECTURE: Combine flows from different data sources
        // This ensures UI refreshes when notes, attachments, OR categories change
        return combine(
            notesDao.getNotesByUserIdFlow(userId),        // Flow for notes changes
            attachmentsDao.getAllAttachmentsFlow(),       // Flow for ANY attachment changes
            noteCategoriesDao.getAllNoteCategoriesFlow()  // Flow for ANY category relationship changes
        ) { notesList, allAttachments, allNoteCategories ->
            
            // Create efficient lookup maps
            val attachmentsByNoteId = allAttachments.groupBy { it.nota_id }
            val categoryIdsByNoteId = allNoteCategories.groupBy { it.note_id }
            
            // Transform notes with their corresponding attachments and categories
            notesList.map { note ->
                val noteAttachments = attachmentsByNoteId[note.id] ?: emptyList()
                val noteCategoryRelations = categoryIdsByNoteId[note.id] ?: emptyList()
                
                // Get categories for this note (we need to fetch the actual category data)
                val categories = if (noteCategoryRelations.isNotEmpty()) {
                    // Note: This is not ideal for performance, but it's the simplest approach for now
                    // In a real production app, we'd optimize this with a more complex flow combination
                    // or use a custom query that JOINs the tables
                    runCatching {
                        categoriesDao.getCategoriesByIds(noteCategoryRelations.map { it.category_id })
                            .map { categoryDomainMapper.toDomain(it) }
                    }.getOrElse { emptyList() }
                } else {
                    emptyList()
                }
                
                // Convert to domain model with attachments and categories
                Note(
                    id = note.id,
                    titulo = note.titulo,
                    contenido = note.contenido,
                    fechaCreacion = note.fecha_creacion.toLongOrNull() ?: getCurrentTimestamp(),
                    fechaModificacion = note.fecha_modificacion.toLongOrNull() ?: getCurrentTimestamp(),
                    usuarioId = note.usuario_id,
                    archivosAdjuntos = attachmentDomainMapper.toDomainModelList(noteAttachments),
                    categories = categories
                )
            }
        }
    }
    
    // === Categories Support ===
    
    suspend fun getCategoriesByUserId(userId: String): List<Category> {
        return categoriesDao.getCategoriesByUserId(userId)
            .map { categoryDomainMapper.toDomain(it) }
    }
    
    fun getCategoriesByUserIdFlow(userId: String): Flow<List<Category>> {
        return categoriesDao.getCategoriesByUserIdFlow(userId)
            .map { categories -> 
                categories.map { categoryDomainMapper.toDomain(it) }
            }
    }
    
    // Note: All mapping functions have been extracted to Domain Mappers
    // following Single Responsibility Principle and Clean Architecture
}