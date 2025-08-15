package com.vicherarr.memora.domain.usecase

import kotlin.system.exitProcess

/**
 * iOS implementation for ExitAppUseCase
 * 
 * Handles platform-specific app exit logic for iOS.
 */
class ExitAppUseCaseImpl : ExitAppUseCase {
    
    /**
     * Exits the application completely on iOS
     */
    override suspend fun execute() {
        exitProcess(0)
    }
}