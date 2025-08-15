package com.vicherarr.memora.domain.usecase

/**
 * Factory for creating platform-specific ExitAppUseCase implementation
 */
expect fun createExitAppUseCase(): ExitAppUseCase