package com.vicherarr.memora.data.database

import com.vicherarr.memora.database.MemoraDatabase
import com.vicherarr.memora.database.Deletions

/**
 * Data Access Object for Deletions table (Tombstones)
 * Tracks deleted records for sync purposes without modifying original tables
 */
class DeletionsDao(private val database: MemoraDatabase) {
    
    private val queries = database.deletionsQueries
    
    /**
     * Obtener todas las eliminaciones de un usuario
     */
    suspend fun getDeletionsByUserId(userId: String): List<Deletions> {
        return queries.getDeletionsByUserId(userId).executeAsList()
    }
    
    /**
     * Obtener eliminaciones que necesitan sincronización
     */
    suspend fun getDeletionsNeedingSync(): List<Deletions> {
        return queries.getDeletionsNeedingSync().executeAsList()
    }
    
    /**
     * Insertar nuevo tombstone (registro de eliminación)
     */
    fun insertDeletion(
        tableName: String,
        recordId: String,
        userId: String
    ) {
        val tombstoneId = "tombstone_${getCurrentTimestamp()}_${recordId.hashCode()}"
        val now = getCurrentTimestamp()
        
        queries.insertDeletion(
            id = tombstoneId,
            table_name = tableName,
            record_id = recordId,
            usuario_id = userId,
            deleted_at = now,
            sync_status = "PENDING",
            needs_upload = 1
        )
        
        println("DeletionsDao: 🪦 Tombstone creado: $tableName/$recordId")
    }
    
    /**
     * Marcar tombstone como sincronizado
     */
    fun markDeletionAsSynced(deletionId: String) {
        queries.markDeletionAsSynced(deletionId)
        println("DeletionsDao: ✅ Tombstone marcado como sincronizado: $deletionId")
    }
    
    /**
     * Marcar tombstone como fallido en sync
     */
    fun markDeletionSyncFailed(deletionId: String) {
        queries.markDeletionSyncFailed(deletionId)
        println("DeletionsDao: ❌ Tombstone marcado como fallido: $deletionId")
    }
    
    /**
     * Verificar si un registro está marcado como eliminado
     * Útil para evitar re-crear elementos eliminados durante sync
     */
    suspend fun isRecordDeleted(tableName: String, recordId: String, userId: String): Boolean {
        val deletions = queries.getDeletionsByUserId(userId).executeAsList()
        return deletions.any { 
            it.table_name == tableName && 
            it.record_id == recordId 
        }
    }
    
    /**
     * Limpieza: eliminar tombstones antiguos ya sincronizados
     * Para evitar que la tabla crezca indefinidamente
     */
    suspend fun cleanOldSyncedDeletions(olderThanTimestamp: Long) {
        queries.deleteOldSyncedDeletions(olderThanTimestamp)
        println("DeletionsDao: 🧹 Tombstones antiguos limpiados")
    }
}