package com.vicherarr.memora.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vicherarr.memora.domain.repository.NotesRepository
import com.vicherarr.memora.domain.models.Note
import com.vicherarr.memora.presentation.utils.ErrorHandler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel específico para edición/creación de notas
 * Principio SRP: Solo maneja la funcionalidad de editar y crear notas
 */
class NoteEditViewModel(
    private val notesRepository: NotesRepository
) : ViewModel() {
    
    // UI State específico para edición de nota
    private val _uiState = MutableStateFlow<NoteEditUiState>(NoteEditUiState.Idle)
    val uiState: StateFlow<NoteEditUiState> = _uiState.asStateFlow()
    
    // Estado del formulario
    private val _formState = MutableStateFlow<NoteFormState>(NoteFormState())
    val formState: StateFlow<NoteFormState> = _formState.asStateFlow()
    
    /**
     * Inicializar para crear nueva nota
     */
    fun initializeForCreate() {
        _formState.value = NoteFormState()
        _uiState.value = NoteEditUiState.Idle
    }
    
    /**
     * Inicializar para editar nota existente
     */
    fun initializeForEdit(noteId: String) {
        _uiState.value = NoteEditUiState.Loading
        
        viewModelScope.launch {
            try {
                val note = notesRepository.getNoteById(noteId)
                if (note != null) {
                    _formState.value = NoteFormState(
                        noteId = note.id,
                        title = note.title ?: "",
                        content = note.content,
                        isEditMode = true
                    )
                    _uiState.value = NoteEditUiState.Idle
                } else {
                    _uiState.value = NoteEditUiState.Error(ErrorHandler.NotesErrors.notFoundError())
                }
            } catch (error: Throwable) {
                _uiState.value = NoteEditUiState.Error(
                    ErrorHandler.processError(error, "load")
                )
            }
        }
    }
    
    /**
     * Actualizar título
     */
    fun updateTitle(title: String) {
        _formState.value = _formState.value.copy(
            title = title,
            hasChanges = true
        )
    }
    
    /**
     * Actualizar contenido
     */
    fun updateContent(content: String) {
        _formState.value = _formState.value.copy(
            content = content,
            hasChanges = true
        )
    }
    
    /**
     * Validar formulario
     */
    fun validateForm(): NoteValidation {
        val state = _formState.value
        
        return when {
            state.content.isBlank() -> NoteValidation.ContentRequired
            state.content.length > 10000 -> NoteValidation.ContentTooLong
            state.title.length > 200 -> NoteValidation.TitleTooLong
            else -> NoteValidation.Valid
        }
    }
    
    /**
     * Guardar nota (crear o actualizar)
     */
    fun saveNote(onSuccess: () -> Unit) {
        val validation = validateForm()
        if (validation != NoteValidation.Valid) {
            _uiState.value = NoteEditUiState.ValidationError(
                ErrorHandler.NotesErrors.validationError(
                    when (validation) {
                        NoteValidation.ContentRequired -> "content"
                        NoteValidation.ContentTooLong -> "content"
                        NoteValidation.TitleTooLong -> "title"
                        else -> "form"
                    }
                )
            )
            return
        }
        
        val state = _formState.value
        _uiState.value = NoteEditUiState.Saving
        
        viewModelScope.launch {
            try {
                if (state.isEditMode && state.noteId != null) {
                    // Actualizar nota existente
                    notesRepository.updateNote(
                        noteId = state.noteId,
                        title = state.title.takeIf { it.isNotBlank() },
                        content = state.content
                    )
                } else {
                    // Crear nueva nota
                    notesRepository.createNote(
                        title = state.title.takeIf { it.isNotBlank() },
                        content = state.content
                    )
                }
                
                _uiState.value = NoteEditUiState.Saved
                onSuccess()
            } catch (error: Throwable) {
                _uiState.value = NoteEditUiState.Error(
                    ErrorHandler.processError(error, "save")
                )
            }
        }
    }
    
    /**
     * Limpiar estado de error
     */
    fun clearError() {
        if (_uiState.value is NoteEditUiState.Error || 
            _uiState.value is NoteEditUiState.ValidationError) {
            _uiState.value = NoteEditUiState.Idle
        }
    }
}

/**
 * Estado del formulario de edición
 */
data class NoteFormState(
    val noteId: String? = null,
    val title: String = "",
    val content: String = "",
    val isEditMode: Boolean = false,
    val hasChanges: Boolean = false
) {
    val isSaveEnabled: Boolean
        get() = content.isNotBlank() && hasChanges
    
    val isEmpty: Boolean
        get() = title.isBlank() && content.isBlank()
}

/**
 * Estados específicos para edición de nota
 * Principio OCP: Extensible para nuevos estados
 */
sealed class NoteEditUiState {
    object Idle : NoteEditUiState()
    object Loading : NoteEditUiState()
    object Saving : NoteEditUiState()
    object Saved : NoteEditUiState()
    data class Error(val errorInfo: ErrorHandler.ErrorInfo) : NoteEditUiState()
    data class ValidationError(val errorInfo: ErrorHandler.ErrorInfo) : NoteEditUiState()
}

/**
 * Validaciones del formulario
 */
sealed class NoteValidation(val message: String) {
    object Valid : NoteValidation("")
    object ContentRequired : NoteValidation("El contenido es obligatorio")
    object ContentTooLong : NoteValidation("El contenido no puede exceder 10,000 caracteres")
    object TitleTooLong : NoteValidation("El título no puede exceder 200 caracteres")
}