package com.vicherarr.memora.platform.camera

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.vicherarr.memora.domain.models.MediaFile
import com.vicherarr.memora.domain.models.MediaType
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.camera.CAMERA
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import kotlinx.coroutines.launch
import java.io.File

/**
 * Android implementation of CameraManager using moko-permissions and ActivityResultContracts.TakePicture
 * Professional implementation following moko-permissions best practices
 */
@Composable
actual fun rememberCameraManager(
    onResult: (MediaFile?) -> Unit
): CameraManager {
    val context = LocalContext.current
    val contentResolver: ContentResolver = context.contentResolver
    val coroutineScope = rememberCoroutineScope()
    
    // Professional moko-permissions setup following official docs
    val factory = rememberPermissionsControllerFactory()
    val controller: PermissionsController = remember(factory) { 
        factory.createPermissionsController() 
    }
    
    // Essential for proper lifecycle handling on Android
    BindEffect(controller)
    
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                val uri = tempPhotoUri
                if (uri != null) {
                    try {
                        val inputStream = contentResolver.openInputStream(uri)
                        val bytes = inputStream?.readBytes()
                        inputStream?.close()
                        
                        if (bytes != null) {
                            val mediaFile = MediaFile(
                                data = bytes,
                                fileName = "photo_${System.currentTimeMillis()}.jpg",
                                mimeType = "image/jpeg",
                                type = MediaType.IMAGE,
                                sizeBytes = bytes.size.toLong()
                            )
                            onResult(mediaFile)
                        } else {
                            onResult(null)
                        }
                    } catch (_: Exception) {
                        onResult(null)
                    }
                } else {
                    onResult(null)
                }
            } else {
                onResult(null)
            }
        }
    )
    
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
                        
                        // Create temporary file for photo
                        val photoFile = File.createTempFile(
                            "photo_${System.currentTimeMillis()}",
                            ".jpg",
                            context.cacheDir
                        )
                        
                        tempPhotoUri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            photoFile
                        )
                        
                        // Launch camera  
                        val uri = tempPhotoUri
                        if (uri != null) {
                            cameraLauncher.launch(uri)
                        } else {
                            onResult(null)
                        }
                        
                    } catch (deniedAlways: DeniedAlwaysException) {
                        // Permission is permanently denied - redirect to settings
                        controller.openAppSettings()
                        onResult(null)
                    } catch (denied: DeniedException) {
                        // Permission was denied this time
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
 * Android implementation of GalleryManager using moko-permissions and ActivityResultContracts.GetContent
 * Professional implementation with proper gallery permissions handling
 */
@Composable
actual fun rememberGalleryManager(
    onResult: (MediaFile?) -> Unit
): GalleryManager {
    val context = LocalContext.current
    val contentResolver: ContentResolver = context.contentResolver
    val coroutineScope = rememberCoroutineScope()
    
    // Professional moko-permissions setup following official docs
    val factory = rememberPermissionsControllerFactory()
    val controller: PermissionsController = remember(factory) { 
        factory.createPermissionsController() 
    }
    
    // Essential for proper lifecycle handling on Android
    BindEffect(controller)
    
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                try {
                    val inputStream = contentResolver.openInputStream(uri)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()
                    
                    if (bytes != null) {
                        // Get file name from URI
                        val fileName = uri.pathSegments.lastOrNull() ?: "image_${System.currentTimeMillis()}"
                        
                        // Determine MIME type
                        val mimeType = contentResolver.getType(uri) ?: "image/*"
                        val mediaType = if (mimeType.startsWith("video/")) MediaType.VIDEO else MediaType.IMAGE
                        
                        val mediaFile = MediaFile(
                            data = bytes,
                            fileName = fileName,
                            mimeType = mimeType,
                            type = mediaType,
                            sizeBytes = bytes.size.toLong()
                        )
                        onResult(mediaFile)
                    } else {
                        onResult(null)
                    }
                } catch (_: Exception) {
                    onResult(null)
                }
            } else {
                onResult(null)
            }
        }
    )
    
    return remember {
        GalleryManager(
            onLaunch = {
                coroutineScope.launch {
                    try {
                        // Android's ActivityResultContracts.GetContent() doesn't require explicit permissions
                        // for accessing MediaStore content in modern Android versions
                        
                        // Launch gallery picker
                        galleryLauncher.launch("image/*")
                        
                    } catch (deniedAlways: DeniedAlwaysException) {
                        // Permission is permanently denied - redirect to settings
                        controller.openAppSettings()
                        onResult(null)
                    } catch (denied: DeniedException) {
                        // Permission was denied this time
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
 * Android implementation of VideoPickerManager using ActivityResultContracts.GetContent
 * Specifically configured for video selection only
 */
@Composable
actual fun rememberVideoPickerManager(
    onResult: (MediaFile?) -> Unit
): VideoPickerManager {
    val context = LocalContext.current
    val contentResolver = context.contentResolver
    val coroutineScope = rememberCoroutineScope()
    
    // Professional moko-permissions setup following official docs
    val factory = rememberPermissionsControllerFactory()
    val controller: PermissionsController = remember(factory) { 
        factory.createPermissionsController() 
    }
    
    // Essential for proper lifecycle handling on Android
    BindEffect(controller)
    
    val videoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            println("Android VideoPickerManager: URI received: ${uri != null}")
            if (uri != null) {
                try {
                    val inputStream = contentResolver.openInputStream(uri)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()
                    
                    if (bytes != null) {
                        // Get file name from URI
                        val fileName = uri.pathSegments.lastOrNull() ?: "video_${System.currentTimeMillis()}.mp4"
                        
                        // Get MIME type (should be video/*)
                        val mimeType = contentResolver.getType(uri) ?: "video/mp4"
                        println("Android VideoPickerManager: Video loaded, size: ${bytes.size}, mimeType: $mimeType")
                        
                        val mediaFile = MediaFile(
                            data = bytes,
                            fileName = fileName,
                            mimeType = mimeType,
                            type = MediaType.VIDEO,
                            sizeBytes = bytes.size.toLong()
                        )
                        onResult(mediaFile)
                        println("Android VideoPickerManager: MediaFile created and callback executed")
                    } else {
                        onResult(null)
                        println("Android VideoPickerManager: Failed to read video bytes")
                    }
                } catch (e: Exception) {
                    println("Android VideoPickerManager: Exception: ${e.message}")
                    onResult(null)
                }
            } else {
                onResult(null)
                println("Android VideoPickerManager: URI is null (cancelled)")
            }
        }
    )
    
    return remember {
        VideoPickerManager(
            onLaunch = {
                println("Android VideoPickerManager: onLaunch called")
                coroutineScope.launch {
                    try {
                        // Android's ActivityResultContracts.GetContent() doesn't require explicit permissions
                        // for accessing MediaStore content in modern Android versions
                        
                        // Launch video picker (only videos)
                        println("Android VideoPickerManager: Launching video picker")
                        videoLauncher.launch("video/*")
                        
                    } catch (deniedAlways: DeniedAlwaysException) {
                        // Permission is permanently denied - redirect to settings
                        controller.openAppSettings()
                        onResult(null)
                    } catch (denied: DeniedException) {
                        // Permission was denied this time
                        onResult(null)
                    } catch (e: Exception) {
                        println("Android VideoPickerManager: Launch exception: ${e.message}")
                        onResult(null)
                    }
                }
            }
        )
    }
}

actual class VideoPickerManager actual constructor(
    private val onLaunch: () -> Unit
) {
    actual fun launch() {
        onLaunch()
    }
}