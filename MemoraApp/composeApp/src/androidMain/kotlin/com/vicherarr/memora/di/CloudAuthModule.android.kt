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
import com.vicherarr.memora.sync.GoogleDriveStorageProvider
import com.vicherarr.memora.sync.GoogleDriveAttachmentSyncRepository
import com.vicherarr.memora.sync.LazyGoogleDriveAttachmentSyncRepository
import com.vicherarr.memora.sync.HashCalculator
import com.vicherarr.memora.sync.SyncEngine
import com.vicherarr.memora.data.database.AttachmentsDao
import com.vicherarr.memora.domain.platform.FileManager
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
    
    // Database Merger
    single { DatabaseMerger() }
    
    // Database Sync Service
    single { DatabaseSyncService(get()) }
    
    // Sync Engine
    single { SyncEngine(get(), get(), get(), get()) }
    
    // Attachment Sync Components
    single<HashCalculator> { 
        HashCalculator() 
    }
    
    single<AttachmentSyncRepository> { 
        LazyGoogleDriveAttachmentSyncRepository(
            context = androidContext(),
            cloudStorageProvider = get<CloudStorageProvider>() as GoogleDriveStorageProvider
        ) 
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