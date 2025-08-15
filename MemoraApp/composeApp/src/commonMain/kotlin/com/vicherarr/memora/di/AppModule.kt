package com.vicherarr.memora.di

import org.koin.dsl.module

/**
 * Main Koin module combining all app modules
 */
val appModule = module {
    includes(
        databaseModule,
        mapperModule,
        networkModule,
        repositoryModule,
        useCaseModule,
        smartSyncModule,
        viewModelModule,
        cloudAuthViewModelModule
    )
}