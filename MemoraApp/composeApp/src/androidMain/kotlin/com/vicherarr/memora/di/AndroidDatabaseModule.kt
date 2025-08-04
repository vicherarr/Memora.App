package com.vicherarr.memora.di

import com.vicherarr.memora.data.database.DatabaseDriverFactory
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Android-specific database module
 * Provides DatabaseDriverFactory for Android platform
 */
val androidDatabaseModule = module {
    single<DatabaseDriverFactory> {
        DatabaseDriverFactory(androidContext())
    }
}