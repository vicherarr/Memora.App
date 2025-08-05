package com.vicherarr.memora.domain.factories

import com.vicherarr.memora.data.media.CameraController
import com.vicherarr.memora.data.media.MediaPicker
import com.vicherarr.memora.data.media.PermissionManager

/**
 * iOS implementation of MediaFactory
 * Creates platform-specific media components
 */
class IosMediaFactory : MediaFactory {
    
    override fun createCameraController(): CameraController {
        return CameraController()
    }
    
    override fun createMediaPicker(): MediaPicker {
        return MediaPicker()
    }
    
    override fun createPermissionManager(): PermissionManager {
        return PermissionManager()
    }
}