package com.vicherarr.memora.data.api.dto

import kotlinx.serialization.Serializable

/**
 * DTOs para operaciones de autenticación
 */

@Serializable
data class LoginRequestDto(
    val correoElectronico: String,
    val contrasena: String
)

@Serializable
data class RegisterRequestDto(
    val nombreCompleto: String, // Cambiado de nombreUsuario
    val correoElectronico: String,
    val contrasena: String
)

@Serializable
data class LoginResponseDto( // Cambiado nombre para coincidir con backend
    val token: String,
    val usuario: UsuarioDto,
    val expiresAt: String // Agregado campo que faltaba
)

@Serializable
data class UsuarioDto(
    val id: String, // Backend usa Guid pero se serializa como string
    val nombreCompleto: String, // Cambiado de nombreUsuario
    val correoElectronico: String,
    val fechaCreacion: String // Backend usa DateTime pero se serializa como string
)