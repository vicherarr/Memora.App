package com.vicherarr.memora.di

import com.vicherarr.memora.data.repository.AuthRepositoryImpl
import com.vicherarr.memora.data.repository.AuthRepositoryMock
import com.vicherarr.memora.data.repository.NotesRepositoryImpl
import com.vicherarr.memora.domain.repository.AuthRepository
import com.vicherarr.memora.domain.repository.NotesRepository
import org.koin.dsl.module

/**
 * Módulo de Koin para implementaciones de repositorios
 */
val repositoryModule = module {
    
    // AuthRepository - Implementación de autenticación
    // TODO: Cambiar a AuthRepositoryImpl cuando se conecte con backend real
    single<AuthRepository> { 
        AuthRepositoryMock()
    }
    
    // AuthRepository real (comentado durante desarrollo)
    // single<AuthRepository> { 
    //     AuthRepositoryImpl(
    //         apiService = get(),
    //         localDatabase = get()
    //     )
    // }
    
    // NotesRepository - Implementación de gestión de notas
    single<NotesRepository> { 
        NotesRepositoryImpl(
            apiService = get(),
            localDatabase = get()
        )
    }
}