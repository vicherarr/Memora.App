package com.vicherarr.memora.di

import com.vicherarr.memora.domain.factories.IosMediaFactory
import com.vicherarr.memora.domain.factories.MediaFactory
import org.koin.dsl.module

/**
 * iOS-specific media module
 * Provides MediaFactory for creating platform-specific media components
 */
val iosMediaModule = module {
    single<MediaFactory> {
        IosMediaFactory()
    }
}