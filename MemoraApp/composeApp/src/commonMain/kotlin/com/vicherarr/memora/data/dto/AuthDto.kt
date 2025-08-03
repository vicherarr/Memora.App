package com.vicherarr.memora.data.dto

import kotlinx.serialization.Serializable

/**
 * DTOs for Authentication API requests and responses
 * These match EXACTLY the API endpoints in Memora.API
 */

@Serializable
data class LoginUserDto(
    val correoElectronico: String,
    val contrasena: String
)

@Serializable
data class RegisterUserDto(
    val nombreCompleto: String,
    val correoElectronico: String, 
    val contrasena: String
)

@Serializable
data class LoginResponseDto(
    val token: String,
    val usuario: UsuarioDto,
    val expiresAt: String // DateTime as ISO string
)

@Serializable
data class RegisterResponseDto(
    val token: String,
    val usuario: UsuarioDto,
    val expiresAt: String // DateTime as ISO string
)

@Serializable
data class UsuarioDto(
    val id: String, // Guid as string
    val nombreCompleto: String,
    val correoElectronico: String,
    val fechaCreacion: String // DateTime as ISO string
)