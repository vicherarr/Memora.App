package com.vicherarr.memora.di

import com.vicherarr.memora.domain.platform.FileManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Android-specific Koin module for platform dependencies.
 *
 * This module provides an instance of [FileManager] configured with the
 * Android application context.
 */
actual fun platformModule(): Module = module {
    single { FileManager(androidContext()) }
}