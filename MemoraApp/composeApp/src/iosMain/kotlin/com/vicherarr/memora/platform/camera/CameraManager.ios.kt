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
import kotlinx.cinterop.refTo
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.*
import platform.UIKit.*
import platform.UniformTypeIdentifiers.UTTypeImage
import platform.darwin.NSObject

/**
 * iOS implementation of CameraManager using UIImagePickerController and moko-permissions
 * Professional implementation following moko-permissions best practices
 */
@Composable
actual fun rememberCameraManager(
    onResult: (MediaFile?) -> Unit
): CameraManager {
    val coroutineScope = rememberCoroutineScope()
    
    // Professional moko-permissions setup following official docs
    val factory = rememberPermissionsControllerFactory()
    val controller: PermissionsController = remember(factory) { 
        factory.createPermissionsController() 
    }
    
    // Essential for proper lifecycle handling
    BindEffect(controller)
    
    return remember {
        CameraManager(
            onLaunch = {
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
                            picker.mediaTypes = listOf(UTTypeImage.identifier)
                            
                            val delegate = createCameraDelegate(coroutineScope, onResult)
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
        )
    }
}

actual class CameraManager actual constructor(
    private val onLaunch: () -> Unit
) {
    actual fun launch() {
        onLaunch()
    }
}

// Global delegate holder to prevent garbage collection
private var currentGalleryDelegate: UIImagePickerControllerDelegateProtocol? = null

/**
 * iOS implementation of GalleryManager using UIImagePickerController
 * Simplified without moko-permissions since iOS Photo Library doesn't require explicit permissions
 */
@Composable
actual fun rememberGalleryManager(
    onResult: (MediaFile?) -> Unit
): GalleryManager {
    return remember {
        GalleryManager(
            onLaunch = {
                println("iOS GalleryManager: onLaunch called")
                
                // Direct UIKit integration without coroutines complications
                if (UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary)) {
                    println("iOS GalleryManager: Photo library is available")
                    
                    val picker = UIImagePickerController()
                    picker.sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
                    picker.mediaTypes = listOf(UTTypeImage.identifier)
                    
                    // Create delegate and keep reference to prevent GC
                    val delegate = createSimpleGalleryDelegate(onResult)
                    currentGalleryDelegate = delegate // Keep strong reference
                    picker.delegate = delegate as UINavigationControllerDelegateProtocol
                    
                    println("iOS GalleryManager: Delegate set, about to present picker")
                    
                    // Present photo library on main thread
                    val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
                    if (rootViewController != null) {
                        println("iOS GalleryManager: Root view controller found, presenting picker")
                        rootViewController.presentViewController(picker, animated = true, completion = null)
                    } else {
                        println("iOS GalleryManager: ERROR - Root view controller is null")
                        onResult(null)
                    }
                } else {
                    println("iOS GalleryManager: ERROR - Photo library not available")
                    onResult(null)
                }
            }
        )
    }
}

actual class GalleryManager actual constructor(
    private val onLaunch: () -> Unit
) {
    actual fun launch() {
        onLaunch()
    }
}

/**
 * Creates camera delegate for handling UIImagePickerController results
 * Ensures callbacks are executed on the Main thread to update UI properly
 */
@OptIn(ExperimentalForeignApi::class)
private fun createCameraDelegate(
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
            
            // Process image in background and callback on Main thread
            coroutineScope.launch {
                try {
                    val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
                    
                    val mediaFile = if (image != null) {
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
            
            // Ensure callback is on Main thread
            coroutineScope.launch {
                withContext(Dispatchers.Main) {
                    onResult(null)
                }
            }
        }
    }
}

/**
 * Creates simplified gallery delegate for handling UIImagePickerController results
 * Direct callback without coroutines to avoid threading issues
 */
@OptIn(ExperimentalForeignApi::class)
private fun createSimpleGalleryDelegate(
    onResult: (MediaFile?) -> Unit
): UIImagePickerControllerDelegateProtocol {
    return object : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
        override fun imagePickerController(
            picker: UIImagePickerController,
            didFinishPickingMediaWithInfo: Map<Any?, *>
        ) {
            println("iOS Gallery Delegate: imagePickerController didFinishPickingMediaWithInfo called")
            
            // Process image immediately (no dismissal delay)
            val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
            println("iOS Gallery Delegate: image extracted: ${image != null}")
            
            val mediaFile = if (image != null) {
                val imageData = UIImageJPEGRepresentation(image, 0.8)
                println("iOS Gallery Delegate: imageData created: ${imageData != null}")
                
                if (imageData != null) {
                    val bytes = ByteArray(imageData.length.toInt())
                    bytes.usePinned { pinned ->
                        imageData.getBytes(pinned.addressOf(0), imageData.length.toULong())
                    }
                    
                    val mediaFile = MediaFile(
                        data = bytes,
                        fileName = "image_${NSDate().timeIntervalSince1970.toLong()}.jpg",
                        mimeType = "image/jpeg",
                        type = MediaType.IMAGE,
                        sizeBytes = bytes.size.toLong()
                    )
                    println("iOS Gallery Delegate: MediaFile created, size: ${bytes.size}")
                    mediaFile
                } else null
            } else null
            
            println("iOS Gallery Delegate: About to call onResult with mediaFile: ${mediaFile != null}")
            
            // Dismiss picker AFTER processing
            picker.dismissViewControllerAnimated(true, completion = null)
            
            // Clean up delegate reference
            currentGalleryDelegate = null
            
            // Direct callback - we're already on main thread
            onResult(mediaFile)
            println("iOS Gallery Delegate: onResult called")
        }
        
        override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
            println("iOS Gallery Delegate: imagePickerControllerDidCancel called")
            picker.dismissViewControllerAnimated(true, completion = null)
            
            // Clean up delegate reference
            currentGalleryDelegate = null
            
            onResult(null)
            println("iOS Gallery Delegate: onResult(null) called for cancel")
        }
    }
}