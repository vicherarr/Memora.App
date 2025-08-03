package com.vicherarr.memora.di

import com.vicherarr.memora.presentation.viewmodels.AuthViewModel
import com.vicherarr.memora.presentation.viewmodels.LoginViewModel
import com.vicherarr.memora.presentation.viewmodels.RegisterViewModel
import com.vicherarr.memora.presentation.viewmodels.CreateNoteViewModel
import com.vicherarr.memora.presentation.viewmodels.NotesViewModel
import org.koin.dsl.module

/**
 * Koin module for ViewModels following Dependency Inversion Principle
 */
val viewModelModule = module {
    factory { AuthViewModel(get()) }
    factory { LoginViewModel(get(), get()) } // AuthRepository + ValidationService
    factory { RegisterViewModel(get(), get()) } // AuthRepository + ValidationService
    factory { CreateNoteViewModel(get()) }
    factory { NotesViewModel(get()) }
}