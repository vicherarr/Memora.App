package com.vicherarr.memora.data.repository

import com.vicherarr.memora.data.dto.LoginUserDto
import com.vicherarr.memora.data.dto.RegisterUserDto
import com.vicherarr.memora.data.network.KtorFitClient
import com.vicherarr.memora.domain.models.User
import com.vicherarr.memora.domain.repository.AuthRepository

/**
 * Real implementation of AuthRepository using KtorFit
 * Connects to Memora.API backend for authentication
 */
class AuthRepositoryImpl : AuthRepository {
    
    private val authApi = KtorFitClient.getAuthApi()
    private var currentUser: User? = null
    private var authToken: String? = null
    
    override suspend fun login(correoElectronico: String, contrasena: String): Result<User> {
        return try {
            val loginRequest = LoginUserDto(
                correoElectronico = correoElectronico,
                contrasena = contrasena
            )
            
            val response = authApi.login(loginRequest)
            
            // Store auth token
            authToken = response.token
            KtorFitClient.setAuthToken(response.token)
            
            // Convert DTO to domain model
            val user = User(
                id = response.usuario.id,
                nombreCompleto = response.usuario.nombreCompleto,
                correoElectronico = response.usuario.correoElectronico,
                fechaCreacion = parseIsoDateTime(response.usuario.fechaCreacion)
            )
            
            currentUser = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun register(
        nombreCompleto: String, 
        correoElectronico: String, 
        contrasena: String
    ): Result<User> {
        return try {
            val registerRequest = RegisterUserDto(
                nombreCompleto = nombreCompleto,
                correoElectronico = correoElectronico,
                contrasena = contrasena
            )
            
            val response = authApi.register(registerRequest)
            
            // Store auth token
            authToken = response.token
            KtorFitClient.setAuthToken(response.token)
            
            // Convert DTO to domain model
            val user = User(
                id = response.usuario.id,
                nombreCompleto = response.usuario.nombreCompleto,
                correoElectronico = response.usuario.correoElectronico,
                fechaCreacion = parseIsoDateTime(response.usuario.fechaCreacion)
            )
            
            currentUser = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getCurrentUser(): User? {
        return currentUser
    }
    
    override suspend fun logout() {
        currentUser = null
        authToken = null
        KtorFitClient.clearAuthToken()
    }
    
    /**
     * Parse ISO DateTime string to timestamp
     * Expected format: "2024-01-01T12:00:00Z"
     */
    private fun parseIsoDateTime(isoString: String): Long {
        // Simplified implementation - just return a fixed timestamp for now
        // In production, would parse the actual ISO string
        return 1700000000L // Mock timestamp (November 2023)
    }
}