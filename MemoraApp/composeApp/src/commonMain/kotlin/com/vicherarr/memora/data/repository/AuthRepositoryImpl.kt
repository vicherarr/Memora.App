package com.vicherarr.memora.data.repository

import com.vicherarr.memora.domain.models.User
import com.vicherarr.memora.domain.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Mock implementation of AuthRepository for development and testing
 * TODO: Replace with real API implementation using Ktor
 */
class AuthRepositoryImpl : AuthRepository {
    
    private var currentUser: User? = null
    
    override suspend fun login(correoElectronico: String, contrasena: String): Result<User> {
        return try {
            // Simulate network delay
            delay(1000)
            
            // Mock validation - accept any email with password "123456"
            if (contrasena == "123456") {
                val user = User(
                    id = "user_${Random.nextInt(1000, 9999)}",
                    nombreUsuario = correoElectronico.substringBefore("@"),
                    correoElectronico = correoElectronico,
                    fechaCreacion = 1700000000L // Mock timestamp
                )
                currentUser = user
                Result.success(user)
            } else {
                Result.failure(Exception("Invalid credentials"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun register(
        nombreUsuario: String, 
        correoElectronico: String, 
        contrasena: String
    ): Result<User> {
        return try {
            // Simulate network delay
            delay(1000)
            
            // Mock registration - always succeeds
            val user = User(
                id = "user_${Random.nextInt(1000, 9999)}",
                nombreUsuario = nombreUsuario,
                correoElectronico = correoElectronico,
                fechaCreacion = 1700000000L // Mock timestamp
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
    }
}