package com.vicherarr.memora.data.network

/**
 * Android-specific API configuration
 * Android Emulator uses 10.0.2.2 to access host machine localhost
 */
actual fun getBaseUrl(): String = "http://192.168.1.153:${ApiConfig.API_PORT}"