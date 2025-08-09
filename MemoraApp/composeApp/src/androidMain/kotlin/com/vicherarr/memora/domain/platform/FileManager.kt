package com.vicherarr.memora.domain.platform

import android.content.Context
import com.vicherarr.memora.domain.models.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.security.MessageDigest

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

    // ===== ENHANCED METHODS FOR ATTACHMENT SYNC =====

    actual suspend fun saveFileAtPath(bytes: ByteArray, filePath: String): SavedFile? = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            // Create parent directories if they don't exist
            file.parentFile?.mkdirs()
            file.writeBytes(bytes)
            SavedFile(path = file.absolutePath, size = bytes.size.toLong())
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    actual suspend fun moveFile(sourcePath: String, destinationPath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val sourceFile = File(sourcePath)
            val destinationFile = File(destinationPath)
            
            if (!sourceFile.exists()) return@withContext false
            
            // Create parent directories if they don't exist
            destinationFile.parentFile?.mkdirs()
            
            // Move file using rename (most efficient)
            sourceFile.renameTo(destinationFile)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    actual suspend fun copyFile(sourcePath: String, destinationPath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val sourceFile = File(sourcePath)
            val destinationFile = File(destinationPath)
            
            if (!sourceFile.exists()) return@withContext false
            
            // Create parent directories if they don't exist
            destinationFile.parentFile?.mkdirs()
            
            // Copy file content
            sourceFile.copyTo(destinationFile, overwrite = true)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    actual suspend fun fileExists(filePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            File(filePath).exists()
        } catch (e: Exception) {
            false
        }
    }

    actual suspend fun getFileMetadata(filePath: String, includeHash: Boolean): FileMetadata? = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (!file.exists()) return@withContext null
            
            val hash = if (includeHash) {
                calculateFileHash(filePath)
            } else null
            
            FileMetadata(
                path = file.absolutePath,
                size = file.length(),
                exists = true,
                lastModified = file.lastModified(),
                contentHash = hash
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    actual suspend fun createDirectory(directoryPath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val directory = File(directoryPath)
            if (directory.exists()) {
                directory.isDirectory
            } else {
                directory.mkdirs()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    actual suspend fun calculateFileHash(filePath: String): String? = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (!file.exists()) return@withContext null
            
            val bytes = file.readBytes()
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(bytes)
            
            // Convert to hex string
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    actual suspend fun listFiles(directoryPath: String): List<String> = withContext(Dispatchers.IO) {
        try {
            val directory = File(directoryPath)
            if (directory.exists() && directory.isDirectory) {
                directory.listFiles()?.map { it.absolutePath } ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    actual suspend fun getFileSize(filePath: String): Long = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (file.exists()) {
                file.length()
            } else {
                -1L
            }
        } catch (e: Exception) {
            -1L
        }
    }

    actual suspend fun cleanupEmptyDirectories(directoryPath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val directory = File(directoryPath)
            if (!directory.exists() || !directory.isDirectory) return@withContext false
            
            // Recursively clean up empty directories
            fun cleanupRecursive(dir: File): Boolean {
                if (!dir.isDirectory) return false
                
                val files = dir.listFiles() ?: return false
                var hasContent = false
                
                for (file in files) {
                    if (file.isDirectory) {
                        if (!cleanupRecursive(file)) {
                            hasContent = true
                        }
                    } else {
                        hasContent = true
                    }
                }
                
                // If directory is empty, delete it
                return if (!hasContent) {
                    dir.delete()
                } else {
                    false
                }
            }
            
            cleanupRecursive(directory)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}