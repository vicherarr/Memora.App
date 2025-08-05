package com.vicherarr.memora.data.repository

import com.vicherarr.memora.domain.factories.MediaFactory
import com.vicherarr.memora.domain.models.MediaResult
import com.vicherarr.memora.domain.models.Permission
import com.vicherarr.memora.domain.models.PermissionResult
import com.vicherarr.memora.domain.repository.MediaRepository

/**
 * Implementation of MediaRepository that coordinates platform-specific media operations
 * Uses MediaFactory to create platform-specific components as needed
 */
class MediaRepositoryImpl(
    private val mediaFactory: MediaFactory
) : MediaRepository {
    
    override suspend fun capturePhoto(): MediaResult {
        // Temporary: Skip permission check to test camera functionality
        val cameraController = mediaFactory.createCameraController()
        return cameraController.capturePhoto()
    }
    
    override suspend fun recordVideo(): MediaResult {
        // Temporary: Skip permission check to test camera functionality
        val cameraController = mediaFactory.createCameraController()
        return cameraController.recordVideo()
    }
    
    override suspend fun pickImage(): MediaResult {
        // Check photo library permission first
        val photoPermission = requestPermission(Permission.PHOTO_LIBRARY)
        if (photoPermission != PermissionResult.Granted) {
            return MediaResult.Error("Photo library permission not granted")
        }
        
        val mediaPicker = mediaFactory.createMediaPicker()
            ?: return MediaResult.Error("Media picker not available")
        return mediaPicker.pickImage()
    }
    
    override suspend fun pickVideo(): MediaResult {
        // Check photo library permission first
        val photoPermission = requestPermission(Permission.PHOTO_LIBRARY)
        if (photoPermission != PermissionResult.Granted) {
            return MediaResult.Error("Photo library permission not granted")
        }
        
        val mediaPicker = mediaFactory.createMediaPicker()
            ?: return MediaResult.Error("Media picker not available")
        return mediaPicker.pickVideo()
    }
    
    override suspend fun pickMultipleImages(maxSelection: Int): List<MediaResult> {
        // Check photo library permission first
        val photoPermission = requestPermission(Permission.PHOTO_LIBRARY)
        if (photoPermission != PermissionResult.Granted) {
            return listOf(MediaResult.Error("Photo library permission not granted"))
        }
        
        val mediaPicker = mediaFactory.createMediaPicker()
            ?: return listOf(MediaResult.Error("Media picker not available"))
        return mediaPicker.pickMultipleImages(maxSelection)
    }
    
    override suspend fun requestPermission(permission: Permission): PermissionResult {
        val permissionManager = mediaFactory.createPermissionManager()
            ?: return PermissionResult.Denied
        return permissionManager.requestPermission(permission)
    }
    
    override fun isPermissionGranted(permission: Permission): Boolean {
        val permissionManager = mediaFactory.createPermissionManager()
            ?: return false
        return permissionManager.isPermissionGranted(permission)
    }
    
    override suspend fun requestPermissions(permissions: List<Permission>): Map<Permission, PermissionResult> {
        val permissionManager = mediaFactory.createPermissionManager()
            ?: return permissions.associateWith { PermissionResult.Denied }
        return permissionManager.requestPermissions(permissions)
    }
    
    override fun isCameraAvailable(): Boolean {
        val cameraController = mediaFactory.createCameraController()
        return cameraController.isCameraAvailable()
    }
}