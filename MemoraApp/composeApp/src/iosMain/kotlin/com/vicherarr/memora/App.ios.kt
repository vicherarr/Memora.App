package com.vicherarr.memora

import com.vicherarr.memora.di.iosDatabaseModule
import org.koin.core.module.Module

/**
 * iOS implementation - provides iOS-specific database module
 */
actual fun getPlatformDatabaseModule(): Module = iosDatabaseModule