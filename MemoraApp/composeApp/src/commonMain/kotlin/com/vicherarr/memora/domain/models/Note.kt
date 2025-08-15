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
    val usuarioId: String,
    val archivosAdjuntos: List<ArchivoAdjunto> = emptyList(),
    val categories: List<Category> = emptyList() // Categories assigned to this note
)

/**
 * Domain model representing an attachment file
 */
data class ArchivoAdjunto(
    val id: String,
    val filePath: String?, // Local path to the file
    val remoteUrl: String? = null, // Remote URL after sync
    val nombreOriginal: String,
    val tipoArchivo: TipoDeArchivo,
    val tipoMime: String, // MIME type (image/jpeg, video/mp4, etc.)
    val tamanoBytes: Long, // File size in bytes
    val fechaSubida: Long, // timestamp in milliseconds
    val notaId: String
)

/**
 * File type enumeration matching API model
 */
enum class TipoDeArchivo {
    Imagen,
    Video
}