package com.vicherarr.memora.data.auth

import com.vicherarr.memora.domain.model.AuthState
import com.vicherarr.memora.domain.model.User
import kotlinx.coroutines.flow.StateFlow

/**
 * Provider de autenticación específico para cada plataforma
 * - Android: Implementación real con Google Sign-In
 * - iOS: Mock temporal preparado para Apple Sign-In
 */
expect class CloudAuthProvider {
    
    /**
     * Inicia proceso de autenticación OAuth
     */
    suspend fun signIn(): Result<User>
    
    /**
     * Cierra sesión actual
     */
    suspend fun signOut(): Result<Unit>
    
    /**
     * Obtiene usuario actual
     */
    suspend fun getCurrentUser(): User?
    
    /**
     * Verifica si está autenticado
     */
    suspend fun isAuthenticated(): Boolean
    
    /**
     * Estado reactivo de autenticación
     */
    val authState: StateFlow<AuthState>
}