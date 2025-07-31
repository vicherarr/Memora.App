package com.vicherarr.memora.domain.repository

import com.vicherarr.memora.domain.models.User

/**
 * Repository interface for authentication operations
 */
interface AuthRepository {
    suspend fun login(correoElectronico: String, contrasena: String): Result<User>
    suspend fun register(nombreUsuario: String, correoElectronico: String, contrasena: String): Result<User>
    suspend fun getCurrentUser(): User?
    suspend fun logout()
}