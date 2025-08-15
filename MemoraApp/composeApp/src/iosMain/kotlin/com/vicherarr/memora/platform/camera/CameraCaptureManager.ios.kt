package com.vicherarr.memora.platform.camera

import androidx.compose.runtime.*
import com.vicherarr.memora.domain.models.MediaFile
import com.vicherarr.memora.domain.models.MediaType
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.camera.CAMERA
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.*
import platform.UIKit.*
import platform.UniformTypeIdentifiers.UTTypeImage
import platform.UniformTypeIdentifiers.UTTypeMovie
import platform.darwin.NSObject

// Global delegate holder to prevent garbage collection
private var currentCameraCaptureDelegate: UIImagePickerControllerDelegateProtocol? = null

/**
 * iOS implementation of CameraCaptureManager
 * Supports both photo and video capture from camera
 * Following MVVM, Clean Architecture and SOLID principles
 */
@Composable
actual fun rememberCameraCaptureManager(
    onResult: (MediaFile?) -> Unit
): CameraCaptureManager {
    val coroutineScope = rememberCoroutineScope()
    
    // Professional moko-permissions setup following official docs
    val factory = rememberPermissionsControllerFactory()
    val controller: PermissionsController = remember(factory) { 
        factory.createPermissionsController() 
    }
    
    // Essential for proper lifecycle handling
    BindEffect(controller)
    
    return remember {
        CameraCaptureManager(
            onLaunch = {
                // This will be called by the specific launch(mode) method
            }
        )
    }.apply {
        // Inject the launch logic with permission handling
        setLaunchLogic { mode ->
            coroutineScope.launch {
                try {
                    // Professional permission handling with moko-permissions
                    val isGranted = controller.isPermissionGranted(Permission.CAMERA)
                    if (!isGranted) {
                        controller.providePermission(Permission.CAMERA)
                    }
                    
                    if (UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera)) {
                        val picker = UIImagePickerController()
                        picker.sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
                        
                        // Configure media types based on mode
                        when (mode) {
                            CameraCaptureMode.PHOTO -> {
                                picker.mediaTypes = listOf(UTTypeImage.identifier)
                            }
                            CameraCaptureMode.VIDEO -> {
                                picker.mediaTypes = listOf(UTTypeMovie.identifier)
                            }
                        }
                        
                        // Create delegate and keep reference to prevent GC
                        val delegate = createCameraCaptureDelegate(coroutineScope, onResult)
                        currentCameraCaptureDelegate = delegate // Keep strong reference
                        picker.delegate = delegate as UINavigationControllerDelegateProtocol
                        
                        // Present camera
                        val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
                        rootViewController?.presentViewController(picker, animated = true, completion = null)
                    } else {
                        onResult(null)
                    }
                    
                } catch (deniedAlways: DeniedAlwaysException) {
                    // Permission permanently denied
                    onResult(null)
                } catch (denied: DeniedException) {
                    // Permission temporarily denied
                    onResult(null)
                } catch (e: Exception) {
                    onResult(null)
                }
            }
        }
    }
}

actual class CameraCaptureManager actual constructor(
    private val onLaunch: () -> Unit
) {
    private var launchLogic: ((CameraCaptureMode) -> Unit)? = null
    
    /**
     * Internal method to inject launch logic from the Composable
     * This follows SOLID principles by allowing dependency injection
     */
    internal fun setLaunchLogic(logic: (CameraCaptureMode) -> Unit) {
        this.launchLogic = logic
    }
    
    actual fun launch(mode: CameraCaptureMode) {
        launchLogic?.invoke(mode) ?: onLaunch()
    }
}

/**
 * Creates camera capture delegate for handling UIImagePickerController results
 * Handles both photos and videos based on the media type returned
 * Ensures callbacks are executed on the Main thread to update UI properly
 */
@OptIn(ExperimentalForeignApi::class)
private fun createCameraCaptureDelegate(
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    onResult: (MediaFile?) -> Unit
): UIImagePickerControllerDelegateProtocol {
    return object : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
        override fun imagePickerController(
            picker: UIImagePickerController,
            didFinishPickingMediaWithInfo: Map<Any?, *>
        ) {
            // Dismiss picker first
            picker.dismissViewControllerAnimated(true, completion = null)
            
            // Clean up delegate reference
            currentCameraCaptureDelegate = null
            
            // Process media in background and callback on Main thread
            coroutineScope.launch {
                try {
                    val mediaFile = when {
                        // Handle image capture
                        didFinishPickingMediaWithInfo.containsKey(UIImagePickerControllerOriginalImage) -> {
                            val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
                            
                            if (image != null) {
                                val imageData = UIImageJPEGRepresentation(image, 0.8)
                                
                                if (imageData != null) {
                                    val bytes = ByteArray(imageData.length.toInt())
                                    bytes.usePinned { pinned ->
                                        imageData.getBytes(pinned.addressOf(0), imageData.length.toULong())
                                    }
                                    
                                    MediaFile(
                                        data = bytes,
                                        fileName = "photo_${NSDate().timeIntervalSince1970.toLong()}.jpg",
                                        mimeType = "image/jpeg",
                                        type = MediaType.IMAGE,
                                        sizeBytes = bytes.size.toLong()
                                    )
                                } else null
                            } else null
                        }
                        
                        // Handle video capture
                        didFinishPickingMediaWithInfo.containsKey(UIImagePickerControllerMediaURL) -> {
                            val videoURL = didFinishPickingMediaWithInfo[UIImagePickerControllerMediaURL] as? NSURL
                            
                            if (videoURL != null) {
                                try {
                                    // Read video data from URL
                                    val videoData = NSData.dataWithContentsOfURL(videoURL)
                                    
                                    if (videoData != null) {
                                        val bytes = ByteArray(videoData.length.toInt())
                                        bytes.usePinned { pinned ->
                                            videoData.getBytes(pinned.addressOf(0), videoData.length.toULong())
                                        }
                                        
                                        MediaFile(
                                            data = bytes,
                                            fileName = "video_${NSDate().timeIntervalSince1970.toLong()}.mp4",
                                            mimeType = "video/mp4",
                                            type = MediaType.VIDEO,
                                            sizeBytes = bytes.size.toLong()
                                        )
                                    } else null
                                } catch (e: Exception) {
                                    null
                                }
                            } else null
                        }
                        
                        else -> null
                    }
                    
                    // Ensure callback is on Main thread
                    withContext(Dispatchers.Main) {
                        onResult(mediaFile)
                    }
                } catch (e: Exception) {
                    // Ensure error callback is also on Main thread
                    withContext(Dispatchers.Main) {
                        onResult(null)
                    }
                }
            }
        }
        
        override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
            picker.dismissViewControllerAnimated(true, completion = null)
            
            // Clean up delegate reference
            currentCameraCaptureDelegate = null
            
            // Ensure callback is on Main thread
            coroutineScope.launch {
                withContext(Dispatchers.Main) {
                    onResult(null)
                }
            }
        }
    }
}