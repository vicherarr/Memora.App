package com.vicherarr.memora.domain.usecase.auth

import com.vicherarr.memora.domain.repository.CloudAuthRepository

/**
 * Use case para cerrar sesión de servicios cloud
 */
class CloudSignOutUseCase(
    private val cloudAuthRepository: CloudAuthRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return cloudAuthRepository.signOut()
    }
}