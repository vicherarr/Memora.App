package com.vicherarr.memora.sync

import com.vicherarr.memora.data.database.Attachment
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.vicherarr.memora.data.database.getCurrentTimestamp

/**
 * Implementación mock para iOS (desarrollo)
 * TODO: Implementar con iCloud cuando esté disponible
 */
actual interface AttachmentSyncRepository {
    actual suspend fun uploadAttachment(
        attachment: Attachment,
        fileData: ByteArray,
        userId: String
    ): Result<String>
    
    actual suspend fun downloadAttachment(remoteId: String): Result<ByteArray>
    actual suspend fun listRemoteAttachments(userId: String): List<RemoteAttachmentInfo>
    actual suspend fun deleteRemoteAttachment(remoteId: String): Result<Boolean>
    actual suspend fun remoteAttachmentExists(remoteId: String): Result<Boolean>
    actual suspend fun getRemoteAttachmentMetadata(remoteId: String): Result<RemoteAttachmentMetadata>
    actual suspend fun updateRemoteAttachmentMetadata(
        remoteId: String, 
        metadata: RemoteAttachmentMetadata
    ): Result<Boolean>
}

/**
 * Implementación mock para iOS
 */
class MockAttachmentSyncRepository : AttachmentSyncRepository {
    
    companion object {
        private const val TAG = "MockAttachmentSync"
    }
    
    // Simulamos almacenamiento en memoria para desarrollo
    private val mockStorage = mutableMapOf<String, ByteArray>()
    private val mockMetadata = mutableMapOf<String, RemoteAttachmentMetadata>()
    
    override suspend fun uploadAttachment(
        attachment: Attachment,
        fileData: ByteArray,
        userId: String
    ): Result<String> = withContext(Dispatchers.Default) {
        try {
            println("$TAG: 📤 [MOCK] Simulando subida de attachment: ${attachment.nombre_original}")
            println("$TAG: 📤 [MOCK] Tamaño: ${fileData.size} bytes")
            println("$TAG: 📤 [MOCK] Usuario: $userId")
            
            // Simular delay de red
            delay(1500)
            
            // Generar ID remoto simulado
            val remoteId = "mock_${attachment.id}_${getCurrentTimestamp()}"
            
            // Almacenar en "storage" simulado
            mockStorage[remoteId] = fileData
            
            // Crear metadatos simulados
            val metadata = RemoteAttachmentMetadata(
                remoteId = remoteId,
                fileName = attachment.nombre_original,
                size = fileData.size.toLong(),
                mimeType = attachment.tipo_mime,
                contentHash = attachment.content_hash,
                createdAt = getCurrentTimestamp(),
                modifiedAt = getCurrentTimestamp(),
                userId = userId,
                noteId = attachment.nota_id,
                customProperties = mapOf(
                    "mockUpload" to "true",
                    "attachmentId" to attachment.id
                )
            )
            
            mockMetadata[remoteId] = metadata
            
            println("$TAG: 📤 [MOCK] ✅ Subida simulada exitosa - Remote ID: $remoteId")
            Result.success(remoteId)
            
        } catch (e: Exception) {
            println("$TAG: 📤 [MOCK] ❌ Error simulando subida: ${e.message}")
            Result.failure(Exception("Mock upload error: ${e.message}"))
        }
    }
    
    override suspend fun downloadAttachment(remoteId: String): Result<ByteArray> = withContext(Dispatchers.Default) {
        try {
            println("$TAG: 📥 [MOCK] Simulando descarga de attachment: $remoteId")
            
            // Simular delay de red
            delay(1000)
            
            val fileData = mockStorage[remoteId]
            if (fileData != null) {
                println("$TAG: 📥 [MOCK] ✅ Descarga simulada exitosa - ${fileData.size} bytes")
                Result.success(fileData)
            } else {
                println("$TAG: 📥 [MOCK] ❌ Archivo no encontrado en storage simulado")
                Result.failure(Exception("Mock file not found: $remoteId"))
            }
            
        } catch (e: Exception) {
            println("$TAG: 📥 [MOCK] ❌ Error simulando descarga: ${e.message}")
            Result.failure(Exception("Mock download error: ${e.message}"))
        }
    }
    
