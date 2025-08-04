package com.vicherarr.memora

import com.vicherarr.memora.di.androidDatabaseModule
import org.koin.core.module.Module

/**
 * Android implementation - provides Android-specific database module
 */
actual fun getPlatformDatabaseModule(): Module = androidDatabaseModule