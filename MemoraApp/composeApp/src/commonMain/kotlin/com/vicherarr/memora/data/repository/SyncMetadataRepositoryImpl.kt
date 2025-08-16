package com.vicherarr.memora.data.repository

import com.vicherarr.memora.data.database.SyncMetadataDao
import com.vicherarr.memora.data.database.getCurrentTimestamp
import com.vicherarr.memora.domain.models.SyncMetadata
import com.vicherarr.memora.domain.models.SyncComparisonResult
import com.vicherarr.memora.domain.repository.SyncMetadataRepository
import com.vicherarr.memora.sync.CloudStorageProvider
import com.vicherarr.memora.sync.FingerprintGenerator
import com.vicherarr.memora.sync.GoogleDriveMetadataManager

/**
 * Implementación de SyncMetadataRepository - Clean Architecture Data Layer
 * 
 * Responsabilidades:
 * - Coordinar entre almacenamiento local (SQLDelight) y remoto (Google Drive/iCloud)
 * - Generar y comparar fingerprints para sincronización incremental
 * - Gestionar el ciclo de vida completo de metadatos de sync
 */
class SyncMetadataRepositoryImpl(
    private val syncMetadataDao: SyncMetadataDao,
    private val cloudStorageProvider: CloudStorageProvider,
    private val fingerprintGenerator: FingerprintGenerator,
    private val metadataManager: GoogleDriveMetadataManager
) : SyncMetadataRepository {
    
    override suspend fun getLocalSyncMetadata(userId: String): SyncMetadata? {
        return try {
            println("SyncMetadataRepository: 📖 Obteniendo metadata local para usuario: $userId")
            val metadata = syncMetadataDao.getSyncMetadataByUserId(userId)
            if (metadata != null) {
                println("SyncMetadataRepository: ✅ Metadata local encontrado: ${metadata.notesCount} notas, ${metadata.attachmentsCount} attachments, ${metadata.categoriesCount} categorías, ${metadata.noteCategoriesCount} relaciones")
            } else {
                println("SyncMetadataRepository: 📄 No hay metadata local para usuario $userId")
            }
            metadata
        } catch (e: Exception) {
            println("SyncMetadataRepository: ❌ Error obteniendo metadata local: ${e.message}")
            null
        }
    }
    
    override suspend fun getRemoteSyncMetadata(userId: String): Result<SyncMetadata?> {
        return try {
            println("SyncMetadataRepository: 📡 Obteniendo metadata remoto para usuario: $userId")
            
            val jsonContentResult = cloudStorageProvider.loadMetadata(userId)
            if (jsonContentResult.isFailure) {
                println("SyncMetadataRepository: ❌ Error cargando metadata remoto: ${jsonContentResult.exceptionOrNull()?.message}")
                return Result.failure(jsonContentResult.exceptionOrNull() ?: Exception("Error desconocido"))
            }
            
            val jsonContent = jsonContentResult.getOrNull()
            if (jsonContent == null) {
                println("SyncMetadataRepository: 📄 No hay metadata remoto para usuario $userId")
                return Result.success(null)
            }
            
            val metadataResult = metadataManager.deserializeMetadata(jsonContent)
            if (metadataResult.isFailure) {
                println("SyncMetadataRepository: ❌ Error deserializando metadata remoto: ${metadataResult.exceptionOrNull()?.message}")
                return Result.failure(metadataResult.exceptionOrNull() ?: Exception("Error deserializando metadata"))
            }
            
            val metadata = metadataResult.getOrNull()
            if (metadata != null) {
                println("SyncMetadataRepository: ✅ Metadata remoto encontrado: ${metadata.notesCount} notas, ${metadata.attachmentsCount} attachments, ${metadata.categoriesCount} categorías, ${metadata.noteCategoriesCount} relaciones")
            }
            
            Result.success(metadata)
            
        } catch (e: Exception) {
            println("SyncMetadataRepository: ❌ Error obteniendo metadata remoto: ${e.message}")
            Result.failure(e)
        }
    }
    
    override suspend fun saveLocalSyncMetadata(metadata: SyncMetadata): Result<Unit> {
        return try {
            println("SyncMetadataRepository: 💾 Guardando metadata local para usuario: ${metadata.userId}")
            syncMetadataDao.upsertSyncMetadata(metadata)
            println("SyncMetadataRepository: ✅ Metadata local guardado exitosamente")
            Result.success(Unit)
        } catch (e: Exception) {
            println("SyncMetadataRepository: ❌ Error guardando metadata local: ${e.message}")
            Result.failure(e)
        }
    }
    
    override suspend fun saveRemoteSyncMetadata(metadata: SyncMetadata): Result<Unit> {
        return try {
            println("SyncMetadataRepository: 📡 Guardando metadata remoto para usuario: ${metadata.userId}")
            
            val jsonContent = metadataManager.serializeMetadata(metadata)
            val saveResult = cloudStorageProvider.saveMetadata(metadata.userId, jsonContent)
            
            if (saveResult.isFailure) {
                println("SyncMetadataRepository: ❌ Error guardando metadata remoto: ${saveResult.exceptionOrNull()?.message}")
                return Result.failure(saveResult.exceptionOrNull() ?: Exception("Error desconocido"))
            }
            
            val fileId = saveResult.getOrNull()
            println("SyncMetadataRepository: ✅ Metadata remoto guardado exitosamente. FileID: $fileId")
            Result.success(Unit)
            
        } catch (e: Exception) {
            println("SyncMetadataRepository: ❌ Error guardando metadata remoto: ${e.message}")
            Result.failure(e)
        }
    }
    
    override suspend fun deleteLocalSyncMetadata(userId: String): Result<Unit> {
        return try {
            println("SyncMetadataRepository: 🗑️ Eliminando metadata local para usuario: $userId")
            syncMetadataDao.deleteSyncMetadata(userId)
            println("SyncMetadataRepository: ✅ Metadata local eliminado exitosamente")
            Result.success(Unit)
        } catch (e: Exception) {
            println("SyncMetadataRepository: ❌ Error eliminando metadata local: ${e.message}")
            Result.failure(e)
        }
    }
    
    override suspend fun deleteRemoteSyncMetadata(userId: String): Result<Unit> {
        return try {
            println("SyncMetadataRepository: 📡 Eliminando metadata remoto para usuario: $userId")
            
            val deleteResult = cloudStorageProvider.deleteMetadata(userId)
            if (deleteResult.isFailure) {
                println("SyncMetadataRepository: ❌ Error eliminando metadata remoto: ${deleteResult.exceptionOrNull()?.message}")
                return Result.failure(deleteResult.exceptionOrNull() ?: Exception("Error desconocido"))
            }
            
            println("SyncMetadataRepository: ✅ Metadata remoto eliminado exitosamente")
            Result.success(Unit)
            
        } catch (e: Exception) {
            println("SyncMetadataRepository: ❌ Error eliminando metadata remoto: ${e.message}")
            Result.failure(e)
        }
    }
    
    override suspend fun compareSyncMetadata(userId: String): Result<SyncComparisonResult> {
        return try {
            println("SyncMetadataRepository: 🔍 Comparando metadata para usuario: $userId")
            
            // Obtener metadata local
            val localMetadata = getLocalSyncMetadata(userId)
            
            // Obtener metadata remoto
            val remoteMetadataResult = getRemoteSyncMetadata(userId)
            if (remoteMetadataResult.isFailure) {
                return Result.failure(remoteMetadataResult.exceptionOrNull() ?: Exception("Error obteniendo metadata remoto"))
            }
            
            val remoteMetadata = remoteMetadataResult.getOrNull()
            
            // Realizar comparación
            val comparisonResult = when {
                localMetadata == null && remoteMetadata == null -> {
                    println("SyncMetadataRepository: 📄 No hay metadata local ni remoto")
                    SyncComparisonResult.NoLocalMetadata
                }
                
                localMetadata == null -> {
                    println("SyncMetadataRepository: 📄 No hay metadata local, pero sí remoto")
                    SyncComparisonResult.NoLocalMetadata
                }
                
                remoteMetadata == null -> {
                    println("SyncMetadataRepository: 📄 No hay metadata remoto, pero sí local")
                    SyncComparisonResult.NoRemoteMetadata
                }
                
                localMetadata.syncVersion != remoteMetadata.syncVersion -> {
                    println("SyncMetadataRepository: ⚠️ Versiones de sync incompatibles: local=${localMetadata.syncVersion}, remoto=${remoteMetadata.syncVersion}")
                    SyncComparisonResult.VersionMismatch(localMetadata.syncVersion, remoteMetadata.syncVersion)
                }
                
                localMetadata.contentFingerprint == remoteMetadata.contentFingerprint -> {
                    println("SyncMetadataRepository: ✅ Metadata coincide - datos sincronizados")
                    SyncComparisonResult.InSync
                }
                
                else -> {
                    println("SyncMetadataRepository: 🔄 Metadata no coincide - se necesita sync")
                    println("SyncMetadataRepository: Local fingerprint: ${localMetadata.contentFingerprint}")
                    println("SyncMetadataRepository: Remote fingerprint: ${remoteMetadata.contentFingerprint}")
                    SyncComparisonResult.OutOfSync
                }
            }
            
            Result.success(comparisonResult)
            
        } catch (e: Exception) {
            println("SyncMetadataRepository: ❌ Error comparando metadata: ${e.message}")
            Result.failure(e)
        }
    }
    
    override suspend fun generateCurrentSyncMetadata(userId: String): Result<SyncMetadata> {
        return try {
            println("SyncMetadataRepository: 🔨 Generando metadata actual para usuario: $userId")
            
            // Generar fingerprint basado en datos actuales
            val fingerprintResult = fingerprintGenerator.generateContentFingerprint(userId)
            val now = getCurrentTimestamp()
            
            // ✅ CORREGIDO: Usar conteos reales de la base de datos (Fase 6: Incluye categorías)
            val contentFingerprint = fingerprintResult.fingerprint
            val notesCount = fingerprintResult.notesCount
            val attachmentsCount = fingerprintResult.attachmentsCount
            val categoriesCount = fingerprintResult.categoriesCount
            val noteCategoriesCount = fingerprintResult.noteCategoriesCount
            
            // Obtener metadata existente para preservar otros campos
            val existingMetadata = getLocalSyncMetadata(userId)
            
            val newMetadata = if (existingMetadata != null) {
                existingMetadata.withLocalUpdate(
                    notesCount = notesCount,
                    attachmentsCount = attachmentsCount,
                    categoriesCount = categoriesCount,
                    noteCategoriesCount = noteCategoriesCount,
                    contentFingerprint = contentFingerprint,
                    timestamp = now
                )
            } else {
                SyncMetadata.createInitial(
                    userId = userId,
                    notesCount = notesCount,
                    attachmentsCount = attachmentsCount,
                    categoriesCount = categoriesCount,
                    noteCategoriesCount = noteCategoriesCount,
                    contentFingerprint = contentFingerprint,
                    timestamp = now
                )
            }
            
            println("SyncMetadataRepository: ✅ Metadata actual generado:")
            println("SyncMetadataRepository:   - Fingerprint: ${contentFingerprint.take(16)}...")
            println("SyncMetadataRepository:   - Notas: $notesCount, Attachments: $attachmentsCount, Categories: $categoriesCount, NoteCategories: $noteCategoriesCount")
            Result.success(newMetadata)
            
        } catch (e: Exception) {
            println("SyncMetadataRepository: ❌ Error generando metadata actual: ${e.message}")
            Result.failure(e)
        }
    }
}