    override suspend fun listRemoteAttachments(userId: String): List<RemoteAttachmentInfo> = withContext(Dispatchers.Default) {
        try {
            println("$TAG: 📋 [MOCK] Simulando listado de attachments para usuario: $userId")
            
            // Simular delay de red
            delay(800)
            
            // Filtrar metadatos por usuario
            val userAttachments = mockMetadata.values.filter { it.userId == userId }
            
            val remoteInfoList = userAttachments.map { metadata ->
                RemoteAttachmentInfo(
                    attachmentId = metadata.customProperties["attachmentId"] ?: "",
                    noteId = metadata.noteId,
                    remoteId = metadata.remoteId,
                    fileName = metadata.fileName,
                    mediaType = when {
                        metadata.mimeType.startsWith("image/") -> 1
                        metadata.mimeType.startsWith("video/") -> 2
                        else -> 1
                    },
                    mimeType = metadata.mimeType,
                    contentHash = metadata.contentHash,
                    uploadDate = metadata.createdAt,
                    remotePath = "mock://storage/${metadata.remoteId}"
                )
            }
            
            println("$TAG: 📋 [MOCK] ✅ Listado simulado: ${remoteInfoList.size} attachments")
            remoteInfoList
            
        } catch (e: Exception) {
            println("$TAG: 📋 [MOCK] ❌ Error simulando listado: ${e.message}")
            emptyList()
        }
    }
    
    override suspend fun deleteRemoteAttachment(remoteId: String): Result<Boolean> = withContext(Dispatchers.Default) {
        try {
            println("$TAG: 🗑️ [MOCK] Simulando eliminación de attachment: $remoteId")
            
            // Simular delay de red
            delay(500)
            
            val existed = mockStorage.remove(remoteId) != null
            mockMetadata.remove(remoteId)
            
            if (existed) {
                println("$TAG: 🗑️ [MOCK] ✅ Eliminación simulada exitosa")
                Result.success(true)
            } else {
                println("$TAG: 🗑️ [MOCK] ⚠️ Archivo no existía en storage simulado")
                Result.success(false)
            }
            
        } catch (e: Exception) {
            println("$TAG: 🗑️ [MOCK] ❌ Error simulando eliminación: ${e.message}")
            Result.failure(Exception("Mock delete error: ${e.message}"))
        }
    }
    
    override suspend fun remoteAttachmentExists(remoteId: String): Result<Boolean> = withContext(Dispatchers.Default) {
        try {
            println("$TAG: 🔍 [MOCK] Verificando existencia de attachment: $remoteId")
            
            // Simular delay de red
            delay(200)
            
            val exists = mockStorage.containsKey(remoteId)
            println("$TAG: 🔍 [MOCK] Existe: $exists")
            
            Result.success(exists)
            
        } catch (e: Exception) {
            println("$TAG: 🔍 [MOCK] ❌ Error verificando existencia: ${e.message}")
            Result.failure(Exception("Mock exists check error: ${e.message}"))
        }
    }
    
    override suspend fun getRemoteAttachmentMetadata(remoteId: String): Result<RemoteAttachmentMetadata> = withContext(Dispatchers.Default) {
        try {
            println("$TAG: ℹ️ [MOCK] Obteniendo metadatos de attachment: $remoteId")
            
            // Simular delay de red
            delay(300)
            
            val metadata = mockMetadata[remoteId]
            if (metadata != null) {
                println("$TAG: ℹ️ [MOCK] ✅ Metadatos obtenidos: ${metadata.fileName}")
                Result.success(metadata)
            } else {
                println("$TAG: ℹ️ [MOCK] ❌ Metadatos no encontrados")
                Result.failure(Exception("Mock metadata not found: $remoteId"))
            }
            
        } catch (e: Exception) {
            println("$TAG: ℹ️ [MOCK] ❌ Error obteniendo metadatos: ${e.message}")
            Result.failure(Exception("Mock metadata error: ${e.message}"))
        }
    }
    
    override suspend fun updateRemoteAttachmentMetadata(
        remoteId: String, 
        metadata: RemoteAttachmentMetadata
    ): Result<Boolean> = withContext(Dispatchers.Default) {
        try {
            println("$TAG: ✏️ [MOCK] Actualizando metadatos de attachment: $remoteId")
            
            // Simular delay de red
            delay(400)
            
            if (mockMetadata.containsKey(remoteId)) {
                mockMetadata[remoteId] = metadata
                println("$TAG: ✏️ [MOCK] ✅ Metadatos actualizados")
                Result.success(true)
            } else {
                println("$TAG: ✏️ [MOCK] ❌ Attachment no encontrado para actualizar")
                Result.success(false)
            }
            
        } catch (e: Exception) {
            println("$TAG: ✏️ [MOCK] ❌ Error actualizando metadatos: ${e.message}")
            Result.failure(Exception("Mock metadata update error: ${e.message}"))
        }
    }
}

/**
 * Factory para iOS
 */
actual object AttachmentSyncRepositoryFactory {
    actual fun create(): AttachmentSyncRepository {
        return MockAttachmentSyncRepository()
    }
}