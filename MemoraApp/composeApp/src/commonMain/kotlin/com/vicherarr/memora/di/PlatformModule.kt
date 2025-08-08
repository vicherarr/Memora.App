package com.vicherarr.memora.di

import com.vicherarr.memora.domain.platform.FileManager
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Koin module that provides platform-specific dependencies.
 *
 * This `expect` function declares a Koin module that will be defined separately
 * for each platform (Android and iOS), allowing for platform-specific implementations
 * of shared interfaces like [FileManager].
 */
expect fun platformModule(): Module
