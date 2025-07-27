package com.vicherarr.memora.di

import org.koin.dsl.module

/**
 * Módulo principal que agrupa todos los módulos de la aplicación
 */
val appModule = module {
    includes(
        databaseModule,
        networkModule,
        repositoryModule,
        viewModelModule
    )
}