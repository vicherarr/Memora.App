package com.vicherarr.memora.data.network

/**
 * API configuration with platform-specific URLs for development
 */
object ApiConfig {
    
    /**
     * API port number
     */
    const val API_PORT = 5003
    
    /**
     * Base URL for API - platform specific
     * Android Emulator: 10.0.2.2 (special IP for host machine)
     * iOS Simulator: localhost (works directly)
     */
    val BASE_URL: String
        get() = getBaseUrl()
    
    /**
     * Full API base URL with version
     */
    val API_BASE_URL: String
        get() = "$BASE_URL/api"
    
    /**
     * Authentication endpoints
     */
    object Auth {
        const val LOGIN = "/autenticacion/login"
        const val REGISTER = "/autenticacion/registrar"
    }
    
    /**
     * Notes endpoints
     */
    object Notes {
        const val NOTES = "/notas"
        fun noteById(id: String) = "/notas/$id"
    }
    
    /**
     * Files endpoints
     */
    object Files {
        fun uploadToNote(noteId: String) = "/notas/$noteId/archivos"
        fun fileById(fileId: String) = "/archivos/$fileId"
    }
}

/**
 * Platform-specific base URL configuration
 * Will be implemented in androidMain and iosMain
 */
expect fun getBaseUrl(): String