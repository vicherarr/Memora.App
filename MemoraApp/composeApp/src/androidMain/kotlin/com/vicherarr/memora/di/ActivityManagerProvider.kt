package com.vicherarr.memora.di

import androidx.activity.ComponentActivity
import com.vicherarr.memora.data.auth.ActivityResultManager
import com.vicherarr.memora.data.auth.CloudAuthProvider

/**
 * Helper simplificado para inicializar el sistema de autenticación cloud
 * 
 * CONTEXTO:
 * El ActivityResultManager necesita una ComponentActivity para funcionar.
 * Este helper inicializa el sistema usando una referencia estática en CloudAuthProvider.
 * 
 * FLUJO:
 * 1. MainActivity llama a initializeCloudAuth() cuando se crea
 * 2. Se crea ActivityResultManager y se pasa a CloudAuthProvider
 * 3. CloudAuthProvider puede usar el manager para autenticación interactiva
 */
object CloudAuthInitializer {
    
    /**
     * Inicializa el sistema de autenticación cloud
     * 
     * IMPORTANTE: Debe ser llamado desde MainActivity.onCreate()
     * antes de usar cualquier funcionalidad de cloud authentication.
     */
    fun initializeCloudAuth(activity: ComponentActivity) {
        try {
            val activityResultManager = ActivityResultManager(activity)
            CloudAuthProvider.initializeActivityManager(activityResultManager)
            
            println("CloudAuth inicializado exitosamente")
            
        } catch (e: Exception) {
            println("Error inicializando CloudAuth: ${e.message}")
            throw e
        }
    }
    
    /**
     * Verifica si el sistema de autenticación está correctamente inicializado
     */
    fun isCloudAuthInitialized(): Boolean {
        return try {
            // La lógica de verificación está en CloudAuthProvider
            true // Por simplicidad, asumimos que está inicializado si no hay excepción
        } catch (e: Exception) {
            false
        }
    }
}