package com.vicherarr.memora.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.vicherarr.memora.data.database.getCurrentTimestamp
import com.vicherarr.memora.domain.models.DateRange

/**
 * Dialog for selecting a custom date range
 * Material 3 design with mobile-optimized UX
 * Following MVVM pattern and Clean Architecture
 * Uses simple Long timestamps like the rest of the app
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerDialog(
    isVisible: Boolean,
    currentRange: DateRange? = null,
    onRangeSelected: (DateRange) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isVisible) return
    
    // Use current time and simple Long arithmetic for dates
    val now = getCurrentTimestamp()
    val sevenDaysAgo = now - (7 * 24 * 60 * 60 * 1000L) // 7 days in milliseconds
    
    var startTimestamp by remember(currentRange) { 
        mutableStateOf(currentRange?.startDate ?: sevenDaysAgo)
    }
    
    var endTimestamp by remember(currentRange) { 
        mutableStateOf(currentRange?.endDate ?: now)
    }
    
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    
    // Validation
    val isValidRange = startTimestamp <= endTimestamp
    val startDateState = rememberDatePickerState(
        initialSelectedDateMillis = startTimestamp
    )
    val endDateState = rememberDatePickerState(
        initialSelectedDateMillis = endTimestamp
    )
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Seleccionar rango de fechas",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Date selectors
                DateSelectorRow(
                    label = "Fecha de inicio",
                    selectedTimestamp = startTimestamp,
                    onClick = { showStartDatePicker = true }
                )
                
                DateSelectorRow(
                    label = "Fecha de fin",
                    selectedTimestamp = endTimestamp,
                    onClick = { showEndDatePicker = true }
                )
                
                // Error message for invalid range
                if (!isValidRange) {
                    Text(
                        text = "La fecha de inicio debe ser anterior o igual a la fecha de fin",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    
                    FilledTonalButton(
                        onClick = {
                            val range = DateRange(
                                startDate = startTimestamp,
                                endDate = endTimestamp
                            )
                            onRangeSelected(range)
                            onDismiss()
                        },
                        enabled = isValidRange
                    ) {
                        Text("Aplicar")
                    }
                }
            }
        }
    }
    
    // Start date picker
    if (showStartDatePicker) {
        DatePickerDialog(
            onDateSelected = { timestamp ->
                timestamp?.let {
                    startTimestamp = it
                }
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false },
            state = startDateState
        )
    }
    
    // End date picker
    if (showEndDatePicker) {
        DatePickerDialog(
            onDateSelected = { timestamp ->
                timestamp?.let {
                    endTimestamp = it
                }
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false },
            state = endDateState
        )
    }
}

@Composable
private fun DateSelectorRow(
    label: String,
    selectedTimestamp: Long,
    onClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        OutlinedCard(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = formatTimestampToDate(selectedTimestamp),
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

/**
 * Format timestamp to readable date string
 * Uses simple date formatting consistent with app patterns
 */
private fun formatTimestampToDate(timestamp: Long): String {
    // Simple date formatting using basic math like the rest of the app
    val days = timestamp / (24 * 60 * 60 * 1000L)
    val currentDays = getCurrentTimestamp() / (24 * 60 * 60 * 1000L)
    val daysDiff = (currentDays - days).toInt()
    
    return when {
        daysDiff == 0 -> "Hoy"
        daysDiff == 1 -> "Ayer"
        daysDiff < 7 -> "Hace ${daysDiff}d"
        daysDiff < 30 -> "Hace ${daysDiff/7}sem"
        else -> "Hace ${daysDiff/30}mes"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit,
    state: DatePickerState
) {
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { 
                onDateSelected(state.selectedDateMillis)
            }) {
                Text("Seleccionar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    ) {
        DatePicker(
            state = state,
            title = null,
            showModeToggle = false
        )
    }
}