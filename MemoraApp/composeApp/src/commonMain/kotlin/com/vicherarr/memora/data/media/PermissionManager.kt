package com.vicherarr.memora.data.media

import com.vicherarr.memora.domain.models.Permission
import com.vicherarr.memora.domain.models.PermissionResult

/**
 * Platform-specific permission manager for handling media-related permissions
 */
expect class PermissionManager {
    /**
     * Request a specific permission
     * @param permission The permission to request
     * @return PermissionResult indicating the result
     */
    suspend fun requestPermission(permission: Permission): PermissionResult

    /**
     * Check if a permission is already granted
     * @param permission The permission to check
     * @return true if granted, false otherwise
     */
    fun isPermissionGranted(permission: Permission): Boolean

    /**
     * Request multiple permissions at once
     * @param permissions List of permissions to request
     * @return Map of permissions to their results
     */
    suspend fun requestPermissions(permissions: List<Permission>): Map<Permission, PermissionResult>

    /**
     * Check if permission can be requested (not permanently denied)
     * @param permission The permission to check
     * @return true if can be requested, false if permanently denied
     */
    fun canRequestPermission(permission: Permission): Boolean
}