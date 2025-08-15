package com.vicherarr.memora.data.database

import com.vicherarr.memora.database.Categories
import com.vicherarr.memora.database.MemoraDatabase
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Categories table
 * Handles CRUD operations and sync management for user-defined categories
 * Following Clean Architecture and SOLID principles
 */
class CategoriesDao(private val database: MemoraDatabase) {
    
    private val queries = database.categoriesQueries
    
    // === CRUD Operations ===
    
    /**
     * Insert a new category
     */
    suspend fun insertCategory(
        id: String,
        name: String,
        color: String,
        icon: String?,
        createdAt: String,
        modifiedAt: String,
        userId: String,
        syncStatus: String = "PENDING",
        needsUpload: Long = 1,
        localCreatedAt: Long,
        remoteId: String? = null
    ) {
        queries.insertCategory(
            id = id,
            name = name,
            color = color,
            icon = icon,
            created_at = createdAt,
            modified_at = modifiedAt,
            user_id = userId,
            sync_status = syncStatus,
            needs_upload = needsUpload,
            local_created_at = localCreatedAt,
            remote_id = remoteId
        )
    }
    
    /**
     * Update an existing category
     */
    suspend fun updateCategory(
        id: String,
        name: String,
        color: String,
        icon: String?,
        modifiedAt: String
    ) {
        queries.updateCategory(
            name = name,
            color = color,
            icon = icon,
            modified_at = modifiedAt,
            id = id
        )
    }
    
    /**
     * Delete a category by ID
     */
    suspend fun deleteCategory(categoryId: String) {
        queries.deleteCategory(categoryId)
    }
    
    /**
     * Get category by ID
     */
    suspend fun getCategoryById(categoryId: String): Categories? {
        return queries.getCategoryById(categoryId).executeAsOneOrNull()
    }
    
    // === User-specific Queries ===
    
    /**
     * Get all categories for a specific user
     */
    suspend fun getCategoriesByUserId(userId: String): List<Categories> {
        return queries.getCategoriesByUserId(userId).executeAsList()
    }
    
    /**
     * Get categories as Flow for reactive UI updates
     */
    fun getCategoriesByUserIdFlow(userId: String): Flow<List<Categories>> {
        return queries.getCategoriesByUserIdFlow(userId).asFlow().mapToList(Dispatchers.IO)
    }
    
    // === Category Management ===
    
    /**
     * Find category by name and user ID
     * Used for preventing duplicates and finding existing categories
     */
    suspend fun getCategoryByNameAndUserId(name: String, userId: String): Categories? {
        return queries.getCategoryByNameAndUserId(name, userId).executeAsOneOrNull()
    }
    
    /**
     * Get categories by list of IDs
     * Used for efficient category lookup in note listings
     */
    suspend fun getCategoriesByIds(categoryIds: List<String>): List<Categories> {
        return if (categoryIds.isEmpty()) {
            emptyList()
        } else {
            queries.getCategoriesByIds(categoryIds).executeAsList()
        }
    }
    
    /**
     * Get categories with note count
     * Useful for UI displaying how many notes each category has
     */
    suspend fun getCategoriesWithNoteCount(userId: String): List<CategoryWithNoteCount> {
        return queries.getCategoriesWithNoteCount(userId).executeAsList().map { row ->
            CategoryWithNoteCount(
                category = Categories(
                    id = row.id,
                    name = row.name,
                    color = row.color,
                    icon = row.icon,
                    created_at = row.created_at,
                    modified_at = row.modified_at,
                    user_id = row.user_id,
                    sync_status = row.sync_status,
                    needs_upload = row.needs_upload,
                    local_created_at = row.local_created_at,
                    last_sync_attempt = row.last_sync_attempt,
                    remote_id = row.remote_id
                ),
                notesCount = row.notesCount?.toInt() ?: 0
            )
        }
    }
    
    /**
     * Get categories that have no notes assigned
     * Used for cleanup operations
     */
    suspend fun getUnusedCategories(userId: String): List<Categories> {
        return queries.getUnusedCategories(userId).executeAsList()
    }
    
    // === Sync Operations ===
    
    /**
     * Get categories pending synchronization
     */
    suspend fun getCategoriesPendingSync(userId: String): List<Categories> {
        return queries.getCategoriesPendingSync(userId).executeAsList()
    }
    
    /**
     * Mark category as successfully synced
     */
    suspend fun markCategoryAsSynced(categoryId: String) {
        val currentTimestamp = getCurrentTimestamp()
        queries.markCategoryAsSynced(currentTimestamp, categoryId)
    }
    
    /**
     * Mark category sync as failed
     */
    suspend fun markCategoryAsFailed(categoryId: String) {
        val currentTimestamp = getCurrentTimestamp()
        queries.markCategoryAsFailed(currentTimestamp, categoryId)
    }
    
    /**
     * Update remote ID after successful upload
     */
    suspend fun updateRemoteId(categoryId: String, remoteId: String) {
        queries.updateRemoteId(remoteId, categoryId)
    }
    
    // === Statistics for Sync Metadata ===
    
    /**
     * Get total count of categories for a user
     */
    suspend fun getCategoriesCount(userId: String): Long {
        return queries.getCategoriesCount(userId).executeAsOne()
    }
    
    /**
     * Get hash data for sync metadata calculation
     */
    suspend fun getCategoriesHashData(userId: String): List<CategoryHashData> {
        return queries.getCategoriesHashData(userId).executeAsList().map { row ->
            CategoryHashData(
                id = row.id,
                name = row.name,
                color = row.color,
                modifiedAt = row.modified_at
            )
        }
    }
}

/**
 * Data class for category with note count
 */
data class CategoryWithNoteCount(
    val category: Categories,
    val notesCount: Int
)

/**
 * Data class for hash calculation
 */
data class CategoryHashData(
    val id: String,
    val name: String,
    val color: String,
    val modifiedAt: String
)