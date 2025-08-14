package com.vicherarr.memora.presentation.states

/**
 * Image Viewer State - Single Source of Truth for image viewer
 * Following Clean Code principles and MVVM pattern
 * Shared across ViewModels to avoid duplication (DRY principle)
 */
data class ImageViewerState(
    val isVisible: Boolean = false,
    val imageData: Any? = null,
    val imageName: String? = null
)

/**
 * Video Viewer State - Single Source of Truth for video viewer
 * Following Clean Code principles and MVVM pattern
 * Shared across ViewModels to avoid duplication (DRY principle)
 */
data class VideoViewerState(
    val isVisible: Boolean = false,
    val videoData: Any? = null,
    val videoName: String? = null
)