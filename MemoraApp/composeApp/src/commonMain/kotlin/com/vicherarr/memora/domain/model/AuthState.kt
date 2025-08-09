package com.vicherarr.memora.domain.model

/**
 * Estados posibles del sistema de autenticación
 */
sealed class AuthState {
    /**
     * Usuario no autenticado
     */
    object Unauthenticated : AuthState()
    
    /**
     * Proceso de autenticación en curso
     */
    object Loading : AuthState()
    
    /**
     * Usuario autenticado exitosamente
     */
    data class Authenticated(val user: User) : AuthState()
    
    /**
     * Error en el proceso de autenticación
     */
    data class Error(val message: String) : AuthState()
}