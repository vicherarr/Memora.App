package com.vicherarr.memora.domain.models

/**
 * Domain model for sync metadata - Clean Architecture
 * Representa la información de metadatos para sincronización incremental
 * 
 * @property userId ID único del usuario
 * @property lastSyncTimestamp Timestamp de última sincronización exitosa
 * @property notesCount Cantidad total de notas del usuario
 * @property attachmentsCount Cantidad total de attachments del usuario
 * @property contentFingerprint Hash SHA256 de metadatos críticos (local)
 * @property remoteFingerprint Hash SHA256 de metadatos remotos (Google Drive)
 * @property syncVersion Versión del esquema de sync para compatibilidad futura
 */
data class SyncMetadata(
    val userId: String,
    val lastSyncTimestamp: Long,
    val notesCount: Int,
    val attachmentsCount: Int,
    val contentFingerprint: String,
    val remoteFingerprint: String? = null,
    val syncVersion: Int = CURRENT_SYNC_VERSION,
    val createdAt: Long,
    val updatedAt: Long
) {
    companion object {
        const val CURRENT_SYNC_VERSION = 1
        
        /**
         * Factory method para crear metadata inicial
         */
        fun createInitial(
            userId: String, 
            notesCount: Int, 
            attachmentsCount: Int, 
            contentFingerprint: String,
            timestamp: Long
        ): SyncMetadata {
            return SyncMetadata(
                userId = userId,
                lastSyncTimestamp = timestamp,
                notesCount = notesCount,
                attachmentsCount = attachmentsCount,
                contentFingerprint = contentFingerprint,
                remoteFingerprint = null,
                syncVersion = CURRENT_SYNC_VERSION,
                createdAt = timestamp,
                updatedAt = timestamp
            )
        }
    }
    
    /**
     * Verifica si los metadatos locales y remotos están sincronizados
     */
    fun isSyncedWithRemote(): Boolean {
        return remoteFingerprint != null && contentFingerprint == remoteFingerprint
    }
    
    /**
     * Crea una copia con fingerprint remoto actualizado
     */
    fun withRemoteFingerprint(remoteFp: String, timestamp: Long): SyncMetadata {
        return copy(
            remoteFingerprint = remoteFp,
            updatedAt = timestamp
        )
    }
    
    /**
     * Crea una copia con datos locales actualizados
     */
    fun withLocalUpdate(
        notesCount: Int,
        attachmentsCount: Int, 
        contentFingerprint: String,
        timestamp: Long
    ): SyncMetadata {
        return copy(
            lastSyncTimestamp = timestamp,
            notesCount = notesCount,
            attachmentsCount = attachmentsCount,
            contentFingerprint = contentFingerprint,
            updatedAt = timestamp
        )
    }
}

/**
 * Resultado de comparación de metadatos para sync incremental
 */
sealed class SyncComparisonResult {
    object InSync : SyncComparisonResult()
    object OutOfSync : SyncComparisonResult()
    object NoRemoteMetadata : SyncComparisonResult()
    object NoLocalMetadata : SyncComparisonResult()
    data class VersionMismatch(val localVersion: Int, val remoteVersion: Int) : SyncComparisonResult()
}