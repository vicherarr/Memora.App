package com.vicherarr.memora.domain.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for SearchFilters domain models
 * Following Clean Code Testing principles:
 * - Test business logic and domain rules
 * - Verify enum values and properties
 * - Test SearchFilters data class behavior
 * - Edge cases and boundary conditions
 */
class SearchFiltersTest {

    // DateFilter enum tests
    
    @Test
    fun dateFilter_all_hasCorrectDisplayName() {
        // Arrange & Act
        val filter = DateFilter.ALL
        
        // Assert
        assertEquals("Todas las fechas", filter.displayName)
    }
    
    @Test
    fun dateFilter_today_hasCorrectDisplayName() {
        // Arrange & Act
        val filter = DateFilter.TODAY
        
        // Assert
        assertEquals("Hoy", filter.displayName)
    }
    
    @Test
    fun dateFilter_week_hasCorrectDisplayName() {
        // Arrange & Act
        val filter = DateFilter.WEEK
        
        // Assert
        assertEquals("Esta semana", filter.displayName)
    }
    
    @Test
    fun dateFilter_month_hasCorrectDisplayName() {
        // Arrange & Act
        val filter = DateFilter.MONTH
        
        // Assert
        assertEquals("Este mes", filter.displayName)
    }
    
    @Test
    fun dateFilter_enumValues_containsAllExpectedValues() {
        // Arrange & Act
        val values = DateFilter.values()
        
        // Assert
        assertEquals(4, values.size)
        assertTrue(values.contains(DateFilter.ALL))
        assertTrue(values.contains(DateFilter.TODAY))
        assertTrue(values.contains(DateFilter.WEEK))
        assertTrue(values.contains(DateFilter.MONTH))
    }

    // FileTypeFilter enum tests
    
    @Test
    fun fileTypeFilter_all_hasCorrectDisplayNameAndIcon() {
        // Arrange & Act
        val filter = FileTypeFilter.ALL
        
        // Assert
        assertEquals("Todas las notas", filter.displayName)
        assertEquals(Icons.Default.Description, filter.icon)
    }
    
    @Test
    fun fileTypeFilter_withImages_hasCorrectDisplayNameAndIcon() {
        // Arrange & Act
        val filter = FileTypeFilter.WITH_IMAGES
        
        // Assert
        assertEquals("Con imágenes", filter.displayName)
        assertEquals(Icons.Default.Image, filter.icon)
    }
    
    @Test
    fun fileTypeFilter_withVideos_hasCorrectDisplayNameAndIcon() {
        // Arrange & Act
        val filter = FileTypeFilter.WITH_VIDEOS
        
        // Assert
        assertEquals("Con videos", filter.displayName)
        assertEquals(Icons.Default.Videocam, filter.icon)
    }
    
    @Test
    fun fileTypeFilter_withAttachments_hasCorrectDisplayNameAndIcon() {
        // Arrange & Act
        val filter = FileTypeFilter.WITH_ATTACHMENTS
        
        // Assert
        assertEquals("Con archivos adjuntos", filter.displayName)
        assertEquals(Icons.Default.Attachment, filter.icon)
    }
    
    @Test
    fun fileTypeFilter_textOnly_hasCorrectDisplayNameAndIcon() {
        // Arrange & Act
        val filter = FileTypeFilter.TEXT_ONLY
        
        // Assert
        assertEquals("Solo texto", filter.displayName)
        assertEquals(Icons.Default.TextFields, filter.icon)
    }

    // SearchFilters data class tests
    
    @Test
    fun searchFilters_defaultConstructor_hasDefaultValues() {
        // Arrange & Act
        val filters = SearchFilters()
        
        // Assert
        assertEquals("", filters.query)
        assertEquals(DateFilter.ALL, filters.dateFilter)
        assertEquals(FileTypeFilter.ALL, filters.fileTypeFilter)
    }
    
    @Test
    fun searchFilters_customConstructor_setsValues() {
        // Arrange & Act
        val filters = SearchFilters(
            query = "test query",
            dateFilter = DateFilter.TODAY,
            fileTypeFilter = FileTypeFilter.WITH_IMAGES
        )
        
        // Assert
        assertEquals("test query", filters.query)
        assertEquals(DateFilter.TODAY, filters.dateFilter)
        assertEquals(FileTypeFilter.WITH_IMAGES, filters.fileTypeFilter)
    }
    
    @Test
    fun searchFilters_hasActiveFilters_whenNoFilters_returnsFalse() {
        // Arrange
        val filters = SearchFilters()
        
        // Act & Assert
        assertFalse(filters.hasActiveFilters)
    }
    
