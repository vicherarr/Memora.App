package com.vicherarr.memora.domain.models

import kotlin.time.Instant

/**
 * Tipo de archivo adjunto
 */
enum class AttachmentType {
    IMAGE,
    VIDEO
}

/**
 * Modelo de dominio para Archivo Adjunto
 */
@OptIn(kotlin.time.ExperimentalTime::class)
data class Attachment(
    val id: String,
    val originalName: String,
    val type: AttachmentType,
    val mimeType: String,
    val sizeBytes: Long,
    val uploadedAt: Instant,
    val noteId: String,
    val localPath: String? = null,
    val isUploaded: Boolean = false
)