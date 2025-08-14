package com.vicherarr.memora.config

import platform.Foundation.NSBundle

/**
 * iOS implementation of BuildConfiguration
 * 
 * Provides access to iOS Bundle information and configuration values.
 * This follows the expect/actual pattern for platform-specific implementations.
 */
actual object BuildConfiguration {
    actual val isDebugBuild: Boolean = 
        NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleConfiguration") == "Debug"
    
    actual val buildType: String = 
        NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleConfiguration") as? String ?: "Unknown"
    
    actual val versionName: String = 
        NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: "1.0.0"
    
    actual val versionCode: Int = 
        (NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleVersion") as? String)?.toIntOrNull() ?: 1
}