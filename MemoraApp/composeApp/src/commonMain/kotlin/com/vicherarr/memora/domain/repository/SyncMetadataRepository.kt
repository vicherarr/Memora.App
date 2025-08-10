package com.vicherarr.memora.domain.repository

import com.vicherarr.memora.domain.models.SyncMetadata
import com.vicherarr.memora.domain.models.SyncComparisonResult

/**
 * Repository interface para gestión de metadatos de sincronización
 * Clean Architecture - Domain Layer
 * 
 * Responsabilidades:
 * - Gestionar metadatos de sync locales y remotos
 * - Comparar fingerprints para sincronización incremental
 * - Abstracción entre domain y data layer
 */
interface SyncMetadataRepository {
    
    /**
     * Obtiene los metadatos de sync locales para un usuario
     * @param userId ID del usuario
     * @return SyncMetadata local o null si no existe
     */
    suspend fun getLocalSyncMetadata(userId: String): SyncMetadata?
    
    /**
     * Obtiene los metadatos de sync remotos desde Google Drive
     * @param userId ID del usuario
     * @return SyncMetadata remoto o null si no existe
     */
    suspend fun getRemoteSyncMetadata(userId: String): Result<SyncMetadata?>
    
    /**
     * Guarda o actualiza metadatos de sync locales
     * @param metadata Metadatos a guardar
     * @return Result indicando éxito o error
     */
    suspend fun saveLocalSyncMetadata(metadata: SyncMetadata): Result<Unit>
    
    /**
     * Guarda o actualiza metadatos de sync en Google Drive
     * @param metadata Metadatos a guardar remotamente
     * @return Result indicando éxito o error
     */
    suspend fun saveRemoteSyncMetadata(metadata: SyncMetadata): Result<Unit>
    
    /**
     * Elimina metadatos locales para un usuario
     * @param userId ID del usuario
     * @return Result indicando éxito o error
     */
    suspend fun deleteLocalSyncMetadata(userId: String): Result<Unit>
    
    /**
     * Elimina metadatos remotos para un usuario
     * @param userId ID del usuario
     * @return Result indicando éxito o error
     */
    suspend fun deleteRemoteSyncMetadata(userId: String): Result<Unit>
    
    /**
     * Compara metadatos locales vs remotos para determinar si se necesita sync
     * @param userId ID del usuario
     * @return SyncComparisonResult con el resultado de la comparación
     */
    suspend fun compareSyncMetadata(userId: String): Result<SyncComparisonResult>
    
    /**
     * Genera metadatos actualizados basados en el estado actual de la DB local
     * @param userId ID del usuario
     * @return SyncMetadata generado desde datos actuales
     */
    suspend fun generateCurrentSyncMetadata(userId: String): Result<SyncMetadata>
}