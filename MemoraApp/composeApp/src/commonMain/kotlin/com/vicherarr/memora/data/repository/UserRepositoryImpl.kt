package com.vicherarr.memora.data.repository

import com.vicherarr.memora.config.BuildConfiguration
import com.vicherarr.memora.data.auth.CloudAuthProvider
import com.vicherarr.memora.data.database.getCurrentTimestamp
import com.vicherarr.memora.domain.model.AuthState
import com.vicherarr.memora.domain.models.AppInfo
import com.vicherarr.memora.domain.models.UserProfile
import com.vicherarr.memora.domain.models.UserStatistics
import com.vicherarr.memora.domain.repository.NotesRepository
import com.vicherarr.memora.domain.repository.UserRepository
import kotlin.random.Random

/**
 * User Repository Implementation
 * 
 * Concrete implementation of UserRepository interface.
 * Following Single Responsibility Principle - only handles user data.
 * Following Dependency Inversion - depends on NotesRepository abstraction.
 */
class UserRepositoryImpl(
    private val notesRepository: NotesRepository,
    private val cloudAuthProvider: CloudAuthProvider
) : UserRepository {
    
    override suspend fun getCurrentUserProfile(): Result<UserProfile> {
        return try {
            // Get real user from Google Auth
            val googleUser = cloudAuthProvider.getCurrentUser()
            
            if (googleUser != null) {
                val userProfile = UserProfile(
                    id = googleUser.id,
                    displayName = googleUser.displayName ?: "Usuario",
                    email = googleUser.email,
                    avatarUrl = googleUser.profilePictureUrl // Real Google avatar
                )
                Result.success(userProfile)
            } else {
                // Fallback if no Google user authenticated
                Result.failure(Exception("Usuario no autenticado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getUserStatistics(): Result<UserStatistics> {
        return try {
            // Calculate statistics from real data
            val statistics = calculateStatisticsFromNotes()
            
            Result.success(statistics)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun logout(): Result<Unit> {
        return try {
            // Use real Google Auth logout
            cloudAuthProvider.signOut()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun isUserAuthenticated(): Boolean {
        return cloudAuthProvider.isAuthenticated()
    }
    
    /**
     * Calculate user statistics from notes data
     * 
     * Private method following Single Responsibility Principle.
     * Business logic encapsulated within the repository.
     */
    private suspend fun calculateStatisticsFromNotes(): UserStatistics {
        return try {
            // Get all notes to calculate statistics
            val allNotesResult = notesRepository.getNotes()
            
            if (allNotesResult.isFailure) {
                // Return default statistics if notes fetch fails
                return getDefaultStatistics()
            }
            
            val notes = allNotesResult.getOrNull() ?: emptyList()
            
            // Calculate total notes
            val totalNotes = notes.size
            
            // Calculate total attachments
            val totalAttachments = notes.sumOf { it.archivosAdjuntos.size }
            
            // Calculate storage usage 
            val localStorageBytes = calculateLocalStorage(notes)
            val isSynced = cloudAuthProvider.isAuthenticated() // Simple sync status
            
            // Calculate notes this month using timestamp comparison
            val currentTimestamp = getCurrentTimestamp()
            val oneMonthAgo = currentTimestamp - (30 * 24 * 60 * 60 * 1000L) // 30 days in milliseconds
            val notesThisMonth = notes.count { note ->
                note.fechaCreacion >= oneMonthAgo
            }
            
            // Get last sync date (simplified - using null for now)
            val lastSyncDate = null // TODO: Implement real sync tracking
            
            UserStatistics(
                totalNotes = totalNotes,
                totalAttachments = totalAttachments,
                localStorageBytes = localStorageBytes,
                isSynced = isSynced,
                notesThisMonth = notesThisMonth,
                lastSyncDate = lastSyncDate
            )
            
        } catch (e: Exception) {
            getDefaultStatistics()
        }
    }
    
    /**
     * Calculate local storage usage
     * 
     * Business logic for storage calculation.
     */
    private fun calculateLocalStorage(notes: List<com.vicherarr.memora.domain.models.Note>): Long {
        // Simplified calculation: estimate based on content and attachments
        val textStorage = notes.sumOf { note ->
            (note.titulo?.length ?: 0) * 2 + note.contenido.length * 2 // 2 bytes per char
        }
        
        val attachmentStorage = notes.sumOf { note ->
            note.archivosAdjuntos.sumOf { attachment ->
                attachment.tamanoBytes ?: 0L
            }
        }
        
        return textStorage.toLong() + attachmentStorage
    }
    
    
    /**
     * Get default statistics when calculation fails
     * 
     * Fallback method following defensive programming.
     */
    private fun getDefaultStatistics(): UserStatistics {
        return UserStatistics(
            totalNotes = 0,
            totalAttachments = 0,
            localStorageBytes = 0L,
            isSynced = false,
            notesThisMonth = 0,
            lastSyncDate = null
        )
    }
    
    /**
     * Get application information
     * 
     * Utility method for app metadata.
     */
    fun getAppInfo(): AppInfo {
        return AppInfo(
            versionName = BuildConfiguration.versionName,
            versionCode = BuildConfiguration.versionCode,
            buildType = BuildConfiguration.buildType,
            termsUrl = "https://memora.app/terms", // Placeholder
            privacyUrl = "https://memora.app/privacy", // Placeholder
            supportEmail = "soporte@memora.app" // Placeholder
        )
    }
}