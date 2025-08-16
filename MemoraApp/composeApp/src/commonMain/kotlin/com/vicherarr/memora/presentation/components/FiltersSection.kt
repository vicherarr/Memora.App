package com.vicherarr.memora.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vicherarr.memora.domain.models.DateFilter
import com.vicherarr.memora.domain.models.DateRange
import com.vicherarr.memora.domain.models.FileTypeFilter
import com.vicherarr.memora.domain.models.CategoryFilter
import com.vicherarr.memora.domain.models.Category
import com.vicherarr.memora.domain.utils.DateTimeUtils

/**
 * Reusable filters section component for advanced search
 * Following Material Design 3 guidelines and Clean Architecture
 */
@Composable
fun FiltersSection(
    selectedDateFilter: DateFilter,
    onDateFilterChanged: (DateFilter) -> Unit,
    selectedFileType: FileTypeFilter,
    onFileTypeChanged: (FileTypeFilter) -> Unit,
    customDateRange: DateRange? = null,
    onCustomDateRangeChanged: (DateRange?) -> Unit = {},
    selectedCategoryFilter: CategoryFilter,
    onCategoryFilterChanged: (CategoryFilter) -> Unit,
    selectedCategoryId: String?,
    onSelectedCategoryChanged: (String?) -> Unit,
    availableCategories: List<Category>,
    modifier: Modifier = Modifier
) {
    var showDateRangePicker by remember { mutableStateOf(false) }
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(16.dp)
                .heightIn(max = 400.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                CategoryFilterSection(
                    selectedCategoryFilter = selectedCategoryFilter,
                    onCategoryFilterChanged = onCategoryFilterChanged,
                    selectedCategoryId = selectedCategoryId,
                    onSelectedCategoryChanged = onSelectedCategoryChanged,
                    availableCategories = availableCategories,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            item {
                DateFilterSection(
                    selectedDateFilter = selectedDateFilter,
                    onDateFilterChanged = { filter ->
                        onDateFilterChanged(filter)
                        if (filter == DateFilter.CUSTOM_RANGE) {
                            showDateRangePicker = true
                        }
                    },
                    customDateRange = customDateRange
                )
            }
            
            item {
                FileTypeFilterSection(
                    selectedFileType = selectedFileType,
                    onFileTypeChanged = onFileTypeChanged
                )
            }
        }
    }
    
    // Date range picker dialog
    DateRangePickerDialog(
        isVisible = showDateRangePicker,
        currentRange = customDateRange,
        onRangeSelected = { range ->
            onCustomDateRangeChanged(range)
        },
        onDismiss = { showDateRangePicker = false }
    )
}

@Composable
private fun DateFilterSection(
    selectedDateFilter: DateFilter,
    onDateFilterChanged: (DateFilter) -> Unit,
    customDateRange: DateRange?
) {
    Text(
        text = "Filtrar por fecha",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    
    Column(Modifier.selectableGroup()) {
        DateFilter.values().forEach { filter ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (selectedDateFilter == filter),
                        onClick = { onDateFilterChanged(filter) },
                        role = Role.RadioButton
                    )
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (selectedDateFilter == filter),
                    onClick = null
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    if (filter == DateFilter.CUSTOM_RANGE) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Column {
                        Text(
                            text = filter.displayName,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        // Show current range if CUSTOM_RANGE is selected
                        if (filter == DateFilter.CUSTOM_RANGE && 
                            selectedDateFilter == DateFilter.CUSTOM_RANGE && 
                            customDateRange != null) {
                            val startDateFormatted = DateTimeUtils.formatRelativeTime(customDateRange.startDate)
                            val endDateFormatted = DateTimeUtils.formatRelativeTime(customDateRange.endDate)
                            Text(
                                text = "$startDateFormatted → $endDateFormatted",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FileTypeFilterSection(
    selectedFileType: FileTypeFilter,
    onFileTypeChanged: (FileTypeFilter) -> Unit
) {
    Text(
        text = "Filtrar por contenido",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    
    Column(Modifier.selectableGroup()) {
        FileTypeFilter.values().forEach { filter ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (selectedFileType == filter),
                        onClick = { onFileTypeChanged(filter) },
                        role = Role.RadioButton
                    )
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (selectedFileType == filter),
                    onClick = null
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Icon(
                        imageVector = filter.icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = filter.displayName,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

