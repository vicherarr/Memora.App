package com.vicherarr.memora.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vicherarr.memora.domain.models.DateFilter
import com.vicherarr.memora.domain.models.DateRange
import com.vicherarr.memora.domain.models.FileTypeFilter
import com.vicherarr.memora.domain.models.CategoryFilter
import com.vicherarr.memora.domain.models.Category
import com.vicherarr.memora.domain.models.displayName

/**
 * Component to display active search filters as elegant chips
 * Following Material Design 3 guidelines and Clean Architecture
 * Provides clear visual feedback of current filtering state
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveFiltersChips(
    searchQuery: String,
    selectedDateFilter: DateFilter,
    customDateRange: DateRange?,
    selectedFileType: FileTypeFilter,
    selectedCategoryFilter: CategoryFilter,
    selectedCategoryId: String?,
    availableCategories: List<Category>,
    onClearSearch: () -> Unit,
    onClearDateFilter: () -> Unit,
    onClearFileTypeFilter: () -> Unit,
    onClearCategoryFilter: () -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Determine active filters
    val activeFilters = remember(searchQuery, selectedDateFilter, selectedFileType, customDateRange, selectedCategoryFilter, selectedCategoryId) {
        buildList {
            // Search query filter
            if (searchQuery.isNotBlank()) {
                add(ActiveFilter.Search(searchQuery, onClearSearch))
            }
            
            // Date filter
            if (selectedDateFilter != DateFilter.ALL) {
                add(ActiveFilter.Date(selectedDateFilter, customDateRange, onClearDateFilter))
            }
            
            // File type filter
            if (selectedFileType != FileTypeFilter.ALL) {
                add(ActiveFilter.FileType(selectedFileType, onClearFileTypeFilter))
            }
            
            // Category filter
            if (selectedCategoryFilter != CategoryFilter.ALL) {
                val selectedCategory = if (selectedCategoryFilter == CategoryFilter.SPECIFIC_CATEGORY && selectedCategoryId != null) {
                    availableCategories.find { it.id == selectedCategoryId }
                } else null
                add(ActiveFilter.Category(selectedCategoryFilter, selectedCategory, onClearCategoryFilter))
            }
        }
    }
    
    // Show chips only if there are active filters
    AnimatedVisibility(
        visible = activeFilters.isNotEmpty(),
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filtros activos",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                
                // Clear all button
                if (activeFilters.size > 1) {
                    TextButton(
                        onClick = onClearAll,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Limpiar todo",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Active filter chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(activeFilters, key = { it.id }) { filter ->
                    AnimatedVisibility(
                        visible = true,
                        enter = scaleIn(animationSpec = tween(200)) + fadeIn(animationSpec = tween(200)),
                        exit = scaleOut(animationSpec = tween(150)) + fadeOut(animationSpec = tween(150))
                    ) {
                        ActiveFilterChip(
                            filter = filter
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActiveFilterChip(
    filter: ActiveFilter,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = true,
        onClick = filter.onClear,
        label = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = filter.icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Column {
                    Text(
                        text = filter.displayText,
                        style = MaterialTheme.typography.labelMedium
                    )
                    // Show exact dates for date filters
                    if (filter is ActiveFilter.Date && filter.exactDates != null) {
                        Text(
                            text = filter.exactDates,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Quitar filtro",
                modifier = Modifier.size(14.dp)
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedTrailingIconColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = true,
            borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        ),
        modifier = modifier
    )
}

/**
 * Sealed class representing different types of active filters
 * Following Clean Architecture - Domain models for UI state
 */
