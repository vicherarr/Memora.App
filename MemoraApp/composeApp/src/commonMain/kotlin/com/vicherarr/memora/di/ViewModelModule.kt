package com.vicherarr.memora.di

import com.vicherarr.memora.presentation.viewmodels.LoginViewModel
import com.vicherarr.memora.presentation.viewmodels.RegisterViewModel
import com.vicherarr.memora.presentation.viewmodels.CreateNoteViewModel
import com.vicherarr.memora.presentation.viewmodels.NotesViewModel
import com.vicherarr.memora.presentation.viewmodels.MediaViewModel
import org.koin.dsl.module

/**
 * Koin module for ViewModels following SOLID principles
 * Single Responsibility: Each ViewModel has one specific responsibility
 * Dependency Inversion: All ViewModels depend on abstractions (interfaces)
 */
val viewModelModule = module {
    factory { LoginViewModel(get(), get()) } // AuthRepository + ValidationService
    factory { RegisterViewModel(get(), get()) } // AuthRepository + ValidationService
    factory { CreateNoteViewModel(get()) }
    factory { NotesViewModel(get()) }
    factory { MediaViewModel() } // No dependencies - uses CameraManager/GalleryManager directly
}