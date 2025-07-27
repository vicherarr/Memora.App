package com.vicherarr.memora.presentation.viewmodels

import com.vicherarr.memora.domain.repository.AuthRepository
import com.vicherarr.memora.presentation.utils.AsyncUiState
import com.vicherarr.memora.domain.models.User
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope

/**
 * ViewModel para operaciones de autenticación
 */
class AuthViewModel(
    private val authRepository: AuthRepository
) : BaseViewModel<AuthUiState>(AuthUiState()) {
    
    // Estados individuales para las pantallas
    val loginState: StateFlow<AsyncUiState<User>> = uiState
        .map { it.loginState }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AsyncUiState.Idle
        )
    
    val registerState: StateFlow<AsyncUiState<User>> = uiState
        .map { it.registerState }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AsyncUiState.Idle
        )
    
    val currentUser: StateFlow<User?> = uiState
        .map { it.currentUser }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    
    fun login(email: String, password: String) {
        updateState { it.copy(loginState = AsyncUiState.Loading) }
        
        launchSafe(
            onError = { error ->
                updateState { 
                    it.copy(loginState = AsyncUiState.Error(error))
                }
            }
        ) {
            val user = authRepository.login(email, password)
            updateState { 
                it.copy(
                    loginState = AsyncUiState.Success(user),
                    currentUser = user
                )
            }
        }
    }
    
    fun register(username: String, email: String, password: String) {
        updateState { it.copy(registerState = AsyncUiState.Loading) }
        
        launchSafe(
            onError = { error ->
                updateState { 
                    it.copy(registerState = AsyncUiState.Error(error))
                }
            }
        ) {
            val user = authRepository.register(username, email, password)
            updateState { 
                it.copy(
                    registerState = AsyncUiState.Success(user),
                    currentUser = user
                )
            }
        }
    }
    
    fun logout() {
        launchSafe {
            authRepository.logout()
            updateState { 
                it.copy(
                    currentUser = null,
                    loginState = AsyncUiState.Idle,
                    registerState = AsyncUiState.Idle
                )
            }
        }
    }
    
    fun clearLoginState() {
        updateState { it.copy(loginState = AsyncUiState.Idle) }
    }
    
    fun clearRegisterState() {
        updateState { it.copy(registerState = AsyncUiState.Idle) }
    }
}

/**
 * Estado de UI para autenticación
 */
data class AuthUiState(
    val currentUser: User? = null,
    val loginState: AsyncUiState<User> = AsyncUiState.Idle,
    val registerState: AsyncUiState<User> = AsyncUiState.Idle
) : com.vicherarr.memora.presentation.utils.UiState