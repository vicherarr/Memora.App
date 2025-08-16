package com.vicherarr.memora.domain.usecase

import com.vicherarr.memora.domain.models.Category
import com.vicherarr.memora.data.database.CategoriesDao
import com.vicherarr.memora.data.mappers.CategoryDomainMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Use Case: Get categories for a user
 * Clean Architecture - Domain Layer
 */
class GetCategoriesByUserUseCase(
    private val categoriesDao: CategoriesDao,
    private val categoryMapper: CategoryDomainMapper
) {
    
    fun execute(userId: String): Flow<List<Category>> {
        return categoriesDao.getCategoriesByUserIdFlow(userId)
            .map { entities -> 
                entities.map { entity -> categoryMapper.toDomain(entity) }
            }
    }
    
    suspend fun executeOnce(userId: String): List<Category> {
        return categoriesDao.getCategoriesByUserId(userId)
            .map { entity -> categoryMapper.toDomain(entity) }
    }
}