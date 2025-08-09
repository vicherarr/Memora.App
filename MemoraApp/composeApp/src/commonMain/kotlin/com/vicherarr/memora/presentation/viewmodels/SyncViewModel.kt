package com.vicherarr.memora.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vicherarr.memora.data.auth.CloudAuthProvider
import com.vicherarr.memora.domain.model.AuthState
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
    private val cloudAuthProvider: CloudAuthProvider
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
}
