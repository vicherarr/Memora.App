package com.vicherarr.memora.di

import com.vicherarr.memora.presentation.viewmodels.AuthViewModel
import com.vicherarr.memora.presentation.viewmodels.NotesViewModel
import com.vicherarr.memora.presentation.viewmodels.NotesListViewModel
import com.vicherarr.memora.presentation.viewmodels.NoteDetailViewModel
import com.vicherarr.memora.presentation.viewmodels.NoteEditViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Módulo de Koin para ViewModels
 * Aplicando principio SRP - ViewModels específicos para cada responsabilidad
 */
val viewModelModule = module {
    
    // AuthViewModel - ViewModel para autenticación
    viewModel<AuthViewModel> { 
        AuthViewModel(
            authRepository = get()
        )
    }
    
    // NotesViewModel - ViewModel legado (mantener temporalmente para compatibilidad)
    viewModel<NotesViewModel> { 
        NotesViewModel(
            notesRepository = get()
        )
    }
    
    // NUEVOS ViewModels con responsabilidades específicas (SRP)
    
    // NotesListViewModel - Solo para lista de notas
    viewModel<NotesListViewModel> { 
        NotesListViewModel(
            notesRepository = get()
        )
    }
    
    // NoteDetailViewModel - Solo para detalle de nota
    viewModel<NoteDetailViewModel> { 
        NoteDetailViewModel(
            notesRepository = get()
        )
    }
    
    // NoteEditViewModel - Solo para edición/creación de notas
    viewModel<NoteEditViewModel> { 
        NoteEditViewModel(
            notesRepository = get()
        )
    }
}