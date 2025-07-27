package com.vicherarr.memora.di

import com.vicherarr.memora.data.database.DatabaseDriverFactory
import com.vicherarr.memora.data.database.LocalDatabaseManager
import org.koin.dsl.module

/**
 * Módulo de Koin para dependencias de base de datos
 * Nota: DatabaseDriverFactory se debe configurar por plataforma antes de inicializar Koin
 */
val databaseModule = module {
    
    // LocalDatabaseManager - Singleton para toda la aplicación
    single { LocalDatabaseManager(get()) }
}