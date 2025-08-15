package com.vicherarr.memora.domain.usecases

import com.vicherarr.memora.domain.models.Category
import com.vicherarr.memora.data.database.CategoriesDao
import com.vicherarr.memora.data.database.getCurrentTimestamp
import com.vicherarr.memora.data.mappers.CategoryDomainMapper

/**
 * Use Case: Create new category
 * Clean Architecture - Domain Layer
 */
class CreateCategoryUseCase(
    private val categoriesDao: CategoriesDao,
    private val categoryMapper: CategoryDomainMapper
) {
    
    suspend fun execute(
        name: String,
        userId: String,
        color: String = "#6750A4",
        icon: String? = null
    ): Result<Category> {
        return try {
            // Check if category already exists (case-insensitive)
            val existing = categoriesDao.getCategoryByNameAndUserId(name.trim().lowercase(), userId)
            if (existing != null) {
                return Result.failure(Exception("Category already exists"))
            }
            
            val timestamp = getCurrentTimestamp()
            val categoryId = "cat_${timestamp}_${userId.take(8)}"
            
            val category = Category(
                id = categoryId,
                name = name.trim(),
                color = color,
                icon = icon,
                createdAt = timestamp.toString(),
                modifiedAt = timestamp.toString(),
                userId = userId
            )
            
            categoriesDao.insertCategory(
                id = category.id,
                name = categoryMapper.normalizeCategoryName(category.name),
                color = category.color,
                icon = category.icon,
                createdAt = category.createdAt,
                modifiedAt = category.modifiedAt,
                userId = category.userId,
                localCreatedAt = timestamp
            )
            
            Result.success(category)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}