package com.vicherarr.memora.data.database

import com.vicherarr.memora.sync.SyncStatus

/**
 * Domain model for file attachments with sync capabilities
 * This extends the basic SQLDelight model with sync-specific fields
 */
data class Attachment(
    val id: String,
    val datos_archivo: ByteArray? = null, // Legacy: BLOB data (not used in sync workflow)
    val nombre_original: String,
    val tipo_archivo: Int, // 1 = Image, 2 = Video (from MediaType enum)
    val tipo_mime: String,
    val tamano_bytes: Long,
    val fecha_subida: Long,
    val nota_id: String,
    
    // Sync-specific fields
    val ruta_local: String? = null, // Local file path
    val sync_status: SyncStatus? = SyncStatus.PENDING,
    val needs_upload: Boolean = true,
    val remote_id: String? = null, // ID in cloud storage
    val content_hash: String? = null, // SHA256 hash for integrity
    val last_sync_attempt: Long? = null,
    val sync_retry_count: Int = 0,
    val local_created_at: Long? = null,
    val remote_path: String? = null // Path in cloud storage
) {
    
    /**
     * Check if this attachment is ready for sync
     */
    fun isReadyForSync(): Boolean {
        return ruta_local != null && 
               sync_status == SyncStatus.PENDING && 
               needs_upload
    }
    
    /**
     * Check if this attachment is fully synced
     */
    fun isSynced(): Boolean {
        return sync_status == SyncStatus.SYNCED && 
               remote_id != null && 
               !needs_upload
    }
    
    /**
     * Check if this attachment has sync conflicts
     */
    fun hasConflict(): Boolean {
        return sync_status == SyncStatus.CONFLICT
    }
    
    /**
     * Check if sync failed
     */
    fun hasSyncFailed(): Boolean {
        return sync_status == SyncStatus.FAILED
    }
    
    /**
     * Get display name for this attachment
     */
    fun getDisplayName(): String {
        return nombre_original.ifEmpty { "Attachment_${id.take(8)}" }
    }
    
    /**
     * Get file extension from original name
     */
    fun getFileExtension(): String {
        return nombre_original.substringAfterLast('.', "")
    }
    
    /**
     * Check if this is an image attachment
     */
    fun isImage(): Boolean {
        return tipo_archivo == 1 || tipo_mime.startsWith("image/")
    }
    
    /**
     * Check if this is a video attachment
     */
    fun isVideo(): Boolean {
        return tipo_archivo == 2 || tipo_mime.startsWith("video/")
    }
    
    /**
     * Get human-readable file size
     */
    fun getReadableFileSize(): String {
        val bytes = tamano_bytes
        val units = arrayOf("B", "KB", "MB", "GB")
        var size = bytes.toDouble()
        var unitIndex = 0
        
        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }
        
        // Format to 1 decimal place (multiplatform compatible)
        val formattedSize = (size * 10).toInt() / 10.0
        return "$formattedSize ${units[unitIndex]}"
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Attachment

        if (id != other.id) return false
        if (datos_archivo != null) {
            if (other.datos_archivo == null) return false
            if (!datos_archivo.contentEquals(other.datos_archivo)) return false
        } else if (other.datos_archivo != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (datos_archivo?.contentHashCode() ?: 0)
        return result
    }
}