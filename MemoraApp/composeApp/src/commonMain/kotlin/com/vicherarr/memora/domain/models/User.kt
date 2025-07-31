package com.vicherarr.memora.domain.models

/**
 * Domain model representing a user in the application
 */
data class User(
    val id: String,
    val nombreUsuario: String,
    val correoElectronico: String,
    val fechaCreacion: Long // timestamp in milliseconds
)