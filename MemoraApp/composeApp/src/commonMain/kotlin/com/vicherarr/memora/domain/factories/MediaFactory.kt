package com.vicherarr.memora.domain.factories

import com.vicherarr.memora.data.media.CameraController
import com.vicherarr.memora.data.media.MediaPicker
import com.vicherarr.memora.data.media.PermissionManager

/**
 * Factory interface for creating media-related components
 * Uses factory pattern to handle platform-specific dependencies
 */
interface MediaFactory {
    fun createCameraController(): CameraController
    fun createMediaPicker(): MediaPicker?
    fun createPermissionManager(): PermissionManager?
}