package com.vicherarr.memora.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vicherarr.memora.sync.SyncEngine
import com.vicherarr.memora.sync.SyncState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para manejar la sincronización cloud
 */
class SyncViewModel(
    private val syncEngine: SyncEngine
) : ViewModel() {
    
    val syncState: StateFlow<SyncState> = syncEngine.syncState
    
    /**
     * Inicia sincronización manual
     */
    fun iniciarSincronizacion() {
        viewModelScope.launch {
            try {
                syncEngine.iniciarSincronizacion()
            } catch (e: Exception) {
                println("SyncViewModel: Error iniciando sincronización - ${e.message}")
            }
        }
    }
}