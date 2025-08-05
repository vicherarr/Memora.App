package com.vicherarr.memora.domain.models

/**
 * Represents the result of a media operation (capture or selection)
 */
sealed class MediaResult {
    data class Success(val mediaFile: MediaFile) : MediaResult()
    data class Error(val message: String) : MediaResult()
    object Cancelled : MediaResult()
}

/**
 * Represents a media file with its metadata
 */
data class MediaFile(
    val data: ByteArray,
    val fileName: String,
    val mimeType: String,
    val type: MediaType,
    val sizeBytes: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as MediaFile

        if (!data.contentEquals(other.data)) return false
        if (fileName != other.fileName) return false
        if (mimeType != other.mimeType) return false
        if (type != other.type) return false
        if (sizeBytes != other.sizeBytes) return false

        return true
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + fileName.hashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + sizeBytes.hashCode()
        return result
    }
}

/**
 * Types of media supported by the application
 */
enum class MediaType {
    IMAGE,
    VIDEO
}

/**
 * Types of media operations being performed
 */
enum class MediaOperationType {
    NONE,
    PHOTO_CAPTURE,
    VIDEO_RECORDING,
    IMAGE_SELECTION,
    VIDEO_SELECTION,
    MULTIPLE_SELECTION
}

/**
 * Permissions required for media operations
 */
enum class Permission {
    CAMERA,
    PHOTO_LIBRARY,
    STORAGE
}

/**
 * Result of a permission request
 */
sealed class PermissionResult {
    object Granted : PermissionResult()
    object Denied : PermissionResult()
    object PermanentlyDenied : PermissionResult()
}