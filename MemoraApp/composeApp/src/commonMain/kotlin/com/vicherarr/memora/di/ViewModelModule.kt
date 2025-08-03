package com.vicherarr.memora.di

import com.vicherarr.memora.presentation.viewmodels.AuthViewModel
import com.vicherarr.memora.presentation.viewmodels.LoginViewModel
import com.vicherarr.memora.presentation.viewmodels.RegisterViewModel
import com.vicherarr.memora.presentation.viewmodels.NotesViewModel
import org.koin.dsl.module

/**
 * Koin module for ViewModels
 */
val viewModelModule = module {
    factory { AuthViewModel(get()) }
    factory { LoginViewModel(get()) }
    factory { RegisterViewModel(get()) }
    factory { NotesViewModel(get()) }
}