package com.vicherarr.memora.di

import com.vicherarr.memora.data.auth.CloudAuthProvider
import com.vicherarr.memora.data.repository.CloudAuthRepositoryImpl
import com.vicherarr.memora.domain.repository.CloudAuthRepository
import com.vicherarr.memora.domain.usecase.auth.CloudSignInUseCase
import com.vicherarr.memora.domain.usecase.auth.CloudSignOutUseCase
import com.vicherarr.memora.domain.usecase.auth.GetCurrentCloudUserUseCase
import com.vicherarr.memora.sync.CloudStorageProvider
import com.vicherarr.memora.sync.GoogleDriveStorageProvider
import com.vicherarr.memora.sync.SyncEngine
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Módulo de inyección de dependencias para autenticación cloud en Android
 * 
 * IMPORTANTE: 
 * - ActivityResultManager será inicializado desde MainActivity usando CloudAuthProvider.initializeActivityManager()
 * - CloudAuthProvider usa una referencia estática al ActivityResultManager
 */
val cloudAuthModuleAndroid = module {
    
    // CloudAuthProvider (Android-specific)
    single<CloudAuthProvider> { 
        CloudAuthProvider(androidContext()) 
    }
    
    // Repository
    single<CloudAuthRepository> { 
        CloudAuthRepositoryImpl(get()) 
    }
    
    // Use Cases
    factory { CloudSignInUseCase(get()) }
    factory { CloudSignOutUseCase(get()) }
    factory { GetCurrentCloudUserUseCase(get()) }
    
    // Google Drive Storage Provider
    single<CloudStorageProvider> { 
        GoogleDriveStorageProvider(
            context = androidContext(),
            cloudAuthProvider = get()
        ) 
    }
    
    // Sync Engine
    single { SyncEngine(get()) }
}