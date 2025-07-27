package com.vicherarr.memora.di

import com.vicherarr.memora.presentation.viewmodels.AuthViewModel
import com.vicherarr.memora.presentation.viewmodels.NotesViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Módulo de Koin para ViewModels
 */
val viewModelModule = module {
    
    // AuthViewModel - ViewModel para autenticación
    viewModel { 
        AuthViewModel(
            authRepository = get()
        )
    }
    
    // NotesViewModel - ViewModel para gestión de notas
    viewModel { 
        NotesViewModel(
            notesRepository = get()
        )
    }
}