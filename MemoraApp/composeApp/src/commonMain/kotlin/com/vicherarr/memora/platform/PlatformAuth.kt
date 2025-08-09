package com.vicherarr.memora.platform

/**
 * Textos de autenticación específicos por plataforma
 */
expect object PlatformAuth {
    /**
     * Texto del botón de inicio de sesión siguiendo las guías de cada plataforma
     * - Android: "Iniciar sesión con Google" (Material Design)
     * - iOS: "Continuar con Apple" (Human Interface Guidelines)
     */
    val signInButtonText: String
}