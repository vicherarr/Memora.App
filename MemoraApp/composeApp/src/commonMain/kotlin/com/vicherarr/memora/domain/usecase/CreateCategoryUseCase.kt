package com.vicherarr.memora.domain.usecase

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
            println("CreateCategoryUseCase: 🔍 INICIANDO creación de categoría '$name' para usuario $userId")
            
            // Check if category already exists (case-insensitive)
            val normalizedName = categoryMapper.normalizeCategoryName(name)
            println("CreateCategoryUseCase: Verificando si existe categoría normalizada: '$normalizedName'")
            
            val existing = categoriesDao.getCategoryByNameAndUserId(normalizedName, userId)
            if (existing != null) {
                println("CreateCategoryUseCase: ❌ CATEGORÍA YA EXISTE: ${existing.id}")
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
            
            println("CreateCategoryUseCase: 💾 INSERTANDO categoría en base de datos:")
            println("  - ID: ${category.id}")
            println("  - Nombre: '${category.name}'")
            println("  - Color: ${category.color}")
            println("  - Usuario: ${category.userId} (TIPO: ${category.userId::class.simpleName})")
            println("  - ¿Es email?: ${category.userId.contains("@")}")
            
            categoriesDao.insertCategory(
                id = category.id,
                name = category.name, // ✅ FIX: Usar nombre original, no normalizado
                color = category.color,
                icon = category.icon,
                createdAt = category.createdAt,
                modifiedAt = category.modifiedAt,
                userId = category.userId,
                syncStatus = "PENDING", // ✅ FIX: Agregar parámetro faltante
                needsUpload = 1, // ✅ FIX: Agregar parámetro faltante
                localCreatedAt = timestamp,
                remoteId = null // ✅ FIX: Agregar parámetro faltante
            )
            
            println("CreateCategoryUseCase: ✅ CATEGORÍA INSERTADA EXITOSAMENTE: ${category.id}")
            
            // Verificar que se insertó correctamente
            val inserted = categoriesDao.getCategoryById(category.id)
            if (inserted != null) {
                println("CreateCategoryUseCase: ✅ VERIFICACIÓN EXITOSA - Categoría existe en DB: ${inserted.name}")
            } else {
                println("CreateCategoryUseCase: ❌ ERROR CRÍTICO - Categoría NO se encuentra después de insertar!")
            }
            
            Result.success(category)
        } catch (e: Exception) {
            println("CreateCategoryUseCase: ❌ EXCEPCIÓN durante creación: ${e.message}")
            Result.failure(e)
        }
    }
}