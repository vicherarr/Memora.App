package com.vicherarr.memora.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vicherarr.memora.domain.models.Category
import com.vicherarr.memora.domain.models.displayName

/**
 * Display categories in read-only mode
 * Following Clean Architecture - View Layer
 * @deprecated Use NoteCategoryDisplay instead for better performance
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDisplay(
    selectedCategories: List<String>,
    availableCategories: List<Category>,
    modifier: Modifier = Modifier
) {
    // Filter categories that are selected for this note
    val noteCategories = availableCategories.filter { it.id in selectedCategories }
    
    if (noteCategories.isNotEmpty()) {
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
            
            // Categories chips row (read-only)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(noteCategories) { category ->
                    ReadOnlyCategoryChip(category = category)
                }
            }
        }
    }
}

/**
 * Display note categories in read-only mode - Optimized version
 * Following Clean Architecture - View Layer
 * Direct display of note categories without filtering
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteCategoryDisplay(
    noteCategories: List<Category>,
    modifier: Modifier = Modifier
) {
    if (noteCategories.isNotEmpty()) {
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
            
            // Categories chips row (read-only)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(noteCategories) { category ->
                    ReadOnlyCategoryChip(category = category)
                }
            }
        }
    }
}

/**
 * Read-only category chip - no interaction
 * Material Design 3 style
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReadOnlyCategoryChip(
    category: Category,
    modifier: Modifier = Modifier
) {
    AssistChip(
        onClick = { /* No action in read-only mode */ },
        label = { Text(category.displayName) },
        modifier = modifier,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = parseHexColor(category.color).copy(alpha = 0.15f),
            labelColor = parseHexColor(category.color)
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