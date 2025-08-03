package com.vicherarr.memora.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vicherarr.memora.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Login UI State - Single Source of Truth
 * Immutable data class representing the complete state of the Login screen
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoggedIn: Boolean = false
)

/**
 * ViewModel dedicated to Login screen following JetBrains KMP patterns
 * Simple, direct methods without event system complexity
 */
class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    init {
        checkCurrentLoginState()
    }
    
    /**
     * Update email field - Direct method call
     */
    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }
    
    /**
     * Update password field - Direct method call
     */
    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }
    
    /**
     * Perform login operation - Direct method call
     */
    fun login() {
        val currentState = _uiState.value
        
        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, errorMessage = null)
            
            val validationError = validateLoginInput(currentState.email, currentState.password)
            if (validationError != null) {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    errorMessage = validationError
                )
                return@launch
            }
            
            authRepository.login(currentState.email.trim(), currentState.password)
                .onSuccess { 
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        isLoggedIn = true
                    )
                }
                .onFailure { exception ->
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Error al iniciar sesión"
                    )
                }
        }
    }
    
    private fun checkCurrentLoginState() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            if (user != null) {
                _uiState.value = _uiState.value.copy(isLoggedIn = true)
            }
        }
    }
    
    private fun validateLoginInput(email: String, password: String): String? {
        return when {
            email.isBlank() -> "El correo electrónico es requerido"
            !isValidEmail(email) -> "El correo electrónico no tiene un formato válido"
            password.isBlank() -> "La contraseña es requerida"
            password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            else -> null
        }
    }
    
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return emailRegex.matches(email.trim())
    }
}