package com.vicherarr.memora.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vicherarr.memora.data.auth.CloudAuthProvider
import com.vicherarr.memora.domain.model.AuthState
import com.vicherarr.memora.domain.repository.NotesRepository
import com.vicherarr.memora.sync.AttachmentSyncEngine
import com.vicherarr.memora.sync.AttachmentSyncState
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
    private val attachmentSyncEngine: AttachmentSyncEngine,
    private val cloudAuthProvider: CloudAuthProvider,
    private val notesRepository: NotesRepository,
    // NUEVO: IncrementalSyncUseCase para Smart Sync
    private val incrementalSyncUseCase: com.vicherarr.memora.domain.usecase.IncrementalSyncUseCase
) : ViewModel() {

    val syncState: StateFlow<SyncState> = syncEngine.syncState
    val attachmentSyncState: StateFlow<AttachmentSyncState> = attachmentSyncEngine.syncState
    private var isFirstSyncDone = false

    init {
        // 🧠 SMART AUTO-SYNC - Enabled with intelligent sync
        viewModelScope.launch {
            // Espera hasta que el estado sea 'Authenticated'
            cloudAuthProvider.authState.collect { authState ->
                if (authState is AuthState.Authenticated && !isFirstSyncDone) {
                    println("SyncViewModel: 🧠 Usuario autenticado. Iniciando primera Smart Sync automática.")
                    isFirstSyncDone = true
                    // Use Smart Sync for better performance
                    iniciarSmartSync()
                }
            }
        }
        println("SyncViewModel: 🧠 SMART AUTO-SYNC ENABLED - Intelligent sync activated")
    }

    /**
     * Inicia sincronización manual completa (notas + attachments)
     * Incluye refresh explícito del repositorio
     */
    fun iniciarSincronizacionManual() {
        viewModelScope.launch {
            // Asegurarse de que el usuario está autenticado antes de intentar una sincronización manual
            val authState = cloudAuthProvider.authState.value
            if (authState is AuthState.Authenticated) {
                println("SyncViewModel: Iniciando sincronización manual completa.")
                try {
                    // Sincronizar notas primero
                    syncEngine.iniciarSincronizacion()
                    
                    // Sincronizar attachments (incluye descarga de imágenes)
                    syncAttachments(authState.user.email)
                    
                    // Los flows reactivos del repositorio se actualizarán automáticamente
                    println("SyncViewModel: Sincronización completa finalizada")
                    
                } catch (e: Exception) {
                    println("SyncViewModel: Error en sincronización manual - ${e.message}")
                }
            } else {
                println("SyncViewModel: No se puede iniciar sincronización manual. Usuario no autenticado.")
            }
        }
    }
    
    /**
     * Sincronizar attachments para un usuario
     */
    private suspend fun syncAttachments(userId: String) {
        try {
            println("SyncViewModel: 📎 Iniciando sincronización de attachments para $userId")
            val result = attachmentSyncEngine.startFullSync(userId)
            
            if (result.isSuccess) {
                val syncResult = result.getOrNull()
                println("SyncViewModel: 📎 ✅ Attachments sincronizados: ${syncResult?.uploadedCount} subidos, ${syncResult?.downloadedCount} descargados")
            } else {
                println("SyncViewModel: 📎 ❌ Error sincronizando attachments: ${result.exceptionOrNull()?.message}")
            }
        } catch (e: Exception) {
            println("SyncViewModel: 📎 ❌ Error en sync de attachments: ${e.message}")
        }
    }
    
    /**
     * Sincronizar un attachment específico (on-demand)
     */
    fun syncSingleAttachment(attachmentId: String) {
        viewModelScope.launch {
            try {
                println("SyncViewModel: 📎 Sincronizando attachment individual: $attachmentId")
                val result = attachmentSyncEngine.syncSingleAttachment(attachmentId)
                
                if (result.isSuccess) {
                    println("SyncViewModel: 📎 ✅ Attachment sincronizado exitosamente")
                } else {
                    println("SyncViewModel: 📎 ❌ Error sincronizando attachment: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                println("SyncViewModel: 📎 ❌ Error en sync individual: ${e.message}")
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
    
    /**
     * 🧠 NUEVO: Sincronización inteligente usando metadata
     * 
     * Usa IncrementalSyncUseCase para decidir si hacer sync o no:
     * - Si los datos coinciden → Skip sync (súper rápido ⚡)
     * - Si hay cambios → Sync completo + actualizar metadata
     * - Con manejo robusto de errores de inicialización
     */
    fun iniciarSmartSync() {
        viewModelScope.launch {
            val authState = cloudAuthProvider.authState.value
            if (authState is AuthState.Authenticated) {
                println("SyncViewModel: 🧠 Iniciando SMART SYNC...")
                try {
                    // Usar IncrementalSyncUseCase directamente (arquitectura correcta)
                    val result = incrementalSyncUseCase.execute(authState.user.email)
                    
                    if (result.isSuccess) {
                        val syncResult = result.getOrNull()!!
                        println("SyncViewModel: 🧠 ✅ Smart sync completado: $syncResult")
                        
                        // Si se hizo sync completo, también sincronizar attachments
                        when (syncResult) {
                            is com.vicherarr.memora.domain.usecase.IncrementalSyncResult.FullSyncCompleted,
                            is com.vicherarr.memora.domain.usecase.IncrementalSyncResult.FirstSyncCompleted,
                            is com.vicherarr.memora.domain.usecase.IncrementalSyncResult.VersionMismatchResolved -> {
                                println("SyncViewModel: 🧠 Sync completo ejecutado - sincronizando attachments...")
                                syncAttachments(authState.user.email)
                            }
                            is com.vicherarr.memora.domain.usecase.IncrementalSyncResult.AlreadyInSync -> {
                                println("SyncViewModel: 🧠 ⚡ Datos ya sincronizados - attachments también están actualizados")
                            }
                            is com.vicherarr.memora.domain.usecase.IncrementalSyncResult.SyncFailed -> {
                                println("SyncViewModel: 🧠 ❌ Smart sync falló: ${syncResult.error}")
                            }
                        }
                    } else {
                        val error = result.exceptionOrNull()?.message ?: "Error desconocido"
                        println("SyncViewModel: 🧠 ❌ Error en smart sync: $error")
                        
                        // Si es error de inicialización, reintentar después de delay
                        if (error.contains("not initialized", ignoreCase = true)) {
                            println("SyncViewModel: 🧠 🔄 Google Drive no inicializado, reintentando en 3s...")
                            kotlinx.coroutines.delay(3000)
                            iniciarSmartSyncRetry()
                        }
                    }
                } catch (e: Exception) {
                    println("SyncViewModel: 🧠 ❌ Error ejecutando smart sync: ${e.message}")
                    
                    // Si es error de inicialización, reintentar después de delay
                    if (e.message?.contains("not initialized", ignoreCase = true) == true) {
                        println("SyncViewModel: 🧠 🔄 Google Drive no inicializado, reintentando en 3s...")
                        kotlinx.coroutines.delay(3000)
                        iniciarSmartSyncRetry()
                    }
                }
            } else {
                println("SyncViewModel: 🧠 No se puede hacer smart sync. Usuario no autenticado.")
            }
        }
    }
    
    /**
     * Reintentar Smart Sync con lógica de fallback a sync tradicional
     */
    private fun iniciarSmartSyncRetry() {
        viewModelScope.launch {
            val authState = cloudAuthProvider.authState.value
            if (authState is AuthState.Authenticated) {
                println("SyncViewModel: 🧠 🔄 REINTENTANDO SMART SYNC...")
                try {
                    val result = incrementalSyncUseCase.execute(authState.user.email)
                    
                    if (result.isSuccess) {
                        val syncResult = result.getOrNull()!!
                        println("SyncViewModel: 🧠 ✅ Smart sync retry exitoso: $syncResult")
                        
                        when (syncResult) {
                            is com.vicherarr.memora.domain.usecase.IncrementalSyncResult.FullSyncCompleted,
                            is com.vicherarr.memora.domain.usecase.IncrementalSyncResult.FirstSyncCompleted,
                            is com.vicherarr.memora.domain.usecase.IncrementalSyncResult.VersionMismatchResolved -> {
                                syncAttachments(authState.user.email)
                            }
                            else -> {
                                println("SyncViewModel: 🧠 Smart sync retry completado")
                            }
                        }
                    } else {
                        // Si falla de nuevo, usar sync tradicional como fallback
                        println("SyncViewModel: 🧠 ⚠️ Smart sync falló después de retry - usando sync tradicional como fallback")
                        iniciarSincronizacionManual()
                    }
                } catch (e: Exception) {
                    // Si falla de nuevo, usar sync tradicional como fallback
                    println("SyncViewModel: 🧠 ⚠️ Smart sync retry falló - usando sync tradicional: ${e.message}")
                    iniciarSincronizacionManual()
                }
            }
        }
    }
}
