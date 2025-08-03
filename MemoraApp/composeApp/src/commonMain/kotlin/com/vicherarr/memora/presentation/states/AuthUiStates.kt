package com.vicherarr.memora.presentation.states

/**
 * Login UI State following Open/Closed Principle
 * Extends BaseUiState interface without modification
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val operationState: OperationState = OperationState(),
    val isLoggedIn: Boolean = false
) : BaseUiState by operationState {
    
    // Computed properties for better encapsulation
    val canLogin: Boolean get() = email.isNotBlank() && password.isNotBlank() && !isLoading
    val shouldShowError: Boolean get() = operationState.isError && !isLoading
}

/**
 * Register UI State following Open/Closed Principle
 * Consistent with LoginUiState structure
 */
data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val operationState: OperationState = OperationState(),
    val isRegistered: Boolean = false
) : BaseUiState by operationState {
    
    // Computed properties for better encapsulation
    val canRegister: Boolean get() = name.isNotBlank() && email.isNotBlank() && password.isNotBlank() && !isLoading
    val shouldShowError: Boolean get() = operationState.isError && !isLoading
}

/**
 * Create Note UI State following the same pattern
 */
data class CreateNoteUiState(
    val titulo: String = "",
    val contenido: String = "",
    val operationState: OperationState = OperationState(),
    val isNoteSaved: Boolean = false
) : BaseUiState by operationState {
    
    // Computed properties for better encapsulation
    val canSaveNote: Boolean get() = contenido.isNotBlank() && !isLoading
    val shouldShowError: Boolean get() = operationState.isError && !isLoading
}