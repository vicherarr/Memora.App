package com.vicherarr.memora.di

import com.vicherarr.memora.data.database.DatabaseDriverFactory
import org.koin.dsl.module

/**
 * iOS-specific database module
 * Provides DatabaseDriverFactory for iOS platform
 */
val iosDatabaseModule = module {
    single<DatabaseDriverFactory> {
        DatabaseDriverFactory()
    }
}