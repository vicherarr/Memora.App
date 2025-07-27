package com.vicherarr.memora.domain.models

import kotlin.time.Instant

/**
 * Modelo de dominio para Nota
 */
data class Note(
    val id: String,
    val title: String?,
    val content: String,
    val createdAt: Instant,
    val modifiedAt: Instant,
    val userId: String,
    val attachments: List<Attachment> = emptyList(),
    val isLocalOnly: Boolean = false
)