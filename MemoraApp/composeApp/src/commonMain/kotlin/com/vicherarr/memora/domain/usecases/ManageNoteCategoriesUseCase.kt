package com.vicherarr.memora.domain.usecases

import com.vicherarr.memora.domain.models.Category
import com.vicherarr.memora.data.database.NoteCategoriesDao
import com.vicherarr.memora.data.database.CategoriesDao
import com.vicherarr.memora.data.database.getCurrentTimestamp
import com.vicherarr.memora.data.mappers.CategoryDomainMapper

/**
 * Use Case: Manage note-category relationships
 * Clean Architecture - Domain Layer
 * Single Responsibility: Handle all note-category relationship operations
 */
class ManageNoteCategoriesUseCase(
    private val noteCategoriesDao: NoteCategoriesDao,
    private val categoriesDao: CategoriesDao,
    private val categoryMapper: CategoryDomainMapper
) {
    
    /**
     * Assign categories to a note (replaces existing)
     * Following Clean Architecture and business rules
     */
    suspend fun assignCategoriesToNote(
        noteId: String,
        categoryIds: List<String>
    ): Result<Unit> {
        return try {
            // Use the batch assignment method from DAO
            val timestamp = getCurrentTimestamp().toString()
            noteCategoriesDao.assignCategoriesToNote(noteId, categoryIds, timestamp)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get categories assigned to a specific note
     * Following Clean Architecture - returns Domain models
     */
    suspend fun getCategoriesForNote(noteId: String): Result<List<Category>> {
        return try {
            val categories = noteCategoriesDao.getCategoriesByNoteId(noteId)
                .map { categoryMapper.toDomain(it) }
            Result.success(categories)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Remove a note from a specific category
     * Following Single Responsibility Principle
     */
    suspend fun removeNoteFromCategory(
        noteId: String, 
        categoryId: String
    ): Result<Unit> {
        return try {
            noteCategoriesDao.removeNoteFromCategory(noteId, categoryId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get all categories with their note counts
     * Following Clean Architecture - business logic in domain layer
     */
    suspend fun getCategoriesWithNoteCounts(userId: String): Result<List<CategoryWithNoteCount>> {
        return try {
            val categoryNoteCounts = noteCategoriesDao.countNotesByCategory(userId)
            val categories = categoriesDao.getCategoriesByUserId(userId)
            
            val result = categories.map { category ->
                val noteCount = categoryNoteCounts.find { it.categoryId == category.id }?.noteCount ?: 0
                CategoryWithNoteCount(
                    category = categoryMapper.toDomain(category),
                    noteCount = noteCount
                )
            }
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Cleanup orphaned categories (categories with no notes)
     * Following business rules for data integrity
     */
    suspend fun cleanupUnusedCategories(userId: String): Result<Int> {
        return try {
            val orphanedCategories = noteCategoriesDao.getOrphanedCategories(userId)
            orphanedCategories.forEach { category ->
                categoriesDao.deleteCategory(category.id)
            }
            Result.success(orphanedCategories.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Domain model for category with note count
 * Clean Architecture - Domain Layer
 */
data class CategoryWithNoteCount(
    val category: Category,
    val noteCount: Int
)