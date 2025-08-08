package com.vicherarr.memora

import android.app.Application
import com.vicherarr.memora.di.initKoin
import com.vicherarr.memora.di.androidDatabaseModule
import com.vicherarr.memora.di.androidMediaModule
import org.koin.android.ext.koin.androidContext

/**
 * Android Application class for Koin initialization
 */
class MemoraApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Koin using the central initializer
        initKoin {
            androidContext(this@MemoraApplication)
            modules(androidDatabaseModule, androidMediaModule)
        }
    }
}