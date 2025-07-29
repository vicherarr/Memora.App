package com.vicherarr.memora.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Rutas type-safe usando @Serializable para Navigation Compose
 * Simplificadas sin deep links - solo para navegación interna
 */

// Rutas principales de la aplicación
@Serializable
sealed class AppRoute {
    @Serializable
    data object Splash : AppRoute()
    
    @Serializable
    data object Auth : AppRoute()
    
    @Serializable
    data object Main : AppRoute()
}

// Rutas específicas de autenticación
@Serializable
sealed class AuthRoute {
    @Serializable
    data object Login : AuthRoute()
    
    @Serializable
    data object Register : AuthRoute()
}

// Rutas principales de la aplicación (post-autenticación)
@Serializable
sealed class MainRoute {
    @Serializable
    data object Notes : MainRoute()
    
    @Serializable
    data class NoteDetail(val noteId: String) : MainRoute()
    
    @Serializable
    data class NoteEdit(val noteId: String) : MainRoute() // Siempre requiere ID para editar
    
    @Serializable
    data object NoteCreate : MainRoute() // Crear nueva nota - explícito y claro
}