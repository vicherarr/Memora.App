package com.vicherarr.memora.data.media

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.vicherarr.memora.domain.models.Permission
import com.vicherarr.memora.domain.models.PermissionResult
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Android implementation of PermissionManager using Activity Result API
 */
actual class PermissionManager(
    private val context: Context,
    private val activity: AppCompatActivity
) {
    private var singlePermissionLauncher: ActivityResultLauncher<String>? = null
    private var multiplePermissionLauncher: ActivityResultLauncher<Array<String>>? = null
    
    private var currentSingleContinuation: kotlin.coroutines.Continuation<PermissionResult>? = null
    private var currentMultipleContinuation: kotlin.coroutines.Continuation<Map<Permission, PermissionResult>>? = null
    private var pendingPermissions: List<Permission>? = null
    
    init {
        setupLaunchers()
    }
    
    private fun setupLaunchers() {
        // Single permission launcher
        singlePermissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            val result = if (isGranted) {
                PermissionResult.Granted
            } else {
                PermissionResult.Denied
            }
            currentSingleContinuation?.resume(result)
            currentSingleContinuation = null
        }
        
        // Multiple permissions launcher
        multiplePermissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val pendingPerms = pendingPermissions ?: emptyList()
            val results = mutableMapOf<Permission, PermissionResult>()
            
            pendingPerms.forEach { permission ->
                val androidPermission = permission.toAndroidPermission()
                val isGranted = permissions[androidPermission] == true
                results[permission] = if (isGranted) {
                    PermissionResult.Granted
                } else {
                    PermissionResult.Denied
                }
            }
            
            currentMultipleContinuation?.resume(results)
            currentMultipleContinuation = null
            pendingPermissions = null
        }
    }
    
    actual suspend fun requestPermission(permission: Permission): PermissionResult = 
        suspendCancellableCoroutine { continuation ->
            // Check if permission is already granted
            if (isPermissionGranted(permission)) {
                continuation.resume(PermissionResult.Granted)
                return@suspendCancellableCoroutine
            }
            
            currentSingleContinuation = continuation
            val androidPermission = permission.toAndroidPermission()
            
            try {
                singlePermissionLauncher?.launch(androidPermission)
            } catch (e: Exception) {
                continuation.resume(PermissionResult.Denied)
            }
        }
    
    actual fun isPermissionGranted(permission: Permission): Boolean {
        val androidPermission = permission.toAndroidPermission()
        return ContextCompat.checkSelfPermission(context, androidPermission) == PackageManager.PERMISSION_GRANTED
    }
    
    actual suspend fun requestPermissions(permissions: List<Permission>): Map<Permission, PermissionResult> = 
        suspendCancellableCoroutine { continuation ->
            // Check which permissions are already granted
            val results = mutableMapOf<Permission, PermissionResult>()
            val permissionsToRequest = mutableListOf<Permission>()
            
            permissions.forEach { permission ->
                if (isPermissionGranted(permission)) {
                    results[permission] = PermissionResult.Granted
                } else {
                    permissionsToRequest.add(permission)
                }
            }
            
            // If all permissions are already granted, return immediately
            if (permissionsToRequest.isEmpty()) {
                continuation.resume(results)
                return@suspendCancellableCoroutine
            }
            
            // Request the remaining permissions
            currentMultipleContinuation = continuation
            pendingPermissions = permissionsToRequest
            val androidPermissions = permissionsToRequest.map { it.toAndroidPermission() }.toTypedArray()
            
            try {
                multiplePermissionLauncher?.launch(androidPermissions)
            } catch (e: Exception) {
                // If launching fails, mark all as denied
                permissionsToRequest.forEach { permission ->
                    results[permission] = PermissionResult.Denied
                }
                continuation.resume(results)
            }
        }
    
    actual fun canRequestPermission(permission: Permission): Boolean {
        val androidPermission = permission.toAndroidPermission()
        
        // If permission is already granted, we don't need to request it
        if (isPermissionGranted(permission)) {
            return true
        }
        
        // We can request if we should show rationale OR if we haven't asked before
        return activity.shouldShowRequestPermissionRationale(androidPermission) || 
               ContextCompat.checkSelfPermission(context, androidPermission) == PackageManager.PERMISSION_DENIED
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