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
                            
                            val delegate = createCameraDelegate(onResult)
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

/**
 * iOS implementation of GalleryManager using UIImagePickerController
 */
@Composable
actual fun rememberGalleryManager(
    onResult: (MediaFile?) -> Unit
): GalleryManager {
    val coroutineScope = rememberCoroutineScope()
    
    // Professional moko-permissions setup following official docs
    val factory = rememberPermissionsControllerFactory()
    val controller: PermissionsController = remember(factory) { 
        factory.createPermissionsController() 
    }
    
    // Essential for proper lifecycle handling
    BindEffect(controller)
    
    return remember {
        GalleryManager(
            onLaunch = {
                coroutineScope.launch {
                    try {
                        // iOS doesn't require explicit permissions for Photo Library access
                        // PHPhotoLibrary access is handled automatically by the system
                        
                        if (UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary)) {
                            val picker = UIImagePickerController()
                            picker.sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
                            picker.mediaTypes = listOf(UTTypeImage.identifier)
                            
                            val delegate = createGalleryDelegate(onResult)
                            picker.delegate = delegate as UINavigationControllerDelegateProtocol
                            
                            // Present photo library
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

actual class GalleryManager actual constructor(
    private val onLaunch: () -> Unit
) {
    actual fun launch() {
        onLaunch()
    }
}

/**
 * Creates camera delegate for handling UIImagePickerController results
 */
@OptIn(ExperimentalForeignApi::class)
private fun createCameraDelegate(onResult: (MediaFile?) -> Unit): UIImagePickerControllerDelegateProtocol {
    return object : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
        override fun imagePickerController(
            picker: UIImagePickerController,
            didFinishPickingMediaWithInfo: Map<Any?, *>
        ) {
            val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
            
            if (image != null) {
                val imageData = UIImageJPEGRepresentation(image, 0.8)
                
                if (imageData != null) {
                    val bytes = ByteArray(imageData.length.toInt())
                    bytes.usePinned { pinned ->
                        imageData.getBytes(pinned.addressOf(0), imageData.length.toULong())
                    }
                    
                    val mediaFile = MediaFile(
                        data = bytes,
                        fileName = "photo_${NSDate().timeIntervalSince1970.toLong()}.jpg",
                        mimeType = "image/jpeg",
                        type = MediaType.IMAGE,
                        sizeBytes = bytes.size.toLong()
                    )
                    onResult(mediaFile)
                } else {
                    onResult(null)
                }
            } else {
                onResult(null)
            }
            
            picker.dismissViewControllerAnimated(true, completion = null)
        }
        
        override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
            onResult(null)
            picker.dismissViewControllerAnimated(true, completion = null)
        }
    }
}

/**
 * Creates gallery delegate for handling UIImagePickerController results
 */
@OptIn(ExperimentalForeignApi::class)
private fun createGalleryDelegate(onResult: (MediaFile?) -> Unit): UIImagePickerControllerDelegateProtocol {
    return object : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
        override fun imagePickerController(
            picker: UIImagePickerController,
            didFinishPickingMediaWithInfo: Map<Any?, *>
        ) {
            val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
            
            if (image != null) {
                val imageData = UIImageJPEGRepresentation(image, 0.8)
                
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
                    onResult(mediaFile)
                } else {
                    onResult(null)
                }
            } else {
                onResult(null)
            }
            
            picker.dismissViewControllerAnimated(true, completion = null)
        }
        
        override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
            onResult(null)
            picker.dismissViewControllerAnimated(true, completion = null)
        }
    }
}