package com.vicherarr.memora.domain.usecase

/**
 * Exit App Use Case
 * 
 * Handles the business logic for exiting the application.
 * Following Clean Architecture principles.
 */
interface ExitAppUseCase {
    
    /**
     * Exits the application completely
     */
    suspend fun execute()
}