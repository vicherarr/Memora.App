package com.vicherarr.memora.platform.camera

import androidx.compose.runtime.Composable
import com.vicherarr.memora.domain.models.MediaFile

/**
 * Camera capture mode for selecting what type of media to capture
 */
enum class CameraCaptureMode {
    PHOTO,
    VIDEO
}

/**
 * Multiplatform Camera Capture Manager using expect/actual pattern
 * Allows capturing both photos and videos from camera
 * Following Clean Architecture and SOLID principles
 */
@Composable
expect fun rememberCameraCaptureManager(
    onResult: (MediaFile?) -> Unit
): CameraCaptureManager

expect class CameraCaptureManager(
    onLaunch: () -> Unit
) {
    /**
     * Launch camera with specified capture mode
     * @param mode CameraCaptureMode.PHOTO or CameraCaptureMode.VIDEO
     */
    fun launch(mode: CameraCaptureMode)
}