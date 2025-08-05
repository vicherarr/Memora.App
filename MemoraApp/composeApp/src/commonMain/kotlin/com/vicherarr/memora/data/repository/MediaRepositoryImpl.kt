package com.vicherarr.memora.data.repository

import com.vicherarr.memora.data.media.CameraController
import com.vicherarr.memora.data.media.MediaPicker
import com.vicherarr.memora.data.media.PermissionManager
import com.vicherarr.memora.domain.models.MediaResult
import com.vicherarr.memora.domain.models.Permission
import com.vicherarr.memora.domain.models.PermissionResult
import com.vicherarr.memora.domain.repository.MediaRepository

/**
 * Implementation of MediaRepository that coordinates platform-specific media operations
 */
class MediaRepositoryImpl(
    private val cameraController: CameraController,
    private val mediaPicker: MediaPicker,
    private val permissionManager: PermissionManager
) : MediaRepository {
    
    override suspend fun capturePhoto(): MediaResult {
        // Check camera permission first
        val cameraPermission = requestPermission(Permission.CAMERA)
        if (cameraPermission != PermissionResult.Granted) {
            return MediaResult.Error("Camera permission not granted")
        }
        
        return cameraController.capturePhoto()
    }
    
    override suspend fun recordVideo(): MediaResult {
        // Check camera permission first
        val cameraPermission = requestPermission(Permission.CAMERA)
        if (cameraPermission != PermissionResult.Granted) {
            return MediaResult.Error("Camera permission not granted")
        }
        
        return cameraController.recordVideo()
    }
    
    override suspend fun pickImage(): MediaResult {
        // Check photo library permission first
        val photoPermission = requestPermission(Permission.PHOTO_LIBRARY)
        if (photoPermission != PermissionResult.Granted) {
            return MediaResult.Error("Photo library permission not granted")
        }
        
        return mediaPicker.pickImage()
    }
    
    override suspend fun pickVideo(): MediaResult {
        // Check photo library permission first
        val photoPermission = requestPermission(Permission.PHOTO_LIBRARY)
        if (photoPermission != PermissionResult.Granted) {
            return MediaResult.Error("Photo library permission not granted")
        }
        
        return mediaPicker.pickVideo()
    }
    
    override suspend fun pickMultipleImages(maxSelection: Int): List<MediaResult> {
        // Check photo library permission first
        val photoPermission = requestPermission(Permission.PHOTO_LIBRARY)
        if (photoPermission != PermissionResult.Granted) {
            return listOf(MediaResult.Error("Photo library permission not granted"))
        }
        
        return mediaPicker.pickMultipleImages(maxSelection)
    }
    
    override suspend fun requestPermission(permission: Permission): PermissionResult {
        return permissionManager.requestPermission(permission)
    }
    
    override fun isPermissionGranted(permission: Permission): Boolean {
        return permissionManager.isPermissionGranted(permission)
    }
    
    override suspend fun requestPermissions(permissions: List<Permission>): Map<Permission, PermissionResult> {
        return permissionManager.requestPermissions(permissions)
    }
    
    override fun isCameraAvailable(): Boolean {
        return cameraController.isCameraAvailable()
    }
}