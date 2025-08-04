package com.vicherarr.memora.data.database

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

/**
 * iOS implementation for timestamp provider
 */
actual fun getCurrentTimestamp(): Long {
    return (NSDate().timeIntervalSince1970 * 1000).toLong()
}