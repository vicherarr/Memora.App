package com.vicherarr.memora.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vicherarr.memora.data.auth.CloudAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

/**
 * Splash Screen Navigation State
 * 
 * Sealed class for type-safe navigation decisions.
 */
sealed class SplashNavigationState {
    object Loading : SplashNavigationState()
    object NavigateToMain : SplashNavigationState()
    object NavigateToWelcome : SplashNavigationState()
}

/**
 * Splash ViewModel
 * 
 * Handles authentication verification and navigation decision.
 * Following MVVM pattern with Clean Architecture principles.
 */
class SplashViewModel(
    private val cloudAuthProvider: CloudAuthProvider
) : ViewModel() {
    
    private val _navigationState = MutableStateFlow<SplashNavigationState>(SplashNavigationState.Loading)
    val navigationState: StateFlow<SplashNavigationState> = _navigationState.asStateFlow()
    
    init {
        checkAuthentication()
    }
    
    /**
     * Check if user is authenticated and decide navigation
     * 
     * Verifies actual session validity against Google servers, not just local tokens.
     * Following Single Responsibility Principle.
     */
    private fun checkAuthentication() {
        viewModelScope.launch {
            try {
                println("SplashViewModel: 🔍 Iniciando verificación de autenticación...")
                
                // First check if we have stored credentials locally
                val hasStoredAuth = cloudAuthProvider.isAuthenticated()
                println("SplashViewModel: 🔑 Auth local almacenado: $hasStoredAuth")
                
                if (!hasStoredAuth) {
                    println("SplashViewModel: ❌ No hay auth local → Navegando a Welcome")
                    delay(2000)
                    _navigationState.value = SplashNavigationState.NavigateToWelcome
                    return@launch
                }
                
                // Verify if we can get current user from auth provider
                println("SplashViewModel: 🌐 Verificando usuario actual...")
                val currentUser = cloudAuthProvider.getCurrentUser()
                println("SplashViewModel: 👤 Usuario obtenido: ${currentUser?.email ?: "null"}")
                
                // Wait 2 seconds for better UX before navigating
                delay(2000)
                
                _navigationState.value = if (currentUser != null) {
                    // Valid user found
                    println("SplashViewModel: ✅ Usuario válido → Navegando a Main")
                    SplashNavigationState.NavigateToMain
                } else {
                    // No valid user - need to re-authenticate
                    println("SplashViewModel: ❌ No hay usuario válido → Navegando a Welcome")
                    SplashNavigationState.NavigateToWelcome
                }
            } catch (e: Exception) {
                // If there's an error checking authentication, go to welcome
                println("SplashViewModel: 💥 Error verificando auth: ${e.message} → Navegando a Welcome")
                delay(2000)
                _navigationState.value = SplashNavigationState.NavigateToWelcome
            }
        }
    }
}