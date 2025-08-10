package com.vicherarr.memora.di

import com.vicherarr.memora.presentation.viewmodels.CloudLoginViewModel
import com.vicherarr.memora.presentation.viewmodels.SyncViewModel
import org.koin.dsl.module

/**
 * Módulo común de inyección de dependencias para ViewModels de autenticación cloud y sync
 */
val cloudAuthViewModelModule = module {
    
    factory { 
        CloudLoginViewModel(
            cloudAuthProvider = get()
        ) 
    }
    
    factory { 
        SyncViewModel(
            syncEngine = get(),
            attachmentSyncEngine = get(),
            cloudAuthProvider = get(),
            notesRepository = get<com.vicherarr.memora.domain.repository.NotesRepository>()
        )
    }
}