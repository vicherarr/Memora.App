package com.vicherarr.memora.data.mappers

import com.vicherarr.memora.domain.models.ArchivoAdjunto
import com.vicherarr.memora.domain.models.TipoDeArchivo
import com.vicherarr.memora.database.Attachments
import com.vicherarr.memora.data.database.getCurrentTimestamp

/**
 * Domain Mapper for Attachments following Clean Architecture principles
 * Single Responsibility: Only handles Attachments entity <-> Domain model conversion
 * No external dependencies - pure transformation logic
 */
class AttachmentDomainMapper {
    
    /**
     * Convert SQLDelight Attachments entity to Domain ArchivoAdjunto model
     * Type-safe conversion with proper enum mapping
     */
    fun toDomainModel(entity: Attachments): ArchivoAdjunto {
        return ArchivoAdjunto(
            id = entity.id,
            filePath = entity.file_path,
            remoteUrl = entity.remote_url,
            nombreOriginal = entity.nombre_original,
            tipoArchivo = mapTipoArchivo(entity.tipo_archivo),
            tipoMime = entity.tipo_mime,
            tamanoBytes = entity.tamano_bytes,
            fechaSubida = entity.fecha_subida.toLongOrNull() ?: getCurrentTimestamp(),
            notaId = entity.nota_id
        )
    }
    
    /**
     * Convert list of Attachments entities to Domain models
     * Optimized for bulk operations
     */
    fun toDomainModelList(entities: List<Attachments>): List<ArchivoAdjunto> {
        return entities.map { entity ->
            toDomainModel(entity)
        }
    }
    
    /**
     * Convert domain ArchivoAdjunto to SQLDelight compatible values
     * Used for creating/updating entities in database
     */
    fun fromDomainModel(domain: ArchivoAdjunto): AttachmentEntityData {
        return AttachmentEntityData(
            id = domain.id,
            filePath = domain.filePath,
            remoteUrl = domain.remoteUrl,
            nombreOriginal = domain.nombreOriginal,
            tipoArchivo = mapFromTipoArchivo(domain.tipoArchivo),
            tipoMime = domain.tipoMime,
            tamanoBytes = domain.tamanoBytes,
            fechaSubida = domain.fechaSubida.toString(),
            notaId = domain.notaId
        )
    }
    
    /**
     * Map database tipo_archivo (Long) to domain TipoDeArchivo enum
     * Type-safe conversion with fallback
     */
    private fun mapTipoArchivo(tipoArchivo: Long): TipoDeArchivo {
        return when (tipoArchivo) {
            1L -> TipoDeArchivo.Imagen
            2L -> TipoDeArchivo.Video
            else -> TipoDeArchivo.Imagen // Safe fallback
        }
    }
    
    /**
     * Map domain TipoDeArchivo enum to database tipo_archivo (Long)
     * Reverse conversion for database operations
     */
    private fun mapFromTipoArchivo(tipoArchivo: TipoDeArchivo): Long {
        return when (tipoArchivo) {
            TipoDeArchivo.Imagen -> 1L
            TipoDeArchivo.Video -> 2L
        }
    }
    
}

/**
 * Data class representing attachment entity data for database operations
 * Separates database concerns from domain logic
 */
data class AttachmentEntityData(
    val id: String,
    val filePath: String?,
    val remoteUrl: String?,
    val nombreOriginal: String,
    val tipoArchivo: Long,
    val tipoMime: String,
    val tamanoBytes: Long,
    val fechaSubida: String,
    val notaId: String
)