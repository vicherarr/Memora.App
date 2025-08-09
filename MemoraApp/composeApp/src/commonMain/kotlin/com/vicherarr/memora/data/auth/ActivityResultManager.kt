package com.vicherarr.memora.data.auth

/**
 * Manager para manejar Activity Results de forma multiplataforma
 * 
 * IMPORTANTE KMP:
 * - commonMain: Solo define el contrato
 * - androidMain: Implementación real con ActivityResultLauncher + GoogleSignInClient
 * - iosMain: Mock que simula el comportamiento (no usa Activity Results)
 */
expect class ActivityResultManager {
    
    /**
     * Lanza el flujo interactivo de autenticación específico de la plataforma
     * - Android: Google Sign-In con Activity Result API
     * - iOS: Mock que simula Apple Sign-In
     * 
     * @return true si la autenticación fue exitosa, false si se canceló
     * @throws Exception si hay error en la autenticación
     */
    suspend fun launchInteractiveSignIn(): Boolean
    
    /**
     * Verifica si el manager está correctamente inicializado
     * @return true si está listo para usar
     */
    fun isReady(): Boolean
}