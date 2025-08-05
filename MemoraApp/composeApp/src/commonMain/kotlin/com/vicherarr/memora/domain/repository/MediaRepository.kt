package com.vicherarr.memora.domain.repository

import com.vicherarr.memora.domain.models.MediaResult
import com.vicherarr.memora.domain.models.Permission
import com.vicherarr.memora.domain.models.PermissionResult

/**
 * Repository interface for handling multimedia operations
 */
interface MediaRepository {
    /**
     * Capture a photo using the device camera
     */
    suspend fun capturePhoto(): MediaResult
    
    /**
     * Record a video using the device camera
     */
    suspend fun recordVideo(): MediaResult
    
    /**
     * Pick an image from the device gallery
     */
    suspend fun pickImage(): MediaResult
    
    /**
     * Pick a video from the device gallery
     */
    suspend fun pickVideo(): MediaResult
    
    /**
     * Pick multiple images from the device gallery
     */
    suspend fun pickMultipleImages(maxSelection: Int = 5): List<MediaResult>
    
    /**
     * Request a specific permission
     */
    suspend fun requestPermission(permission: Permission): PermissionResult
    
    /**
     * Check if a permission is granted
     */
    fun isPermissionGranted(permission: Permission): Boolean
    
    /**
     * Request multiple permissions
     */
    suspend fun requestPermissions(permissions: List<Permission>): Map<Permission, PermissionResult>
    
    /**
     * Check if camera is available on the device
     */
    fun isCameraAvailable(): Boolean
}