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
}