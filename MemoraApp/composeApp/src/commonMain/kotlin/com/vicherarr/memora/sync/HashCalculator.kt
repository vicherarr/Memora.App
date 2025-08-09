package com.vicherarr.memora.sync

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Utility for calculating file hashes for integrity verification during sync
 * Uses platform-specific implementations for optimal performance
 */
expect class HashCalculator {
    /**
     * Calculate SHA256 hash of byte array
     * @param data The data to hash
     * @return SHA256 hash as hex string
     */
    suspend fun calculateSHA256(data: ByteArray): String
    
    /**
     * Calculate MD5 hash of byte array (for quick comparison)
     * @param data The data to hash  
     * @return MD5 hash as hex string
     */
    suspend fun calculateMD5(data: ByteArray): String
}

/**
 * Common interface for file integrity operations
 */
class FileHashService(private val hashCalculator: HashCalculator) {
    
    /**
     * Calculate content hash for attachment integrity verification
     * Uses SHA256 for strong integrity guarantees
     */
    suspend fun calculateContentHash(fileData: ByteArray): String = withContext(Dispatchers.Default) {
        hashCalculator.calculateSHA256(fileData)
    }
    
    /**
     * Calculate quick hash for duplicate detection
     * Uses MD5 for faster comparison (not security-critical)
     */
    suspend fun calculateQuickHash(fileData: ByteArray): String = withContext(Dispatchers.Default) {
        hashCalculator.calculateMD5(fileData)
    }
    
    /**
     * Verify file integrity by comparing hashes
     */
    suspend fun verifyIntegrity(fileData: ByteArray, expectedHash: String): Boolean {
        val actualHash = calculateContentHash(fileData)
        return actualHash.equals(expectedHash, ignoreCase = true)
    }
    
    /**
     * Generate short hash for attachment ID generation
     * Takes first 8 characters of SHA256
     */
    suspend fun generateShortHash(fileData: ByteArray): String {
        val fullHash = calculateContentHash(fileData)
        return fullHash.take(8)
    }
}

/**
 * Utility functions for hash operations
 */
object HashUtils {
    
    /**
     * Convert byte array to hex string
     */
    fun ByteArray.toHexString(): String {
        return joinToString("") { byte -> 
            val unsigned = byte.toInt() and 0xFF
            if (unsigned < 16) "0${unsigned.toString(16)}" else unsigned.toString(16)
        }
    }
    
    /**
     * Validate hash format (SHA256 should be 64 hex characters)
     */
    fun isValidSHA256Hash(hash: String): Boolean {
        return hash.length == 64 && hash.all { it.isDigit() || it.lowercaseChar() in 'a'..'f' }
    }
    
    /**
     * Validate hash format (MD5 should be 32 hex characters)
     */
    fun isValidMD5Hash(hash: String): Boolean {
        return hash.length == 32 && hash.all { it.isDigit() || it.lowercaseChar() in 'a'..'f' }
    }
}