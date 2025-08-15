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
 * Android implementation of CameraCaptureManager
 * Supports both photo and video capture from camera
 * Following MVVM, Clean Architecture and SOLID principles
 */
@Composable
actual fun rememberCameraCaptureManager(
    onResult: (MediaFile?) -> Unit
): CameraCaptureManager {
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
    
    var tempMediaUri by remember { mutableStateOf<Uri?>(null) }
    var currentCaptureMode by remember { mutableStateOf<CameraCaptureMode?>(null) }
    
    // Photo capture launcher
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                val uri = tempMediaUri
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
    
    // Video capture launcher
    val videoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo(),
        onResult = { success ->
            if (success) {
                val uri = tempMediaUri
                if (uri != null) {
                    try {
                        val inputStream = contentResolver.openInputStream(uri)
                        val bytes = inputStream?.readBytes()
                        inputStream?.close()
                        
                        if (bytes != null) {
                            val mediaFile = MediaFile(
                                data = bytes,
                                fileName = "video_${System.currentTimeMillis()}.mp4",
                                mimeType = "video/mp4",
                                type = MediaType.VIDEO,
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
                    
                    currentCaptureMode = mode
                    
                    when (mode) {
                        CameraCaptureMode.PHOTO -> {
                            // Create temporary file for photo
                            val photoFile = File.createTempFile(
                                "photo_${System.currentTimeMillis()}",
                                ".jpg",
                                context.cacheDir
                            )
                            
                            tempMediaUri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                photoFile
                            )
                            
                            // Launch photo capture
                            val uri = tempMediaUri
                            if (uri != null) {
                                photoLauncher.launch(uri)
                            } else {
                                onResult(null)
                            }
                        }
                        
                        CameraCaptureMode.VIDEO -> {
                            // Create temporary file for video
                            val videoFile = File.createTempFile(
                                "video_${System.currentTimeMillis()}",
                                ".mp4",
                                context.cacheDir
                            )
                            
                            tempMediaUri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                videoFile
                            )
                            
                            // Launch video capture
                            val uri = tempMediaUri
                            if (uri != null) {
                                videoLauncher.launch(uri)
                            } else {
                                onResult(null)
                            }
                        }
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