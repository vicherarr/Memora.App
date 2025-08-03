package com.vicherarr.memora.presentation.states

/**
 * Base UI State interface following Open/Closed Principle
 * Allows extension without modification of existing code
 */
interface BaseUiState {
    val isLoading: Boolean
    val errorMessage: String?
}

/**
 * Loading state sealed class for type-safe state management
 * Follows Single Responsibility Principle - only handles loading states
 */
sealed class LoadingState {
    object Idle : LoadingState()
    object Loading : LoadingState()
    data class Error(val message: String) : LoadingState()
    object Success : LoadingState()
    
    val isLoading: Boolean get() = this is Loading
    val errorMessage: String? get() = (this as? Error)?.message
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
}

/**
 * Common UI operations state
 * Centralized approach following DRY principle
 */
data class OperationState(
    val loadingState: LoadingState = LoadingState.Idle,
    val successMessage: String? = null
) : BaseUiState {
    override val isLoading: Boolean get() = loadingState.isLoading
    override val errorMessage: String? get() = loadingState.errorMessage
    
    val isSuccess: Boolean get() = loadingState.isSuccess
    val isError: Boolean get() = loadingState.isError
    val hasSuccessMessage: Boolean get() = successMessage != null
}