package com.vicherarr.memora.di

import android.content.Context
import com.vicherarr.memora.data.database.DatabaseDriverFactory
import org.koin.dsl.module

/**
 * Módulo específico de Android para DatabaseDriverFactory
 */
val androidDatabaseModule = module {
    single { DatabaseDriverFactory(get<Context>()) }
}