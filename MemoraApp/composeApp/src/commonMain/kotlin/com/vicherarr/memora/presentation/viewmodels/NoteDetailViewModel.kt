package com.vicherarr.memora.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vicherarr.memora.domain.models.Note
import com.vicherarr.memora.domain.models.ArchivoAdjunto
import com.vicherarr.memora.domain.models.MediaFile
import com.vicherarr.memora.domain.repository.NotesRepository
import com.vicherarr.memora.presentation.states.BaseUiState
import com.vicherarr.memora.presentation.states.ImageViewerState
import com.vicherarr.memora.presentation.states.VideoViewerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Note Detail UI State - Single Source of Truth
 * Immutable data class representing the complete state of the Note Detail screen
 */
data class NoteDetailUiState(
    val note: Note? = null,
    val isEditMode: Boolean = false,
    val editTitulo: String = "",
    val editContenido: String = "",
    val editAttachments: List<ArchivoAdjunto> = emptyList(), // Current attachments in edit mode
    val imageViewer: ImageViewerState = ImageViewerState(), // Image viewer state following MVVM
    val videoViewer: VideoViewerState = VideoViewerState(), // Video viewer state following MVVM
    val isNoteDeleted: Boolean = false,
    val isNoteSaved: Boolean = false,
    override val isLoading: Boolean = false,
    override val errorMessage: String? = null
) : BaseUiState

/**
 * ViewModel for Note Detail screen following JetBrains KMP patterns
 * Single Responsibility: Only handles note detail operations (view, edit, delete)
 */
