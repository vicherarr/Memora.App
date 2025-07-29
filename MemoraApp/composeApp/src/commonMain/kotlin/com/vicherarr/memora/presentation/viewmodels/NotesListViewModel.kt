package com.vicherarr.memora.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vicherarr.memora.domain.repository.NotesRepository
import com.vicherarr.memora.domain.models.Note
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel específico para la lista de notas
 * Principio SRP: Solo maneja la funcionalidad de lista de notas
 */
class NotesListViewModel(
    private val notesRepository: NotesRepository
) : ViewModel() {
    
    // UI State específico para lista de notas
    private val _uiState = MutableStateFlow<NotesListUiState>(NotesListUiState.Loading)
    val uiState: StateFlow<NotesListUiState> = _uiState.asStateFlow()
    
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()
    
    init {
        loadNotes()
    }
    
    /**
     * Cargar lista de notas
     */
    fun loadNotes() {
        _uiState.value = NotesListUiState.Loading
        
        viewModelScope.launch {
            try {
                notesRepository.getAllNotes().collectLatest { notesList ->
                    _notes.value = notesList
                    _uiState.value = if (notesList.isEmpty()) {
                        NotesListUiState.Empty
                    } else {
                        NotesListUiState.Success
                    }
                }
            } catch (error: Throwable) {
                _uiState.value = NotesListUiState.Error(
                    message = error.message ?: "Error al cargar las notas"
                )
            }
        }
    }
    
    /**
     * Refrescar lista de notas (pull-to-refresh)
     */
    fun refreshNotes() {
        if (_uiState.value != NotesListUiState.Loading) {
            _uiState.value = NotesListUiState.Refreshing
            loadNotes()
        }
    }
    
    /**
     * Buscar notas por texto
     */
    fun searchNotes(query: String) {
        if (query.isBlank()) {
            loadNotes()
            return
        }
        
        _uiState.value = NotesListUiState.Loading
        
        viewModelScope.launch {
            try {
                val filteredNotes = _notes.value.filter { note ->
                    note.title?.contains(query, ignoreCase = true) == true ||
                    note.content.contains(query, ignoreCase = true)
                }
                
                _notes.value = filteredNotes
                _uiState.value = if (filteredNotes.isEmpty()) {
                    NotesListUiState.EmptySearch(query)
                } else {
                    NotesListUiState.Success
                }
            } catch (error: Throwable) {
                _uiState.value = NotesListUiState.Error(
                    message = "Error al buscar notas: ${error.message}"
                )
            }
        }
    }
    
    /**
     * Eliminar nota desde la lista
     * Actualiza automáticamente la lista sin navegación
     */
    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            try {
                // Eliminar del repositorio
                notesRepository.deleteNote(noteId)
                
                // Actualizar lista local inmediatamente (UX optimista)
                val updatedNotes = _notes.value.filter { it.id != noteId }
                _notes.value = updatedNotes
                
                // Actualizar estado según el resultado
                _uiState.value = if (updatedNotes.isEmpty()) {
                    NotesListUiState.Empty
                } else {
                    NotesListUiState.Success
                }
                
            } catch (error: Throwable) {
                // En caso de error, recargar para mostrar estado real
                loadNotes()
                _uiState.value = NotesListUiState.Error(
                    message = "Error al eliminar la nota: ${error.message}"
                )
            }
        }
    }
}

/**
 * Estados específicos para la lista de notas
 * Principio OCP: Extensible para nuevos estados
 */
sealed class NotesListUiState {
    object Loading : NotesListUiState()
    object Refreshing : NotesListUiState()
    object Success : NotesListUiState()
    object Empty : NotesListUiState()
    data class EmptySearch(val query: String) : NotesListUiState()
    data class Error(val message: String) : NotesListUiState()
}