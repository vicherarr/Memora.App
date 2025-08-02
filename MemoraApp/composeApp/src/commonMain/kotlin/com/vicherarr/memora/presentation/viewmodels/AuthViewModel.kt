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
            
            authRepository.login(correoElectronico, contrasena)
                .onSuccess { user ->
                    _currentUser.value = user
                    _isLoggedIn.value = true
                }
                .onFailure { exception ->
                    setError(exception.message ?: "Error during login")
                }
            
            setLoading(false)
        }
    }
    
    fun register(nombreUsuario: String, correoElectronico: String, contrasena: String) {
        viewModelScope.launch {
            setLoading(true)
            clearError()
            
            authRepository.register(nombreUsuario, correoElectronico, contrasena)
                .onSuccess { user ->
                    _currentUser.value = user
                    _isLoggedIn.value = true
                }
                .onFailure { exception ->
                    setError(exception.message ?: "Error during registration")
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
}