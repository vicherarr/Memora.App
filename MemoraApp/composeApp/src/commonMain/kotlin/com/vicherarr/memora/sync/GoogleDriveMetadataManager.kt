package com.vicherarr.memora.sync

import com.vicherarr.memora.domain.models.SyncMetadata
import com.vicherarr.memora.data.database.getCurrentTimestamp
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

/**
 * Manager para metadatos de sincronización en Google Drive
 * Clean Architecture - Infrastructure Service
 * 
 * Responsabilidades:
 * - Serializar/deserializar metadatos para Google Drive
 * - Gestionar archivos de metadata en AppDataFolder
 * - Abstracción entre domain models y Google Drive storage
 */
class GoogleDriveMetadataManager {
    
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    /**
     * Nombre del archivo de metadata en Google Drive
     */
    fun getMetadataFileName(userId: String): String {
        return "memora_sync_metadata_${userId.hashCode().toString(16)}.json"
    }
    
    /**
     * Serializa SyncMetadata a JSON para almacenar en Google Drive
     */
    fun serializeMetadata(syncMetadata: SyncMetadata): String {
        val dto = SyncMetadataDto.fromDomain(syncMetadata)
        return json.encodeToString(dto)
    }
    
    /**
     * Deserializa JSON desde Google Drive a SyncMetadata
     */
    fun deserializeMetadata(jsonContent: String): Result<SyncMetadata> {
        return try {
            val dto = json.decodeFromString<SyncMetadataDto>(jsonContent)
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            println("GoogleDriveMetadataManager: Error deserializando metadata: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Valida que el contenido JSON sea válido
     */
    fun validateMetadataContent(jsonContent: String): Boolean {
        return try {
            json.decodeFromString<SyncMetadataDto>(jsonContent)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Genera contenido de metadata de ejemplo para testing (Fase 6: Con categorías)
     */
    fun generateSampleMetadata(userId: String): String {
        val sampleMetadata = SyncMetadata.createInitial(
            userId = userId,
            notesCount = 0,
            attachmentsCount = 0,
            categoriesCount = 0, // Fase 6
            noteCategoriesCount = 0, // Fase 6
            contentFingerprint = "sample_fingerprint",
            timestamp = getCurrentTimestamp()
        )
        return serializeMetadata(sampleMetadata)
    }
}

/**
 * DTO para serialización JSON de metadatos (Fase 6: Con categorías)
 * Separado del domain model para evitar acoplamiento con serialización
 */
@Serializable
private data class SyncMetadataDto(
    val userId: String,
    val lastSyncTimestamp: Long,
    val notesCount: Int,
    val attachmentsCount: Int,
    val categoriesCount: Int = 0, // Fase 6: Con default para backward compatibility
    val noteCategoriesCount: Int = 0, // Fase 6: Con default para backward compatibility
    val contentFingerprint: String,
    val remoteFingerprint: String? = null,
    val syncVersion: Int = 1,
    val createdAt: Long,
    val updatedAt: Long,
    val schemaVersion: String = "2.0", // Fase 6: Actualizada a 2.0 para incluir categorías
    val generatedBy: String = "Memora_Android_KMP"
) {
    companion object {
        fun fromDomain(syncMetadata: SyncMetadata): SyncMetadataDto {
            return SyncMetadataDto(
                userId = syncMetadata.userId,
                lastSyncTimestamp = syncMetadata.lastSyncTimestamp,
                notesCount = syncMetadata.notesCount,
                attachmentsCount = syncMetadata.attachmentsCount,
                categoriesCount = syncMetadata.categoriesCount,
                noteCategoriesCount = syncMetadata.noteCategoriesCount,
                contentFingerprint = syncMetadata.contentFingerprint,
                remoteFingerprint = syncMetadata.remoteFingerprint,
                syncVersion = syncMetadata.syncVersion,
                createdAt = syncMetadata.createdAt,
                updatedAt = syncMetadata.updatedAt
            )
        }
    }

    fun toDomain(): SyncMetadata {
        return SyncMetadata(
            userId = userId,
            lastSyncTimestamp = lastSyncTimestamp,
            notesCount = notesCount,
            attachmentsCount = attachmentsCount,
            categoriesCount = categoriesCount,
            noteCategoriesCount = noteCategoriesCount,
            contentFingerprint = contentFingerprint,
            remoteFingerprint = remoteFingerprint,
            syncVersion = syncVersion,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}