package com.vicherarr.memora.data.auth

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import com.vicherarr.memora.domain.model.AuthState
import com.vicherarr.memora.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

/**
 * Implementación Android de CloudAuthProvider usando Google Sign-In con Activity Result API
 */
actual class CloudAuthProvider(
    private val context: Context
) {
    
    companion object {
        private const val TAG = "CloudAuthProvider"
        // Web Client ID (para requestIdToken)
        private const val WEB_CLIENT_ID = "1042434065446-nga3i4gt2c1vhadlfs87r44vcrk7e1mb.apps.googleusercontent.com"
        
        // Reference estática al ActivityResultManager actual
        private var activityResultManager: ActivityResultManager? = null
        
        /**
         * Inicializa el ActivityResultManager desde MainActivity
         */
        fun initializeActivityManager(manager: ActivityResultManager) {
            activityResultManager = manager
            Log.d(TAG, "ActivityResultManager inicializado correctamente")
        }
    }
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    actual val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .build()
        
        GoogleSignIn.getClient(context, gso)
    }
    
    actual suspend fun signIn(): Result<User> {
        return try {
            Log.d(TAG, "Iniciando flujo de Google Sign-In...")
            _authState.value = AuthState.Loading
            
            // PASO 1: Verificar si ya hay cuenta autenticada válida
            val existingAccount = GoogleSignIn.getLastSignedInAccount(context)
            if (existingAccount != null && GoogleSignIn.hasPermissions(existingAccount, Scope(DriveScopes.DRIVE_APPDATA))) {
                Log.d(TAG, "Cuenta existente válida encontrada: ${existingAccount.email}")
                val user = createUserFromAccount(existingAccount)
                _authState.value = AuthState.Authenticated(user)
                return Result.success(user)
            }
            
            // PASO 2: Intentar autenticación silenciosa
            Log.d(TAG, "Intentando autenticación silenciosa...")
            try {
                val silentAccount = googleSignInClient.silentSignIn().await()
                if (silentAccount != null && GoogleSignIn.hasPermissions(silentAccount, Scope(DriveScopes.DRIVE_APPDATA))) {
                    Log.d(TAG, "Autenticación silenciosa exitosa: ${silentAccount.email}")
                    val user = createUserFromAccount(silentAccount)
                    _authState.value = AuthState.Authenticated(user)
                    return Result.success(user)
                }
            } catch (e: Exception) {
                Log.d(TAG, "Autenticación silenciosa falló: ${e.message}")
                // Continuar con autenticación interactiva
            }
            
            // PASO 3: Verificar que ActivityResultManager esté listo
            val manager = activityResultManager
            if (manager == null || !manager.isReady()) {
                throw Exception("Sistema de autenticación no está listo. Inténtalo de nuevo.")
            }
            
            // PASO 4: Lanzar autenticación interactiva
            Log.d(TAG, "Lanzando autenticación interactiva...")
            val interactiveSuccess = manager.launchInteractiveSignIn()
            
            if (!interactiveSuccess) {
                Log.d(TAG, "Usuario canceló la autenticación")
                _authState.value = AuthState.Unauthenticated
                return Result.failure(Exception("Autenticación cancelada por el usuario"))
            }
            
            // PASO 5: Obtener cuenta después de autenticación interactiva
            val finalAccount = GoogleSignIn.getLastSignedInAccount(context)
            if (finalAccount == null || !GoogleSignIn.hasPermissions(finalAccount, Scope(DriveScopes.DRIVE_APPDATA))) {
                throw Exception("Error obteniendo cuenta después de autenticación exitosa")
            }
            
            Log.d(TAG, "Autenticación interactiva completada exitosamente: ${finalAccount.email}")
            val user = createUserFromAccount(finalAccount)
            _authState.value = AuthState.Authenticated(user)
            Result.success(user)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error en flujo de autenticación: ${e.message}", e)
            val errorMessage = when {
                e.message?.contains("SIGN_IN_REQUIRED") == true -> 
                    "Se requiere autenticación interactiva"
                e.message?.contains("NETWORK_ERROR") == true -> 
                    "Error de conexión. Verifica tu internet"
                e.message?.contains("cancelada") == true -> 
                    e.message!! // Mensaje ya formateado
                else -> "Error de autenticación: ${e.message}"
            }
            _authState.value = AuthState.Error(errorMessage)
            Result.failure(Exception(errorMessage))
        }
    }
    
    /**
     * Helper para crear User desde GoogleSignInAccount
     */
    private fun createUserFromAccount(account: com.google.android.gms.auth.api.signin.GoogleSignInAccount): User {
        return User(
            id = account.id ?: throw Exception("ID de cuenta no disponible"),
            email = account.email ?: throw Exception("Email no disponible"),
            displayName = account.displayName,
            profilePictureUrl = account.photoUrl?.toString()
        )
    }
    
    actual suspend fun signOut(): Result<Unit> {
        return try {
            Log.d(TAG, "Cerrando sesión de Google...")
            googleSignInClient.signOut().await()
            _authState.value = AuthState.Unauthenticated
            Log.d(TAG, "Sesión cerrada exitosamente")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error cerrando sesión: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    actual suspend fun getCurrentUser(): User? {
        return try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account != null && GoogleSignIn.hasPermissions(account, Scope(DriveScopes.DRIVE_APPDATA))) {
                User(
                    id = account.id ?: return null,
                    email = account.email ?: return null,
                    displayName = account.displayName,
                    profilePictureUrl = account.photoUrl?.toString()
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo usuario actual: ${e.message}", e)
            null
        }
    }
    
    actual suspend fun isAuthenticated(): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return account != null && GoogleSignIn.hasPermissions(account, Scope(DriveScopes.DRIVE_APPDATA))
    }
}