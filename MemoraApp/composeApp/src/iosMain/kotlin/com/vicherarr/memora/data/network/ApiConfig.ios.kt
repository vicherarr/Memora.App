package com.vicherarr.memora.data.network

/**
 * iOS-specific API configuration  
 * iOS Simulator can access localhost directly (shares host network)
 */
actual fun getBaseUrl(): String = "http://localhost:${ApiConfig.API_PORT}"