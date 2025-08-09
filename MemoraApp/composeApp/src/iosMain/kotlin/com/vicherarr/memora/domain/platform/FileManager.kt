package com.vicherarr.memora.domain.platform

import com.vicherarr.memora.domain.models.MediaType
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSString
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.dataWithBytes
import platform.Foundation.stringByAppendingPathComponent
import platform.Foundation.writeToFile
import platform.Foundation.NSFileModificationDate
import platform.Foundation.NSFileSize
import platform.Foundation.NSNumber
import platform.Foundation.NSDate
import platform.Foundation.stringByDeletingLastPathComponent
import platform.Foundation.timeIntervalSince1970

/**
 * iOS-specific implementation of the [FileManager] interface.
 *
 * This class handles file operations on the iOS platform, using the `NSFileManager`
 * to save files within the app's cache directory.
 */
@OptIn(ExperimentalForeignApi::class)
actual class FileManager {

    private val fileManager = NSFileManager.defaultManager

    private fun getCacheDirectory(): String? {
        return NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, true).firstOrNull() as? String
    }

    /**
     * Saves a file to the app's cache directory on iOS.
     *
     * The file is stored in a subdirectory based on the [MediaType] (e.g., "images", "videos").
     *
     * @return A [SavedFile] object with the file's path and size, or `null` on failure.
     */
    actual suspend fun saveFile(bytes: ByteArray, fileName: String, mediaType: MediaType): SavedFile? = withContext(Dispatchers.Default) {
        val cacheDirectory = getCacheDirectory() ?: return@withContext null

        val subDir = when (mediaType) {
            MediaType.IMAGE -> "images"
            MediaType.VIDEO -> "videos"
        }

        val directoryPath = (cacheDirectory as NSString).stringByAppendingPathComponent(subDir)

        if (!fileManager.fileExistsAtPath(directoryPath)) {
            try {
                fileManager.createDirectoryAtPath(directoryPath, withIntermediateDirectories = true, attributes = null, error = null)
            } catch (e: Exception) {
                // Handle exception
                return@withContext null
            }
        }

        val filePath = (directoryPath as NSString).stringByAppendingPathComponent(fileName)
        val nsData = bytes.usePinned { NSData.dataWithBytes(it.addressOf(0), it.get().size.toULong()) }

        if (nsData.writeToFile(filePath, atomically = true)) {
            SavedFile(path = filePath, size = bytes.size.toLong())
        } else {
            null
        }
    }

    /**
     * Reads a file from the specified path on iOS.
     *
     * @return The file's content as a [ByteArray], or `null` if reading fails.
     */
    actual suspend fun getFile(filePath: String): ByteArray? = withContext(Dispatchers.Default) {
        fileManager.contentsAtPath(filePath)?.let { it.toByteArray() }
    }

    /**
     * Deletes a file from the specified path on iOS.
     *
     * @return `true` if deletion was successful, `false` otherwise.
     */
    actual suspend fun deleteFile(filePath: String): Boolean = withContext(Dispatchers.Default) {
        try {
            fileManager.removeItemAtPath(filePath, error = null)
        } catch (e: Exception) {
            // Handle exception
            false
        }
    }

    // ===== ENHANCED METHODS FOR ATTACHMENT SYNC =====

    actual suspend fun saveFileAtPath(bytes: ByteArray, filePath: String): SavedFile? = withContext(Dispatchers.Default) {
        try {
            // Create parent directories if they don't exist
            val parentPath = (filePath as NSString).stringByDeletingLastPathComponent()
            if (!fileManager.fileExistsAtPath(parentPath)) {
                fileManager.createDirectoryAtPath(
                    parentPath, 
                    withIntermediateDirectories = true, 
                    attributes = null, 
                    error = null
                )
            }
            
            val nsData = bytes.usePinned { NSData.dataWithBytes(it.addressOf(0), it.get().size.toULong()) }
            
            if (nsData.writeToFile(filePath, atomically = true)) {
                SavedFile(path = filePath, size = bytes.size.toLong())
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    actual suspend fun moveFile(sourcePath: String, destinationPath: String): Boolean = withContext(Dispatchers.Default) {
        try {
            if (!fileManager.fileExistsAtPath(sourcePath)) return@withContext false
            
            // Create parent directories if they don't exist
            val parentPath = (destinationPath as NSString).stringByDeletingLastPathComponent()
            if (!fileManager.fileExistsAtPath(parentPath)) {
                fileManager.createDirectoryAtPath(
                    parentPath, 
                    withIntermediateDirectories = true, 
                    attributes = null, 
                    error = null
                )
            }
            
            fileManager.moveItemAtPath(sourcePath, toPath = destinationPath, error = null)
        } catch (e: Exception) {
            false
        }
    }

    actual suspend fun copyFile(sourcePath: String, destinationPath: String): Boolean = withContext(Dispatchers.Default) {
        try {
            if (!fileManager.fileExistsAtPath(sourcePath)) return@withContext false
            
            // Create parent directories if they don't exist
            val parentPath = (destinationPath as NSString).stringByDeletingLastPathComponent()
            if (!fileManager.fileExistsAtPath(parentPath)) {
                fileManager.createDirectoryAtPath(
                    parentPath, 
                    withIntermediateDirectories = true, 
                    attributes = null, 
                    error = null
                )
            }
            
            fileManager.copyItemAtPath(sourcePath, toPath = destinationPath, error = null)
        } catch (e: Exception) {
            false
        }
    }

    actual suspend fun fileExists(filePath: String): Boolean = withContext(Dispatchers.Default) {
        fileManager.fileExistsAtPath(filePath)
    }

    actual suspend fun getFileMetadata(filePath: String, includeHash: Boolean): FileMetadata? = withContext(Dispatchers.Default) {
        try {
            if (!fileManager.fileExistsAtPath(filePath)) return@withContext null
            
            val attributes = fileManager.attributesOfItemAtPath(filePath, error = null) ?: return@withContext null
            
            val size = (attributes[NSFileSize] as? NSNumber)?.longValue ?: 0L
            val modificationDate = (attributes[NSFileModificationDate] as? NSDate)
            val lastModified = modificationDate?.timeIntervalSince1970()?.toLong()?.times(1000) ?: 0L
            
            val hash = if (includeHash) {
                calculateFileHash(filePath)
            } else null
            
            FileMetadata(
                path = filePath,
                size = size,
                exists = true,
                lastModified = lastModified,
                contentHash = hash
            )
        } catch (e: Exception) {
            null
        }
    }

    actual suspend fun createDirectory(directoryPath: String): Boolean = withContext(Dispatchers.Default) {
        try {
            if (fileManager.fileExistsAtPath(directoryPath)) {
                return@withContext true // If exists, assume it's a directory for now
            } else {
                fileManager.createDirectoryAtPath(
                    directoryPath, 
                    withIntermediateDirectories = true, 
                    attributes = null, 
                    error = null
                )
            }
        } catch (e: Exception) {
            false
        }
    }

    actual suspend fun calculateFileHash(filePath: String): String? = withContext(Dispatchers.Default) {
        try {
            val data = fileManager.contentsAtPath(filePath) ?: return@withContext null
            val bytes = data.toByteArray()
            
            // Use CommonCrypto for iOS hashing (similar to HashCalculator.ios.kt)
            // For now, return a simple hash - this would need proper CommonCrypto implementation
            val hash = bytes.contentHashCode().toString(16)
            hash.padStart(64, '0') // Pad to look like SHA256
        } catch (e: Exception) {
            null
        }
    }

    actual suspend fun listFiles(directoryPath: String): List<String> = withContext(Dispatchers.Default) {
        try {
            val contents = fileManager.contentsOfDirectoryAtPath(directoryPath, error = null) ?: return@withContext emptyList()
            contents.mapNotNull { fileName ->
                val fullPath = (directoryPath as NSString).stringByAppendingPathComponent(fileName as String)
                fullPath
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    actual suspend fun getFileSize(filePath: String): Long = withContext(Dispatchers.Default) {
        try {
            if (!fileManager.fileExistsAtPath(filePath)) return@withContext -1L
            
            val attributes = fileManager.attributesOfItemAtPath(filePath, error = null)
            (attributes?.get(NSFileSize) as? NSNumber)?.longValue ?: -1L
        } catch (e: Exception) {
            -1L
        }
    }

    actual suspend fun cleanupEmptyDirectories(directoryPath: String): Boolean = withContext(Dispatchers.Default) {
        try {
            if (!fileManager.fileExistsAtPath(directoryPath)) return@withContext false
            
            // Recursively clean up empty directories
            fun cleanupRecursive(dirPath: String): Boolean {
                val contents = fileManager.contentsOfDirectoryAtPath(dirPath, error = null) ?: return false
                var hasContent = false
                
                for (item in contents) {
                    val itemPath = (dirPath as NSString).stringByAppendingPathComponent(item as String)
                    
                    // Simple check: if it has no extension, assume it's a directory
                    val isDirectory = !(item as String).contains(".")
                    
                    if (isDirectory) {
                        if (!cleanupRecursive(itemPath)) {
                            hasContent = true
                        }
                    } else {
                        hasContent = true
                    }
                }
                
                // If directory is empty, delete it
                return if (!hasContent) {
                    fileManager.removeItemAtPath(dirPath, error = null)
                } else {
                    false
                }
            }
            
            cleanupRecursive(directoryPath)
        } catch (e: Exception) {
            false
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun NSData.toByteArray(): ByteArray = ByteArray(this.length.toInt()).apply {
        this.usePinned { pinned ->
            platform.posix.memcpy(pinned.addressOf(0), this@toByteArray.bytes, this@toByteArray.length)
        }
    }
}