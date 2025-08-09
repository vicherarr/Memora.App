package com.vicherarr.memora.sync

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Implementación de CloudStorageProvider para Android usando Google Drive
 */
actual interface CloudStorageProvider {
    actual suspend fun autenticar()
    actual suspend fun descargarDB(): ByteArray?
    actual suspend fun subirDB(data: ByteArray)
    actual suspend fun obtenerMetadatosRemotos(): Long?
}

/**
 * Implementación real para Google Drive AppDataFolder
 */
class GoogleDriveStorageProvider(
    private val context: Context
) : CloudStorageProvider {
    
    companion object {
        private const val TAG = "GoogleDriveStorage"
        private const val DB_FILE_NAME = "memora_sync.db"
        // Web Client ID (para requestIdToken)
        private const val WEB_CLIENT_ID = "1042434065446-nga3i4gt2c1vhadlfs87r44vcrk7e1mb.apps.googleusercontent.com"
    }
    
    private var driveService: Drive? = null
    private var signInClient: GoogleSignInClient? = null
    
    init {
        initializeGoogleSignIn()
    }
    
    private fun initializeGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .build()
        
        signInClient = GoogleSignIn.getClient(context, gso)
    }
    
    override suspend fun autenticar() {
        try {
            Log.d(TAG, "Iniciando autenticación con Google Drive...")
            
            // Verificar si ya hay una cuenta autenticada
            val existingAccount = GoogleSignIn.getLastSignedInAccount(context)
            val account = if (existingAccount != null && GoogleSignIn.hasPermissions(existingAccount, Scope(DriveScopes.DRIVE_APPDATA))) {
                Log.d(TAG, "Cuenta existente encontrada: ${existingAccount.email}")
                existingAccount
            } else {
                // Necesita nueva autenticación
                Log.d(TAG, "Se requiere nueva autenticación")
                signInSilently()
            }
            
            // Crear el servicio Drive
            createDriveService(account)
            Log.d(TAG, "Autenticación completada exitosamente")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error en autenticación: ${e.message}", e)
            throw Exception("Error de autenticación con Google Drive: ${e.message}")
        }
    }
    
    private suspend fun signInSilently(): GoogleSignInAccount = suspendCoroutine { continuation ->
        signInClient?.silentSignIn()?.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result != null) {
                continuation.resume(task.result)
            } else {
                continuation.resumeWithException(
                    Exception("Autenticación silenciosa falló. Se requiere interacción del usuario.")
                )
            }
        }
    }
    
    private fun createDriveService(account: GoogleSignInAccount) {
        val credential = GoogleAccountCredential.usingOAuth2(context, listOf(DriveScopes.DRIVE_APPDATA))
        credential.selectedAccount = account.account
        
        driveService = Drive.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("Memora")
            .build()
    }
    
    override suspend fun descargarDB(): ByteArray? {
        // TODO: Implementar descarga desde AppDataFolder
        Log.d(TAG, "Descarga de DB - TODO")
        return null
    }
    
    override suspend fun subirDB(data: ByteArray) {
        // TODO: Implementar subida a AppDataFolder
        Log.d(TAG, "Subida de DB - TODO: ${data.size} bytes")
    }
    
    override suspend fun obtenerMetadatosRemotos(): Long? {
        // TODO: Implementar obtención de metadatos del archivo remoto
        Log.d(TAG, "Obtener metadatos - TODO")
        return null
    }
}

/**
 * Factory function para Android
 */
actual fun getCloudStorageProvider(): CloudStorageProvider {
    // Se necesitará el contexto - esto se manejará con DI más adelante
    throw NotImplementedError("Use getCloudStorageProvider(context) instead")
}

/**
 * Factory function con contexto para Android
 */
fun getCloudStorageProvider(context: Context): CloudStorageProvider {
    return GoogleDriveStorageProvider(context)
}