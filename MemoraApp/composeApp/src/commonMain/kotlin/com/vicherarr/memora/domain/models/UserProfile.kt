package com.vicherarr.memora.domain.models

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/**
 * User Profile Domain Model
 * 
 * Represents user information in the domain layer.
 * Following Clean Architecture principles - pure domain entity.
 */
data class UserProfile(
    val id: String,
    val displayName: String,
    val email: String,
    val avatarUrl: String? = null,
    val memberSince: LocalDate
) {
    /**
     * Business rule: Display name cannot be empty
     */
    init {
        require(displayName.isNotBlank()) { "Display name cannot be blank" }
        require(email.isNotBlank()) { "Email cannot be blank" }
    }
}

/**
 * User Statistics Domain Model
 * 
 * Encapsulates user metrics and statistics.
 * Following Single Responsibility Principle.
 */
data class UserStatistics(
    val totalNotes: Int,
    val totalAttachments: Int,
    val localStorageBytes: Long,
    val remoteStorageBytes: Long,
    val notesThisMonth: Int,
    val lastSyncDate: LocalDateTime?
) {
    /**
     * Business rules for statistics
     */
    init {
        require(totalNotes >= 0) { "Total notes cannot be negative" }
        require(totalAttachments >= 0) { "Total attachments cannot be negative" }
        require(localStorageBytes >= 0) { "Local storage cannot be negative" }
        require(remoteStorageBytes >= 0) { "Remote storage cannot be negative" }
        require(notesThisMonth >= 0) { "Notes this month cannot be negative" }
    }
    
    /**
     * Business logic: Calculate total storage across local and remote
     */
    val totalStorageBytes: Long
        get() = localStorageBytes + remoteStorageBytes
    
    /**
     * Business logic: Format storage size for display
     */
    fun getFormattedLocalStorage(): String = formatBytes(localStorageBytes)
    fun getFormattedRemoteStorage(): String = formatBytes(remoteStorageBytes)
    fun getFormattedTotalStorage(): String = formatBytes(totalStorageBytes)
    
    private fun formatBytes(bytes: Long): String {
        val kb = 1024
        val mb = kb * 1024
        val gb = mb * 1024
        
        return when {
            bytes >= gb -> String.format("%.1f GB", bytes.toDouble() / gb)
            bytes >= mb -> String.format("%.1f MB", bytes.toDouble() / mb)
            bytes >= kb -> String.format("%.1f KB", bytes.toDouble() / kb)
            else -> "$bytes B"
        }
    }
}

/**
 * Application Information Domain Model
 * 
 * Contains app metadata and build information.
 * Following Open/Closed Principle - easy to extend.
 */
data class AppInfo(
    val versionName: String,
    val versionCode: Int,
    val buildType: String,
    val termsUrl: String? = null,
    val privacyUrl: String? = null,
    val supportEmail: String? = null
)