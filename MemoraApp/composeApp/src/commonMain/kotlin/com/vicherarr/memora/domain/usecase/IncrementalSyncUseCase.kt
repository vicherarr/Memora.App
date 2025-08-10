package com.vicherarr.memora.domain.usecase

import com.vicherarr.memora.data.database.getCurrentTimestamp
import com.vicherarr.memora.domain.models.SyncComparisonResult
import com.vicherarr.memora.domain.repository.SyncMetadataRepository
import com.vicherarr.memora.sync.SyncEngine
import com.vicherarr.memora.sync.SyncResult

/**
 * Use Case para sincronización incremental inteligente
 * Clean Architecture - Domain Layer
 * 
 * Responsabilidades:
 * - Decidir si es necesario hacer sync completo o no
 * - Coordinar entre metadata y sync engine
 * - Implementar la lógica de negocio de sincronización incremental
 * - Mantener metadata actualizado tras operaciones de sync
 */
class IncrementalSyncUseCase(
    private val syncMetadataRepository: SyncMetadataRepository,
    private val syncEngine: SyncEngine // SyncEngine existente para sync completo
) {
    
    /**
     * Ejecuta sincronización inteligente para un usuario
     * 
     * Flujo:
     * 1. Comparar fingerprints local vs remoto
     * 2. Si coinciden → Skip sync (súper rápido)
     * 3. Si no coinciden → Sync completo + actualizar metadata
     * 
     * @param userId ID del usuario a sincronizar
     * @return IncrementalSyncResult con detalles del proceso
     */
    suspend fun execute(userId: String): Result<IncrementalSyncResult> {
        return try {
            println("IncrementalSyncUseCase: 🚀 ====== INICIANDO SYNC INTELIGENTE ======")
            println("IncrementalSyncUseCase: Usuario: $userId")
            val startTime = getCurrentTimestamp()
            
            // PASO 1: Comparar metadata local vs remoto
            println("IncrementalSyncUseCase: 🔍 Comparando fingerprints...")
            val comparisonResult = syncMetadataRepository.compareSyncMetadata(userId)
            
            if (comparisonResult.isFailure) {
                val error = "Error comparando metadata: ${comparisonResult.exceptionOrNull()?.message}"
                println("IncrementalSyncUseCase: ❌ $error")
                return Result.failure(Exception(error))
            }
            
            val comparison = comparisonResult.getOrNull()!!
            
            // PASO 2: Decidir acción basado en comparación
            val syncResult = when (comparison) {
                is SyncComparisonResult.InSync -> {
                    // 🎉 CASO OPTIMIZADO: No hay cambios
                    println("IncrementalSyncUseCase: ✅ Datos ya sincronizados - SKIP SYNC")
                    val elapsedTime = getCurrentTimestamp() - startTime
                    IncrementalSyncResult.AlreadyInSync(
                        customMessage = "Datos ya actualizados - sincronización omitida",
                        elapsedTime = elapsedTime
                    )
                }
                
                is SyncComparisonResult.OutOfSync -> {
                    // 🔄 Hay cambios - ejecutar sync completo
                    println("IncrementalSyncUseCase: 🔄 Datos no sincronizados - EJECUTANDO SYNC COMPLETO")
                    executeFullSyncAndUpdateMetadata(userId, startTime)
                }
                
                is SyncComparisonResult.NoRemoteMetadata -> {
                    // 📤 Primera sync o metadata remoto perdido
                    println("IncrementalSyncUseCase: 📤 No hay metadata remoto - PRIMERA SINCRONIZACIÓN")
                    executeFullSyncAndUpdateMetadata(userId, startTime, isFirstSync = true)
                }
                
                is SyncComparisonResult.NoLocalMetadata -> {
                    // 📥 Primera vez en este dispositivo
                    println("IncrementalSyncUseCase: 📥 No hay metadata local - SINCRONIZACIÓN INICIAL")
                    executeFullSyncAndUpdateMetadata(userId, startTime, isFirstSync = true)
                }
                
                is SyncComparisonResult.VersionMismatch -> {
                    // ⚠️ Versiones incompatibles - forzar sync completo
                    println("IncrementalSyncUseCase: ⚠️ Versiones incompatibles - FORZANDO SYNC")
                    println("IncrementalSyncUseCase: Local v${comparison.localVersion}, Remoto v${comparison.remoteVersion}")
                    executeFullSyncAndUpdateMetadata(userId, startTime, isVersionMismatch = true)
                }
            }
            
            val totalElapsedTime = getCurrentTimestamp() - startTime
            println("IncrementalSyncUseCase: ✅ ====== SYNC INTELIGENTE COMPLETADO ======")
            println("IncrementalSyncUseCase: Tiempo total: ${totalElapsedTime}ms")
            println("IncrementalSyncUseCase: Resultado: $syncResult")
            
            Result.success(syncResult)
            
        } catch (e: Exception) {
            println("IncrementalSyncUseCase: ❌ Error en sync inteligente: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Ejecuta sync completo y actualiza metadata local/remoto
     */
    private suspend fun executeFullSyncAndUpdateMetadata(
        userId: String, 
        startTime: Long,
        isFirstSync: Boolean = false,
        isVersionMismatch: Boolean = false
    ): IncrementalSyncResult {
        
        return try {
            println("IncrementalSyncUseCase: 🔄 Ejecutando sync completo...")
            
            // PASO 1: Ejecutar sync completo usando SyncEngine existente
            syncEngine.iniciarSincronizacion() // SyncEngine actual no retorna Result
            
            // TODO: Mejorar cuando SyncEngine devuelva información detallada
            println("IncrementalSyncUseCase: ✅ Sync completo exitoso")
            
            // PASO 2: Generar y actualizar metadata basado en nuevo estado
            println("IncrementalSyncUseCase: 📝 Actualizando metadata tras sync...")
            updateMetadataAfterSync(userId)
            
            val elapsedTime = getCurrentTimestamp() - startTime
            
            // PASO 3: Determinar tipo de resultado
            when {
                isFirstSync -> IncrementalSyncResult.FirstSyncCompleted(
                    syncedNotesCount = 0, // TODO: Obtener de SyncEngine cuando esté disponible
                    syncedAttachmentsCount = 0, // TODO: Agregar cuando esté disponible
                    elapsedTime = elapsedTime,
                    customMessage = "Primera sincronización completada exitosamente"
                )
                
                isVersionMismatch -> IncrementalSyncResult.VersionMismatchResolved(
                    syncedNotesCount = 0, // TODO: Obtener de SyncEngine cuando esté disponible
                    elapsedTime = elapsedTime,
                    customMessage = "Conflicto de versiones resuelto con sync completo"
                )
                
                else -> IncrementalSyncResult.FullSyncCompleted(
                    syncedNotesCount = 0, // TODO: Obtener de SyncEngine cuando esté disponible
                    syncedAttachmentsCount = 0, // TODO: Agregar cuando esté disponible
                    elapsedTime = elapsedTime,
                    customMessage = "Sincronización completa ejecutada - datos actualizados"
                )
            }
            
        } catch (e: Exception) {
            val error = "Error ejecutando sync completo: ${e.message}"
            println("IncrementalSyncUseCase: ❌ $error")
            IncrementalSyncResult.SyncFailed(error, getCurrentTimestamp() - startTime)
        }
    }
    
    /**
     * Actualiza metadata local y remoto después de un sync exitoso
     */
    private suspend fun updateMetadataAfterSync(userId: String) {
        try {
            println("IncrementalSyncUseCase: 📊 Generando metadata actualizado...")
            
            // Generar metadata basado en estado actual
            val currentMetadataResult = syncMetadataRepository.generateCurrentSyncMetadata(userId)
            if (currentMetadataResult.isFailure) {
                println("IncrementalSyncUseCase: ⚠️ Error generando metadata: ${currentMetadataResult.exceptionOrNull()?.message}")
                return
            }
            
            val currentMetadata = currentMetadataResult.getOrNull()!!
            
            // Actualizar metadata local
            val localSaveResult = syncMetadataRepository.saveLocalSyncMetadata(currentMetadata)
            if (localSaveResult.isFailure) {
                println("IncrementalSyncUseCase: ⚠️ Error guardando metadata local: ${localSaveResult.exceptionOrNull()?.message}")
            } else {
                println("IncrementalSyncUseCase: ✅ Metadata local actualizado")
            }
            
            // Actualizar metadata remoto
            val remoteSaveResult = syncMetadataRepository.saveRemoteSyncMetadata(currentMetadata)
            if (remoteSaveResult.isFailure) {
                println("IncrementalSyncUseCase: ⚠️ Error guardando metadata remoto: ${remoteSaveResult.exceptionOrNull()?.message}")
            } else {
                println("IncrementalSyncUseCase: ✅ Metadata remoto actualizado")
            }
            
        } catch (e: Exception) {
            println("IncrementalSyncUseCase: ⚠️ Error actualizando metadata: ${e.message}")
        }
    }
}

/**
 * Resultado detallado de sincronización incremental
 */
sealed class IncrementalSyncResult(
    val elapsedTimeMs: Long,
    val message: String
) {
    /**
     * Datos ya estaban sincronizados - se omitió el sync (CASO OPTIMIZADO)
     */
    data class AlreadyInSync(
        val customMessage: String,
        val elapsedTime: Long
    ) : IncrementalSyncResult(elapsedTime, customMessage)
    
    /**
     * Se ejecutó sync completo porque había cambios
     */
    data class FullSyncCompleted(
        val syncedNotesCount: Int,
        val syncedAttachmentsCount: Int,
        val elapsedTime: Long,
        val customMessage: String
    ) : IncrementalSyncResult(elapsedTime, customMessage)
    
    /**
     * Primera sincronización en este dispositivo/usuario
     */
    data class FirstSyncCompleted(
        val syncedNotesCount: Int,
        val syncedAttachmentsCount: Int,
        val elapsedTime: Long,
        val customMessage: String
    ) : IncrementalSyncResult(elapsedTime, customMessage)
    
    /**
     * Se resolvió conflicto de versiones con sync completo
     */
    data class VersionMismatchResolved(
        val syncedNotesCount: Int,
        val elapsedTime: Long,
        val customMessage: String
    ) : IncrementalSyncResult(elapsedTime, customMessage)
    
    /**
     * Error durante el proceso de sincronización
     */
    data class SyncFailed(
        val error: String,
        val elapsedTime: Long
    ) : IncrementalSyncResult(elapsedTime, "Error: $error")
}