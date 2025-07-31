package com.vicherarr.memora.di

import com.vicherarr.memora.presentation.viewmodels.AuthViewModel
import com.vicherarr.memora.presentation.viewmodels.NotesViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for ViewModels
 */
val viewModelModule = module {
    viewModel { AuthViewModel() }
    viewModel { NotesViewModel() }
}