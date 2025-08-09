package com.vicherarr.memora.di

import com.vicherarr.memora.presentation.viewmodel.CloudLoginViewModel
import org.koin.dsl.module

/**
 * Módulo común de inyección de dependencias para ViewModels de autenticación cloud
 */
val cloudAuthViewModelModule = module {
    
    factory { 
        CloudLoginViewModel(
            signInUseCase = get(),
            signOutUseCase = get(),
            getCurrentUserUseCase = get()
        ) 
    }
}