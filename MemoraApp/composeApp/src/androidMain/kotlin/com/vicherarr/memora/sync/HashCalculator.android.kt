package com.vicherarr.memora.sync

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest

/**
 * Android-specific implementation of HashCalculator using Java MessageDigest
 */
actual class HashCalculator {
    
    actual suspend fun calculateSHA256(data: ByteArray): String = withContext(Dispatchers.Default) {
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(data)
            hashBytes.toHexString()
        } catch (e: Exception) {
            throw Exception("Error calculating SHA256 hash: ${e.message}", e)
        }
    }
    
    actual suspend fun calculateMD5(data: ByteArray): String = withContext(Dispatchers.Default) {
        try {
            val digest = MessageDigest.getInstance("MD5")
            val hashBytes = digest.digest(data)
            hashBytes.toHexString()
        } catch (e: Exception) {
            throw Exception("Error calculating MD5 hash: ${e.message}", e)
        }
    }
    
    /**
     * Convert byte array to hex string (Android-specific helper)
     */
    private fun ByteArray.toHexString(): String {
        return joinToString("") { "%02x".format(it) }
    }
}