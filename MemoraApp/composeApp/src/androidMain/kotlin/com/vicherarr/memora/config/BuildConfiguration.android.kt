package com.vicherarr.memora.config

/**
 * Android implementation of BuildConfiguration
 * 
 * Provides access to Android BuildConfig values generated during compilation.
 * This follows the expect/actual pattern for platform-specific implementations.
 * 
 * For now, we'll use a simple debug detection method until BuildConfig is properly configured.
 */
actual object BuildConfiguration {
    actual val isDebugBuild: Boolean = kotlin.runCatching {
        Class.forName("com.vicherarr.memora.BuildConfig")
            .getDeclaredField("DEBUG")
            .getBoolean(null)
    }.getOrElse { 
        // Fallback: Detect debug by checking if assertions are enabled
        try {
            assert(false)
            false // Assertions disabled = release
        } catch (e: AssertionError) {
            true // Assertions enabled = debug
        }
    }
    
    actual val buildType: String = if (isDebugBuild) "debug" else "release"
    actual val versionName: String = "1.0.0" // Default version
    actual val versionCode: Int = 1 // Default version code
}