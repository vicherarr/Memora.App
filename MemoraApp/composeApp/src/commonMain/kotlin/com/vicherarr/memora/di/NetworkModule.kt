package com.vicherarr.memora.di

import com.vicherarr.memora.data.api.HttpClientFactory
import com.vicherarr.memora.data.api.MemoraApiService
import io.ktor.client.*
import org.koin.dsl.module

/**
 * Módulo de Koin para dependencias de red y API
 */
val networkModule = module {
    
    // HttpClient - Configurado con factory
    single<HttpClient> { 
        HttpClientFactory.create(
            baseUrl = "http://localhost:5003/api/",
            enableLogging = true,
            tokenProvider = {
                // TODO: Implementar obtención de token desde secure storage
                null
            }
        )
    }
    
    // MemoraApiService - Servicio principal de API
    single { MemoraApiService(get()) }
}