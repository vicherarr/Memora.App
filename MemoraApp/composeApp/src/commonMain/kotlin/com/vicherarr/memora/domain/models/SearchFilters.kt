package com.vicherarr.memora.domain.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Domain models for search and filtering functionality
 * Following Clean Architecture principles
 */

/**
 * Date-based filtering options for notes
 */
enum class DateFilter(val displayName: String) {
    ALL("Todas las fechas"),
    TODAY("Hoy"),
    WEEK("Esta semana"),
    MONTH("Este mes")
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
 * Complete search filters configuration
 * Immutable data class for UI state
 */
data class SearchFilters(
    val query: String = "",
    val dateFilter: DateFilter = DateFilter.ALL,
    val fileTypeFilter: FileTypeFilter = FileTypeFilter.ALL
) {
    /**
     * Check if any filters are active (non-default)
     */
    val hasActiveFilters: Boolean
        get() = query.isNotBlank() || dateFilter != DateFilter.ALL || fileTypeFilter != FileTypeFilter.ALL
        
    /**
     * Reset all filters to default state
     */
    fun reset(): SearchFilters = SearchFilters()
}