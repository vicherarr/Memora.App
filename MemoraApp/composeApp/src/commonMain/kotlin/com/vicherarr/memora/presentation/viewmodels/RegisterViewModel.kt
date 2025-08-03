package com.vicherarr.memora.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vicherarr.memora.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Register UI State - Single Source of Truth
 * Immutable data class representing the complete state of the Register screen
 */
data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRegistered: Boolean = false
)

/**
 * ViewModel dedicated to Register screen following JetBrains KMP patterns
 * Simple, direct methods without event system complexity
 */
class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()
    
    init {
        checkCurrentLoginState()
    }
    
    /**
     * Update name field - Direct method call
     */
    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
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
     * Perform registration operation - Direct method call
     */
    fun register() {
        val currentState = _uiState.value
        
        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, errorMessage = null)
            
            val validationError = validateRegisterInput(
                currentState.name, 
                currentState.email, 
                currentState.password
            )
            if (validationError != null) {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    errorMessage = validationError
                )
                return@launch
            }
            
            authRepository.register(
                currentState.name.trim(), 
                currentState.email.trim(), 
                currentState.password
            )
                .onSuccess { 
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        isRegistered = true
                    )
                }
                .onFailure { exception ->
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Error al crear la cuenta"
                    )
                }
        }
    }
    
    private fun checkCurrentLoginState() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            if (user != null) {
                _uiState.value = _uiState.value.copy(isRegistered = true)
            }
        }
    }
    
    private fun validateRegisterInput(name: String, email: String, password: String): String? {
        return when {
            name.isBlank() -> "El nombre de usuario es requerido"
            name.length < 3 -> "El nombre de usuario debe tener al menos 3 caracteres"
            email.isBlank() -> "El correo electrónico es requerido"
            !isValidEmail(email) -> "El correo electrónico no tiene un formato válido"
            password.isBlank() -> "La contraseña es requerida"
            password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            !isValidPassword(password) -> "La contraseña debe contener al menos una letra y un número"
            else -> null
        }
    }
    
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return emailRegex.matches(email.trim())
    }
    
    private fun isValidPassword(password: String): Boolean {
        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        return hasLetter && hasDigit
    }
}