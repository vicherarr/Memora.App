package com.vicherarr.memora.domain.usecase

import kotlin.system.exitProcess

/**
 * Android implementation for ExitAppUseCase
 * 
 * Handles platform-specific app exit logic for Android.
 */
class ExitAppUseCaseImpl : ExitAppUseCase {
    
    /**
     * Exits the application completely on Android
     */
    override suspend fun execute() {
        exitProcess(0)
    }
}