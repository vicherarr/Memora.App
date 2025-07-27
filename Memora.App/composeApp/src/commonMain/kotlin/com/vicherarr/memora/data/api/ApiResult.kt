package com.vicherarr.memora.data.api

import io.ktor.client.plugins.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

/**
 * Wrapper para resultados de API que puede contener éxito o error
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val exception: ApiException) : ApiResult<Nothing>()
}

/**
 * Excepciones específicas de la API
 */
sealed class ApiException(
    override val message: String,
    override val cause: Throwable? = null
) : Exception(message, cause) {
    
    data class NetworkError(
        override val message: String = "Error de conexión de red",
        override val cause: Throwable? = null
    ) : ApiException(message, cause)
    
    data class ServerError(
        val statusCode: HttpStatusCode,
        override val message: String = "Error del servidor: ${statusCode.value}",
        override val cause: Throwable? = null
    ) : ApiException(message, cause)
    
    data class ClientError(
        val statusCode: HttpStatusCode,
        override val message: String = "Error del cliente: ${statusCode.value}",
        override val cause: Throwable? = null
    ) : ApiException(message, cause)
    
    data class AuthenticationError(
        override val message: String = "Error de autenticación",
        override val cause: Throwable? = null
    ) : ApiException(message, cause)
    
    data class UnknownError(
        override val message: String = "Error desconocido",
        override val cause: Throwable? = null
    ) : ApiException(message, cause)
}

/**
 * DTO para errores del servidor (Problem Details RFC 7807)
 */
@Serializable
data class ProblemDetailsDto(
    val type: String? = null,
    val title: String? = null,
    val detail: String? = null,
    val status: Int? = null,
    val instance: String? = null
)

/**
 * Función extension para ejecutar llamadas API de forma segura
 */
suspend inline fun <T> safeApiCall(
    crossinline apiCall: suspend () -> T
): ApiResult<T> {
    return try {
        val result = apiCall()
        ApiResult.Success(result)
    } catch (e: ClientRequestException) {
        when (e.response.status) {
            HttpStatusCode.Unauthorized -> 
                ApiResult.Error(ApiException.AuthenticationError(cause = e))
            in HttpStatusCode.BadRequest..HttpStatusCode.NotFound ->
                ApiResult.Error(ApiException.ClientError(e.response.status, cause = e))
            else ->
                ApiResult.Error(ApiException.ClientError(e.response.status, cause = e))
        }
    } catch (e: ServerResponseException) {
        ApiResult.Error(
            ApiException.ServerError(
                statusCode = e.response.status,
                cause = e
            )
        )
    } catch (e: Exception) {
        when {
            e.message?.contains("timeout", ignoreCase = true) == true ||
            e.message?.contains("connection", ignoreCase = true) == true ->
                ApiResult.Error(ApiException.NetworkError(cause = e))
            else ->
                ApiResult.Error(ApiException.UnknownError(e.message ?: "Error desconocido", e))
        }
    }
}