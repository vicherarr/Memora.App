package com.vicherarr.memora.sync

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * iOS-specific implementation of HashCalculator (simplified mock version)
 * TODO: Implement proper CommonCrypto integration when needed
 */
actual class HashCalculator {
    
    actual suspend fun calculateSHA256(data: ByteArray): String = withContext(Dispatchers.Default) {
        // Simplified mock implementation using contentHashCode
        // In production, this should use CommonCrypto
        val hash = data.contentHashCode().toString(16).padStart(8, '0')
        // Pad to 64 characters to look like SHA256
        hash.repeat(8).take(64)
    }
    
    actual suspend fun calculateMD5(data: ByteArray): String = withContext(Dispatchers.Default) {
        // Simplified mock implementation using contentHashCode
        // In production, this should use CommonCrypto
        val hash = data.contentHashCode().toString(16).padStart(8, '0')
        // Pad to 32 characters to look like MD5
        hash.repeat(4).take(32)
    }
}