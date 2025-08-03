package com.vicherarr.memora.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vicherarr.memora.domain.repository.AuthRepository
import com.vicherarr.memora.domain.validation.ValidationService
import com.vicherarr.memora.presentation.states.RegisterUiState
import com.vicherarr.memora.presentation.states.withLoading
import com.vicherarr.memora.presentation.states.withError
import com.vicherarr.memora.presentation.states.withSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel dedicated to Register screen following SOLID principles
 * Single Responsibility: Only handles registration UI logic
 * Dependency Inversion: Depends on abstractions (interfaces)
 * Open/Closed: Extensible through composition, closed for modification
 */
class RegisterViewModel(
    private val authRepository: AuthRepository,
    private val validationService: ValidationService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()
    
    init {
        checkCurrentLoginState()
    }
    
    /**
     * Update name field - Direct method call
     * Single Responsibility: Only updates name
     */
    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }
    
    /**
     * Update email field - Direct method call
     * Single Responsibility: Only updates email
     */
    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }
    
    /**
     * Update password field - Direct method call
     * Single Responsibility: Only updates password
     */
    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }
    
    /**
     * Perform registration operation - Direct method call
     * Uses injected services following Dependency Inversion Principle
     */
    fun register() {
        val currentState = _uiState.value
        
        viewModelScope.launch {
            // Set loading state using extension function
            _uiState.value = currentState.withLoading()
            
            // Validate name using ValidationService (Single Responsibility)
            val nameValidation = validationService.validateName(currentState.name)
            if (!nameValidation.isValid) {
                _uiState.value = currentState.withError(nameValidation.errorMessage!!)
                return@launch
            }
            
            // Validate email using ValidationService (Single Responsibility)
            val emailValidation = validationService.validateEmail(currentState.email)
            if (!emailValidation.isValid) {
                _uiState.value = currentState.withError(emailValidation.errorMessage!!)
                return@launch
            }
            
            // Validate password using ValidationService (Single Responsibility)
            val passwordValidation = validationService.validatePassword(currentState.password)
            if (!passwordValidation.isValid) {
                _uiState.value = currentState.withError(passwordValidation.errorMessage!!)
                return@launch
            }
            
            // Perform registration using repository
            authRepository.register(
                currentState.name.trim(), 
                currentState.email.trim(), 
                currentState.password
            )
                .onSuccess { 
                    _uiState.value = currentState.withSuccess()
                }
                .onFailure { exception ->
                    _uiState.value = currentState.withError(
                        exception.message ?: "Error al crear la cuenta"
                    )
                }
        }
    }
    
    /**
     * Check current login state
     * Single Responsibility: Only checks authentication status
     */
    private fun checkCurrentLoginState() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            if (user != null) {
                _uiState.value = _uiState.value.withSuccess()
            }
        }
    }
}