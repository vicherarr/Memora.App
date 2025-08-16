package com.vicherarr.memora.di

import com.vicherarr.memora.data.repository.UserRepositoryImpl
import com.vicherarr.memora.domain.repository.UserRepository
import com.vicherarr.memora.domain.usecase.ExitAppUseCase
import com.vicherarr.memora.domain.usecase.createExitAppUseCase
import com.vicherarr.memora.presentation.viewmodels.LoginViewModel
import com.vicherarr.memora.presentation.viewmodels.RegisterViewModel
import com.vicherarr.memora.presentation.viewmodels.CreateNoteViewModel
import com.vicherarr.memora.presentation.viewmodels.NotesViewModel
import com.vicherarr.memora.presentation.viewmodels.MediaViewModel
import com.vicherarr.memora.presentation.viewmodels.NoteDetailViewModel
import com.vicherarr.memora.presentation.viewmodels.ProfileViewModel
import com.vicherarr.memora.presentation.viewmodels.SplashViewModel
import com.vicherarr.memora.domain.usecase.GetCategoriesByUserUseCase
import com.vicherarr.memora.domain.usecase.CreateCategoryUseCase
import com.vicherarr.memora.domain.usecase.GetCurrentUserIdUseCase
import com.vicherarr.memora.data.usecases.GetCurrentUserIdUseCaseImpl
import org.koin.dsl.module

/**
 * Koin module for ViewModels following SOLID principles
 * Single Responsibility: Each ViewModel has one specific responsibility
 * Dependency Inversion: All ViewModels depend on abstractions (interfaces)
 */
val viewModelModule = module {
    
    // Repository Layer - Following Dependency Inversion Principle
    single<UserRepository> { UserRepositoryImpl(get(), get()) } // NotesRepository + CloudAuthProvider dependencies
    
    // Use Cases Layer - Following Single Responsibility Principle
    factory<ExitAppUseCase> { createExitAppUseCase() } // Platform-specific implementation
    factory { GetCategoriesByUserUseCase(get(), get()) } // CategoriesDao + CategoryDomainMapper
    factory { CreateCategoryUseCase(get(), get()) } // CategoriesDao + CategoryDomainMapper
    single<GetCurrentUserIdUseCase> { GetCurrentUserIdUseCaseImpl(get()) } // CloudAuthProvider - Singleton for consistency
    
    // ViewModel Layer - Following Single Responsibility Principle
    factory { SplashViewModel(get()) } // CloudAuthProvider dependency
    factory { LoginViewModel(get(), get()) } // AuthRepository + ValidationService
    factory { RegisterViewModel(get(), get()) } // AuthRepository + ValidationService
    single { MediaViewModel() } // Singleton - shared across CreateNoteViewModel and CreateNoteScreen
    factory { CreateNoteViewModel(get(), get(), get(), get(), get()) } // CreateNoteUseCase + GetCategoriesByUserUseCase + CreateCategoryUseCase + GetCurrentUserIdUseCase + MediaViewModel
    factory { NotesViewModel(get(), get(), get(), get(), get(), get(), get(), get()) } // NotesRepository, GetNotesUseCase, CreateNoteUseCase, UpdateNoteUseCase, DeleteNoteUseCase, SearchNotesUseCase, GetCategoriesByUserUseCase, GetCurrentUserIdUseCase
    factory { NoteDetailViewModel(get(), get(), get(), get(), get(), get(), get(), get()) } // SearchNotesUseCase + UpdateNoteUseCase + DeleteNoteUseCase + GetCategoriesByUserUseCase + CreateCategoryUseCase + GetCategoriesByNoteIdUseCase + GetCurrentUserIdUseCase + MediaViewModel
    factory { ProfileViewModel(get(), get()) } // UserRepository + ExitAppUseCase dependencies
}