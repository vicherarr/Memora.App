package com.vicherarr.memora.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vicherarr.memora.domain.models.Category
import com.vicherarr.memora.domain.models.displayName

/**
 * Category Section Component - Material Design 3
 * Integrates with CreateNoteScreen following design coherence
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySection(
    availableCategories: List<Category>,
    selectedCategories: List<String>,
    isShowingCategoryDropdown: Boolean,
    isCreatingCategory: Boolean,
    newCategoryName: String,
    onCategoryToggle: (String) -> Unit,
    onShowCategoryDropdown: () -> Unit,
    onHideCategoryDropdown: () -> Unit,
    onShowCreateCategory: () -> Unit,
    onHideCreateCategory: () -> Unit,
    onNewCategoryNameChange: (String) -> Unit,
    onCreateCategory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Title
        Text(
            text = "Categorías",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        // Selected categories + Add button
        Box {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Selected categories with remove X
                items(availableCategories.filter { it.id in selectedCategories }) { category ->
                    SelectedCategoryChip(
                        category = category,
                        onRemove = { 
                            println("📎 CategorySection: Removing selected category '${category.id}'")
                            onCategoryToggle(category.id) 
                        }
                    )
                }
                
                // Add category button
                item {
                    AddCategoryButton(
                        onClick = onShowCategoryDropdown
                    )
                }
            }
            
            // Floating dropdown menu
            DropdownMenu(
                expanded = isShowingCategoryDropdown,
                onDismissRequest = onHideCategoryDropdown,
                modifier = Modifier.wrapContentSize()
            ) {
                // Available categories to select
                availableCategories.filter { it.id !in selectedCategories }.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.displayName) },
                        onClick = {
                            println("📎 CategorySection: Selecting category '${category.id}' from dropdown")
                            onCategoryToggle(category.id)
                        }
                    )
                }
                
                // Divider if there are categories
                if (availableCategories.filter { it.id !in selectedCategories }.isNotEmpty()) {
                    Divider()
                }
                
                // Create new category option
                if (isCreatingCategory) {
                    // Inline text field for new category
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        OutlinedTextField(
                            value = newCategoryName,
                            onValueChange = onNewCategoryNameChange,
                            placeholder = { Text("Nueva categoría") },
                            singleLine = true,
                            modifier = Modifier.width(200.dp)
                        )
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            TextButton(onClick = onCreateCategory) {
                                Text("Crear")
                            }
                            TextButton(onClick = onHideCreateCategory) {
                                Text("Cancelar")
                            }
                        }
                    }
                } else {
                    DropdownMenuItem(
                        text = { Text("➕ Nueva categoría...") },
                        onClick = { onShowCreateCategory() }
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCategoryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        onClick = onClick,
        label = { Text("+") },
        selected = false,
        leadingIcon = {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add category",
                modifier = Modifier.size(16.dp)
            )
        },
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

/**
 * Parse hex color string to Compose Color - KMP compatible
 */
private fun parseHexColor(colorString: String): Color {
    return try {
        val hex = colorString.removePrefix("#")
        when (hex.length) {
            6 -> {
                val colorInt = hex.toLong(16)
                Color(
                    red = ((colorInt shr 16) and 0xFF) / 255f,
                    green = ((colorInt shr 8) and 0xFF) / 255f,
                    blue = (colorInt and 0xFF) / 255f,
                    alpha = 1f
                )
            }
            8 -> {
                val colorInt = hex.toLong(16)
                Color(
                    red = ((colorInt shr 16) and 0xFF) / 255f,
                    green = ((colorInt shr 8) and 0xFF) / 255f,
                    blue = (colorInt and 0xFF) / 255f,
                    alpha = ((colorInt shr 24) and 0xFF) / 255f
                )
            }
            else -> Color(0xFF6750A4)
        }
    } catch (e: Exception) {
        Color(0xFF6750A4)
    }
}

/**
 * Chip for selected categories with remove functionality
 * Clear visual distinction from unselected categories
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectedCategoryChip(
    category: Category,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        onClick = onRemove,
        label = { Text(category.displayName) },
        selected = true,
        trailingIcon = {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove ${category.displayName}",
                modifier = Modifier.size(16.dp)
            )
        },
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = parseHexColor(category.color).copy(alpha = 0.2f),
            selectedLabelColor = parseHexColor(category.color),
            selectedTrailingIconColor = parseHexColor(category.color)
        )
    )
}