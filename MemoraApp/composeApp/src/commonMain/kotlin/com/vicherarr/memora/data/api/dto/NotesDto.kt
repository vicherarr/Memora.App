package com.vicherarr.memora.data.api.dto

import kotlinx.serialization.Serializable

/**
 * DTOs para operaciones de notas
 */

@Serializable
data class NotaDto(
    val id: String, // Guid serializado como string
    val titulo: String?,
    val contenido: String,
    val fechaCreacion: String, // DateTime serializado como string
    val fechaModificacion: String, // DateTime serializado como string
    val usuarioId: String // Guid serializado como string
    // archivosAdjuntos NO está en el NotaDto del backend - se obtiene por separado
)

@Serializable
data class CreateNotaDto( // Cambiado nombre para coincidir con backend
    val titulo: String?,
    val contenido: String
)

@Serializable
data class UpdateNotaDto( // Cambiado nombre para coincidir con backend  
    val titulo: String?,
    val contenido: String
)

@Serializable
data class ArchivoAdjuntoDto(
    val id: String, // Guid serializado como string
    val nombreOriginal: String,
    val tipoArchivo: Int, // TipoDeArchivo enum (1=Imagen, 2=Video)
    val tipoMime: String,
    val tamanoBytes: Long,
    val fechaSubida: String, // DateTime serializado como string
    val notaId: String // Guid serializado como string
)

@Serializable
data class PaginatedNotesResponseDto(
    val notas: List<NotaDto>,
    val totalCount: Int,
    val page: Int,
    val pageSize: Int,
    val hasNextPage: Boolean
)