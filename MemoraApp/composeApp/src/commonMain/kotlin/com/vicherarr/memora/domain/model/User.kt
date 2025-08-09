package com.vicherarr.memora.domain.model

/**
 * Entidad de dominio que representa un usuario autenticado
 */
data class User(
    val id: String,
    val email: String,
    val displayName: String?,
    val profilePictureUrl: String?
)