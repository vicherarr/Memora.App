package com.vicherarr.memora.domain.repository

import com.vicherarr.memora.domain.models.User
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz del repositorio de autenticación
 * Define las operaciones disponibles para la gestión de usuarios y autenticación
 */
interface AuthRepository {
    
    /**
     * Realiza el login del usuario
     * @param email Correo electrónico del usuario
     * @param password Contraseña del usuario
     * @return Usuario autenticado
     */
    suspend fun login(email: String, password: String): User
    
    /**
     * Registra un nuevo usuario
     * @param username Nombre de usuario
     * @param email Correo electrónico
     * @param password Contraseña
     * @return Usuario registrado
     */
    suspend fun register(username: String, email: String, password: String): User
    
    /**
     * Cierra la sesión del usuario actual
     */
    suspend fun logout()
    
    /**
     * Obtiene el usuario actualmente autenticado
     * @return Flow con el usuario actual o null si no está autenticado
     */
    fun getCurrentUser(): Flow<User?>
    
    /**
     * Verifica si hay un usuario autenticado
     * @return true si hay una sesión activa
     */
    suspend fun isUserAuthenticated(): Boolean
    
    /**
     * Obtiene el token JWT actual
     * @return Token JWT o null si no existe
     */
    suspend fun getAuthToken(): String?
    
    /**
     * Guarda el token JWT
     * @param token Token JWT a guardar
     */
    suspend fun saveAuthToken(token: String)
    
    /**
     * Limpia el token JWT almacenado
     */
    suspend fun clearAuthToken()
}