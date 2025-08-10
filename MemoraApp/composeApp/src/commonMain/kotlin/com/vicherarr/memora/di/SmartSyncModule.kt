package com.vicherarr.memora.di

import com.vicherarr.memora.data.database.NotesDao
import com.vicherarr.memora.data.database.AttachmentsDao
import com.vicherarr.memora.data.database.SyncMetadataDao
import com.vicherarr.memora.data.repository.SyncMetadataRepositoryImpl
import com.vicherarr.memora.domain.repository.SyncMetadataRepository
import com.vicherarr.memora.domain.usecase.IncrementalSyncUseCase
import com.vicherarr.memora.sync.CloudStorageProvider
import com.vicherarr.memora.sync.FingerprintGenerator
import com.vicherarr.memora.sync.GoogleDriveMetadataManager
import com.vicherarr.memora.sync.SyncEngine
import org.koin.dsl.module

/**
 * Módulo de inyección de dependencias para Smart Sync
 * 
 * Estructura correcta sin referencias circulares:
 * - SyncEngine (tradicional, sin conocer Smart Sync)
 * - IncrementalSyncUseCase (usa SyncEngine para sync completo)
 * - SyncViewModel (coordina entre ambos)
 */
val smartSyncModule = module {
    
    // ========== SMART SYNC DEPENDENCIES ==========
    
    // Fingerprint generator
    single { 
        FingerprintGenerator(
            notesDao = get<NotesDao>(),
            attachmentsDao = get<AttachmentsDao>()
        )
    }
    
    // Google Drive metadata manager
    single { GoogleDriveMetadataManager() }
    
    // Sync metadata repository
    single<SyncMetadataRepository> { 
        SyncMetadataRepositoryImpl(
            syncMetadataDao = get<SyncMetadataDao>(),
            cloudStorageProvider = get<CloudStorageProvider>(),
            fingerprintGenerator = get<FingerprintGenerator>(),
            metadataManager = get<GoogleDriveMetadataManager>()
        )
    }
    
    // Incremental sync use case (sin referencia circular)
    factory { 
        IncrementalSyncUseCase(
            syncMetadataRepository = get<SyncMetadataRepository>(),
            syncEngine = get<SyncEngine>()
        )
    }
}