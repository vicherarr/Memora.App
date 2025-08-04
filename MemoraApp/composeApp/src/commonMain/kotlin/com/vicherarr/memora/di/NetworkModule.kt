package com.vicherarr.memora.di

import com.vicherarr.memora.data.api.AuthApi
import com.vicherarr.memora.data.api.NotesApi
import com.vicherarr.memora.data.network.KtorFitClient
import org.koin.dsl.module

/**
 * Koin module for network APIs and HTTP client
 */
val networkModule = module {
    
    // API interfaces using KtorFitClient factory methods
    single<AuthApi> {
        KtorFitClient.getAuthApi()
    }
    
    single<NotesApi> {
        KtorFitClient.getNotesApi()
    }
}