private sealed class ActiveFilter(
    val id: String,
    val displayText: String,
    val icon: ImageVector,
    val onClear: () -> Unit
) {
    class Search(
        query: String,
        onClear: () -> Unit
    ) : ActiveFilter(
        id = "search",
        displayText = if (query.length > 20) "\"${query.take(17)}...\"" else "\"$query\"",
        icon = Icons.Default.Search,
        onClear = onClear
    )
    
    class Date(
        dateFilter: DateFilter,
        customRange: DateRange?,
        onClear: () -> Unit
    ) : ActiveFilter(
        id = "date",
        displayText = when (dateFilter) {
            DateFilter.CUSTOM_RANGE -> customRange?.let {
                "${formatTimestampToShortDate(it.startDate)} - ${formatTimestampToShortDate(it.endDate)}"
            } ?: dateFilter.displayName
            else -> dateFilter.displayName
        },
        icon = Icons.Default.DateRange,
        onClear = onClear
    ) {
        val exactDates: String? = when (dateFilter) {
            DateFilter.CUSTOM_RANGE -> customRange?.let {
                "${formatTimestampToExactDate(it.startDate)} - ${formatTimestampToExactDate(it.endDate)}"
            }
            DateFilter.TODAY -> {
                val today = formatTimestampToExactDate(com.vicherarr.memora.data.database.getCurrentTimestamp())
                today
            }
            DateFilter.WEEK, DateFilter.MONTH, DateFilter.LAST_30_DAYS, DateFilter.LAST_90_DAYS -> {
                val now = com.vicherarr.memora.data.database.getCurrentTimestamp()
                val pastTimestamp = when (dateFilter) {
                    DateFilter.WEEK -> now - (7 * 24 * 60 * 60 * 1000L)
                    DateFilter.MONTH -> now - (30 * 24 * 60 * 60 * 1000L)
                    DateFilter.LAST_30_DAYS -> now - (30 * 24 * 60 * 60 * 1000L)
                    DateFilter.LAST_90_DAYS -> now - (90 * 24 * 60 * 60 * 1000L)
                    else -> now
                }
                "${formatTimestampToExactDate(pastTimestamp)} - ${formatTimestampToExactDate(now)}"
            }
            else -> null
        }
    }
    
    class FileType(
        fileTypeFilter: FileTypeFilter,
        onClear: () -> Unit
    ) : ActiveFilter(
        id = "filetype",
        displayText = fileTypeFilter.displayName,
        icon = fileTypeFilter.icon,
        onClear = onClear
    )
    
    class Category(
        categoryFilter: CategoryFilter,
        selectedCategory: com.vicherarr.memora.domain.models.Category?,
        onClear: () -> Unit
    ) : ActiveFilter(
        id = "category",
        displayText = when (categoryFilter) {
            CategoryFilter.ALL -> categoryFilter.displayName
            CategoryFilter.UNCATEGORIZED -> categoryFilter.displayName
            CategoryFilter.SPECIFIC_CATEGORY -> selectedCategory?.displayName ?: categoryFilter.displayName
        },
        icon = categoryFilter.icon,
        onClear = onClear
    )
}

/**
 * Format timestamp to readable date for chip display
 * Uses clear, descriptive formatting
 */
private fun formatTimestampToShortDate(timestamp: Long): String {
    val now = com.vicherarr.memora.data.database.getCurrentTimestamp()
    val daysDiff = ((now - timestamp) / (24 * 60 * 60 * 1000L)).toInt()
    
    return when {
        daysDiff == 0 -> "Hoy"
        daysDiff == 1 -> "Ayer"
        daysDiff < 7 -> when (daysDiff) {
            2 -> "Anteayer"
            else -> "Hace $daysDiff días"
        }
        daysDiff < 14 -> "1 semana"
        daysDiff < 21 -> "2 semanas"
        daysDiff < 28 -> "3 semanas"
        daysDiff < 60 -> "1 mes"
        daysDiff < 90 -> "2 meses"
        daysDiff < 120 -> "3 meses"
        daysDiff < 365 -> "${daysDiff/30} meses"
        else -> "${daysDiff/365} años"
    }
}

/**
 * Format timestamp to exact date (DD/MM/YYYY)
 * Uses simple math for cross-platform compatibility
 */
private fun formatTimestampToExactDate(timestamp: Long): String {
    // Convert timestamp to days since epoch
    val days = (timestamp / (24 * 60 * 60 * 1000L)).toInt()
    
    // Simple epoch calculation (days since 1970-01-01)
    // This is a basic implementation for display purposes
    val epochYear = 1970
    val epochDays = days
    
    // Calculate approximate year (365.25 days per year)
    val yearsSinceEpoch = (epochDays / 365.25).toInt()
    val year = epochYear + yearsSinceEpoch
    
    // Calculate remaining days in year
    val daysInYear = epochDays - (yearsSinceEpoch * 365.25).toInt()
    
    // Simple month/day calculation (approximate)
    val month = ((daysInYear / 30.44) + 1).toInt().coerceIn(1, 12)
    val dayOfMonth = (daysInYear % 30.44).toInt() + 1
    
    return "${dayOfMonth.toString().padStart(2, '0')}/${month.toString().padStart(2, '0')}/$year"
}