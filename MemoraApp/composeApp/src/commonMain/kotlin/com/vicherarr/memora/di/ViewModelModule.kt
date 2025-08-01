package com.vicherarr.memora.di

import androidx.lifecycle.ViewModel
import com.vicherarr.memora.presentation.viewmodels.AuthViewModel
import com.vicherarr.memora.presentation.viewmodels.NotesViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for ViewModels
 */
val viewModelModule = module {
    viewModel<ViewModel> { AuthViewModel() }
    viewModel<ViewModel> { NotesViewModel() }
}