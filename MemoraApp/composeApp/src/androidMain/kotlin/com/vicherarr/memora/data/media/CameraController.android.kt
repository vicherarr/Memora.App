package com.vicherarr.memora.data.media

import android.content.Context
import android.content.pm.PackageManager
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.vicherarr.memora.domain.models.MediaFile
import com.vicherarr.memora.domain.models.MediaResult
import com.vicherarr.memora.domain.models.MediaType
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.resume

/**
 * Android implementation of CameraController using CameraX
 */
actual class CameraController(
    private val context: Context
) {
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    
    init {
        setupCamera()
    }
    
    private fun setupCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            // Try to bind camera if we have access to lifecycle
            tryBindCamera()
        }, ContextCompat.getMainExecutor(context))
    }
    
    private fun tryBindCamera() {
        val lifecycleOwner = com.vicherarr.memora.platform.ActivityRegistry.getCurrentActivity()
        if (lifecycleOwner != null) {
            bindCameraUseCases(lifecycleOwner)
        }
    }
    
    fun bindCameraUseCases(lifecycleOwner: LifecycleOwner) {
        val cameraProvider = cameraProvider ?: return
        
        // Image capture use case
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
        
        // Video capture use case
        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.HD))
            .build()
        videoCapture = VideoCapture.withOutput(recorder)
        
        // Camera selector (back camera)
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        
        try {
            // Unbind use cases before rebinding
            cameraProvider.unbindAll()
            
            // Bind use cases to camera
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                imageCapture,
                videoCapture
            )
        } catch (exc: Exception) {
            // Handle binding errors
        }
    }
    
    actual suspend fun capturePhoto(): MediaResult = suspendCancellableCoroutine { continuation ->
        // Try to bind camera if not already done
        if (imageCapture == null) {
            tryBindCamera()
        }
        
        val imageCapture = imageCapture ?: run {
            continuation.resume(MediaResult.Error("Camera not initialized"))
            return@suspendCancellableCoroutine
        }
        
        // Create output file
        val photoFile = File(
            context.cacheDir,
            "photo_${System.currentTimeMillis()}.jpg"
        )
        
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        
        imageCapture.takePicture(
            outputFileOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    try {
                        val mediaFile = MediaFile(
                            data = photoFile.readBytes(),
                            fileName = photoFile.name,
                            mimeType = "image/jpeg",
                            type = MediaType.IMAGE,
                            sizeBytes = photoFile.length()
                        )
                        photoFile.delete() // Clean up temp file
                        continuation.resume(MediaResult.Success(mediaFile))
                    } catch (e: Exception) {
                        continuation.resume(MediaResult.Error("Failed to read captured image: ${e.message}"))
                    }
                }
                
                override fun onError(exception: ImageCaptureException) {
                    continuation.resume(MediaResult.Error("Image capture failed: ${exception.message}"))
                }
            }
        )
    }
    
    actual suspend fun recordVideo(): MediaResult = suspendCancellableCoroutine { continuation ->
        val videoCapture = videoCapture ?: run {
            continuation.resume(MediaResult.Error("Video capture not initialized"))
            return@suspendCancellableCoroutine
        }
        
        // Create output file
        val videoFile = File(
            context.cacheDir,
            "video_${System.currentTimeMillis()}.mp4"
        )
        
        val outputFileOptions = FileOutputOptions.Builder(videoFile).build()
        
        // Start recording
        val activeRecording = videoCapture.output
            .prepareRecording(context, outputFileOptions)
            .start(cameraExecutor) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        // Recording started
                    }
                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            try {
                                val mediaFile = MediaFile(
                                    data = videoFile.readBytes(),
                                    fileName = videoFile.name,
                                    mimeType = "video/mp4",
                                    type = MediaType.VIDEO,
                                    sizeBytes = videoFile.length()
                                )
                                videoFile.delete() // Clean up temp file
                                continuation.resume(MediaResult.Success(mediaFile))
                            } catch (e: Exception) {
                                continuation.resume(MediaResult.Error("Failed to read recorded video: ${e.message}"))
                            }
                        } else {
                            continuation.resume(MediaResult.Error("Video recording failed: ${recordEvent.error}"))
                        }
                    }
                }
            }
        
        // Auto-stop recording after 30 seconds for demo purposes
        // In a real implementation, you'd provide UI controls to stop recording
        cameraExecutor.execute {
            Thread.sleep(5000) // Record for 5 seconds
            activeRecording.stop()
        }
    }
    
    actual fun isCameraAvailable(): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    }
    
    fun cleanup() {
        cameraExecutor.shutdown()
    }
}