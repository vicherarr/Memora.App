package com.vicherarr.memora.presentation.states

import com.vicherarr.memora.domain.models.MediaFile
import com.vicherarr.memora.domain.models.MediaOperationType

/**
 * Media UI State - Single Source of Truth
 * Immutable data class representing the complete state of media operations
 */
data class MediaUiState(
    val capturedMedia: MediaFile? = null,
    val selectedMedia: List<MediaFile> = emptyList(),
    val isCameraAvailable: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isOperationComplete: Boolean = false,
    val operationType: MediaOperationType = MediaOperationType.NONE
)


/**
 * Extension functions for MediaUiState transitions following the same pattern
 * as LoginUiState and other UI states in the project
 */
fun MediaUiState.withLoading(operationType: MediaOperationType = MediaOperationType.NONE): MediaUiState {
    return copy(
        isLoading = true,
        errorMessage = null,
        isOperationComplete = false,
        operationType = operationType
    )
}

fun MediaUiState.withError(message: String): MediaUiState {
    return copy(
        isLoading = false,
        errorMessage = message,
        isOperationComplete = false
    )
}

fun MediaUiState.withCapturedMedia(mediaFile: MediaFile): MediaUiState {
    return copy(
        isLoading = false,
        errorMessage = null,
        capturedMedia = mediaFile,
        isOperationComplete = true
    )
}

fun MediaUiState.withSelectedMedia(mediaFiles: List<MediaFile>): MediaUiState {
    return copy(
        isLoading = false,
        errorMessage = null,
        selectedMedia = mediaFiles,
        isOperationComplete = true
    )
}

fun MediaUiState.withCameraAvailability(available: Boolean): MediaUiState {
    return copy(isCameraAvailable = available)
}