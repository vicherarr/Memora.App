package com.vicherarr.memora.di

import com.vicherarr.memora.domain.factories.AndroidMediaFactory
import com.vicherarr.memora.domain.factories.MediaFactory
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Android-specific media module
 * Provides MediaFactory for creating platform-specific media components
 */
val androidMediaModule = module {
    single<MediaFactory> {
        AndroidMediaFactory(context = androidContext())
    }
}