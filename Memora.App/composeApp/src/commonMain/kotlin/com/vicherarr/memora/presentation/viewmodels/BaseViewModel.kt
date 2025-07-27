package com.vicherarr.memora.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vicherarr.memora.presentation.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Base ViewModel que proporciona funcionalidad común para todos los ViewModels
 * Incluye manejo de estados de carga, error y datos
 */
abstract class BaseViewModel<TState : UiState>(
    initialState: TState
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<TState> = _uiState.asStateFlow()

    protected fun updateState(update: (TState) -> TState) {
        _uiState.value = update(_uiState.value)
    }

    protected fun launchSafe(
        onError: (Throwable) -> Unit = ::handleError,
        block: suspend () -> Unit
    ) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: Throwable) {
                onError(e)
            }
        }
    }

    protected open fun handleError(error: Throwable) {
        // Implementación base del manejo de errores
        // Los ViewModels específicos pueden sobrescribir este método
    }
}