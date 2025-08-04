package com.vicherarr.memora.data.database

/**
 * Android implementation for timestamp provider
 */
actual fun getCurrentTimestamp(): Long {
    return System.currentTimeMillis()
}