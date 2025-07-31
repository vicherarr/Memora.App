package com.vicherarr.memora.presentation.viewmodels

import androidx.lifecycle.viewModelScope
import com.vicherarr.memora.domain.models.Note
import com.vicherarr.memora.domain.repository.NotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * ViewModel for notes operations
 */
class NotesViewModel : BaseViewModel(), KoinComponent {
    
    private val notesRepository: NotesRepository by inject()
    
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()
    
    private val _selectedNote = MutableStateFlow<Note?>(null)
    val selectedNote: StateFlow<Note?> = _selectedNote.asStateFlow()
    
    init {
        loadNotes()
    }
    
    fun loadNotes() {
        viewModelScope.launch {
            setLoading(true)
            clearError()
            
            notesRepository.getNotes()
                .onSuccess { notesList ->
                    _notes.value = notesList
                }
                .onFailure { exception ->
                    setError(exception.message ?: "Error loading notes")
                }
            
            setLoading(false)
        }
    }
    
    fun selectNote(noteId: String) {
        viewModelScope.launch {
            setLoading(true)
            clearError()
            
            notesRepository.getNoteById(noteId)
                .onSuccess { note ->
                    _selectedNote.value = note
                }
                .onFailure { exception ->
                    setError(exception.message ?: "Error loading note")
                }
            
            setLoading(false)
        }
    }
    
    fun createNote(titulo: String?, contenido: String) {
        viewModelScope.launch {
            setLoading(true)
            clearError()
            
            notesRepository.createNote(titulo, contenido)
                .onSuccess {
                    loadNotes() // Refresh list
                }
                .onFailure { exception ->
                    setError(exception.message ?: "Error creating note")
                }
            
            setLoading(false)
        }
    }
    
    fun updateNote(id: String, titulo: String?, contenido: String) {
        viewModelScope.launch {
            setLoading(true)
            clearError()
            
            notesRepository.updateNote(id, titulo, contenido)
                .onSuccess {
                    loadNotes() // Refresh list
                }
                .onFailure { exception ->
                    setError(exception.message ?: "Error updating note")
                }
            
            setLoading(false)
        }
    }
    
    fun deleteNote(id: String) {
        viewModelScope.launch {
            setLoading(true)
            clearError()
            
            notesRepository.deleteNote(id)
                .onSuccess {
                    loadNotes() // Refresh list
                }
                .onFailure { exception ->
                    setError(exception.message ?: "Error deleting note")
                }
            
            setLoading(false)
        }
    }
    
    fun clearSelectedNote() {
        _selectedNote.value = null
    }
}