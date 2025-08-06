package com.vicherarr.memora.data.network

import platform.UIKit.UIDevice
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSBundle
import platform.Foundation.NSProcessInfo

/**
 * iOS-specific API configuration
 * Automatically detects simulator vs real device to use appropriate IP
 */
object IosApiConfig {
    // Simulator can access localhost directly (shares host network)
    private const val SIMULATOR_HOST = "localhost"
    
    // Real device IP (from common ApiConfig)
    private val DEVICE_HOST = ApiConfig.DEV_MACHINE_IP
    
    /**
     * Detects if running on iOS simulator
     * Uses NSProcessInfo environment variables (most reliable method)
     * iOS Simulator always sets specific environment variables that physical devices don't have
     */
    private fun isSimulator(): Boolean {
        return try {
            val processInfo = NSProcessInfo.processInfo
            val environment = processInfo.environment
            
            // iOS Simulator always sets these environment variables:
            // SIMULATOR_ROOT, SIMULATOR_DEVICE_NAME, SIMULATOR_VERSION
            environment["SIMULATOR_ROOT"] != null ||
            environment["SIMULATOR_DEVICE_NAME"] != null ||
            environment["IPHONE_SIMULATOR_ROOT"] != null ||
            environment["SIMULATOR_HOST_HOME"] != null
            
        } catch (e: Exception) {
            // Fallback - if we can't access environment, assume physical device
            false
        }
    }
    
    fun getHost(): String {
        val isSimulatorDetected = isSimulator()
        val selectedHost = if (isSimulatorDetected) SIMULATOR_HOST else DEVICE_HOST
        
        // Debug log (remove in production)
        println("iOS Device Detection - isSimulator: $isSimulatorDetected, using host: $selectedHost")
        
        return selectedHost
    }
}

actual fun getBaseUrl(): String = "http://${IosApiConfig.getHost()}:${ApiConfig.API_PORT}"