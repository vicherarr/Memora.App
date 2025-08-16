package com.vicherarr.memora.domain.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Domain models for search and filtering functionality
 * Following Clean Architecture principles
 */

/**
 * Represents a custom date range for filtering notes
 * Immutable value object following DDD principles
 */
data class DateRange(
    val startDate: Long,
    val endDate: Long
) {
    init {
        require(startDate <= endDate) { "Start date must be before or equal to end date" }
        require(startDate > 0) { "Start date must be a valid timestamp" }
        require(endDate > 0) { "End date must be a valid timestamp" }
    }
    
    /**
     * Check if a timestamp falls within this date range (inclusive)
     */
    fun contains(timestamp: Long): Boolean {
        return timestamp >= startDate && timestamp <= endDate
    }
    
    /**
     * Get duration of this range in milliseconds
     */
    val durationMillis: Long
        get() = endDate - startDate
}

/**
 * Date-based filtering options for notes
 * Includes presets for common use cases and custom range option
 */
enum class DateFilter(val displayName: String) {
    ALL("Todas las fechas"),
    TODAY("Hoy"),
    WEEK("Esta semana"),
    MONTH("Este mes"),
    LAST_30_DAYS("Últimos 30 días"),
    LAST_90_DAYS("Últimos 90 días"),
    CUSTOM_RANGE("Rango personalizado")
}

/**
 * File type filtering options for notes based on attachments
 */
enum class FileTypeFilter(
    val displayName: String, 
    val icon: ImageVector
) {
    ALL("Todas las notas", Icons.Default.Description),
    WITH_IMAGES("Con imágenes", Icons.Default.Image),
    WITH_VIDEOS("Con videos", Icons.Default.Videocam),
    WITH_ATTACHMENTS("Con archivos adjuntos", Icons.Default.Attachment),
    TEXT_ONLY("Solo texto", Icons.Default.TextFields)
}

/**
 * Category filtering options for notes
 * Supports filtering by specific category or showing uncategorized notes
 */
enum class CategoryFilter(
    val displayName: String,
    val icon: ImageVector
) {
    ALL("Todas las categorías", Icons.Default.Category),
    UNCATEGORIZED("Sin categoría", Icons.Default.FilterList),
    SPECIFIC_CATEGORY("Categoría específica", Icons.Default.Label)
}

/**
 * Complete search filters configuration
 * Immutable data class for UI state
 */
data class SearchFilters(
    val query: String = "",
    val dateFilter: DateFilter = DateFilter.ALL,
    val customDateRange: DateRange? = null,
    val fileTypeFilter: FileTypeFilter = FileTypeFilter.ALL,
    val categoryFilter: CategoryFilter = CategoryFilter.ALL,
    val selectedCategoryId: String? = null
) {
    /**
     * Check if any filters are active (non-default)
     */
    val hasActiveFilters: Boolean
        get() = query.isNotBlank() || 
                dateFilter != DateFilter.ALL || 
                customDateRange != null ||
                fileTypeFilter != FileTypeFilter.ALL ||
                categoryFilter != CategoryFilter.ALL
        
    /**
     * Reset all filters to default state
     */
    fun reset(): SearchFilters = SearchFilters()
    
    /**
     * Get the effective date range based on filter selection
     * Returns the custom range if CUSTOM_RANGE is selected, null otherwise
     */
    val effectiveDateRange: DateRange?
        get() = if (dateFilter == DateFilter.CUSTOM_RANGE) customDateRange else null
        
    /**
     * Get the effective category ID for filtering
     * Returns the selected category ID if SPECIFIC_CATEGORY is selected, null otherwise
     */
    val effectiveCategoryId: String?
        get() = if (categoryFilter == CategoryFilter.SPECIFIC_CATEGORY) selectedCategoryId else null
}