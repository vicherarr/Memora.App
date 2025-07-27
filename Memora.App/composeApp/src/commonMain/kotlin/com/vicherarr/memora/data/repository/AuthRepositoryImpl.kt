package com.vicherarr.memora.data.repository

import com.vicherarr.memora.data.api.MemoraApiService
import com.vicherarr.memora.data.api.safeApiCall
import com.vicherarr.memora.data.database.LocalDatabaseManager
import com.vicherarr.memora.domain.models.User
import com.vicherarr.memora.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Clock

/**
 * Implementación del repositorio de autenticación
 * Combina API remota con cache local
 */
class AuthRepositoryImpl(
    private val apiService: MemoraApiService,
    private val localDatabase: LocalDatabaseManager
) : AuthRepository {
    
    override suspend fun login(email: String, password: String): User {
        val result = safeApiCall {
            apiService.login(email, password)
        }
        
        when (result) {
            is com.vicherarr.memora.data.api.ApiResult.Success -> {
                val response = result.data
                
                // Guardar usuario en cache local
                val user = User(
                    id = response.usuario.id,
                    username = response.usuario.nombreCompleto,
                    email = response.usuario.correoElectronico,
                    createdAt = Clock.System.now() // TODO: parsear fechaCreacion del DTO
                )
                
                localDatabase.insertUser(
                    id = user.id,
                    username = user.username,
                    email = user.email,
                    createdAt = user.createdAt.toEpochMilliseconds()
                )
                
                // TODO: Guardar token JWT de forma segura
                // saveAuthToken(response.token)
                
                return user
            }
            is com.vicherarr.memora.data.api.ApiResult.Error -> {
                throw result.exception
            }
        }
    }
    
    override suspend fun register(username: String, email: String, password: String): User {
        val result = safeApiCall {
            apiService.register(username, email, password)
        }
        
        when (result) {
            is com.vicherarr.memora.data.api.ApiResult.Success -> {
                val response = result.data
                
                // Guardar usuario en cache local
                val user = User(
                    id = response.usuario.id,
                    username = response.usuario.nombreCompleto,
                    email = response.usuario.correoElectronico,
                    createdAt = Clock.System.now() // TODO: parsear fechaCreacion del DTO
                )
                
                localDatabase.insertUser(
                    id = user.id,
                    username = user.username,
                    email = user.email,
                    createdAt = user.createdAt.toEpochMilliseconds()
                )
                
                // TODO: Guardar token JWT de forma segura
                // saveAuthToken(response.token)
                
                return user
            }
            is com.vicherarr.memora.data.api.ApiResult.Error -> {
                throw result.exception
            }
        }
    }
    
    override suspend fun logout() {
        // TODO: Limpiar token JWT
        // clearAuthToken()
        
        // Limpiar datos locales del usuario
        // TODO: Obtener user ID actual para limpiar solo sus datos
    }
    
    override fun getCurrentUser(): Flow<User?> {
        // TODO: Implementar obtención del usuario actual desde cache local
        return flowOf(null)
    }
    
    override suspend fun isUserAuthenticated(): Boolean {
        // TODO: Verificar si hay token JWT válido
        return false
    }
    
    override suspend fun getAuthToken(): String? {
        // TODO: Implementar obtención del token desde secure storage
        return null
    }
    
    override suspend fun saveAuthToken(token: String) {
        // TODO: Implementar guardado seguro del token
    }
    
    override suspend fun clearAuthToken() {
        // TODO: Implementar limpieza del token
    }
}