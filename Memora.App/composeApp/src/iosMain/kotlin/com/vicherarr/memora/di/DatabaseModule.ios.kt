package com.vicherarr.memora.di

import com.vicherarr.memora.data.database.DatabaseDriverFactory
import org.koin.dsl.module

/**
 * Módulo específico de iOS para DatabaseDriverFactory
 */
val iosDatabaseModule = module {
    single { DatabaseDriverFactory() }
}