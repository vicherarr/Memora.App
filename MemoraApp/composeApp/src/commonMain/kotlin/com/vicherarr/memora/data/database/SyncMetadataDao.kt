package com.vicherarr.memora.data.database

import com.vicherarr.memora.database.MemoraDatabase
import com.vicherarr.memora.database.Sync_metadata
import com.vicherarr.memora.domain.models.SyncMetadata

/**
 * Data Access Object for sync_metadata table
 * Data Layer - Clean Architecture
 * 
 * Responsabilidades:
 * - CRUD operations para metadatos de sincronización
 * - Mapeo entre SQLDelight models y domain models
 * - Abstracción de la base de datos para sync metadata
 */
class SyncMetadataDao(private val database: MemoraDatabase) {
    
    private val queries = database.syncMetadataQueries
    
    /**
     * Obtiene metadatos de sync para un usuario específico
     */
    suspend fun getSyncMetadataByUserId(userId: String): SyncMetadata? {
        return queries.getSyncMetadataByUserId(userId).executeAsOneOrNull()?.toDomainModel()
    }
    
    /**
     * Inserta nuevos metadatos de sync
     */
    suspend fun insertSyncMetadata(syncMetadata: SyncMetadata) {
        queries.insertSyncMetadata(
            user_id = syncMetadata.userId,
            last_sync_timestamp = syncMetadata.lastSyncTimestamp,
            notes_count = syncMetadata.notesCount.toLong(),
            attachments_count = syncMetadata.attachmentsCount.toLong(),
            content_fingerprint = syncMetadata.contentFingerprint,
            remote_fingerprint = syncMetadata.remoteFingerprint,
            sync_version = syncMetadata.syncVersion.toLong(),
            created_at = syncMetadata.createdAt,
            updated_at = syncMetadata.updatedAt
        )
    }
    
    /**
     * Actualiza metadatos de sync existentes
     */
    suspend fun updateSyncMetadata(syncMetadata: SyncMetadata) {
        queries.updateSyncMetadata(
            last_sync_timestamp = syncMetadata.lastSyncTimestamp,
            notes_count = syncMetadata.notesCount.toLong(),
            attachments_count = syncMetadata.attachmentsCount.toLong(),
            content_fingerprint = syncMetadata.contentFingerprint,
            remote_fingerprint = syncMetadata.remoteFingerprint,
            updated_at = syncMetadata.updatedAt,
            user_id = syncMetadata.userId
        )
    }
    
    /**
     * Actualiza solo el fingerprint remoto (tras guardar en Google Drive)
     */
    suspend fun updateRemoteFingerprint(userId: String, remoteFingerprint: String, updatedAt: Long) {
        queries.updateRemoteFingerprint(
            remote_fingerprint = remoteFingerprint,
            updated_at = updatedAt,
            user_id = userId
        )
    }
    
    /**
     * Actualiza solo los datos locales (tras cambios locales)
     */
    suspend fun updateLocalData(
        userId: String,
        lastSyncTimestamp: Long,
        notesCount: Int,
        attachmentsCount: Int,
        contentFingerprint: String,
        updatedAt: Long
    ) {
        queries.updateLocalData(
            last_sync_timestamp = lastSyncTimestamp,
            notes_count = notesCount.toLong(),
            attachments_count = attachmentsCount.toLong(),
            content_fingerprint = contentFingerprint,
            updated_at = updatedAt,
            user_id = userId
        )
    }
    
    /**
     * Elimina metadatos de sync para un usuario
     */
    suspend fun deleteSyncMetadata(userId: String) {
        queries.deleteSyncMetadata(userId)
    }
    
    /**
     * Verifica si existen metadatos de sync para un usuario
     */
    suspend fun syncMetadataExists(userId: String): Boolean {
        return queries.syncMetadataExists(userId).executeAsOne() > 0
    }
    
    /**
     * Obtiene todos los metadatos de sync (para debugging)
     */
    suspend fun getAllSyncMetadata(): List<SyncMetadata> {
        return queries.getAllSyncMetadata().executeAsList().map { it.toDomainModel() }
    }
    
    /**
     * Obtiene usuarios con sync desactualizado (para mantenimiento)
     */
    suspend fun getOutdatedSyncUsers(thresholdTimestamp: Long): List<String> {
        return queries.getOutdatedSyncUsers(thresholdTimestamp).executeAsList().map { it.user_id }
    }
    
    /**
     * Inserta o actualiza metadatos de sync (upsert operation)
     */
    suspend fun upsertSyncMetadata(syncMetadata: SyncMetadata) {
        if (syncMetadataExists(syncMetadata.userId)) {
            updateSyncMetadata(syncMetadata)
        } else {
            insertSyncMetadata(syncMetadata)
        }
    }
}

/**
 * Extension function para mapear SQLDelight model a Domain model
 */
private fun Sync_metadata.toDomainModel(): SyncMetadata {
    return SyncMetadata(
        userId = user_id,
        lastSyncTimestamp = last_sync_timestamp,
        notesCount = notes_count.toInt(),
        attachmentsCount = attachments_count.toInt(),
        contentFingerprint = content_fingerprint,
        remoteFingerprint = remote_fingerprint,
        syncVersion = sync_version.toInt(),
        createdAt = created_at,
        updatedAt = updated_at
    )
}