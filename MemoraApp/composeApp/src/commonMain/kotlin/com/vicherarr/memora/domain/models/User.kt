package com.vicherarr.memora.domain.models

import kotlinx.datetime.Instant

/**
 * Modelo de dominio para Usuario
 */
data class User(
    val id: String,
    val username: String,
    val email: String,
    val createdAt: Instant
)