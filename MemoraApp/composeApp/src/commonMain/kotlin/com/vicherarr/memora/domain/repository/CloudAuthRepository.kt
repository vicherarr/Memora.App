package com.vicherarr.memora.domain.repository

import com.vicherarr.memora.domain.model.AuthState
import com.vicherarr.memora.domain.model.User
import kotlinx.coroutines.flow.StateFlow

/**
 * Repository para gestión de autenticación con servicios cloud (Google/Apple Sign-In)
 * Maneja la autenticación OAuth para acceso a Google Drive y iCloud
 */
interface CloudAuthRepository {
    
    /**
     * Inicia el proceso de autenticación OAuth
     * - Android: Google Sign-In para acceso a Google Drive
     * - iOS: Apple Sign-In (futuro) para acceso a iCloud / Mock temporal
     */
    suspend fun signIn(): Result<User>
    
    /**
     * Cierra la sesión del usuario actual
     */
    suspend fun signOut(): Result<Unit>
    
    /**
     * Obtiene el usuario actualmente autenticado
     */
    suspend fun getCurrentUser(): User?
    
    /**
     * Verifica si hay una sesión activa válida
     */
    suspend fun isAuthenticated(): Boolean
    
    /**
     * StateFlow reactivo del estado de autenticación cloud
     */
    val authState: StateFlow<AuthState>
}