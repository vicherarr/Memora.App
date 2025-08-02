package com.vicherarr.memora.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vicherarr.memora.domain.models.User
import com.vicherarr.memora.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for authentication operations
 */
class AuthViewModel(
    private val authRepository: AuthRepository
) : BaseViewModel() {
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()
    
    init {
        checkCurrentUser()
    }
    
    fun login(correoElectronico: String, contrasena: String) {
        viewModelScope.launch {
            setLoading(true)
            clearError()
            
            // Validar entrada
            val validationError = validateLoginInput(correoElectronico, contrasena)
            if (validationError != null) {
                setError(validationError)
                setLoading(false)
                return@launch
            }
            
            authRepository.login(correoElectronico.trim(), contrasena)
                .onSuccess { user ->
                    _currentUser.value = user
                    _isLoggedIn.value = true
                }
                .onFailure { exception ->
                    setError(exception.message ?: "Error al iniciar sesión")
                }
            
            setLoading(false)
        }
    }
    
    fun register(nombreUsuario: String, correoElectronico: String, contrasena: String) {
        viewModelScope.launch {
            setLoading(true)
            clearError()
            
            // Validar entrada
            val validationError = validateRegisterInput(nombreUsuario, correoElectronico, contrasena)
            if (validationError != null) {
                setError(validationError)
                setLoading(false)
                return@launch
            }
            
            authRepository.register(nombreUsuario.trim(), correoElectronico.trim(), contrasena)
                .onSuccess { user ->
                    _currentUser.value = user
                    _isLoggedIn.value = true
                }
                .onFailure { exception ->
                    setError(exception.message ?: "Error al crear la cuenta")
                }
            
            setLoading(false)
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _currentUser.value = null
            _isLoggedIn.value = false
        }
    }
    
    private fun checkCurrentUser() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            _currentUser.value = user
            _isLoggedIn.value = user != null
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
    
    private fun validateRegisterInput(username: String, email: String, password: String): String? {
        return when {
            username.isBlank() -> "El nombre de usuario es requerido"
            username.length < 3 -> "El nombre de usuario debe tener al menos 3 caracteres"
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