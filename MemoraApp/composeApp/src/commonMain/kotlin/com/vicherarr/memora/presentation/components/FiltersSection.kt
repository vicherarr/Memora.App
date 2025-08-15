package com.vicherarr.memora.presentation.components

import androidx.compose.foundation.layout.*
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
    modifier: Modifier = Modifier
) {
    var showDateRangePicker by remember { mutableStateOf(false) }
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Filtro por fecha
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
                                onClick = { 
                                    onDateFilterChanged(filter)
                                    if (filter == DateFilter.CUSTOM_RANGE) {
                                        showDateRangePicker = true
                                    }
                                },
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
                                    val startDateFormatted = formatTimestampToDate(customDateRange.startDate)
                                    val endDateFormatted = formatTimestampToDate(customDateRange.endDate)
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
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Filtro por tipo de archivo
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

/**
 * Format timestamp to readable date string
 * Uses simple date formatting consistent with app patterns
 */
private fun formatTimestampToDate(timestamp: Long): String {
    // Simple date formatting using basic math like the rest of the app
    val days = timestamp / (24 * 60 * 60 * 1000L)
    val currentDays = com.vicherarr.memora.data.database.getCurrentTimestamp() / (24 * 60 * 60 * 1000L)
    val daysDiff = (currentDays - days).toInt()
    
    return when {
        daysDiff == 0 -> "Hoy"
        daysDiff == 1 -> "Ayer"  
        daysDiff < 7 -> "Hace ${daysDiff}d"
        daysDiff < 30 -> "Hace ${daysDiff/7}sem"
        else -> "Hace ${daysDiff/30}mes"
    }
}