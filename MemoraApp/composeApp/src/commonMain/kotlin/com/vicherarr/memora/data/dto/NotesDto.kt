package com.vicherarr.memora.data.dto

import kotlinx.serialization.Serializable

/**
 * DTOs for Notes API requests and responses
 * These match EXACTLY the API endpoints in Memora.API
 */

@Serializable
data class CreateNotaDto(
    val titulo: String? = null,
    val contenido: String
)

@Serializable
data class UpdateNotaDto(
    val titulo: String? = null,
    val contenido: String
)

@Serializable
data class NotaDto(
    val id: String, // Guid as string
    val titulo: String? = null,
    val contenido: String,
    val fechaCreacion: String, // DateTime as ISO string
    val fechaModificacion: String, // DateTime as ISO string
    val usuarioId: String // Guid as string
)

@Serializable
data class PaginatedNotasDto(
    val notas: List<NotaDto>,
    val totalCount: Int,
    val pageNumber: Int,
    val pageSize: Int,
    val totalPages: Int,
    val hasPreviousPage: Boolean,
    val hasNextPage: Boolean
)