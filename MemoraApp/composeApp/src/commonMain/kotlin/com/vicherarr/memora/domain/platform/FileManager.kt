package com.vicherarr.memora.domain.platform

import com.vicherarr.memora.domain.models.MediaType

/**
 * Represents a saved file's metadata.
 * @property path The absolute path to the saved file on the device.
 * @property size The size of the file in bytes.
 */
data class SavedFile(
    val path: String,
    val size: Long
)

/**
 * Represents detailed file metadata for sync operations.
 * @property path The absolute path to the file
 * @property size The size of the file in bytes  
 * @property exists Whether the file exists on disk
 * @property lastModified Last modification timestamp (milliseconds since epoch)
 * @property contentHash SHA256 hash of the file content (for integrity verification)
 */
data class FileMetadata(
    val path: String,
    val size: Long,
    val exists: Boolean,
    val lastModified: Long,
    val contentHash: String? = null
)

/**
 * A multiplatform interface for managing files on the device's local storage.
 *
 * This `expect` interface defines the contract for platform-specific file operations,
 * such as saving, retrieving, and deleting files. Implementations for Android and iOS
 * will handle the specifics of their respective file systems.
 */
expect class FileManager {
    /**
     * Saves a byte array to a file in a dedicated directory for the given media type.
     *
     * @param bytes The raw data of the file to be saved.
     * @param fileName The desired name for the file (e.g., "image.jpg").
     * @param mediaType The type of media (Image or Video), used to determine the storage subdirectory.
     * @return A [SavedFile] object containing the absolute path and size of the newly created file,
     * or `null` if the save operation fails.
     */
    suspend fun saveFile(bytes: ByteArray, fileName: String, mediaType: MediaType): SavedFile?

    /**
     * Retrieves a file as a byte array from the specified path.
     *
     * @param filePath The absolute path of the file to read.
     * @return A [ByteArray] containing the file's data, or `null` if the file cannot be read.
     */
    suspend fun getFile(filePath: String): ByteArray?

    /**
     * Deletes a file from the local storage.
     *
     * @param filePath The absolute path of the file to delete.
     * @return `true` if the file was successfully deleted, `false` otherwise.
     */
    suspend fun deleteFile(filePath: String): Boolean

    // ===== ENHANCED METHODS FOR ATTACHMENT SYNC =====

    /**
     * Saves a byte array to a specific file path, creating directories as needed.
     * Used for saving files with structured paths during sync operations.
     *
     * @param bytes The raw data of the file to be saved.
     * @param filePath The complete file path where the file should be saved.
     * @return A [SavedFile] object containing the path and size, or `null` if the save fails.
     */
    suspend fun saveFileAtPath(bytes: ByteArray, filePath: String): SavedFile?

    /**
     * Moves a file from source path to destination path.
     * Creates destination directories as needed.
     *
     * @param sourcePath The current path of the file.
     * @param destinationPath The new path where the file should be moved.
     * @return `true` if the file was successfully moved, `false` otherwise.
     */
    suspend fun moveFile(sourcePath: String, destinationPath: String): Boolean

    /**
     * Copies a file from source path to destination path.
     * Creates destination directories as needed.
     *
     * @param sourcePath The path of the file to copy.
     * @param destinationPath The path where the copy should be created.
     * @return `true` if the file was successfully copied, `false` otherwise.
     */
    suspend fun copyFile(sourcePath: String, destinationPath: String): Boolean

    /**
     * Checks if a file exists at the specified path.
     *
     * @param filePath The path to check.
     * @return `true` if the file exists, `false` otherwise.
     */
    suspend fun fileExists(filePath: String): Boolean

    /**
     * Gets detailed metadata about a file.
     *
     * @param filePath The path of the file to analyze.
     * @param includeHash Whether to calculate and include the content hash (expensive operation).
     * @return [FileMetadata] with file information, or `null` if the file doesn't exist.
     */
    suspend fun getFileMetadata(filePath: String, includeHash: Boolean = false): FileMetadata?

    /**
     * Creates a directory (and parent directories) if it doesn't exist.
     *
     * @param directoryPath The path of the directory to create.
     * @return `true` if the directory exists or was created successfully, `false` otherwise.
     */
    suspend fun createDirectory(directoryPath: String): Boolean

    /**
     * Calculates the SHA256 hash of a file's content.
     * Used for integrity verification during sync operations.
     *
     * @param filePath The path of the file to hash.
     * @return The SHA256 hash as a hex string, or `null` if the file can't be read.
     */
    suspend fun calculateFileHash(filePath: String): String?

    /**
     * Lists all files in a directory (non-recursive).
     *
     * @param directoryPath The directory to list.
     * @return List of file paths in the directory, or empty list if directory doesn't exist.
     */
    suspend fun listFiles(directoryPath: String): List<String>

    /**
     * Gets the size of a file in bytes.
     *
     * @param filePath The path of the file.
     * @return The size in bytes, or -1 if the file doesn't exist.
     */
    suspend fun getFileSize(filePath: String): Long

    /**
     * Cleans up empty directories in the attachments folder.
     * Used to remove empty note directories after attachment deletion.
     *
     * @param directoryPath The directory to clean up.
     * @return `true` if the directory was cleaned up successfully.
     */
    suspend fun cleanupEmptyDirectories(directoryPath: String): Boolean
}