package com.vicherarr.memora.presentation.viewmodels

import com.vicherarr.memora.domain.repository.NotesRepository
import com.vicherarr.memora.presentation.utils.ListUiState
import com.vicherarr.memora.domain.models.Note
import kotlinx.coroutines.flow.collectLatest

/**
 * ViewModel para gestión de notas
 */
class NotesViewModel(
    private val notesRepository: NotesRepository
) : BaseViewModel<NotesUiState>(NotesUiState()) {
    
    init {
        loadNotes()
    }
    
    fun loadNotes() {
        updateState { it.copy(notesState = it.notesState.copy(isLoading = true)) }
        
        launchSafe(
            onError = { error ->
                updateState { 
                    it.copy(
                        notesState = it.notesState.copy(
                            isLoading = false,
                            error = error.message
                        )
                    )
                }
            }
        ) {
            notesRepository.getAllNotes().collectLatest { notes ->
                updateState { 
                    it.copy(
                        notesState = it.notesState.copy(
                            items = notes,
                            isLoading = false,
                            error = null
                        )
                    )
                }
            }
        }
    }
    
    fun refreshNotes() {
        updateState { it.copy(notesState = it.notesState.copy(isRefreshing = true)) }
        
        launchSafe(
            onError = { error ->
                updateState { 
                    it.copy(
                        notesState = it.notesState.copy(
                            isRefreshing = false,
                            error = error.message
                        )
                    )
                }
            }
        ) {
            notesRepository.syncNotes()
            updateState { 
                it.copy(
                    notesState = it.notesState.copy(isRefreshing = false)
                )
            }
        }
    }
    
    fun createNote(title: String?, content: String) {
        launchSafe(
            onError = { error ->
                updateState { 
                    it.copy(
                        notesState = it.notesState.copy(error = error.message)
                    )
                }
            }
        ) {
            notesRepository.createNote(title, content)
            // loadNotes() se ejecutará automáticamente por el Flow
        }
    }
    
    fun updateNote(note: Note) {
        launchSafe(
            onError = { error ->
                updateState { 
                    it.copy(
                        notesState = it.notesState.copy(error = error.message)
                    )
                }
            }
        ) {
            notesRepository.updateNote(note)
            // loadNotes() se ejecutará automáticamente por el Flow
        }
    }
    
    fun deleteNote(noteId: String) {
        launchSafe(
            onError = { error ->
                updateState { 
                    it.copy(
                        notesState = it.notesState.copy(error = error.message)
                    )
                }
            }
        ) {
            notesRepository.deleteNote(noteId)
            // loadNotes() se ejecutará automáticamente por el Flow
        }
    }
    
    fun searchNotes(query: String) {
        updateState { 
            it.copy(
                notesState = it.notesState.copy(
                    searchQuery = query,
                    isLoading = true
                )
            )
        }
        
        launchSafe(
            onError = { error ->
                updateState { 
                    it.copy(
                        notesState = it.notesState.copy(
                            isLoading = false,
                            error = error.message
                        )
                    )
                }
            }
        ) {
            notesRepository.searchNotes(query).collectLatest { notes ->
                updateState { 
                    it.copy(
                        notesState = it.notesState.copy(
                            items = notes,
                            isLoading = false,
                            error = null
                        )
                    )
                }
            }
        }
    }
    
    fun clearSearch() {
        updateState { 
            it.copy(
                notesState = it.notesState.copy(searchQuery = "")
            )
        }
        loadNotes()
    }
    
    fun clearError() {
        updateState { 
            it.copy(
                notesState = it.notesState.copy(error = null)
            )
        }
    }
}

/**
 * Estado de UI para notas
 */
data class NotesUiState(
    val notesState: ListUiState<Note> = ListUiState()
) : com.vicherarr.memora.presentation.utils.UiState