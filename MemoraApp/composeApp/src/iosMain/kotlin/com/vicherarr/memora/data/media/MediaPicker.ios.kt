package com.vicherarr.memora.data.media

import com.vicherarr.memora.domain.models.MediaResult
import platform.UIKit.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * iOS implementation of MediaPicker using UIImagePickerController
 */
actual class MediaPicker {
    
    actual suspend fun pickImage(): MediaResult = suspendCancellableCoroutine { continuation ->
        // For now, return a placeholder implementation
        // Full iOS implementation would use UIImagePickerController or PHPickerViewController
        continuation.resume(
            MediaResult.Error("iOS Image picker implementation not yet available - coming in next iteration")
        )
    }
    
    actual suspend fun pickVideo(): MediaResult = suspendCancellableCoroutine { continuation ->
        // For now, return a placeholder implementation
        // Full iOS implementation would use UIImagePickerController for video selection
        continuation.resume(
            MediaResult.Error("iOS Video picker implementation not yet available - coming in next iteration")
        )
    }
    
    actual suspend fun pickMultipleImages(maxSelection: Int): List<MediaResult> {
        // For now, return a placeholder implementation
        // Full iOS implementation would use PHPickerViewController with multiple selection
        return listOf(
            MediaResult.Error("iOS Multiple image picker implementation not yet available - coming in next iteration")
        )
    }
}