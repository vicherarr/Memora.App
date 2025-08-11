package com.vicherarr.memora.presentation.states

import com.vicherarr.memora.domain.models.SearchFilters
import com.vicherarr.memora.domain.models.Note

/**
 * UI State for search functionality
 * Following MVVM pattern and Clean Architecture principles
 */
data class SearchUiState(
    val searchFilters: SearchFilters = SearchFilters(),
    val showFilters: Boolean = false,
    val isSearching: Boolean = false,
    val searchResults: List<Note> = emptyList(),
    val errorMessage: String? = null
) {
    /**
     * Helper property to check if search is active
     */
    val isSearchActive: Boolean
        get() = searchFilters.hasActiveFilters
        
    /**
     * Helper property to get search results count for display
     */
    val resultsCount: Int
        get() = searchResults.size
}