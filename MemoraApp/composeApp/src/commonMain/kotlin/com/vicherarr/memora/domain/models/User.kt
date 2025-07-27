package com.vicherarr.memora.domain.models

import kotlin.time.Instant

/**
 * Modelo de dominio para Usuario
 */
data class User(
    val id: String,
    val username: String,
    val email: String,
    val createdAt: Instant
)