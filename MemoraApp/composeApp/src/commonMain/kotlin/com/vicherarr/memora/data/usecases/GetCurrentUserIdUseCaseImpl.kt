package com.vicherarr.memora.data.usecases

import com.vicherarr.memora.domain.usecase.GetCurrentUserIdUseCase
import com.vicherarr.memora.data.auth.CloudAuthProvider
import com.vicherarr.memora.domain.model.AuthState

/**
 * Implementation: Get Current User ID Use Case
 * Clean Architecture - Data Layer
 * 
 * Implements the domain interface while handling the infrastructure
 * details of authentication state management. Centralizes the logic
 * for extracting user ID from authentication state.
 * 
 * This implementation uses email as the consistent userId format,
 * following the established pattern in the codebase for data consistency.
 */
class GetCurrentUserIdUseCaseImpl(
    private val cloudAuthProvider: CloudAuthProvider
) : GetCurrentUserIdUseCase {
    
    override suspend fun execute(): String? {
        return when (val authState = cloudAuthProvider.authState.value) {
            is AuthState.Authenticated -> authState.user.email
            else -> null
        }
    }
}