class NoteDetailViewModel(
    private val notesRepository: NotesRepository,
    private val mediaViewModel: MediaViewModel
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(NoteDetailUiState())
    val uiState: StateFlow<NoteDetailUiState> = _uiState.asStateFlow()
    
    /**
     * Load specific note by ID - Direct method call
     */
    fun loadNote(noteId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            notesRepository.getNoteById(noteId)
                .onSuccess { note ->
                    _uiState.value = _uiState.value.copy(
                        note = note,
                        editTitulo = note.titulo ?: "",
                        editContenido = note.contenido,
                        editAttachments = note.archivosAdjuntos,
                        isLoading = false
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Error al cargar la nota"
                    )
                }
        }
    }
    
    /**
     * Enter edit mode - Direct method call
     */
    fun enterEditMode() {
        val currentNote = _uiState.value.note ?: return
        _uiState.value = _uiState.value.copy(
            isEditMode = true,
            editTitulo = currentNote.titulo ?: "",
            editContenido = currentNote.contenido,
            editAttachments = currentNote.archivosAdjuntos, // Copy original attachments for editing
            errorMessage = null
        )
    }
    
    /**
     * Exit edit mode without saving - Direct method call
     */
    fun exitEditMode() {
        val currentNote = _uiState.value.note ?: return
        mediaViewModel.clearSelectedMedia() // Clear any unsaved selected media
        _uiState.value = _uiState.value.copy(
            isEditMode = false,
            editTitulo = currentNote.titulo ?: "",
            editContenido = currentNote.contenido,
            editAttachments = currentNote.archivosAdjuntos, // Restore original attachments
            errorMessage = null
        )
    }
    
    /**
     * Update titulo in edit mode - Direct method call
     */
    fun updateEditTitulo(titulo: String) {
        if (_uiState.value.isEditMode) {
            _uiState.value = _uiState.value.copy(editTitulo = titulo)
        }
    }
    
    /**
     * Update contenido in edit mode - Direct method call
     */
    fun updateEditContenido(contenido: String) {
        if (_uiState.value.isEditMode) {
            _uiState.value = _uiState.value.copy(editContenido = contenido)
        }
    }
    
    /**
     * Remove specific attachment from edit list
     */
    fun removeAttachment(attachmentId: String) {
        if (_uiState.value.isEditMode) {
            val currentAttachments = _uiState.value.editAttachments.toMutableList()
            currentAttachments.removeAll { it.id == attachmentId }
            _uiState.value = _uiState.value.copy(editAttachments = currentAttachments)
        }
    }
    
    /**
     * Add new media files from MediaViewModel - simplified approach
     * Media files are handled directly by MediaViewModel, no need to duplicate them
     */
    fun addMediaToNote() {
        // This function is no longer needed since we're showing selectedMedia directly in the UI
        // The media files will be retrieved from MediaViewModel when saving
    }
    
    /**
     * Save note changes - Direct method call
     */
    fun saveNote() {
        val currentState = _uiState.value
        val currentNote = currentState.note ?: return
        
        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, errorMessage = null)
            
            val validationError = validateNoteInput(currentState.editTitulo, currentState.editContenido)
            if (validationError != null) {
                _uiState.value = currentState.copy(isLoading = false, errorMessage = validationError)
                return@launch
            }
            
            val newTitulo = if (currentState.editTitulo.isBlank()) null else currentState.editTitulo.trim()
            val newContenido = currentState.editContenido.trim()
            
            val result = notesRepository.updateNoteWithAttachments(
                noteId = currentNote.id,
                titulo = newTitulo,
                contenido = newContenido,
                existingAttachments = currentState.editAttachments,
                newMediaFiles = mediaViewModel.uiState.value.selectedMedia
            )
            
            result.onSuccess {
                loadNote(currentNote.id)
                mediaViewModel.clearSelectedMedia() // Clear selected media after saving
                _uiState.value = _uiState.value.copy(
                    isEditMode = false,
                    isNoteSaved = true
                )
            }.onFailure { exception ->
                _uiState.value = currentState.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "Error al guardar la nota"
                )
            }
        }
    }
    
    /**
     * Delete current note - Direct method call
     */
    fun deleteNote() {
        val currentNote = _uiState.value.note ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            notesRepository.deleteNote(currentNote.id)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isNoteDeleted = true
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Error al eliminar la nota"
                    )
                }
        }
    }
    
    /**
     * Clear success messages after handling - Direct method call
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            isNoteSaved = false
        )
    }
    
    /**
     * Show image viewer with specified image data
     * Following MVVM pattern - state management in ViewModel
     */
    fun showImageViewer(imageData: Any, imageName: String?) {
        _uiState.value = _uiState.value.copy(
            imageViewer = ImageViewerState(
                isVisible = true,
                imageData = imageData,
                imageName = imageName
            )
        )
    }
    
    /**
     * Hide image viewer
     * Following MVVM pattern - state management in ViewModel
     */
    fun hideImageViewer() {
        _uiState.value = _uiState.value.copy(
            imageViewer = ImageViewerState()
        )
    }
    
    /**
     * Show video viewer with specified video data
     * Following MVVM pattern - state management in ViewModel
     */
    fun showVideoViewer(videoData: Any, videoName: String?) {
        _uiState.value = _uiState.value.copy(
            videoViewer = VideoViewerState(
                isVisible = true,
                videoData = videoData,
                videoName = videoName
            )
        )
    }
    
    /**
     * Hide video viewer
     * Following MVVM pattern - state management in ViewModel
     */
    fun hideVideoViewer() {
        _uiState.value = _uiState.value.copy(
            videoViewer = VideoViewerState()
        )
    }
    
    /**
     * Universal media viewer - handles business logic for showing appropriate viewer
     * Following Clean Code principles - View doesn't know about media types
     */
    fun showMediaViewer(attachment: ArchivoAdjunto) {
        if (attachment.filePath.isNullOrBlank()) return
        
        when (attachment.tipoArchivo) {
            com.vicherarr.memora.domain.models.TipoDeArchivo.Imagen -> {
                showImageViewer(attachment.filePath!!, attachment.nombreOriginal ?: "Imagen")
            }
            com.vicherarr.memora.domain.models.TipoDeArchivo.Video -> {
                showVideoViewer(attachment.filePath!!, attachment.nombreOriginal ?: "Video")
            }
        }
    }
    
    /**
     * Universal media viewer for new media files (MediaFile type)
     * Following Clean Code principles - View doesn't know about media types
     */
    fun showMediaViewer(mediaFile: MediaFile) {
        when (mediaFile.type) {
            com.vicherarr.memora.domain.models.MediaType.IMAGE -> {
                showImageViewer(mediaFile.data, mediaFile.fileName)
            }
            com.vicherarr.memora.domain.models.MediaType.VIDEO -> {
                showVideoViewer(mediaFile.data, mediaFile.fileName)
            }
        }
    }
    
    /**
     * Check if note can be saved in current edit state
     */
    fun canSaveNote(): Boolean {
        val currentState = _uiState.value
        return currentState.isEditMode && 
               currentState.editContenido.isNotBlank() && 
               !currentState.isLoading
    }
    
    /**
     * Get validation hint for UI display
     */
    fun getValidationHint(contenido: String): String? {
        return if (contenido.isBlank()) {
            "El contenido es requerido"
        } else {
            null
        }
    }
    
    /**
     * Validate note input data
     * Single Responsibility: Only validates note editing data
     */
    private fun validateNoteInput(titulo: String, contenido: String): String? {
        return when {
            contenido.isBlank() -> "El contenido de la nota es requerido"
            contenido.length > 10000 -> "El contenido es demasiado largo (máximo 10,000 caracteres)"
            titulo.length > 200 -> "El título es demasiado largo (máximo 200 caracteres)"
            else -> null
        }
    }
}