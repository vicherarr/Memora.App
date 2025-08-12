package com.vicherarr.memora.sync

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.vicherarr.memora.data.database.getCurrentTimestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.vicherarr.memora.data.database.AttachmentsDao
import com.vicherarr.memora.domain.platform.FileManager
import com.vicherarr.memora.data.database.Attachment

/**
 * Estados del proceso de sincronización de attachments
 */
sealed class AttachmentSyncState {
    object Idle : AttachmentSyncState()
    data class Syncing(val currentFile: String, val progress: Int, val total: Int) : AttachmentSyncState()
    data class Success(val syncedCount: Int, val message: String) : AttachmentSyncState()
    data class Error(val error: String) : AttachmentSyncState()
}

/**
 * Configuración para el proceso de sincronización
 */
data class AttachmentSyncConfig(
    val syncOnlyWifi: Boolean = true,
    val maxFileSize: Long = 50 * 1024 * 1024, // 50MB
    val compressionQuality: Int = 85, // Para imágenes
    val enableConflictResolution: Boolean = true,
    val batchSize: Int = 5 // Archivos por lote
)

/**
 * Motor principal para sincronización de attachments
 * Coordina subida, descarga y resolución de conflictos para archivos adjuntos
 */
class AttachmentSyncEngine(
    private val attachmentsDao: AttachmentsDao,
    private val fileManager: FileManager,
    private val attachmentSyncRepository: AttachmentSyncRepository,
    private val hashCalculator: HashCalculator,
    private val tombstoneService: TombstoneService,
    private val config: AttachmentSyncConfig = AttachmentSyncConfig()
) {
    
    private val _syncState = MutableStateFlow<AttachmentSyncState>(AttachmentSyncState.Idle)
    val syncState: StateFlow<AttachmentSyncState> = _syncState.asStateFlow()
    
    /**
     * Inicia sincronización completa de attachments para un usuario
     */
    suspend fun startFullSync(userId: String): Result<AttachmentSyncResult> = withContext(Dispatchers.Default) {
        try {
            println("AttachmentSyncEngine: ====== INICIANDO SYNC COMPLETO DE ATTACHMENTS ======")
            println("AttachmentSyncEngine: Usuario: $userId")
            
            _syncState.value = AttachmentSyncState.Syncing("Preparando...", 0, 0)
            
            // PASO 1: Obtener attachments locales pendientes de sync
            val pendingAttachments = attachmentsDao.getAttachmentsPendingSync(userId)
            println("AttachmentSyncEngine: ${pendingAttachments.size} attachments pendientes de sync")
            
            // PASO 2: Upload attachments pendientes
            val uploadResults = uploadPendingAttachments(pendingAttachments, userId)
            
            // PASO 3: Download nuevos attachments remotos
            val downloadResults = downloadNewRemoteAttachments(userId)
            
            // PASO 4: Verificar y resolver conflictos
            val conflictResults = resolveAttachmentConflicts(userId)
            
            val totalSynced = uploadResults.uploadedCount + downloadResults.downloadedCount
            val finalResult = AttachmentSyncResult(
                uploadedCount = uploadResults.uploadedCount,
                downloadedCount = downloadResults.downloadedCount,
                conflictsResolved = conflictResults.conflictsResolved,
                errors = uploadResults.errors + downloadResults.errors + conflictResults.errors
            )
            
            _syncState.value = AttachmentSyncState.Success(
                totalSynced, 
                "Sync completo: ${finalResult.uploadedCount} subidos, ${finalResult.downloadedCount} descargados"
            )
            
            println("AttachmentSyncEngine: ====== SYNC COMPLETO FINALIZADO ======")
            println("AttachmentSyncEngine: Resultado: $finalResult")
            
            Result.success(finalResult)
            
        } catch (e: Exception) {
            val errorMessage = "Error en sync completo: ${e.message}"
            _syncState.value = AttachmentSyncState.Error(errorMessage)
            println("AttachmentSyncEngine: $errorMessage")
            Result.failure(e)
        }
    }
    
    /**
     * Sube attachments pendientes de sincronización
     */
    private suspend fun uploadPendingAttachments(
        pendingAttachments: List<Attachment>, 
        userId: String
    ): UploadSyncResult = withContext(Dispatchers.Default) {
        
        var uploadedCount = 0
        val errors = mutableListOf<String>()
        
        println("AttachmentSyncEngine: === UPLOAD PHASE ===")
        println("AttachmentSyncEngine: ${pendingAttachments.size} attachments para subir")
        
        for ((index, attachment) in pendingAttachments.withIndex()) {
            try {
                _syncState.value = AttachmentSyncState.Syncing(
                    attachment.nombre_original, 
                    index + 1, 
                    pendingAttachments.size
                )
                
                println("AttachmentSyncEngine: Subiendo (${index + 1}/${pendingAttachments.size}): ${attachment.nombre_original}")
                
                // Validar que el archivo local existe
                val localPath = attachment.ruta_local
                if (localPath == null || !fileManager.fileExists(localPath)) {
                    println("AttachmentSyncEngine: ⚠️ Archivo local no existe: $localPath")
                    attachmentsDao.markAsLocallyDeleted(attachment.id)
                    continue
                }
                
                // Leer archivo local
                val fileData = fileManager.getFile(localPath)
                if (fileData == null) {
                    errors.add("No se pudo leer archivo: ${attachment.nombre_original}")
                    continue
                }
                
                // Verificar integridad del archivo
                val currentHash = hashCalculator.calculateSHA256(fileData)
                if (attachment.content_hash != null && attachment.content_hash != currentHash) {
                    println("AttachmentSyncEngine: ⚠️ Hash mismatch detectado para: ${attachment.nombre_original}")
                    println("AttachmentSyncEngine:   Expected: ${attachment.content_hash}")
                    println("AttachmentSyncEngine:   Actual: $currentHash")
                    
                    // Actualizar hash en base de datos
                    attachmentsDao.updateContentHash(attachment.id, currentHash)
                }
                
                // Subir a Google Drive
                val uploadResult = attachmentSyncRepository.uploadAttachment(
                    attachment = attachment,
                    fileData = fileData,
                    userId = userId
                )
                
                if (uploadResult.isSuccess) {
                    val remoteId = uploadResult.getOrNull()
                    if (remoteId != null) {
                        // Marcar como sincronizado
                        attachmentsDao.markAsSynced(attachment.id, remoteId, currentHash)
                        uploadedCount++
                        println("AttachmentSyncEngine: ✅ Subido exitosamente: ${attachment.nombre_original}")
                    }
                } else {
                    val error = "Error subiendo ${attachment.nombre_original}: ${uploadResult.exceptionOrNull()?.message}"
                    errors.add(error)
                    println("AttachmentSyncEngine: ❌ $error")
                }
                
            } catch (e: Exception) {
                val error = "Excepción subiendo ${attachment.nombre_original}: ${e.message}"
                errors.add(error)
                println("AttachmentSyncEngine: ❌ $error")
            }
        }
        
        UploadSyncResult(uploadedCount, errors)
    }
    
    /**
     * Descarga nuevos attachments desde el servidor remoto
     */
    private suspend fun downloadNewRemoteAttachments(userId: String): DownloadSyncResult = withContext(Dispatchers.Default) {
        
        var downloadedCount = 0
        val errors = mutableListOf<String>()
        
        println("AttachmentSyncEngine: === DOWNLOAD PHASE ===")
        
        try {
            // Obtener lista de attachments remotos
            val remoteAttachments = attachmentSyncRepository.listRemoteAttachments(userId)
            println("AttachmentSyncEngine: ${remoteAttachments.size} attachments remotos encontrados")
            
            // Filtrar los que no tenemos localmente Y que NO tienen tombstones
            val newRemoteAttachments = remoteAttachments.filter { remote ->
                // Verificar que no existe localmente
                val notExistsLocally = attachmentsDao.getAttachmentByRemoteId(remote.remoteId) == null
                
                // Verificar que no tiene tombstone (fue eliminado localmente)
                val notDeleted = !tombstoneService.isAttachmentDeleted(remote.attachmentId, userId)
                
                if (!notDeleted) {
                    println("AttachmentSyncEngine: 🪦 Attachment ${remote.attachmentId} ignorado - tiene tombstone local")
                }
                
                notExistsLocally && notDeleted
            }
            
            println("AttachmentSyncEngine: ${newRemoteAttachments.size} attachments nuevos para descargar")
            
            for ((index, remoteAttachment) in newRemoteAttachments.withIndex()) {
                try {
                    _syncState.value = AttachmentSyncState.Syncing(
                        remoteAttachment.fileName, 
                        index + 1, 
                        newRemoteAttachments.size
                    )
                    
                    println("AttachmentSyncEngine: Descargando (${index + 1}/${newRemoteAttachments.size}): ${remoteAttachment.fileName}")
                    
                    // Descargar archivo desde Google Drive
                    val downloadResult = attachmentSyncRepository.downloadAttachment(remoteAttachment.remoteId)
                    
                    if (downloadResult.isSuccess) {
                        val fileData = downloadResult.getOrNull()
                        if (fileData != null) {
                            
                            // Calcular hash para verificar integridad
                            val calculatedHash = hashCalculator.calculateSHA256(fileData)
                            
                            // Verificar integridad si tenemos hash esperado
                            if (remoteAttachment.contentHash != null && remoteAttachment.contentHash != calculatedHash) {
                                errors.add("Hash mismatch para ${remoteAttachment.fileName}: esperado ${remoteAttachment.contentHash}, obtenido $calculatedHash")
                                continue
                            }
                            
                            // Generar ruta local usando AttachmentPathManager
                            val extension = AttachmentPathManager.getExtension(remoteAttachment.fileName)
                            val attachmentId = remoteAttachment.attachmentId.ifEmpty { 
                                "downloaded_${getCurrentTimestamp()}"
                            }
                            val localPath = AttachmentPathManager.buildLocalAttachmentPath(
                                noteId = remoteAttachment.noteId,
                                attachmentId = attachmentId,
                                extension = extension
                            )
                            
                            // Guardar archivo localmente
                            val savedFile = fileManager.saveFileAtPath(fileData, localPath)
                            if (savedFile != null) {
                                
                                // Crear registro en base de datos local
                                val localAttachment = Attachment(
                                    id = remoteAttachment.attachmentId,
                                    datos_archivo = null, // No guardamos en BLOB, solo ruta
                                    nombre_original = remoteAttachment.fileName,
                                    tipo_archivo = remoteAttachment.mediaType,
                                    tipo_mime = remoteAttachment.mimeType,
                                    tamano_bytes = fileData.size.toLong(),
                                    fecha_subida = remoteAttachment.uploadDate,
                                    nota_id = remoteAttachment.noteId,
                                    ruta_local = localPath,
                                    sync_status = SyncStatus.SYNCED,
                                    needs_upload = false,
                                    remote_id = remoteAttachment.remoteId,
                                    content_hash = calculatedHash,
                                    last_sync_attempt = getCurrentTimestamp(),
                                    sync_retry_count = 0,
                                    local_created_at = getCurrentTimestamp(),
                                    remote_path = remoteAttachment.remotePath
                                )
                                
                                // Use UPSERT to prevent UNIQUE constraint conflicts
                                attachmentsDao.upsertAttachment(localAttachment)
                                downloadedCount++
                                println("AttachmentSyncEngine: ✅ Descargado exitosamente: ${remoteAttachment.fileName}")
                                
                            } else {
                                errors.add("No se pudo guardar archivo: ${remoteAttachment.fileName}")
                            }
                        }
                    } else {
                        val error = "Error descargando ${remoteAttachment.fileName}: ${downloadResult.exceptionOrNull()?.message}"
                        errors.add(error)
                        println("AttachmentSyncEngine: ❌ $error")
                    }
                    
                } catch (e: Exception) {
                    val error = "Excepción descargando ${remoteAttachment.fileName}: ${e.message}"
                    errors.add(error)
                    println("AttachmentSyncEngine: ❌ $error")
                }
            }
            
        } catch (e: Exception) {
            errors.add("Error listando attachments remotos: ${e.message}")
            println("AttachmentSyncEngine: ❌ Error listando remotos: ${e.message}")
        }
        
        DownloadSyncResult(downloadedCount, errors)
    }
    
    /**
     * Resuelve conflictos entre attachments locales y remotos
     */
    private suspend fun resolveAttachmentConflicts(userId: String): ConflictSyncResult = withContext(Dispatchers.Default) {
        
        var conflictsResolved = 0
        val errors = mutableListOf<String>()
        
        println("AttachmentSyncEngine: === CONFLICT RESOLUTION PHASE ===")
        
        if (!config.enableConflictResolution) {
            println("AttachmentSyncEngine: Conflict resolution deshabilitado")
            return@withContext ConflictSyncResult(0, errors)
        }
        
        try {
            // Obtener attachments que pueden tener conflictos
            val conflictedAttachments = attachmentsDao.getConflictedAttachments(userId)
            println("AttachmentSyncEngine: ${conflictedAttachments.size} attachments con posibles conflictos")
            
            // TODO: Implementar lógica de resolución de conflictos
            // Por ahora, simplemente logeamos
            conflictedAttachments.forEach { attachment ->
                println("AttachmentSyncEngine: Conflicto detectado: ${attachment.nombre_original}")
                // Estrategia: KEEP_LOCAL, KEEP_REMOTE, MERGE, ASK_USER
            }
            
        } catch (e: Exception) {
            errors.add("Error resolviendo conflictos: ${e.message}")
            println("AttachmentSyncEngine: ❌ Error en conflict resolution: ${e.message}")
        }
        
        ConflictSyncResult(conflictsResolved, errors)
    }
    
    /**
     * Sincroniza un attachment específico (on-demand)
     */
    suspend fun syncSingleAttachment(attachmentId: String): Result<Boolean> = withContext(Dispatchers.Default) {
        try {
            println("AttachmentSyncEngine: Sincronizando attachment individual: $attachmentId")
            
            val attachment = attachmentsDao.getAttachmentByIdDomain(attachmentId)
            if (attachment == null) {
                return@withContext Result.failure(Exception("Attachment no encontrado: $attachmentId"))
            }
            
            _syncState.value = AttachmentSyncState.Syncing(attachment.nombre_original, 1, 1)
            
            // Si está pendiente de subir
            if (attachment.needs_upload) {
                val localPath = attachment.ruta_local
                if (localPath == null) {
                    return@withContext Result.failure(Exception("Attachment no tiene ruta local: ${attachment.id}"))
                }
                val fileData = fileManager.getFile(localPath)
                if (fileData != null) {
                    val uploadResult = attachmentSyncRepository.uploadAttachment(
                        attachment = attachment,
                        fileData = fileData,
                        userId = attachment.nota_id // TODO: Obtener userId correcto
                    )
                    
                    if (uploadResult.isSuccess) {
                        val remoteId = uploadResult.getOrNull()
                        if (remoteId != null) {
                            val hash = hashCalculator.calculateSHA256(fileData)
                            attachmentsDao.markAsSynced(attachment.id, remoteId, hash)
                        }
                    }
                    
                    _syncState.value = AttachmentSyncState.Success(1, "Attachment sincronizado exitosamente")
                    return@withContext uploadResult.map { true }
                }
            }
            
            Result.success(true)
            
        } catch (e: Exception) {
            _syncState.value = AttachmentSyncState.Error("Error sincronizando: ${e.message}")
            Result.failure(e)
        }
    }
}

/**
 * Resultados de sincronización
 */
data class AttachmentSyncResult(
    val uploadedCount: Int,
    val downloadedCount: Int,
    val conflictsResolved: Int,
    val errors: List<String>
)

data class UploadSyncResult(
    val uploadedCount: Int,
    val errors: List<String>
)

data class DownloadSyncResult(
    val downloadedCount: Int,
    val errors: List<String>
)

data class ConflictSyncResult(
    val conflictsResolved: Int,
    val errors: List<String>
)

/**
 * Información de attachment remoto
 */
data class RemoteAttachmentInfo(
    val attachmentId: String,
    val noteId: String,
    val remoteId: String,
    val fileName: String,
    val mediaType: Int,
    val mimeType: String,
    val contentHash: String?,
    val uploadDate: Long,
    val remotePath: String
)

/**
 * Estados de sincronización para attachments
 */
enum class SyncStatus {
    PENDING,    // Pendiente de sincronizar
    SYNCING,    // En proceso de sincronización
    SYNCED,     // Sincronizado exitosamente
    FAILED,     // Falló la sincronización
    CONFLICT    // Hay conflicto que resolver
}