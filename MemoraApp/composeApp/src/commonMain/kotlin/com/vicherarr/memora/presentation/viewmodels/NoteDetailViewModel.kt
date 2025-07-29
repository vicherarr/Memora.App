package com.vicherarr.memora.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vicherarr.memora.domain.repository.NotesRepository
import com.vicherarr.memora.domain.models.Note
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel específico para detalle de nota
 * Principio SRP: Solo maneja la funcionalidad de detalle de una nota
 */
class NoteDetailViewModel(
    private val notesRepository: NotesRepository
) : ViewModel() {
    
    // UI State específico para detalle de nota
    private val _uiState = MutableStateFlow<NoteDetailUiState>(NoteDetailUiState.Loading)
    val uiState: StateFlow<NoteDetailUiState> = _uiState.asStateFlow()
    
    private val _note = MutableStateFlow<Note?>(null)
    val note: StateFlow<Note?> = _note.asStateFlow()
    
    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting.asStateFlow()
    
    /**
     * Cargar nota por ID
     */
    fun loadNote(noteId: String) {
        _uiState.value = NoteDetailUiState.Loading
        
        viewModelScope.launch {
            try {
                val note = notesRepository.getNoteById(noteId)
                if (note != null) {
                    _note.value = note
                    _uiState.value = NoteDetailUiState.Success
                } else {
                    _uiState.value = NoteDetailUiState.NotFound
                }
            } catch (error: Throwable) {
                _uiState.value = NoteDetailUiState.Error(
                    message = error.message ?: "Error al cargar la nota"
                )
            }
        }
    }
    
    /**
     * Eliminar nota actual
     */
    fun deleteNote(onSuccess: () -> Unit) {
        val currentNote = _note.value ?: return
        
        _isDeleting.value = true
        
        viewModelScope.launch {
            try {
                notesRepository.deleteNote(currentNote.id)
                _isDeleting.value = false
                onSuccess()
            } catch (error: Throwable) {
                _isDeleting.value = false
                _uiState.value = NoteDetailUiState.Error(
                    message = "Error al eliminar la nota: ${error.message}"
                )
            }
        }
    }
    
    /**
     * Limpiar estado cuando se sale de la pantalla
     */
    fun clearState() {
        _note.value = null
        _uiState.value = NoteDetailUiState.Loading
        _isDeleting.value = false
    }
}

/**
 * Estados específicos para detalle de nota
 * Principio OCP: Extensible para nuevos estados
 */
sealed class NoteDetailUiState {
    object Loading : NoteDetailUiState()
    object Success : NoteDetailUiState()
    object NotFound : NoteDetailUiState()
    data class Error(val message: String) : NoteDetailUiState()
}