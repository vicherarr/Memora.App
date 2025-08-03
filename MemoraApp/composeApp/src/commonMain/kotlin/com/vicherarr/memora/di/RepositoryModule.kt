package com.vicherarr.memora.di

import com.vicherarr.memora.data.repository.AuthRepositoryImpl
import com.vicherarr.memora.data.repository.NotesRepositoryImpl
import com.vicherarr.memora.domain.repository.AuthRepository
import com.vicherarr.memora.domain.repository.NotesRepository
import com.vicherarr.memora.domain.validation.ValidationService
import com.vicherarr.memora.domain.validation.ValidationServiceImpl
import org.koin.dsl.module

/**
 * Koin module for Repository implementations and validation services
 */
val repositoryModule = module {
    single<AuthRepository> { AuthRepositoryImpl() }
    single<NotesRepository> { NotesRepositoryImpl() }
    single<ValidationService> { ValidationServiceImpl() }
}