package com.vicherarr.memora.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vicherarr.memora.data.auth.CloudAuthProvider
import com.vicherarr.memora.domain.model.AuthState
import com.vicherarr.memora.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para manejo de autenticación cloud (Google/Apple Sign-In)
 * Centraliza la lógica de autenticación para acceso a servicios de nube.
 * DELEGA toda la gestión de estado al CloudAuthProvider.
 */
class CloudLoginViewModel(
    private val cloudAuthProvider: CloudAuthProvider // Inyectar el proveedor central
) : ViewModel() {

    // Exponer el estado directamente desde la fuente única de verdad
    val authState: StateFlow<AuthState> = cloudAuthProvider.authState

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        // Al iniciar el ViewModel, intentar un inicio de sesión silencioso
        // para restaurar el estado de autenticación si la app se reinicia.
        checkCurrentUser()
    }

    /**
     * Inicia el proceso de autenticación.
     * El CloudAuthProvider se encarga de la lógica (silencioso, interactivo) y de actualizar su propio estado.
     */
    fun signIn() {
        viewModelScope.launch {
            _isLoading.value = true
            cloudAuthProvider.signIn() // Delegar la llamada
            _isLoading.value = false
        }
    }

    /**
     * Cierra la sesión del usuario actual.
     */
    fun signOut() {
        viewModelScope.launch {
            _isLoading.value = true
            cloudAuthProvider.signOut() // Delegar la llamada
            _isLoading.value = false
        }
    }

    /**
     * Verifica si hay un usuario autenticado actualmente.
     * Esto es útil para restaurar el estado al iniciar la app.
     */
    private fun checkCurrentUser() {
        viewModelScope.launch {
            // La función signIn del proveedor ya maneja la lógica de "verificar y autenticar si es posible".
            // No es necesario un estado de carga aquí para que sea una comprobación silenciosa.
            println("CloudLoginViewModel: Verificando usuario actual (inicio silencioso)...")
            cloudAuthProvider.signIn()
        }
    }

    /**
     * Obtiene el usuario actualmente autenticado.
     */
    fun getCurrentUser(): User? {
        return when (val state = authState.value) {
            is AuthState.Authenticated -> state.user
            else -> null
        }
    }
}
