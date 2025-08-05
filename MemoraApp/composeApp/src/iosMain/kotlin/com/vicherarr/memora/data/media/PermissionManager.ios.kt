package com.vicherarr.memora.data.media

import com.vicherarr.memora.domain.models.Permission
import com.vicherarr.memora.domain.models.PermissionResult
import platform.AVFoundation.*
import platform.Photos.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * iOS implementation of PermissionManager using iOS permission APIs
 */
actual class PermissionManager {
    
    actual suspend fun requestPermission(permission: Permission): PermissionResult = 
        suspendCancellableCoroutine { continuation ->
            when (permission) {
                Permission.CAMERA -> {
                    // Check current camera authorization status
                    val status = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)
                    when (status) {
                        AVAuthorizationStatusAuthorized -> {
                            continuation.resume(PermissionResult.Granted)
                        }
                        AVAuthorizationStatusNotDetermined -> {
                            // Request camera permission
                            AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
                                val result = if (granted) PermissionResult.Granted else PermissionResult.Denied
                                continuation.resume(result)
                            }
                        }
                        AVAuthorizationStatusDenied, AVAuthorizationStatusRestricted -> {
                            continuation.resume(PermissionResult.PermanentlyDenied)
                        }
                        else -> {
                            continuation.resume(PermissionResult.Denied)
                        }
                    }
                }
                Permission.PHOTO_LIBRARY -> {
                    // Check current photo library authorization status
                    val status = PHPhotoLibrary.authorizationStatus()
                    when (status) {
                        PHAuthorizationStatusAuthorized, PHAuthorizationStatusLimited -> {
                            continuation.resume(PermissionResult.Granted)
                        }
                        PHAuthorizationStatusNotDetermined -> {
                            // Request photo library permission
                            PHPhotoLibrary.requestAuthorization { newStatus ->
                                val result = when (newStatus) {
                                    PHAuthorizationStatusAuthorized, PHAuthorizationStatusLimited -> PermissionResult.Granted
                                    PHAuthorizationStatusDenied, PHAuthorizationStatusRestricted -> PermissionResult.PermanentlyDenied
                                    else -> PermissionResult.Denied
                                }
                                continuation.resume(result)
                            }
                        }
                        PHAuthorizationStatusDenied, PHAuthorizationStatusRestricted -> {
                            continuation.resume(PermissionResult.PermanentlyDenied)
                        }
                        else -> {
                            continuation.resume(PermissionResult.Denied)
                        }
                    }
                }
                Permission.STORAGE -> {
                    // On iOS, storage permission is same as photo library permission
                    // Handle it the same way as PHOTO_LIBRARY to avoid recursion
                    val status = PHPhotoLibrary.authorizationStatus()
                    when (status) {
                        PHAuthorizationStatusAuthorized, PHAuthorizationStatusLimited -> {
                            continuation.resume(PermissionResult.Granted)
                        }
                        PHAuthorizationStatusNotDetermined -> {
                            // Request photo library permission
                            PHPhotoLibrary.requestAuthorization { newStatus ->
                                val result = when (newStatus) {
                                    PHAuthorizationStatusAuthorized, PHAuthorizationStatusLimited -> PermissionResult.Granted
                                    PHAuthorizationStatusDenied, PHAuthorizationStatusRestricted -> PermissionResult.PermanentlyDenied
                                    else -> PermissionResult.Denied
                                }
                                continuation.resume(result)
                            }
                        }
                        PHAuthorizationStatusDenied, PHAuthorizationStatusRestricted -> {
                            continuation.resume(PermissionResult.PermanentlyDenied)
                        }
                        else -> {
                            continuation.resume(PermissionResult.Denied)
                        }
                    }
                }
            }
        }
    
    actual fun isPermissionGranted(permission: Permission): Boolean {
        return when (permission) {
            Permission.CAMERA -> {
                val status = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)
                status == AVAuthorizationStatusAuthorized
            }
            Permission.PHOTO_LIBRARY -> {
                val status = PHPhotoLibrary.authorizationStatus()
                status == PHAuthorizationStatusAuthorized || status == PHAuthorizationStatusLimited
            }
            Permission.STORAGE -> {
                // On iOS, storage permission is same as photo library permission
                isPermissionGranted(Permission.PHOTO_LIBRARY)
            }
        }
    }
    
    actual suspend fun requestPermissions(permissions: List<Permission>): Map<Permission, PermissionResult> {
        val results = mutableMapOf<Permission, PermissionResult>()
        
        // Request each permission sequentially
        for (permission in permissions) {
            results[permission] = requestPermission(permission)
        }
        
        return results
    }
    
    actual fun canRequestPermission(permission: Permission): Boolean {
        return when (permission) {
            Permission.CAMERA -> {
                val status = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)
                status != AVAuthorizationStatusRestricted
            }
            Permission.PHOTO_LIBRARY -> {
                val status = PHPhotoLibrary.authorizationStatus()
                status != PHAuthorizationStatusRestricted
            }
            Permission.STORAGE -> {
                // On iOS, storage permission is same as photo library permission
                canRequestPermission(Permission.PHOTO_LIBRARY)
            }
        }
    }
}