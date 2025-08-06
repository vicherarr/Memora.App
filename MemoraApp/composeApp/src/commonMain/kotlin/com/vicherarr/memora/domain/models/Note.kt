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
    val archivosAdjuntos: List<ArchivoAdjunto> = emptyList()
)

/**
 * Domain model representing an attachment file
 */
data class ArchivoAdjunto(
    val id: String,
    val datosArchivo: ByteArray, // Binary file data
    val nombreOriginal: String,
    val tipoArchivo: TipoDeArchivo,
    val tipoMime: String, // MIME type (image/jpeg, video/mp4, etc.)
    val tamanoBytes: Long, // File size in bytes
    val fechaSubida: Long, // timestamp in milliseconds
    val notaId: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ArchivoAdjunto

        if (id != other.id) return false
        if (!datosArchivo.contentEquals(other.datosArchivo)) return false
        if (nombreOriginal != other.nombreOriginal) return false
        if (tipoArchivo != other.tipoArchivo) return false
        if (tipoMime != other.tipoMime) return false
        if (tamanoBytes != other.tamanoBytes) return false
        if (fechaSubida != other.fechaSubida) return false
        if (notaId != other.notaId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + datosArchivo.contentHashCode()
        result = 31 * result + nombreOriginal.hashCode()
        result = 31 * result + tipoArchivo.hashCode()
        result = 31 * result + tipoMime.hashCode()
        result = 31 * result + tamanoBytes.hashCode()
        result = 31 * result + fechaSubida.hashCode()
        result = 31 * result + notaId.hashCode()
        return result
    }
}

/**
 * File type enumeration matching API model
 */
enum class TipoDeArchivo {
    Imagen,
    Video
}