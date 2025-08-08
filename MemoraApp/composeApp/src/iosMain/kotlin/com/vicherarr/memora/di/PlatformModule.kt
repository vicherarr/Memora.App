package com.vicherarr.memora.di

import com.vicherarr.memora.domain.platform.FileManager
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * iOS-specific Koin module for platform dependencies.
 *
 * This module provides a singleton instance of the [FileManager] for the iOS platform.
 */
actual fun platformModule(): Module = module {
    single { FileManager() }
}