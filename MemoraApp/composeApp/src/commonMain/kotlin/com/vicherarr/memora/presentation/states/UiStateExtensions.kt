package com.vicherarr.memora.presentation.states

/**
 * Extension functions for UiState updates
 * Follows Open/Closed Principle - adds functionality without modifying existing classes
 * Centralizes state update logic following DRY principle
 */

// Login State Extensions
fun LoginUiState.withLoading(): LoginUiState = 
    copy(operationState = operationState.copy(loadingState = LoadingState.Loading))

fun LoginUiState.withError(message: String): LoginUiState = 
    copy(operationState = operationState.copy(loadingState = LoadingState.Error(message)))

fun LoginUiState.withSuccess(): LoginUiState = 
    copy(operationState = operationState.copy(loadingState = LoadingState.Success), isLoggedIn = true)

fun LoginUiState.clearState(): LoginUiState = 
    copy(operationState = OperationState())

// Register State Extensions
fun RegisterUiState.withLoading(): RegisterUiState = 
    copy(operationState = operationState.copy(loadingState = LoadingState.Loading))

fun RegisterUiState.withError(message: String): RegisterUiState = 
    copy(operationState = operationState.copy(loadingState = LoadingState.Error(message)))

fun RegisterUiState.withSuccess(): RegisterUiState = 
    copy(operationState = operationState.copy(loadingState = LoadingState.Success), isRegistered = true)

fun RegisterUiState.clearState(): RegisterUiState = 
    copy(operationState = OperationState())

// Create Note State Extensions
fun CreateNoteUiState.withLoading(): CreateNoteUiState = 
    copy(operationState = operationState.copy(loadingState = LoadingState.Loading))

fun CreateNoteUiState.withError(message: String): CreateNoteUiState = 
    copy(operationState = operationState.copy(loadingState = LoadingState.Error(message)))

fun CreateNoteUiState.withSuccess(): CreateNoteUiState = 
    copy(operationState = operationState.copy(loadingState = LoadingState.Success), isNoteSaved = true)

fun CreateNoteUiState.clearState(): CreateNoteUiState = 
    copy(operationState = OperationState())