package com.vicherarr.memora.data.network

/**
 * Android-specific API configuration
 * Android Emulator uses 10.0.2.2 to access host machine localhost
 */
actual fun getBaseUrl(): String = "http://10.0.2.2:${ApiConfig.API_PORT}"