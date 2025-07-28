package com.vicherarr.memora.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vicherarr.memora.domain.repository.NotesRepository
import com.vicherarr.memora.presentation.utils.SimpleUiState
import com.vicherarr.memora.domain.models.Note
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel para gestión de notas
 */
class NotesViewModel(
    private val notesRepository: NotesRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<SimpleUiState>(SimpleUiState.Loading)
    val uiState: StateFlow<SimpleUiState> = _uiState.asStateFlow()
    
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()
    
    private val _selectedNote = MutableStateFlow<Note?>(null)
    val selectedNote: StateFlow<Note?> = _selectedNote.asStateFlow()
    
    init {
        loadNotes()
    }
    
    fun loadNotes() {
        _uiState.value = SimpleUiState.Loading
        
        viewModelScope.launch {
            try {
                notesRepository.getAllNotes().collectLatest { notesList ->
                    _notes.value = notesList
                    _uiState.value = SimpleUiState.Success
                }
            } catch (error: Throwable) {
                _uiState.value = SimpleUiState.Error(error.message ?: "Error desconocido")
            }
        }
    }
    
    fun loadNoteById(noteId: String) {
        _uiState.value = SimpleUiState.Loading
        
        viewModelScope.launch {
            try {
                val note = notesRepository.getNoteById(noteId)
                if (note != null) {
                    _selectedNote.value = note
                    _uiState.value = SimpleUiState.Success
                } else {
                    _uiState.value = SimpleUiState.Error("Nota no encontrada")
                }
            } catch (error: Throwable) {
                _uiState.value = SimpleUiState.Error(error.message ?: "Error al cargar la nota")
            }
        }
    }
    
    fun createNote(title: String?, content: String) {
        _uiState.value = SimpleUiState.Loading
        
        viewModelScope.launch {
            try {
                notesRepository.createNote(title, content)
                forceRefreshNotes()
                _uiState.value = SimpleUiState.Success
            } catch (error: Throwable) {
                _uiState.value = SimpleUiState.Error(error.message ?: "Error al crear la nota")
            }
        }
    }
    
    fun updateNote(noteId: String, title: String?, content: String) {
        _uiState.value = SimpleUiState.Loading
        
        viewModelScope.launch {
            try {
                notesRepository.updateNote(noteId, title, content)
                forceRefreshNotes()
                _uiState.value = SimpleUiState.Success
            } catch (error: Throwable) {
                _uiState.value = SimpleUiState.Error(error.message ?: "Error al actualizar la nota")
            }
        }
    }
    
    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            try {
                notesRepository.deleteNote(noteId)
                // Forzar recarga inmediata - actualizar lista local sin Flow
                forceRefreshNotes()
            } catch (error: Throwable) {
                _uiState.value = SimpleUiState.Error(error.message ?: "Error al eliminar la nota")
            }
        }
    }
    
    private suspend fun forceRefreshNotes() {
        try {
            // Obtener lista actualizada directamente sin Flow
            val notesList = notesRepository.getAllNotes().first()
            _notes.value = notesList
            _uiState.value = SimpleUiState.Success
        } catch (error: Throwable) {
            _uiState.value = SimpleUiState.Error(error.message ?: "Error al actualizar la lista")
        }
    }
    
    fun refreshNotes() {
        viewModelScope.launch {
            try {
                notesRepository.syncNotes()
            } catch (error: Throwable) {
                _uiState.value = SimpleUiState.Error(error.message ?: "Error al sincronizar")
            }
        }
    }
    
    fun searchNotes(query: String) {
        _uiState.value = SimpleUiState.Loading
        
        viewModelScope.launch {
            try {
                notesRepository.searchNotes(query).collectLatest { notesList ->
                    _notes.value = notesList
                    _uiState.value = SimpleUiState.Success
                }
            } catch (error: Throwable) {
                _uiState.value = SimpleUiState.Error(error.message ?: "Error en la búsqueda")
            }
        }
    }
    
    fun clearSearch() {
        loadNotes()
    }
    
    fun clearError() {
        if (_uiState.value is SimpleUiState.Error) {
            _uiState.value = SimpleUiState.Success
        }
    }
}