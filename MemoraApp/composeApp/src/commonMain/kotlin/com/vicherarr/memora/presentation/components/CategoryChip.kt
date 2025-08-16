package com.vicherarr.memora.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vicherarr.memora.domain.models.Category
import com.vicherarr.memora.domain.models.displayName

/**
 * Category Chip Component
 * Material Design 3 style chip for categories
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryChip(
    category: Category,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Debug logs
    println("🎨 CategoryChip render: id='${category.id}' name='${category.name}' selected=$isSelected")
    if (isSelected) {
        // Selected chip with remove functionality
        FilterChip(
            onClick = {
                println("👆 CLICK on SELECTED chip: id='${category.id}' name='${category.name}'")
                onClick()
            }, // Clicking anywhere removes it
            label = { 
                Text(text = category.displayName)
            },
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
    } else {
        // Unselected chip
        FilterChip(
            onClick = {
                println("👆 CLICK on UNSELECTED chip: id='${category.id}' name='${category.name}'")
                onClick()
            },
            label = { Text(category.displayName) },
            selected = false,
            modifier = modifier
        )
    }
}

/**
 * Parse hex color string to Compose Color - KMP compatible
 * Supports formats: #RGB, #ARGB, #RRGGBB, #AARRGGBB
 */
private fun parseHexColor(colorString: String): Color {
    return try {
        val hex = colorString.removePrefix("#")
        when (hex.length) {
            6 -> {
                // #RRGGBB
                val colorInt = hex.toLong(16)
                Color(
                    red = ((colorInt shr 16) and 0xFF) / 255f,
                    green = ((colorInt shr 8) and 0xFF) / 255f,
                    blue = (colorInt and 0xFF) / 255f,
                    alpha = 1f
                )
            }
            8 -> {
                // #AARRGGBB
                val colorInt = hex.toLong(16)
                Color(
                    red = ((colorInt shr 16) and 0xFF) / 255f,
                    green = ((colorInt shr 8) and 0xFF) / 255f,
                    blue = (colorInt and 0xFF) / 255f,
                    alpha = ((colorInt shr 24) and 0xFF) / 255f
                )
            }
            else -> Color(0xFF6750A4) // Fallback to Material Design primary
        }
    } catch (e: Exception) {
        Color(0xFF6750A4) // Fallback color
    }
}

/**
 * Horizontal scrollable list of category chips
 */
@Composable
fun CategoryChipList(
    categories: List<Category>,
    selectedCategories: List<String> = emptyList(),
    onCategoryClick: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(categories) { category ->
            CategoryChip(
                category = category,
                isSelected = category.id in selectedCategories,
                onClick = { onCategoryClick(category) }
            )
        }
    }
}