package com.vicherarr.memora.domain.usecase.auth

import com.vicherarr.memora.domain.model.User
import com.vicherarr.memora.domain.repository.CloudAuthRepository

/**
 * Use case para obtener el usuario cloud actualmente autenticado
 */
class GetCurrentCloudUserUseCase(
    private val cloudAuthRepository: CloudAuthRepository
) {
    suspend operator fun invoke(): User? {
        return cloudAuthRepository.getCurrentUser()
    }
}