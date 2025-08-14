package com.vicherarr.memora.config

/**
 * Feature Flags Configuration
 * 
 * Centralized configuration for feature toggles and debug options.
 * Following Clean Architecture principles with separation of concerns.
 * 
 * Usage:
 * - DEBUG builds: All testing features enabled
 * - RELEASE builds: Testing features disabled for production
 * 
 * This approach follows:
 * - Single Responsibility: Only handles feature configuration
 * - Open/Closed: Easy to extend with new flags
 * - Dependency Inversion: UI depends on this abstraction
 */
object FeatureFlags {
    
    /**
     * Controls visibility of testing/debug features in UI
     * 
     * @return true if testing features should be visible, false otherwise
     */
    val isTestingEnabled: Boolean
        get() = BuildConfiguration.isDebugBuild
    
    /**
     * Controls detailed logging and debug information
     */
    val isDebugLoggingEnabled: Boolean
        get() = BuildConfiguration.isDebugBuild
    
    /**
     * Controls experimental features that are not ready for production
     */
    val areExperimentalFeaturesEnabled: Boolean
        get() = BuildConfiguration.isDebugBuild
}

/**
 * Build Configuration - Platform specific implementation
 * 
 * Each platform (Android/iOS) provides its own implementation
 * to access build-specific configuration values.
 */
expect object BuildConfiguration {
    val isDebugBuild: Boolean
    val buildType: String
    val versionName: String
    val versionCode: Int
}