package com.vicherarr.memora

import android.app.Application
import com.vicherarr.memora.di.appModule
import com.vicherarr.memora.di.androidDatabaseModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * Clase Application principal para inicializar Koin
 */
class MemoraApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Inicializar Koin
        startKoin {
            // Log para debug en desarrollo
            androidLogger(Level.DEBUG)
            
            // Contexto de Android
            androidContext(this@MemoraApplication)
            
            // Módulos de la aplicación
            modules(appModule, androidDatabaseModule)
        }
    }
}