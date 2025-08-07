package com.vicherarr.memora.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vicherarr.memora.domain.repository.NotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Create Note UI State - Single Source of Truth
 * Immutable data class representing the complete state of the Create Note screen
 */
data class CreateNoteUiState(
    val titulo: String = "",
    val contenido: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isNoteSaved: Boolean = false
)

/**
 * ViewModel dedicated to Create Note screen following JetBrains KMP patterns
 * Simple, direct methods without event system complexity
 * Single Responsibility: Only handles note creation operations
 */
class CreateNoteViewModel(
    private val notesRepository: NotesRepository,
    private val mediaViewModel: MediaViewModel
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CreateNoteUiState())
    val uiState: StateFlow<CreateNoteUiState> = _uiState.asStateFlow()
    
    /**
     * Update titulo field - Direct method call
     */
    fun updateTitulo(titulo: String) {
        _uiState.value = _uiState.value.copy(titulo = titulo)
    }
    
    /**
     * Update contenido field - Direct method call
     */
    fun updateContenido(contenido: String) {
        _uiState.value = _uiState.value.copy(contenido = contenido)
    }
    
    /**
     * Create note operation - Direct method call with media attachments
     */
    fun createNote() {
        val currentState = _uiState.value
        val selectedMedia = mediaViewModel.uiState.value.selectedMedia
        
        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, errorMessage = null)
            
            // Validate input
            val validationError = validateNoteInput(currentState.titulo, currentState.contenido)
            if (validationError != null) {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    errorMessage = validationError
                )
                return@launch
            }
            
            // Create note through repository with attachments
            val result = if (selectedMedia.isNotEmpty()) {
                // Create note with attachments
                notesRepository.createNoteWithAttachments(
                    titulo = if (currentState.titulo.isBlank()) null else currentState.titulo.trim(),
                    contenido = currentState.contenido.trim(),
                    attachments = selectedMedia
                )
            } else {
                // Create note without attachments
                notesRepository.createNote(
                    titulo = if (currentState.titulo.isBlank()) null else currentState.titulo.trim(),
                    contenido = currentState.contenido.trim()
                )
            }
            
            result
                .onSuccess {
                    // Clear media from MediaViewModel after successful save
                    mediaViewModel.clearSelectedMedia()
                    
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        isNoteSaved = true
                    )
                }
                .onFailure { exception ->
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Error al crear la nota"
                    )
                }
        }
    }
    
    /**
     * Get validation error message for UI display
     */
    fun getValidationHint(contenido: String): String? {
        return if (contenido.isBlank()) {
            "El contenido es requerido"
        } else {
            null
        }
    }
    
    /**
     * Check if note can be saved
     */
    fun canSaveNote(contenido: String): Boolean {
        return contenido.isNotBlank()
    }
    
    /**
     * Validate note input data
     * Single Responsibility: Only validates note creation data
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