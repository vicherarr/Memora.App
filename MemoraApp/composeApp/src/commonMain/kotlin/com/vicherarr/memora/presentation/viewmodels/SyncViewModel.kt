package com.vicherarr.memora.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vicherarr.memora.data.auth.CloudAuthProvider
import com.vicherarr.memora.domain.model.AuthState
import com.vicherarr.memora.domain.repository.NotesRepository
import com.vicherarr.memora.sync.SyncEngine
import com.vicherarr.memora.sync.SyncState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para manejar la sincronización cloud.
 * Inicia una sincronización automáticamente al autenticarse.
 */
class SyncViewModel(
    private val syncEngine: SyncEngine,
    private val cloudAuthProvider: CloudAuthProvider,
    private val notesRepository: NotesRepository
) : ViewModel() {

    val syncState: StateFlow<SyncState> = syncEngine.syncState
    private var isFirstSyncDone = false

    init {
        // Iniciar sincronización automática cuando el usuario se autentique por primera vez
        viewModelScope.launch {
            // Espera hasta que el estado sea 'Authenticated'
            cloudAuthProvider.authState.collect { authState ->
                if (authState is AuthState.Authenticated && !isFirstSyncDone) {
                    println("SyncViewModel: Usuario autenticado. Iniciando primera sincronización automática.")
                    isFirstSyncDone = true
                    syncEngine.iniciarSincronizacion()
                }
            }
        }
    }

    /**
     * Inicia sincronización manual (ej. con un botón de 'refrescar')
     */
    fun iniciarSincronizacionManual() {
        viewModelScope.launch {
            // Asegurarse de que el usuario está autenticado antes de intentar una sincronización manual
            if (cloudAuthProvider.authState.value is AuthState.Authenticated) {
                println("SyncViewModel: Iniciando sincronización manual.")
                try {
                    syncEngine.iniciarSincronizacion()
                } catch (e: Exception) {
                    println("SyncViewModel: Error en sincronización manual - ${e.message}")
                }
            } else {
                println("SyncViewModel: No se puede iniciar sincronización manual. Usuario no autenticado.")
            }
        }
    }
    
    /**
     * TESTING: Borra TODOS los datos remotos en Google Drive (force delete)
     * TODO: Remover después del testing
     */
    fun forceDeleteRemoteData() {
        viewModelScope.launch {
            try {
                println("SyncViewModel: 🚨 INICIANDO BORRADO FORZADO DE DATOS REMOTOS...")
                val result = syncEngine.forceDeleteAllRemoteFiles()
                if (result.isSuccess) {
                    println("SyncViewModel: 🚨 ✅ Datos remotos eliminados exitosamente")
                } else {
                    println("SyncViewModel: 🚨 ❌ Error eliminando datos remotos: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                println("SyncViewModel: 🚨 ❌ Error en borrado forzado: ${e.message}")
            }
        }
    }
    
    /**
     * TESTING: Borra TODOS los datos locales (notas y attachments)
     * TODO: Remover después del testing
     */
    fun forceDeleteLocalData() {
        viewModelScope.launch {
            try {
                println("SyncViewModel: 🚨🚨 INICIANDO BORRADO DE DATOS LOCALES...")
                
                // Obtener usuario actual
                val authState = cloudAuthProvider.authState.value
                if (authState is AuthState.Authenticated) {
                    val userId = authState.user.email
                    
                    // Borrar todas las notas del usuario (esto también borra attachments por CASCADE)
                    notesRepository.deleteAllNotesForUser(userId)
                    
                    println("SyncViewModel: 🚨🚨 ✅ Todas las notas y attachments locales eliminados")
                } else {
                    println("SyncViewModel: 🚨🚨 ❌ Usuario no autenticado, no se pueden borrar datos locales")
                }
            } catch (e: Exception) {
                println("SyncViewModel: 🚨🚨 ❌ Error borrando datos locales: ${e.message}")
            }
        }
    }
    
    /**
     * TESTING: Reset completo - Borra TODOS los datos remotos y locales
     * TODO: Remover después del testing
     */
    fun forceCompleteReset() {
        viewModelScope.launch {
            try {
                println("SyncViewModel: 🚨🚨🚨 INICIANDO RESET COMPLETO...")
                
                // Paso 1: Borrar datos remotos
                println("SyncViewModel: Paso 1/2 - Borrando datos remotos...")
                val remoteResult = syncEngine.forceDeleteAllRemoteFiles()
                
                if (remoteResult.isSuccess) {
                    println("SyncViewModel: ✅ Datos remotos eliminados")
                } else {
                    println("SyncViewModel: ⚠️ Error eliminando datos remotos: ${remoteResult.exceptionOrNull()?.message}")
                }
                
                // Paso 2: Borrar datos locales
                println("SyncViewModel: Paso 2/2 - Borrando datos locales...")
                val authState = cloudAuthProvider.authState.value
                if (authState is AuthState.Authenticated) {
                    val userId = authState.user.email
                    notesRepository.deleteAllNotesForUser(userId)
                    println("SyncViewModel: ✅ Datos locales eliminados")
                } else {
                    println("SyncViewModel: ⚠️ Usuario no autenticado para borrar datos locales")
                }
                
                println("SyncViewModel: 🚨🚨🚨 ✅ RESET COMPLETO FINALIZADO")
                
            } catch (e: Exception) {
                println("SyncViewModel: 🚨🚨🚨 ❌ Error en reset completo: ${e.message}")
            }
        }
    }
}
