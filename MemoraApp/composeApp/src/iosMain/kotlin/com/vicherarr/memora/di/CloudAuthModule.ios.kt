package com.vicherarr.memora.di

import com.vicherarr.memora.data.auth.CloudAuthProvider
import com.vicherarr.memora.data.repository.CloudAuthRepositoryImpl
import com.vicherarr.memora.domain.repository.CloudAuthRepository
import com.vicherarr.memora.domain.usecase.auth.CloudSignInUseCase
import com.vicherarr.memora.domain.usecase.auth.CloudSignOutUseCase
import com.vicherarr.memora.domain.usecase.auth.GetCurrentCloudUserUseCase
import org.koin.dsl.module

/**
 * Módulo de inyección de dependencias para autenticación cloud en iOS
 * 
 * NOTA: iOS usa implementaciones mock simplificadas
 */
val cloudAuthModuleIOS = module {
    
    // CloudAuthProvider (iOS Mock)
    single<CloudAuthProvider> { 
        CloudAuthProvider() 
    }
    
    // Repository
    single<CloudAuthRepository> { 
        CloudAuthRepositoryImpl(get()) 
    }
    
    // Use Cases
    factory { CloudSignInUseCase(get()) }
    factory { CloudSignOutUseCase(get()) }
    factory { GetCurrentCloudUserUseCase(get()) }
}