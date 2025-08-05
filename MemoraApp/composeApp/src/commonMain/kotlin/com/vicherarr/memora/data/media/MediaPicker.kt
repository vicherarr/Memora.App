package com.vicherarr.memora.data.media

import com.vicherarr.memora.domain.models.MediaResult

/**
 * Platform-specific media picker for selecting photos and videos from gallery
 */
expect class MediaPicker {
    /**
     * Pick an image from the device gallery
     * @return MediaResult containing the selected image or error
     */
    suspend fun pickImage(): MediaResult

    /**
     * Pick a video from the device gallery
     * @return MediaResult containing the selected video or error
     */
    suspend fun pickVideo(): MediaResult

    /**
     * Pick multiple images from the device gallery
     * @param maxSelection Maximum number of images to select
     * @return List of MediaResult containing selected images
     */
    suspend fun pickMultipleImages(maxSelection: Int = 5): List<MediaResult>
}