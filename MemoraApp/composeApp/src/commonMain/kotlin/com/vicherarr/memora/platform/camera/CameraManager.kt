package com.vicherarr.memora.platform.camera

import androidx.compose.runtime.Composable
import com.vicherarr.memora.domain.models.MediaFile

/**
 * Multiplatform Camera Manager using expect/actual pattern
 * Simplifies camera operations across Android and iOS platforms
 */
@Composable
expect fun rememberCameraManager(
    onResult: (MediaFile?) -> Unit
): CameraManager

expect class CameraManager(
    onLaunch: () -> Unit
) {
    fun launch()
}

/**
 * Gallery/Photo picker manager using expect/actual pattern
 */
@Composable
expect fun rememberGalleryManager(
    onResult: (MediaFile?) -> Unit
): GalleryManager

expect class GalleryManager(
    onLaunch: () -> Unit
) {
    fun launch()
}