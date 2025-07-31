package com.vicherarr.memora.domain.models

/**
 * Domain model representing a note in the application
 */
data class Note(
    val id: String,
    val titulo: String?,
    val contenido: String,
    val fechaCreacion: Long, // timestamp in milliseconds
    val fechaModificacion: Long, // timestamp in milliseconds
    val usuarioId: String
)