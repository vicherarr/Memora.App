package com.vicherarr.memora.data.auth

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Implementación Android del ActivityResultManager usando Activity Result API
 */
actual class ActivityResultManager(
    private val activity: ComponentActivity
) {
    companion object {
        private const val TAG = "ActivityResultManager"
        // Web Client ID (para requestIdToken)
        private const val WEB_CLIENT_ID = "1042434065446-nga3i4gt2c1vhadlfs87r44vcrk7e1mb.apps.googleusercontent.com"
    }
    
    private var signInLauncher: ActivityResultLauncher<android.content.Intent>? = null
    private var pendingContinuation: kotlinx.coroutines.CancellableContinuation<Boolean>? = null
    
    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .build()
        
        GoogleSignIn.getClient(activity, gso)
    }
    
    init {
        setupActivityResultLauncher()
    }
    
    private fun setupActivityResultLauncher() {
        signInLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            Log.d(TAG, "Activity result received: ${result.resultCode}")
            
            val continuation = pendingContinuation
            pendingContinuation = null
            
            if (continuation?.isActive == true) {
                try {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    val account = task.result
                    
                    if (account != null) {
                        Log.d(TAG, "Google Sign-In exitoso: ${account.email}")
                        continuation.resume(true)
                    } else {
                        Log.d(TAG, "Google Sign-In cancelado por el usuario")
                        continuation.resume(false)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error procesando resultado de Google Sign-In", e)
                    continuation.resumeWithException(e)
                }
            }
        }
    }
    
    actual suspend fun launchInteractiveSignIn(): Boolean = suspendCancellableCoroutine { continuation ->
        try {
            Log.d(TAG, "Iniciando Google Sign-In interactivo...")
            
            val launcher = signInLauncher
            if (launcher == null) {
                continuation.resumeWithException(Exception("ActivityResultLauncher no inicializado"))
                return@suspendCancellableCoroutine
            }
            
            // Cancelar cualquier continuación pendiente
            pendingContinuation?.cancel()
            pendingContinuation = continuation
            
            // Configurar cancelación
            continuation.invokeOnCancellation {
                Log.d(TAG, "Google Sign-In cancelado por corrutina")
                pendingContinuation = null
            }
            
            // Lanzar intent de Google Sign-In
            val signInIntent = googleSignInClient.signInIntent
            launcher.launch(signInIntent)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error lanzando Google Sign-In", e)
            pendingContinuation = null
            continuation.resumeWithException(e)
        }
    }
    
    actual fun isReady(): Boolean {
        return signInLauncher != null
    }
}