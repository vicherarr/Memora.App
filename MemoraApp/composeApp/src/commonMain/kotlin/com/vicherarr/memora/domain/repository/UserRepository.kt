package com.vicherarr.memora.domain.repository

import com.vicherarr.memora.domain.models.UserProfile
import com.vicherarr.memora.domain.models.UserStatistics

/**
 * User Repository Interface
 * 
 * Defines the contract for user-related data operations.
 * Following Dependency Inversion Principle - domain depends on abstractions.
 * Following Interface Segregation Principle - focused interface.
 */
interface UserRepository {
    
    /**
     * Get current user profile information
     * 
     * @return Result containing UserProfile or error
     */
    suspend fun getCurrentUserProfile(): Result<UserProfile>
    
    /**
     * Get user statistics including storage metrics
     * 
     * @return Result containing UserStatistics or error
     */
    suspend fun getUserStatistics(): Result<UserStatistics>
    
    /**
     * Logout current user
     * 
     * @return Result indicating success or failure
     */
    suspend fun logout(): Result<Unit>
    
    /**
     * Check if user is currently authenticated
     * 
     * @return true if authenticated, false otherwise
     */
    suspend fun isUserAuthenticated(): Boolean
}