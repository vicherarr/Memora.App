package com.vicherarr.memora.presentation.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow
import com.vicherarr.memora.presentation.states.BaseUiState

/**
 * Base ViewModel with common functionality for all ViewModels
 * Simplified approach that lets each ViewModel implement their own state management
 * but provides common helper patterns
 */
abstract class BaseViewModel<T : BaseUiState> : ViewModel() {
    
    /**
     * Abstract StateFlow that each ViewModel must implement
     */
    abstract val uiState: StateFlow<T>
    
    /**
     * Abstract update method for type-safe state updates
     * Each ViewModel implements this with their own copy() method
     */
    protected abstract fun updateState(update: T.() -> T)
    
    /**
     * Common helper method to set loading state
     * Uses extension function for type-safe copy operations
     */
    protected fun setLoading(loading: Boolean = true) {
        updateState { 
            setLoadingState(loading, if (loading) null else errorMessage)
        }
    }
    
    /**
     * Common helper method to set error state
     * Uses extension function for type-safe copy operations
     */
    protected fun setError(error: String?) {
        updateState { 
            setLoadingState(false, error)
        }
    }
    
    /**
     * Common helper method to set success state
     * Uses extension function for type-safe copy operations
     */
    protected fun setSuccess() {
        updateState { 
            setLoadingState(false, null)
        }
    }
    
    /**
     * Common helper method to clear error without affecting loading state
     * Uses extension function for type-safe copy operations
     */
    protected fun clearError() {
        updateState { 
            setLoadingState(isLoading, null)
        }
    }
}

/**
 * Extension function for type-safe state updates
 * Follows Open/Closed Principle - open for extension, closed for modification
 * This is the BEST PRACTICE approach for Clean Architecture
 */
@Suppress("UNCHECKED_CAST")
private fun <T : BaseUiState> T.setLoadingState(loading: Boolean, error: String?): T {
    return when (this) {
        is CreateNoteUiState -> {
            this.copy(isLoading = loading, errorMessage = error) as T
        }
        is com.vicherarr.memora.presentation.viewmodels.NotesUiState -> {
            this.copy(isLoading = loading, errorMessage = error) as T
        }
        is com.vicherarr.memora.presentation.viewmodels.NoteDetailUiState -> {
            this.copy(isLoading = loading, errorMessage = error) as T
        }
        // Add other UiState types as we convert them - following Open/Closed Principle
        else -> throw IllegalArgumentException("UiState type ${this::class.simpleName} not supported yet. Please add it to setLoadingState extension.")
    }
}