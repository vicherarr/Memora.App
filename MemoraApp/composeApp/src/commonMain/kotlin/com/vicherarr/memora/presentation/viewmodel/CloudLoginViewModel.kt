package com.vicherarr.memora.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vicherarr.memora.domain.model.AuthState
import com.vicherarr.memora.domain.model.User
import com.vicherarr.memora.domain.usecase.auth.CloudSignInUseCase
import com.vicherarr.memora.domain.usecase.auth.CloudSignOutUseCase
import com.vicherarr.memora.domain.usecase.auth.GetCurrentCloudUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para manejo de autenticación cloud (Google/Apple Sign-In)
 * Centraliza la lógica de autenticación para acceso a servicios de nube
 */
class CloudLoginViewModel(
    private val signInUseCase: CloudSignInUseCase,
    private val signOutUseCase: CloudSignOutUseCase,
    private val getCurrentUserUseCase: GetCurrentCloudUserUseCase
) : ViewModel() {
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        // Verificar si hay usuario autenticado al inicializar
        checkCurrentUser()
    }
    
    /**
     * Inicia el proceso de autenticación
     * - Android: Google Sign-In
     * - iOS: Apple Sign-In Mock
     */
    fun signIn() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _authState.value = AuthState.Loading
                
                val result = signInUseCase()
                
                result.fold(
                    onSuccess = { user ->
                        _authState.value = AuthState.Authenticated(user)
                        println("CloudAuth: Usuario autenticado - ${user.email}")
                    },
                    onFailure = { error ->
                        val errorMessage = error.message ?: "Error desconocido de autenticación"
                        _authState.value = AuthState.Error(errorMessage)
                        println("CloudAuth: Error de autenticación - $errorMessage")
                    }
                )
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Error inesperado: ${e.message}")
                println("CloudAuth: Excepción - ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Cierra la sesión del usuario actual
     */
    fun signOut() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                val result = signOutUseCase()
                
                result.fold(
                    onSuccess = {
                        _authState.value = AuthState.Unauthenticated
                        println("CloudAuth: Sesión cerrada exitosamente")
                    },
                    onFailure = { error ->
                        println("CloudAuth: Error cerrando sesión - ${error.message}")
                        // Aun con error, marcamos como no autenticado para seguridad
                        _authState.value = AuthState.Unauthenticated
                    }
                )
            } catch (e: Exception) {
                println("CloudAuth: Excepción cerrando sesión - ${e.message}")
                _authState.value = AuthState.Unauthenticated
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Verifica si hay un usuario autenticado actualmente
     */
    private fun checkCurrentUser() {
        viewModelScope.launch {
            try {
                val currentUser = getCurrentUserUseCase()
                if (currentUser != null) {
                    _authState.value = AuthState.Authenticated(currentUser)
                    println("CloudAuth: Usuario existente encontrado - ${currentUser.email}")
                } else {
                    _authState.value = AuthState.Unauthenticated
                }
            } catch (e: Exception) {
                println("CloudAuth: Error verificando usuario actual - ${e.message}")
                _authState.value = AuthState.Unauthenticated
            }
        }
    }
    
    /**
     * Obtiene el usuario actualmente autenticado
     */
    fun getCurrentUser(): User? {
        return when (val state = authState.value) {
            is AuthState.Authenticated -> state.user
            else -> null
        }
    }
    
    /**
     * Limpia el estado de error
     */
    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Unauthenticated
        }
    }
}