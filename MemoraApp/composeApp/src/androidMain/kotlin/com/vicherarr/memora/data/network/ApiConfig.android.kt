package com.vicherarr.memora.data.network

import android.os.Build

/**
 * Android-specific API configuration
 * Automatically detects emulator vs real device to use appropriate IP
 */
object AndroidApiConfig {
    // Emulator IP (points to host machine localhost)
    private const val EMULATOR_HOST = "10.0.2.2"
    
    // Real device IP (from common ApiConfig)
    private val DEVICE_HOST = ApiConfig.DEV_MACHINE_IP
    
    /**
     * Detects if running on Android emulator
     * Multiple checks for reliability across different emulator types
     */
    private fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.PRODUCT.contains("sdk_google")
                || Build.PRODUCT.contains("google_sdk")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("sdk_x86")
                || Build.PRODUCT.contains("vbox86p")
                || Build.PRODUCT.contains("emulator")
                || Build.PRODUCT.contains("simulator"))
    }
    
    fun getHost(): String = if (isEmulator()) EMULATOR_HOST else DEVICE_HOST
}

actual fun getBaseUrl(): String = "http://${AndroidApiConfig.getHost()}:${ApiConfig.API_PORT}"