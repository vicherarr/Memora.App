package com.vicherarr.memora.data.media

import com.vicherarr.memora.domain.models.MediaResult
import platform.AVFoundation.*
import platform.Foundation.*
import platform.UIKit.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * iOS implementation of CameraController using AVFoundation
 */
actual class CameraController {
    
    actual suspend fun capturePhoto(): MediaResult = suspendCancellableCoroutine { continuation ->
        // For now, return a placeholder implementation
        // Full iOS implementation would use AVCaptureSession, AVCapturePhotoOutput, etc.
        continuation.resume(
            MediaResult.Error("iOS Camera implementation not yet available - coming in next iteration")
        )
    }
    
    actual suspend fun recordVideo(): MediaResult = suspendCancellableCoroutine { continuation ->
        // For now, return a placeholder implementation
        // Full iOS implementation would use AVCaptureMovieFileOutput
        continuation.resume(
            MediaResult.Error("iOS Video recording implementation not yet available - coming in next iteration")
        )
    }
    
    actual fun isCameraAvailable(): Boolean {
        // Check if camera is available on iOS device
        return UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera)
    }
}