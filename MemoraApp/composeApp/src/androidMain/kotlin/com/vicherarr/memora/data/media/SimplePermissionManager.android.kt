package com.vicherarr.memora.data.media

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.vicherarr.memora.domain.models.Permission
import com.vicherarr.memora.domain.models.PermissionResult

/**
 * Simple permission manager that only checks permissions
 * Does not request them - for that we'd need Activity context
 */
class SimplePermissionManager(
    private val context: Context
) {
    
    fun isPermissionGranted(permission: Permission): Boolean {
        val androidPermission = permission.toAndroidPermission()
        return ContextCompat.checkSelfPermission(context, androidPermission) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun Permission.toAndroidPermission(): String {
        return when (this) {
            Permission.CAMERA -> Manifest.permission.CAMERA
            Permission.PHOTO_LIBRARY -> getMediaPermission()
            Permission.STORAGE -> getMediaPermission()
        }
    }
    
    private fun getMediaPermission(): String {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                // Android 14+ (API 34) - Check if we have partial access
                if (ContextCompat.checkSelfPermission(context, "android.permission.READ_MEDIA_VISUAL_USER_SELECTED") 
                    == PackageManager.PERMISSION_GRANTED) {
                    "android.permission.READ_MEDIA_VISUAL_USER_SELECTED"
                } else {
                    Manifest.permission.READ_MEDIA_IMAGES
                }
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                // Android 13+ (API 33)
                Manifest.permission.READ_MEDIA_IMAGES
            }
            else -> {
                // Android 12 and below (API 32 and below)
                Manifest.permission.READ_EXTERNAL_STORAGE
            }
        }
    }
}