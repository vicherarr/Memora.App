package com.vicherarr.memora.data.media

import com.vicherarr.memora.domain.models.MediaResult

/**
 * Platform-specific camera controller for capturing photos and videos
 */
expect class CameraController {
    /**
     * Capture a photo using the device camera
     * @return MediaResult containing the captured image or error
     */
    suspend fun capturePhoto(): MediaResult

    /**
     * Record a video using the device camera
     * @return MediaResult containing the recorded video or error
     */
    suspend fun recordVideo(): MediaResult

    /**
     * Check if camera is available on the device
     * @return true if camera is available, false otherwise
     */
    fun isCameraAvailable(): Boolean
}