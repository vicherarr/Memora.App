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

    @OptIn(ExperimentalForeignApi::class)
    private fun NSData.toByteArray(): ByteArray = ByteArray(this.length.toInt()).apply {
        this.usePinned { pinned ->
            platform.posix.memcpy(pinned.addressOf(0), this@toByteArray.bytes, this@toByteArray.length)
        }
    }
}