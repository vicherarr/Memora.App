package com.vicherarr.memora.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vicherarr.memora.domain.models.Note
import com.vicherarr.memora.domain.models.ArchivoAdjunto
import com.vicherarr.memora.domain.models.TipoDeArchivo
import com.vicherarr.memora.domain.repository.NotesRepository
import com.vicherarr.memora.presentation.states.BaseUiState
import com.vicherarr.memora.presentation.states.ImageViewerState
import com.vicherarr.memora.presentation.states.VideoViewerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

/**
 * Notes UI State - Single Source of Truth
 * Immutable data class representing the complete state of the Notes List screen
 */
data class NotesUiState(
    val notes: List<Note> = emptyList(),
    val selectedNote: Note? = null,
    val imageViewer: ImageViewerState = ImageViewerState(), // Image viewer state following MVVM
    val videoViewer: VideoViewerState = VideoViewerState(), // Video viewer state following MVVM
    override val isLoading: Boolean = false,
    override val errorMessage: String? = null
) : BaseUiState

/**
 * ViewModel for notes operations following JetBrains KMP patterns
 * Single Responsibility: Only handles notes list operations
 */
class NotesViewModel(
    private val notesRepository: NotesRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()
    
    init {
        // Establish a reactive flow from the repository to the UI state
        viewModelScope.launch {
            notesRepository.getNotesFlow()
                .onStart { 
                    _uiState.value = _uiState.value.copy(isLoading = true) 
                }
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Error loading notes"
                    )
                }
                .collect { notesList ->
                    _uiState.value = _uiState.value.copy(
                        notes = notesList,
                        isLoading = false
                    )
                }
        }
    }
    
    fun selectNote(noteId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            notesRepository.getNoteById(noteId)
                .onSuccess { note ->
                    _uiState.value = _uiState.value.copy(
                        selectedNote = note,
                        isLoading = false
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Error loading note"
                    )
                }
        }
    }
    
    fun createNote(titulo: String?, contenido: String) {
        viewModelScope.launch {
            // The UI will update automatically thanks to the reactive flow. No need to call loadNotes().
            notesRepository.createNote(titulo, contenido)
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = exception.message ?: "Error creating note"
                    )
                }
        }
    }
    
    fun updateNote(id: String, titulo: String?, contenido: String) {
        viewModelScope.launch {
            // The UI will update automatically thanks to the reactive flow. No need to call loadNotes().
            notesRepository.updateNote(id, titulo, contenido)
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = exception.message ?: "Error updating note"
                    )
                }
        }
    }
    
    fun deleteNote(id: String) {
        viewModelScope.launch {
            // The UI will update automatically thanks to the reactive flow. No need to call loadNotes().
            notesRepository.deleteNote(id)
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = exception.message ?: "Error deleting note"
                    )
                }
        }
    }
    
    fun clearSelectedNote() {
        _uiState.value = _uiState.value.copy(selectedNote = null)
    }
    
    // MARK: - Media Viewer Methods (following NoteDetailViewModel pattern)
    
    /**
     * Show image viewer with specified image data
     * Following MVVM pattern - state management in ViewModel
     */
    private fun showImageViewer(imageData: Any, imageName: String?) {
        _uiState.value = _uiState.value.copy(
            imageViewer = ImageViewerState(
                isVisible = true,
                imageData = imageData,
                imageName = imageName
            )
        )
    }
    
    /**
     * Hide image viewer - Reset to initial state
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
    private fun showVideoViewer(videoData: Any, videoName: String?) {
        _uiState.value = _uiState.value.copy(
            videoViewer = VideoViewerState(
                isVisible = true,
                videoData = videoData,
                videoName = videoName
            )
        )
    }
    
    /**
     * Hide video viewer - Reset to initial state
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
     * Single Responsibility: Determines which viewer to show based on attachment type
     */
    fun showMediaViewer(attachment: ArchivoAdjunto) {
        if (attachment.filePath.isNullOrBlank()) return
        
        when (attachment.tipoArchivo) {
            TipoDeArchivo.Imagen -> {
                showImageViewer(attachment.filePath!!, attachment.nombreOriginal ?: "Imagen")
            }
            TipoDeArchivo.Video -> {
                showVideoViewer(attachment.filePath!!, attachment.nombreOriginal ?: "Video")
            }
        }
    }
}
