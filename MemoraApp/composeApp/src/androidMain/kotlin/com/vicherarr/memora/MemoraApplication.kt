package com.vicherarr.memora

import android.app.Application
import com.vicherarr.memora.di.appModule
import com.vicherarr.memora.di.databaseModule
import com.vicherarr.memora.di.androidDatabaseModule
import com.vicherarr.memora.di.androidMediaModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/**
 * Android Application class for Koin initialization
 */
class MemoraApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Koin with androidContext
        startKoin {
            androidContext(this@MemoraApplication)
            modules(
                appModule,
                databaseModule,
                androidDatabaseModule,
                androidMediaModule
            )
        }
    }
}