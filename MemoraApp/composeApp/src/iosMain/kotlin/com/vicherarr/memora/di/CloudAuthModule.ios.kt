package com.vicherarr.memora.di

import com.vicherarr.memora.data.auth.CloudAuthProvider
import com.vicherarr.memora.data.repository.CloudAuthRepositoryImpl
import com.vicherarr.memora.domain.repository.CloudAuthRepository
import com.vicherarr.memora.domain.usecase.auth.CloudSignInUseCase
import com.vicherarr.memora.domain.usecase.auth.CloudSignOutUseCase
import com.vicherarr.memora.domain.usecase.auth.GetCurrentCloudUserUseCase
import com.vicherarr.memora.sync.AttachmentSyncEngine
import com.vicherarr.memora.sync.AttachmentSyncRepository
import com.vicherarr.memora.sync.CloudStorageProvider
import com.vicherarr.memora.sync.DatabaseMerger
import com.vicherarr.memora.sync.DatabaseSyncService
import com.vicherarr.memora.sync.HashCalculator
import com.vicherarr.memora.sync.iCloudStorageProvider
import com.vicherarr.memora.sync.SyncEngine
import com.vicherarr.memora.data.database.AttachmentsDao
import com.vicherarr.memora.domain.platform.FileManager
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
    
    // iCloud Storage Provider (Mock)
    single<CloudStorageProvider> { 
        iCloudStorageProvider() 
    }
    
    // Database Merger
    single { DatabaseMerger(deletionsDao = get()) }
    
    // Database Sync Service
    single { DatabaseSyncService(get()) }
    
    // Sync Engine
    single { SyncEngine(get(), get(), get(), get()) }
    
    // Attachment Sync Components (iOS Mock)
    single<HashCalculator> { 
        com.vicherarr.memora.sync.HashCalculator() 
    }
    
    single<AttachmentSyncRepository> { 
        com.vicherarr.memora.sync.AttachmentSyncRepositoryFactory.create() 
    }
    
    single { 
        AttachmentSyncEngine(
            attachmentsDao = get<AttachmentsDao>(),
            fileManager = get<FileManager>(),
            attachmentSyncRepository = get<AttachmentSyncRepository>(),
            hashCalculator = get<HashCalculator>()
        ) 
    }
}