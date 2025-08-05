package com.vicherarr.memora.presentation.viewmodels

import androidx.lifecycle.ViewModel
import com.vicherarr.memora.domain.models.MediaFile
import com.vicherarr.memora.domain.models.MediaOperationType
import com.vicherarr.memora.presentation.states.MediaUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Simplified MediaViewModel for managing media operations using CameraManager/GalleryManager pattern
 * Media operations are now handled directly in Compose through the managers
 */
class MediaViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(MediaUiState())
    val uiState: StateFlow<MediaUiState> = _uiState.asStateFlow()
    
    /**
     * Handle camera capture result
     */
    fun onCameraResult(mediaFile: MediaFile?) {
        if (mediaFile != null) {
            _uiState.value = _uiState.value.copy(
                capturedMedia = mediaFile,
                operationType = MediaOperationType.PHOTO_CAPTURE,
                isOperationComplete = true,
                isLoading = false,
                errorMessage = null
            )
        } else {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Failed to capture photo",
                isLoading = false,
                isOperationComplete = false
            )
        }
    }
    
    /**
     * Handle gallery selection result
     */
    fun onGalleryResult(mediaFile: MediaFile?) {
        if (mediaFile != null) {
            _uiState.value = _uiState.value.copy(
                capturedMedia = mediaFile,
                operationType = MediaOperationType.IMAGE_SELECTION,
                isOperationComplete = true,
                isLoading = false,
                errorMessage = null
            )
        } else {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Failed to select image from gallery",
                isLoading = false,
                isOperationComplete = false
            )
        }
    }
    
    /**
     * Set loading state when starting media operation
     */
    fun setLoading(isLoading: Boolean) {
        _uiState.value = _uiState.value.copy(
            isLoading = isLoading,
            errorMessage = if (isLoading) null else _uiState.value.errorMessage
        )
    }
    
    /**
     * Clear current media state
     */
    fun clearMediaState() {
        _uiState.value = MediaUiState()
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    /**
     * Add media file to selected media list (for multiple selection)
     */
    fun addToSelectedMedia(mediaFile: MediaFile) {
        val currentList = _uiState.value.selectedMedia.toMutableList()
        currentList.add(mediaFile)
        _uiState.value = _uiState.value.copy(
            selectedMedia = currentList,
            operationType = MediaOperationType.MULTIPLE_SELECTION,
            isOperationComplete = true
        )
    }
    
    /**
     * Remove media file from selected media list
     */
    fun removeFromSelectedMedia(mediaFile: MediaFile) {
        val currentList = _uiState.value.selectedMedia.toMutableList()
        currentList.remove(mediaFile)
        _uiState.value = _uiState.value.copy(selectedMedia = currentList)
    }
    
    /**
     * Clear selected media list
     */
    fun clearSelectedMedia() {
        _uiState.value = _uiState.value.copy(
            selectedMedia = emptyList(),
            operationType = MediaOperationType.NONE
        )
    }
    
    // TEMPORARY METHODS FOR BACKWARD COMPATIBILITY
    // These will be removed once screens are updated to use CameraManager/GalleryManager
    
    /**
     * @deprecated Use CameraManager directly in Compose
     */
    @Deprecated("Use CameraManager directly in Compose")
    fun capturePhoto() {
        // No-op - this functionality is now handled by CameraManager
    }
    
    /**
     * @deprecated Use CameraManager directly in Compose
     */
    @Deprecated("Use CameraManager directly in Compose")
    fun recordVideo() {
        // No-op - this functionality is now handled by CameraManager
    }
    
    /**
     * @deprecated Use GalleryManager directly in Compose
     */
    @Deprecated("Use GalleryManager directly in Compose")
    fun pickImage() {
        // No-op - this functionality is now handled by GalleryManager
    }
    
    /**
     * @deprecated Use GalleryManager directly in Compose
     */
    @Deprecated("Use GalleryManager directly in Compose")
    fun pickVideo() {
        // No-op - this functionality is now handled by GalleryManager
    }
    
    /**
     * @deprecated Use GalleryManager directly in Compose
     */
    @Deprecated("Use GalleryManager directly in Compose")
    fun pickMultipleImages(maxSelection: Int = 5) {
        // No-op - this functionality is now handled by GalleryManager
    }
}