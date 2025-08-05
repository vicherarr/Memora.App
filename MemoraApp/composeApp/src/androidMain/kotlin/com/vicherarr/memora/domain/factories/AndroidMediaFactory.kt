package com.vicherarr.memora.domain.factories

import android.content.Context
import com.vicherarr.memora.data.media.CameraController
import com.vicherarr.memora.data.media.MediaPicker
import com.vicherarr.memora.data.media.PermissionManager
import com.vicherarr.memora.data.media.SimplePermissionManager

/**
 * Android implementation of MediaFactory
 * Creates platform-specific media components with proper dependencies
 */
class AndroidMediaFactory(
    private val context: Context
) : MediaFactory {
    
    private val simplePermissionManager = SimplePermissionManager(context)
    
    override fun createCameraController(): CameraController {
        return CameraController(context)
    }
    
    override fun createMediaPicker(): MediaPicker? {
        // MediaPicker requires Activity, return null for now
        return null
    }
    
    override fun createPermissionManager(): PermissionManager? {
        // Return null for now - permissions can't be requested without Activity
        return null
    }
}