package com.vicherarr.memora.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vicherarr.memora.domain.models.Category
import com.vicherarr.memora.domain.models.CategoryFilter
import com.vicherarr.memora.domain.models.displayName

/**
 * Category filter section component
 * Following Material Design 3 guidelines and Clean Architecture
 */
@Composable
fun CategoryFilterSection(
    selectedCategoryFilter: CategoryFilter,
    onCategoryFilterChanged: (CategoryFilter) -> Unit,
    selectedCategoryId: String?,
    onSelectedCategoryChanged: (String?) -> Unit,
    availableCategories: List<Category>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
            Text(
                text = "Filtrar por categoría",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Column(Modifier.selectableGroup()) {
                // ALL option
                CategoryFilterOption(
                    filter = CategoryFilter.ALL,
                    isSelected = selectedCategoryFilter == CategoryFilter.ALL,
                    onClick = { 
                        onCategoryFilterChanged(CategoryFilter.ALL)
                        onSelectedCategoryChanged(null)
                    }
                )
                
                // UNCATEGORIZED option
                CategoryFilterOption(
                    filter = CategoryFilter.UNCATEGORIZED,
                    isSelected = selectedCategoryFilter == CategoryFilter.UNCATEGORIZED,
                    onClick = { 
                        onCategoryFilterChanged(CategoryFilter.UNCATEGORIZED)
                        onSelectedCategoryChanged(null)
                    }
                )
                
                // Specific categories
                println("CategoryFilterSection: availableCategories count = ${availableCategories.size}")
                if (availableCategories.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Categorías específicas:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    availableCategories.forEach { category ->
                        SpecificCategoryOption(
                            category = category,
                            isSelected = selectedCategoryFilter == CategoryFilter.SPECIFIC_CATEGORY && 
                                         selectedCategoryId == category.id,
                            onClick = {
                                onCategoryFilterChanged(CategoryFilter.SPECIFIC_CATEGORY)
                                onSelectedCategoryChanged(category.id)
                            }
                        )
                    }
                }
            }
        }
}

@Composable
private fun CategoryFilterOption(
    filter: CategoryFilter,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
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

@Composable
private fun SpecificCategoryOption(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = 4.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = null
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            // Category color indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .wrapContentSize()
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = category.displayName,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
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