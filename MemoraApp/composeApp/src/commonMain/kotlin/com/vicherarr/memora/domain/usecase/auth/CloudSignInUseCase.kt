package com.vicherarr.memora.domain.usecase.auth

import com.vicherarr.memora.domain.model.User
import com.vicherarr.memora.domain.repository.CloudAuthRepository

/**
 * Use case para iniciar sesión con servicios cloud (Google/Apple)
 */
class CloudSignInUseCase(
    private val cloudAuthRepository: CloudAuthRepository
) {
    suspend operator fun invoke(): Result<User> {
        return cloudAuthRepository.signIn()
    }
}