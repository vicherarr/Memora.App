package com.vicherarr.memora.sync

import com.vicherarr.memora.data.database.getCurrentTimestamp

/**
 * Utility for managing attachment paths and naming conventions
 * Ensures consistent paths across all platforms and devices
 */
object AttachmentPathManager {
    
    // Path constants
    const val ATTACHMENTS_FOLDER = "memora_attachments"
    const val REMOTE_ATTACHMENTS_FOLDER = "attachments"
    
    /**
     * Generate unique structured attachment ID
     * Format: {noteId}_{index}_{timestamp}_{contentHash}
     * 
     * @param noteId ID of the parent note
     * @param index Index within the note (0, 1, 2...)
     * @param contentHash Short hash of file content (first 8 chars of SHA256)
     * @param timestamp Optional timestamp (uses current if not provided)
     */
    fun generateAttachmentId(
        noteId: String,
        index: Int,
        contentHash: String,
        timestamp: Long = getCurrentTimestamp()
    ): String {
        val shortHash = contentHash.take(8)
        return "${noteId}_${index}_${timestamp}_${shortHash}"
    }
    
    /**
     * Build local attachment path following structured convention
     * Format: {APP_CACHE}/memora_attachments/{noteId}/{attachmentId}.{extension}
     * 
     * @param noteId ID of the parent note
     * @param attachmentId Structured attachment ID
     * @param extension File extension (jpg, png, mp4, etc.)
     */
    fun buildLocalAttachmentPath(
        noteId: String,
        attachmentId: String,
        extension: String
    ): String {
        return "${getLocalAttachmentsBaseDir()}/${ATTACHMENTS_FOLDER}/${noteId}/${attachmentId}.${extension}"
    }
    
    /**
     * Build remote attachment path for Google Drive/iCloud
     * Format: attachments/{noteId}/{attachmentId}.{extension}
     * 
     * @param noteId ID of the parent note
     * @param attachmentId Structured attachment ID  
     * @param extension File extension
     */
    fun buildRemoteAttachmentPath(
        noteId: String,
        attachmentId: String,
        extension: String
    ): String {
        return "${REMOTE_ATTACHMENTS_FOLDER}/${noteId}/${attachmentId}.${extension}"
    }
    
    /**
     * Extract note ID from attachment path
     * Works for both local and remote paths
     */
    fun extractNoteIdFromPath(path: String): String? {
        return try {
            // Look for pattern: .../noteId/attachmentId.ext
            val parts = path.split("/")
            val noteIndex = parts.indexOfFirst { it.startsWith("note_") }
            if (noteIndex >= 0) parts[noteIndex] else null
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Extract attachment ID from filename
     * Removes extension to get pure attachment ID
     */
    fun extractAttachmentIdFromFileName(fileName: String): String? {
        return try {
            // Remove extension: attachmentId.jpg -> attachmentId
            val lastDotIndex = fileName.lastIndexOf(".")
            if (lastDotIndex > 0) {
                fileName.substring(0, lastDotIndex)
            } else {
                fileName
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Get file extension from filename or original name
     */
    fun getExtension(fileName: String): String {
        val lastDotIndex = fileName.lastIndexOf(".")
        return if (lastDotIndex > 0 && lastDotIndex < fileName.length - 1) {
            fileName.substring(lastDotIndex + 1).lowercase()
        } else {
            "bin" // Default extension for unknown files
        }
    }
    
    /**
     * Check if a path follows the original (pre-sync) pattern
     * Original paths typically contain: /cache/images/ or /cache/videos/
     */
    fun isOriginalPath(filePath: String): Boolean {
        return filePath.contains("/images/") || 
               filePath.contains("/videos/") ||
               filePath.contains("/cache/") && !filePath.contains(ATTACHMENTS_FOLDER)
    }
    
    /**
     * Check if a path follows the structured (post-sync) pattern
     * Structured paths contain: /memora_attachments/
     */
    fun isStructuredPath(filePath: String): Boolean {
        return filePath.contains("/$ATTACHMENTS_FOLDER/")
    }
    
    /**
     * Validate attachment ID format
     * Expected format: noteId_index_timestamp_hash
     */
    fun isValidAttachmentId(attachmentId: String): Boolean {
        val parts = attachmentId.split("_")
        return parts.size == 4 && 
               parts[0].startsWith("note_") &&  // noteId starts with "note_"
               parts[1].toIntOrNull() != null && // index is numeric
               parts[2].toLongOrNull() != null && // timestamp is numeric
               parts[3].length == 8 && parts[3].all { it.isDigit() || it.lowercaseChar() in 'a'..'f' } // hash is 8 hex chars
    }
    
    /**
     * Generate collision-safe attachment ID
     * If the generated ID already exists, append collision suffix
     */
    fun generateCollisionSafeId(
        noteId: String,
        index: Int,
        contentHash: String,
        existingIds: Set<String>,
        timestamp: Long = getCurrentTimestamp()
    ): String {
        var baseId = generateAttachmentId(noteId, index, contentHash, timestamp)
        var counter = 1
        
        while (existingIds.contains(baseId)) {
            baseId = "${generateAttachmentId(noteId, index, contentHash, timestamp)}_collision_${counter}"
            counter++
        }
        
        return baseId
    }
    
    /**
     * Build directory path for a note's attachments
     * Used to create folder structure before saving files
     */
    fun buildNoteAttachmentsDir(noteId: String): String {
        return "${getLocalAttachmentsBaseDir()}/${ATTACHMENTS_FOLDER}/${noteId}"
    }
    
}

/**
 * Platform-specific base directory for attachments
 * Will be implemented per platform with expect/actual
 */
expect fun getLocalAttachmentsBaseDir(): String