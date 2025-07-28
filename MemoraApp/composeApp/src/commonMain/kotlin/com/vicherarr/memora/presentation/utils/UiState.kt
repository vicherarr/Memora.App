package com.vicherarr.memora.presentation.utils

/**
 * Interfaz base para todos los estados de UI
 */
interface UiState

/**
 * Estados comunes para operaciones asíncronas
 */
sealed class AsyncUiState<out T> : UiState {
    object Idle : AsyncUiState<Nothing>()
    object Loading : AsyncUiState<Nothing>()
    data class Success<T>(val data: T) : AsyncUiState<T>()
    data class Error(val exception: Throwable, val message: String = exception.message ?: "Error desconocido") : AsyncUiState<Nothing>()
}

/**
 * Estado para listas con funcionalidad de búsqueda y filtros
 */
data class ListUiState<T>(
    val items: List<T> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val hasMoreItems: Boolean = true
) : UiState

/**
 * Estado para formularios
 */
data class FormUiState(
    val isLoading: Boolean = false,
    val isValid: Boolean = false,
    val errors: Map<String, String> = emptyMap(),
    val successMessage: String? = null,
    val errorMessage: String? = null
) : UiState

/**
 * Estados simples para operaciones básicas
 */
sealed class SimpleUiState {
    object Loading : SimpleUiState()
    object Success : SimpleUiState()
    data class Error(val message: String) : SimpleUiState()
}