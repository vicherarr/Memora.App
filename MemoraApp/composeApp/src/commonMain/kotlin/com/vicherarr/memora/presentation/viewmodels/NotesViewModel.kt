package com.vicherarr.memora.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vicherarr.memora.domain.models.Note
import com.vicherarr.memora.domain.repository.NotesRepository
import com.vicherarr.memora.presentation.states.BaseUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Notes UI State - Single Source of Truth
 * Immutable data class representing the complete state of the Notes List screen
 */
data class NotesUiState(
    val notes: List<Note> = emptyList(),
    val selectedNote: Note? = null,
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
        loadNotes()
    }
    
    fun loadNotes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            notesRepository.getNotes()
                .onSuccess { notesList ->
                    _uiState.value = _uiState.value.copy(
                        notes = notesList,
                        isLoading = false
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Error loading notes"
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
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            notesRepository.createNote(titulo, contenido)
                .onSuccess {
                    loadNotes() // Refresh list
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Error creating note"
                    )
                }
        }
    }
    
    fun updateNote(id: String, titulo: String?, contenido: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            notesRepository.updateNote(id, titulo, contenido)
                .onSuccess {
                    loadNotes() // Refresh list
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Error updating note"
                    )
                }
        }
    }
    
    fun deleteNote(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            notesRepository.deleteNote(id)
                .onSuccess {
                    loadNotes() // Refresh list
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Error deleting note"
                    )
                }
        }
    }
    
    fun clearSelectedNote() {
        _uiState.value = _uiState.value.copy(selectedNote = null)
    }
}