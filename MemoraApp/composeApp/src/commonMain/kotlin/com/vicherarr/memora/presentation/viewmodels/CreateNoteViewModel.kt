package com.vicherarr.memora.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vicherarr.memora.domain.usecases.CreateNoteUseCase
import com.vicherarr.memora.presentation.states.BaseUiState
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
    val isNoteSaved: Boolean = false,
    override val isLoading: Boolean = false,
    override val errorMessage: String? = null
) : BaseUiState

/**
 * ViewModel dedicated to Create Note screen following JetBrains KMP patterns
 * Simple, direct methods without event system complexity
 * Single Responsibility: Only handles note creation operations
 */
class CreateNoteViewModel(
    private val createNoteUseCase: CreateNoteUseCase,
    private val mediaViewModel: MediaViewModel
) : BaseViewModel<CreateNoteUiState>() {
    
    private val _uiState = MutableStateFlow(CreateNoteUiState())
    override val uiState: StateFlow<CreateNoteUiState> = _uiState.asStateFlow()
    
    override fun updateState(update: CreateNoteUiState.() -> CreateNoteUiState) {
        _uiState.value = _uiState.value.update()
    }
    
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
            setLoading(true)
            
            // Note: Validation is now handled by the Use Case
            // This follows Clean Architecture - business logic in Use Case layer
            
            // Create note through Use Case with business logic validation
            val result = if (selectedMedia.isNotEmpty()) {
                // Create note with attachments
                createNoteUseCase.executeWithAttachments(
                    titulo = if (currentState.titulo.isBlank()) null else currentState.titulo.trim(),
                    contenido = currentState.contenido.trim(),
                    attachments = selectedMedia
                )
            } else {
                // Create note without attachments
                createNoteUseCase.execute(
                    titulo = if (currentState.titulo.isBlank()) null else currentState.titulo.trim(),
                    contenido = currentState.contenido.trim()
                )
            }
            
            result
                .onSuccess {
                    // Clear media from MediaViewModel after successful save
                    mediaViewModel.clearSelectedMedia()
                    
                    // Update specific state and clear loading/error
                    updateState { copy(isNoteSaved = true, isLoading = false, errorMessage = null) }
                }
                .onFailure { exception ->
                    setError(exception.message ?: "Error al crear la nota")
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