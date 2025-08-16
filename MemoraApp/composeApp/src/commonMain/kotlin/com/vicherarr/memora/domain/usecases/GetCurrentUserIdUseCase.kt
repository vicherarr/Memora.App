package com.vicherarr.memora.domain.usecases

/**
 * Use Case: Get Current User ID
 * Clean Architecture - Domain Layer
 * 
 * Provides the current authenticated user's ID in a consistent format
 * across the entire application. Abstracts away authentication implementation
 * details from the presentation and other domain layers.
 * 
 * Returns the user's email as the consistent userId format used throughout
 * the application for data storage and retrieval.
 */
interface GetCurrentUserIdUseCase {
    
    /**
     * Get the current authenticated user's ID
     * 
     * @return The user ID (email) if authenticated, null if not authenticated
     */
    suspend fun execute(): String?
}