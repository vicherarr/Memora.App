package com.vicherarr.memora.data.api

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Factory para crear y configurar el cliente HTTP Ktor
 * Incluye interceptors para autenticación, logging y manejo de errores
 */
object HttpClientFactory {
    
    fun create(
        baseUrl: String = "https://localhost:7241/api/",
        enableLogging: Boolean = true,
        tokenProvider: suspend () -> String? = { null }
    ): HttpClient {
        return HttpClient {
            // Configuración base
            defaultRequest {
                url(baseUrl)
            }
            
            // Serialización JSON
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            
            // Logging (solo en debug)
            if (enableLogging) {
                install(Logging) {
                    logger = Logger.DEFAULT
                    level = LogLevel.INFO
                    filter { request ->
                        request.url.host.contains("localhost") ||
                        request.url.host.contains("127.0.0.1")
                    }
                }
            }
            
            // Autenticación JWT
            install(Auth) {
                bearer {
                    loadTokens {
                        val token = tokenProvider()
                        token?.let {
                            BearerTokens(accessToken = it, refreshToken = "")
                        }
                    }
                    
                    refreshTokens {
                        // Aquí se implementará la lógica de refresh token si es necesario
                        null
                    }
                    
                    sendWithoutRequest { request ->
                        // Enviar token solo para endpoints que lo requieren
                        !request.url.pathSegments.joinToString("/").contains("autenticacion")
                    }
                }
            }
            
            // Configuración de timeouts
            engine {
                // Configuración básica de timeouts
                // Específica por plataforma se puede configurar más adelante
            }
        }
    }
}