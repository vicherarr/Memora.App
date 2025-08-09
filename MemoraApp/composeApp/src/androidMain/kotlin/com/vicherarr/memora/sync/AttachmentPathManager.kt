package com.vicherarr.memora.sync

import android.content.Context
import org.koin.mp.KoinPlatform

/**
 * Android implementation for attachment base directory
 */
actual fun getLocalAttachmentsBaseDir(): String {
    val context: Context = KoinPlatform.getKoin().get()
    return context.cacheDir.absolutePath
}