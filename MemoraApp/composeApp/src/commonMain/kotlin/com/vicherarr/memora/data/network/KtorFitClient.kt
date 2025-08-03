package com.vicherarr.memora.data.network

import com.vicherarr.memora.data.api.AuthApi
import com.vicherarr.memora.data.api.NotesApi
import com.vicherarr.memora.data.api.createAuthApi
import com.vicherarr.memora.data.api.createNotesApi
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Factory for creating KtorFit API client instances
 * Configures HTTP client with authentication, logging, and JSON serialization
 */
object KtorFitClient {
    
    private var currentToken: String? = null
    
    private val httpClient by lazy {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    prettyPrint = true
                })
            }
            
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        println("HTTP Client: $message")
                    }
                }
                level = LogLevel.INFO
            }
            
            install(Auth) {
                bearer {
                    loadTokens {
                        currentToken?.let { token ->
                            BearerTokens(accessToken = token, refreshToken = "")
                        }
                    }
                }
            }
        }
    }
    
    private val ktorfit by lazy {
        Ktorfit.Builder()
            .baseUrl(ApiConfig.API_BASE_URL)
            .httpClient(httpClient)
            .build()
    }
    
    /**
     * Update the JWT token for authenticated requests
     */
    fun setAuthToken(token: String?) {
        currentToken = token
    }
    
    /**
     * Clear the authentication token
     */
    fun clearAuthToken() {
        currentToken = null
    }
    
    /**
     * Get AuthApi instance for authentication endpoints
     */
    fun getAuthApi(): AuthApi = ktorfit.createAuthApi()
    
    /**
     * Get NotesApi instance for notes endpoints (requires authentication)
     */
    fun getNotesApi(): NotesApi = ktorfit.createNotesApi()
}