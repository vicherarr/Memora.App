package com.vicherarr.memora.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vicherarr.memora.domain.usecases.CreateNoteUseCase
import com.vicherarr.memora.domain.usecases.GetCategoriesByUserUseCase
import com.vicherarr.memora.domain.usecases.CreateCategoryUseCase
import com.vicherarr.memora.domain.models.Category
import com.vicherarr.memora.data.auth.CloudAuthProvider
import com.vicherarr.memora.domain.model.AuthState
import com.vicherarr.memora.domain.model.User
import kotlinx.coroutines.flow.combine
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
    val selectedCategories: List<String> = emptyList(),
    val availableCategories: List<com.vicherarr.memora.domain.models.Category> = emptyList(),
    val isShowingCategoryDropdown: Boolean = false,
    val isCreatingCategory: Boolean = false,
    val newCategoryName: String = "",
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
    private val getCategoriesByUserUseCase: GetCategoriesByUserUseCase,
    private val createCategoryUseCase: CreateCategoryUseCase,
    private val cloudAuthProvider: CloudAuthProvider,
    private val mediaViewModel: MediaViewModel
) : BaseViewModel<CreateNoteUiState>() {
    
    private val _uiState = MutableStateFlow(CreateNoteUiState())
    override val uiState: StateFlow<CreateNoteUiState> = _uiState.asStateFlow()
    
    override fun updateState(update: CreateNoteUiState.() -> CreateNoteUiState) {
        _uiState.value = _uiState.value.update()
    }
    
    init {
        loadCategories()
    }
    
    private fun loadCategories() {
        getCurrentUserId()?.let { userId ->
            viewModelScope.launch {
                getCategoriesByUserUseCase.execute(userId).collect { categories ->
                    updateState { copy(availableCategories = categories) }
                }
            }
        }
    }
    
    private fun getCurrentUserId(): String? {
        return when (val authState = cloudAuthProvider.authState.value) {
            is AuthState.Authenticated -> authState.user.id
            else -> null
        }
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
     * Toggle category selection - Following Single Responsibility Principle
     */
    fun toggleCategory(categoryId: String) {
        val currentSelected = _uiState.value.selectedCategories
        val newSelected = if (categoryId in currentSelected) {
            currentSelected - categoryId
        } else {
            currentSelected + categoryId
        }
        updateState { copy(selectedCategories = newSelected) }
    }
    
    /**
     * Show category dropdown - Following Single Responsibility Principle
     */
    fun showCategoryDropdown() {
        updateState { copy(isShowingCategoryDropdown = true) }
    }
    
    /**
     * Hide category dropdown - Following Single Responsibility Principle
     */
    fun hideCategoryDropdown() {
        updateState { 
            copy(
                isShowingCategoryDropdown = false,
                isCreatingCategory = false,
                newCategoryName = ""
            ) 
        }
    }
    
    /**
     * Show create category field - Following Single Responsibility Principle
     */
    fun showCreateCategory() {
        updateState { copy(isCreatingCategory = true, newCategoryName = "") }
    }
    
    /**
     * Hide create category field
     */
    fun hideCreateCategory() {
        updateState { copy(isCreatingCategory = false, newCategoryName = "") }
    }
    
    /**
     * Update new category name
     */
    fun updateNewCategoryName(name: String) {
        updateState { copy(newCategoryName = name) }
    }
    
    /**
     * Create new category - Following Clean Architecture
     */
    fun createNewCategory() {
        val name = _uiState.value.newCategoryName.trim()
        if (name.isEmpty()) return
        
        getCurrentUserId()?.let { userId ->
            viewModelScope.launch {
                createCategoryUseCase.execute(name, userId).fold(
                    onSuccess = { category ->
                        // Auto-select the new category
                        val newSelected = _uiState.value.selectedCategories + category.id
                        updateState { 
                            copy(
                                selectedCategories = newSelected,
                                isCreatingCategory = false,
                                newCategoryName = "",
                                isShowingCategoryDropdown = false
                            ) 
                        }
                    },
                    onFailure = { error ->
                        setError(error.message ?: "Error creating category")
                        updateState { copy(isCreatingCategory = false) }
                    }
                )
            }
        }
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
            
            // Create note through Use Case with business logic validation and categories
            val result = if (selectedMedia.isNotEmpty()) {
                // Create note with attachments
                createNoteUseCase.executeWithAttachments(
                    titulo = if (currentState.titulo.isBlank()) null else currentState.titulo.trim(),
                    contenido = currentState.contenido.trim(),
                    attachments = selectedMedia,
                    categoryIds = currentState.selectedCategories
                )
            } else {
                // Create note without attachments
                createNoteUseCase.execute(
                    titulo = if (currentState.titulo.isBlank()) null else currentState.titulo.trim(),
                    contenido = currentState.contenido.trim(),
                    categoryIds = currentState.selectedCategories
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