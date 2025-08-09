package com.vicherarr.memora.sync

import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

/**
 * iOS implementation for attachment base directory
 */
actual fun getLocalAttachmentsBaseDir(): String {
    val cacheDirectories = NSSearchPathForDirectoriesInDomains(
        NSCachesDirectory, 
        NSUserDomainMask, 
        true
    )
    return cacheDirectories.firstOrNull() as? String ?: "/tmp"
}