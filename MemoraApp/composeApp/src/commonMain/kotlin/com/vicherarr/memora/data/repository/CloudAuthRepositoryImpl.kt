package com.vicherarr.memora.data.repository

import com.vicherarr.memora.data.auth.CloudAuthProvider
import com.vicherarr.memora.domain.model.AuthState
import com.vicherarr.memora.domain.model.User
import com.vicherarr.memora.domain.repository.CloudAuthRepository
import kotlinx.coroutines.flow.StateFlow

/**
 * Implementación del CloudAuthRepository
 * Delega a la implementación específica de cada plataforma (CloudAuthProvider)
 */
class CloudAuthRepositoryImpl(
    private val cloudAuthProvider: CloudAuthProvider
) : CloudAuthRepository {
    
    override suspend fun signIn(): Result<User> {
        return cloudAuthProvider.signIn()
    }
    
    override suspend fun signOut(): Result<Unit> {
        return cloudAuthProvider.signOut()
    }
    
    override suspend fun getCurrentUser(): User? {
        return cloudAuthProvider.getCurrentUser()
    }
    
    override suspend fun isAuthenticated(): Boolean {
        return cloudAuthProvider.isAuthenticated()
    }
    
    override val authState: StateFlow<AuthState>
        get() = cloudAuthProvider.authState
}