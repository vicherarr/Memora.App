package com.vicherarr.memora.testing

import com.vicherarr.memora.data.database.CategoriesDao
import com.vicherarr.memora.data.database.NoteCategoriesDao
import com.vicherarr.memora.data.database.getCurrentTimestamp

/**
 * Utilities para testing de categorías en desarrollo
 * Solo para verificar que la migración funciona correctamente
 */
object CategoriesTestUtils {
    
    /**
     * Test rápido para verificar que las tablas de categorías funcionan
     */
    suspend fun testCategoriesTables(
        categoriesDao: CategoriesDao,
        noteCategoriesDao: NoteCategoriesDao,
        userId: String
    ): TestResult {
        return try {
            val timestamp = getCurrentTimestamp()
            val categoryId = "test_${timestamp}"
            val noteId = "note_test_${timestamp}"
            
            // 1. Crear categoría de prueba
            categoriesDao.insertCategory(
                id = categoryId,
                name = "Test Migration",
                color = "#6750A4",
                icon = "test",
                createdAt = timestamp.toString(),
                modifiedAt = timestamp.toString(),
                userId = userId,
                localCreatedAt = timestamp
            )
            
            // 2. Verificar que se creó
            val category = categoriesDao.getCategoryById(categoryId)
            if (category == null) {
                return TestResult.Error("Failed to create category")
            }
            
            // 3. Crear relación nota-categoría
            noteCategoriesDao.insertNoteCategory(
                id = "rel_${timestamp}",
                noteId = noteId,
                categoryId = categoryId,
                createdAt = timestamp.toString(),
                localCreatedAt = timestamp
            )
            
            // 4. Verificar relación
            val exists = noteCategoriesDao.existsNoteCategory(noteId, categoryId)
            if (!exists) {
                return TestResult.Error("Failed to create note-category relationship")
            }
            
            // 5. Limpiar datos de prueba
            categoriesDao.deleteCategory(categoryId)
            
            TestResult.Success("Migration test passed successfully!")
            
        } catch (e: Exception) {
            TestResult.Error("Migration test failed: ${e.message}")
        }
    }
    
    /**
     * Crear categorías de ejemplo para testing
     */
    suspend fun createSampleCategories(
        categoriesDao: CategoriesDao,
        userId: String
    ): List<String> {
        val timestamp = getCurrentTimestamp()
        val categories = listOf(
            Triple("Work", "#6750A4", "work"),
            Triple("Personal", "#FF9800", "person"),
            Triple("Ideas", "#4CAF50", "lightbulb"),
            Triple("Important", "#F44336", "priority_high")
        )
        
        val categoryIds = mutableListOf<String>()
        
        categories.forEachIndexed { index, (name, color, icon) ->
            val categoryId = "sample_${timestamp}_$index"
            categoryIds.add(categoryId)
            
            categoriesDao.insertCategory(
                id = categoryId,
                name = name,
                color = color,
                icon = icon,
                createdAt = timestamp.toString(),
                modifiedAt = timestamp.toString(),
                userId = userId,
                localCreatedAt = timestamp
            )
        }
        
        return categoryIds
    }
}

sealed class TestResult {
    data class Success(val message: String) : TestResult()
    data class Error(val message: String) : TestResult()
}