    @Test
    fun searchFilters_hasActiveFilters_whenQueryOnly_returnsTrue() {
        // Arrange
        val filters = SearchFilters(query = "search")
        
        // Act & Assert
        assertTrue(filters.hasActiveFilters)
    }
    
    @Test
    fun searchFilters_hasActiveFilters_whenDateFilterOnly_returnsTrue() {
        // Arrange
        val filters = SearchFilters(dateFilter = DateFilter.TODAY)
        
        // Act & Assert
        assertTrue(filters.hasActiveFilters)
    }
    
    @Test
    fun searchFilters_hasActiveFilters_whenFileTypeFilterOnly_returnsTrue() {
        // Arrange
        val filters = SearchFilters(fileTypeFilter = FileTypeFilter.WITH_IMAGES)
        
        // Act & Assert
        assertTrue(filters.hasActiveFilters)
    }
    
    @Test
    fun searchFilters_hasActiveFilters_whenAllFiltersActive_returnsTrue() {
        // Arrange
        val filters = SearchFilters(
            query = "test",
            dateFilter = DateFilter.WEEK,
            fileTypeFilter = FileTypeFilter.TEXT_ONLY
        )
        
        // Act & Assert
        assertTrue(filters.hasActiveFilters)
    }
    
    @Test
    fun searchFilters_hasActiveFilters_whenBlankQuery_returnsFalse() {
        // Arrange
        val filters = SearchFilters(query = "   ") // Whitespace only
        
        // Act & Assert
        assertFalse(filters.hasActiveFilters)
    }
    
    @Test
    fun searchFilters_reset_returnsDefaultValues() {
        // Arrange
        val filters = SearchFilters(
            query = "test query",
            dateFilter = DateFilter.TODAY,
            fileTypeFilter = FileTypeFilter.WITH_VIDEOS
        )
        
        // Act
        val resetFilters = filters.reset()
        
        // Assert
        assertEquals("", resetFilters.query)
        assertEquals(DateFilter.ALL, resetFilters.dateFilter)
        assertEquals(FileTypeFilter.ALL, resetFilters.fileTypeFilter)
        assertFalse(resetFilters.hasActiveFilters)
    }
    
    @Test
    fun searchFilters_reset_doesNotMutateOriginal() {
        // Arrange
        val originalFilters = SearchFilters(
            query = "test query",
            dateFilter = DateFilter.TODAY,
            fileTypeFilter = FileTypeFilter.WITH_VIDEOS
        )
        
        // Act
        val resetFilters = originalFilters.reset()
        
        // Assert - original should be unchanged
        assertEquals("test query", originalFilters.query)
        assertEquals(DateFilter.TODAY, originalFilters.dateFilter)
        assertEquals(FileTypeFilter.WITH_VIDEOS, originalFilters.fileTypeFilter)
        
        // Reset should be different
        assertEquals("", resetFilters.query)
        assertEquals(DateFilter.ALL, resetFilters.dateFilter)
        assertEquals(FileTypeFilter.ALL, resetFilters.fileTypeFilter)
    }
    
    // Edge case tests
    
    @Test
    fun searchFilters_emptyStringQuery_notConsideredActive() {
        // Arrange
        val filters = SearchFilters(query = "")
        
        // Act & Assert
        assertFalse(filters.hasActiveFilters)
    }
    
    @Test
    fun searchFilters_singleCharacterQuery_consideredActive() {
        // Arrange
        val filters = SearchFilters(query = "a")
        
        // Act & Assert
        assertTrue(filters.hasActiveFilters)
    }
    
    @Test
    fun searchFilters_dataClassEquality_worksCorrectly() {
        // Arrange
        val filters1 = SearchFilters(query = "test", dateFilter = DateFilter.TODAY)
        val filters2 = SearchFilters(query = "test", dateFilter = DateFilter.TODAY)
        val filters3 = SearchFilters(query = "different")
        
        // Act & Assert
        assertEquals(filters1, filters2) // Should be equal
        assertTrue(filters1 != filters3) // Should not be equal
    }
    
    @Test
    fun searchFilters_copy_worksCorrectly() {
        // Arrange
        val original = SearchFilters(query = "test", dateFilter = DateFilter.TODAY)
        
        // Act
        val copied = original.copy(query = "modified")
        
        // Assert
        assertEquals("test", original.query) // Original unchanged
        assertEquals("modified", copied.query) // Copy modified
        assertEquals(DateFilter.TODAY, copied.dateFilter) // Other fields preserved
    }
}