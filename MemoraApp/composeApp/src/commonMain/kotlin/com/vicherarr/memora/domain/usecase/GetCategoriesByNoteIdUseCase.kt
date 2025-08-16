package com.vicherarr.memora.domain.usecase

import com.vicherarr.memora.domain.models.Category
import com.vicherarr.memora.data.database.NoteCategoriesDao
import com.vicherarr.memora.data.mappers.CategoryDomainMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Use Case: Get categories for a specific note
 * Clean Architecture - Domain Layer
 * Single Responsibility: Only handles fetching categories for a note
 */
class GetCategoriesByNoteIdUseCase(
    private val noteCategoriesDao: NoteCategoriesDao,
    private val categoryMapper: CategoryDomainMapper
) {
    
    /**
     * Get categories for a note as Flow (reactive)
     * Following Clean Architecture - returns Domain models
     */
    fun execute(noteId: String): Flow<List<Category>> {
        return noteCategoriesDao.getCategoriesByNoteIdFlow(noteId)
            .map { entities -> 
                entities.map { entity -> categoryMapper.toDomain(entity) }
            }
    }
    
    /**
     * Get categories for a note (one-time query)
     * Following Clean Architecture - returns Domain models
     */
    suspend fun executeOnce(noteId: String): List<Category> {
        return noteCategoriesDao.getCategoriesByNoteId(noteId)
            .map { entity -> categoryMapper.toDomain(entity) }
    }
}