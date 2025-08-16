package com.vicherarr.memora.sync

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.vicherarr.memora.data.database.getCurrentTimestamp

/**
 * Estados posibles del proceso de sincronización
 */
sealed class SyncState {
    object Idle : SyncState()
    object Syncing : SyncState()
    data class Success(val message: String) : SyncState()
    data class Error(val error: String) : SyncState()
}

/**
 * Motor de sincronización principal.
 * Orquesta todo el proceso de sincronización entre la base de datos local
 * y el almacenamiento en la nube (Google Drive/iCloud).
 */
class SyncEngine(
    private val cloudProvider: CloudStorageProvider,
    private val databaseMerger: DatabaseMerger,
    private val databaseSyncService: DatabaseSyncService,
    private val cloudAuthProvider: com.vicherarr.memora.data.auth.CloudAuthProvider
) {
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()
    
    /**
     * Inicia el proceso completo de sincronización
     */
    suspend fun iniciarSincronizacion() {
        // OBTENER USUARIO REAL
        val currentUserId = getCurrentUserId()
        println("SyncEngine: ====== INICIANDO SINCRONIZACIÓN ======")
        println("SyncEngine: Usuario actual: $currentUserId")
        try {
            _syncState.value = SyncState.Syncing
            
            // PASO 1: Autenticar con el proveedor de nube
            println("SyncEngine: Autenticando con proveedor cloud...")
            cloudProvider.autenticar()
            
            // PASO 2: Obtener metadatos remotos para comparar
            println("SyncEngine: Obteniendo metadatos remotos...")
            val timestampRemoto = cloudProvider.obtenerMetadatosRemotos()
            
            // PASO 3: Descargar DB remota si existe y es más nueva
            val dbRemota = if (timestampRemoto != null) {
                println("SyncEngine: Descargando DB remota (timestamp: $timestampRemoto)...")
                cloudProvider.descargarDB()
            } else {
                println("SyncEngine: No hay DB remota disponible")
                null
            }
            
            // PASO 4: Fusionar cambios usando DatabaseMerger con datos REALES
            if (dbRemota != null) {
                println("SyncEngine: DB remota encontrada (${dbRemota.size} bytes)")
                println("SyncEngine: Iniciando fusión con datos REALES...")
                
                // ✅ NUEVO: Deserializar datos remotos CON tombstones y categorías (Fase 6)
                val remoteDatabaseData = databaseSyncService.deserializeRemoteDatabaseWithDeletions(dbRemota)
                val remoteNotes = remoteDatabaseData.notes
                val remoteCategories = remoteDatabaseData.categories
                val remoteNoteCategories = remoteDatabaseData.noteCategories
                val remoteDeletions = remoteDatabaseData.deletions
                
                println("SyncEngine: ${remoteNotes.size} notas deserializadas de la DB remota")
                println("SyncEngine: ${remoteCategories.size} categorías deserializadas de la DB remota") // Fase 6
                println("SyncEngine: ${remoteNoteCategories.size} relaciones nota-categoría deserializadas de la DB remota") // Fase 6
                println("SyncEngine: ${remoteDeletions.size} tombstones remotos encontrados")
                
                // Obtener notas locales REALES
                val localNotes = databaseSyncService.getLocalNotesForMerging(currentUserId)
                println("SyncEngine: ${localNotes.size} notas locales obtenidas")
                
                // Fusionar con DatabaseMerger
                val mergeResult = databaseMerger.mergeNotes(
                    localNotes = localNotes,
                    remoteNotes = remoteNotes,
                    strategy = ConflictResolutionStrategy.KEEP_NEWER,
                    userId = currentUserId
                )
                
                println("SyncEngine: Fusión completada - ${mergeResult.notasInsertadas} insertadas, " +
                       "${mergeResult.notasActualizadas} actualizadas, ${mergeResult.notasEliminadas} eliminadas, " +
                       "${mergeResult.conflictosResueltos} conflictos resueltos")
                       
                // Aplicar notas fusionadas a la DB local
                databaseSyncService.applyMergedNotes(mergeResult.mergedNotes, currentUserId)
                println("SyncEngine: Notas fusionadas aplicadas a la base de datos local")
                
                // ✅ FASE 6: Aplicar categorías remotas
                if (remoteCategories.isNotEmpty() || remoteNoteCategories.isNotEmpty()) {
                    databaseSyncService.applyRemoteCategories(remoteCategories, remoteNoteCategories, currentUserId)
                    println("SyncEngine: Categorías remotas aplicadas - ${remoteCategories.size} categorías, ${remoteNoteCategories.size} relaciones")
                }
                
                // ✅ NUEVO: Aplicar tombstones remotos (eliminar notas borradas en otros dispositivos)
                if (remoteDeletions.isNotEmpty()) {
                    databaseSyncService.applyRemoteDeletions(remoteDeletions, currentUserId)
                    println("SyncEngine: Tombstones remotos aplicados - ${remoteDeletions.size} eliminaciones procesadas")
                }
                       
                if (mergeResult.conflicts.isNotEmpty()) {
                    println("SyncEngine: Se encontraron ${mergeResult.conflicts.size} conflictos que fueron resueltos automáticamente")
                }
            } else {
                println("SyncEngine: No hay DB remota - primera sincronización")
            }
            
            // PASO 5: Subir DB local actualizada con datos REALES
            println("SyncEngine: Subiendo DB local actualizada...")
            println("SyncEngine: Serializando datos REALES de SQLDelight...")
            val dbLocal = databaseSyncService.serializeLocalDatabase(currentUserId)
            
            println("SyncEngine: Subiendo ${dbLocal.size} bytes a la nube...")
            cloudProvider.subirDB(dbLocal)
            
            // ✅ NUEVO: Marcar tombstones locales como sincronizados
            databaseSyncService.markLocalDeletionsAsSynced(currentUserId)
            
            _syncState.value = SyncState.Success("Sincronización completada exitosamente")
            println("SyncEngine: Sincronización completada exitosamente")
            
        } catch (e: Exception) {
            val errorMessage = "Error en sincronización: ${e.message}"
            _syncState.value = SyncState.Error(errorMessage)
            println("SyncEngine: $errorMessage")
        }
    }
    
    /**
     * Obtiene el ID del usuario actual autenticado desde CloudAuthProvider
     */
    private fun getCurrentUserId(): String {
        val authState = cloudAuthProvider.authState.value
        println("SyncEngine: 🔍 DEBUGGING getCurrentUserId():")
        println("SyncEngine:   - AuthState type: ${authState::class.simpleName}")
        println("SyncEngine:   - AuthState value: $authState")
        
        return when (authState) {
            is com.vicherarr.memora.domain.model.AuthState.Authenticated -> {
                val email = authState.user.email
                println("SyncEngine: ✅ Usuario autenticado obtenido: $email")
                email
            }
            else -> {
                println("SyncEngine: ❌ Usuario NO autenticado! La sincronización no puede continuar.")
                throw IllegalStateException("Error Crítico: Intento de sincronización sin un usuario autenticado.")
            }
        }
    }
    
    /**
     * Obtiene la base de datos local como ByteArray (FALLBACK)
     * TODO: Eliminar cuando DatabaseSyncService esté completamente integrado
     */
    private fun obtenerDBLocal(): ByteArray {
        // Por ahora, simulamos datos locales
        val mockLocalData = "mock_local_db_content_${getCurrentTimestamp()}"
        return mockLocalData.encodeToByteArray()
    }
    
    /**
     * TEMPORARY: Force delete remote database for fresh start
     * TODO: Remove this method after testing
     */
    suspend fun forceDeleteRemoteDatabase(): Result<Boolean> {
        return try {
            println("SyncEngine: 🚨 INICIANDO BORRADO FORZADO DE DB REMOTA...")
            
            // Use the method from CloudStorageProvider interface
            val result = cloudProvider.forceDeleteRemoteDatabase()
            if (result.isSuccess) {
                println("SyncEngine: 🚨 ✅ DB remota eliminada exitosamente")
                _syncState.value = SyncState.Success("🚨 DB remota eliminada - listo para empezar desde cero")
            } else {
                println("SyncEngine: 🚨 ❌ Error eliminando DB remota: ${result.exceptionOrNull()?.message}")
                _syncState.value = SyncState.Error("Error eliminando DB: ${result.exceptionOrNull()?.message}")
            }
            result
        } catch (e: Exception) {
            println("SyncEngine: 🚨 ❌ Error en borrado forzado: ${e.message}")
            _syncState.value = SyncState.Error("Error en borrado: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * TEMPORARY: Force delete ALL remote files for nuclear reset
     * TODO: Remove this method after testing
     */
    suspend fun forceDeleteAllRemoteFiles(): Result<Boolean> {
        return try {
            println("SyncEngine: 🚨🚨 INICIANDO BORRADO NUCLEAR DE TODOS LOS ARCHIVOS...")
            
            // Use the method from CloudStorageProvider interface
            val result = cloudProvider.forceDeleteAllRemoteFiles()
            if (result.isSuccess) {
                println("SyncEngine: 🚨🚨 ✅ TODOS los archivos remotos eliminados")
                _syncState.value = SyncState.Success("🚨🚨 BORRADO NUCLEAR completado - AppDataFolder limpio")
            } else {
                println("SyncEngine: 🚨🚨 ❌ Error en borrado nuclear: ${result.exceptionOrNull()?.message}")
                _syncState.value = SyncState.Error("Error borrado nuclear: ${result.exceptionOrNull()?.message}")
            }
            result
        } catch (e: Exception) {
            println("SyncEngine: 🚨🚨 ❌ Error en borrado nuclear: ${e.message}")
            _syncState.value = SyncState.Error("Error borrado nuclear: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Verifica si hay cambios pendientes de sincronizar
     */
    suspend fun hayCambiosPendientes(): Boolean {
        // TODO: Consultar base de datos local para cambios no sincronizados
        return false
    }
    
}

/**
 * Resultado básico de sync completo (para compatibilidad)
 */
data class SyncResult(
    val syncedNotesCount: Int,
    val syncedAttachmentsCount: Int,
    val message: String
)