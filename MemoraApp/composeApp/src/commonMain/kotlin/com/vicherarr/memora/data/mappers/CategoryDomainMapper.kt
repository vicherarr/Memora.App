package com.vicherarr.memora.data.mappers

import com.vicherarr.memora.domain.models.Category
import com.vicherarr.memora.database.Categories
import com.vicherarr.memora.data.database.getCurrentTimestamp

/**
 * Domain Mapper for Categories
 * Clean Architecture - Data Layer
 */
class CategoryDomainMapper {
    
    /**
     * Normalize category name to avoid case-sensitive duplicates
     * "Trabajo" = "TRABAJO" = "traBAJo"
     */
    fun normalizeCategoryName(name: String): String {
        return name.trim().lowercase()
    }
    
    /**
     * Check if two category names are equivalent (case-insensitive)
     */
    fun areNamesEquivalent(name1: String, name2: String): Boolean {
        return normalizeCategoryName(name1) == normalizeCategoryName(name2)
    }
    
    fun toDomain(entity: Categories): Category {
        return Category(
            id = entity.id,
            name = entity.name, // ✅ FIX: No cambiar case - usar nombre original
            color = entity.color,
            icon = entity.icon,
            createdAt = entity.created_at,
            modifiedAt = entity.modified_at,
            userId = entity.user_id,
            syncStatus = entity.sync_status,
            needsUpload = entity.needs_upload == 1L
        )
    }
    
    fun toEntity(domain: Category): Categories {
        return Categories(
            id = domain.id,
            name = domain.name, // ✅ FIX: No normalizar aquí - usar nombre original
            color = domain.color,
            icon = domain.icon,
            created_at = domain.createdAt,
            modified_at = domain.modifiedAt,
            user_id = domain.userId,
            sync_status = domain.syncStatus,
            needs_upload = if (domain.needsUpload) 1L else 0L,
            local_created_at = getCurrentTimestamp(),
            last_sync_attempt = null,
            remote_id = null
        )
    }
}