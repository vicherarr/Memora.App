package com.vicherarr.memora.domain.platform

import android.content.Context
import com.vicherarr.memora.domain.models.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/**
 * Android-specific implementation of the [FileManager] interface.
 *
 * This class handles file operations on the Android platform, saving files to the app's
 * dedicated cache directory.
 *
 * @property context The Android application context, used to access the file system.
 */
actual class FileManager(private val context: Context) {

    /**
     * Saves a file to the app's cache directory on Android.
     *
     * The file is stored in a subdirectory based on the [MediaType] (e.g., "images", "videos").
     *
     * @return A [SavedFile] object with the file's path and size, or `null` on failure.
     */
    actual suspend fun saveFile(bytes: ByteArray, fileName: String, mediaType: MediaType): SavedFile? = withContext(Dispatchers.IO) {
        try {
            val subDir = when (mediaType) {
                MediaType.IMAGE -> "images"
                MediaType.VIDEO -> "videos"
            }
            val directory = File(context.cacheDir, subDir)
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val file = File(directory, fileName)
            file.writeBytes(bytes)
            SavedFile(path = file.absolutePath, size = bytes.size.toLong())
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Reads a file from the specified path on Android.
     *
     * @return The file's content as a [ByteArray], or `null` if reading fails.
     */
    actual suspend fun getFile(filePath: String): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (file.exists()) {
                file.readBytes()
            } else {
                null
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Deletes a file from the specified path on Android.
     *
     * @return `true` if deletion was successful, `false` otherwise.
     */
    actual suspend fun deleteFile(filePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            } else {
                false // File didn't exist, so not "successfully" deleted now.
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            false
        }
    }
}