package com.vicherarr.memora.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Rutas type-safe usando @Serializable para Navigation Compose 2.8+
 * Esto proporciona type safety y elimina errores de rutas incorrectas
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
    data class NoteEdit(val noteId: String? = null) : MainRoute() // null para crear nueva nota
    
    @Serializable
    data object NoteCreate : MainRoute()
    
    @Serializable
    data object Search : MainRoute()
    
    @Serializable
    data object Profile : MainRoute()
    
    @Serializable
    data object Settings : MainRoute()
}

// Rutas para multimedia (pueden ser usadas desde múltiples contextos)
@Serializable
sealed class MediaRoute {
    @Serializable
    data class MediaViewer(
        val mediaId: String,
        val mediaType: String // "image" o "video"
    ) : MediaRoute()
    
    @Serializable
    data object Camera : MediaRoute()
    
    @Serializable
    data object MediaPicker : MediaRoute()
}