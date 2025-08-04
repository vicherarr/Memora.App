package com.vicherarr.memora.data.database

/**
 * Platform-specific timestamp provider
 * Provides current time in milliseconds without experimental APIs
 */
expect fun getCurrentTimestamp(): Long