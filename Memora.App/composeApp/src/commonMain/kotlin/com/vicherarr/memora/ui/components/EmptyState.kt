package com.vicherarr.memora.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Generic empty state component
 */
@Composable
fun EmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    emoji: String = "📝",
    action: @Composable (() -> Unit)? = null
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            if (action != null) {
                Spacer(modifier = Modifier.height(24.dp))
                action()
            }
        }
    }
}

/**
 * Empty notes state
 */
@Composable
fun EmptyNotesState(
    modifier: Modifier = Modifier,
    onCreateNote: (() -> Unit)? = null
) {
    EmptyState(
        title = "No tienes notas aún",
        description = "Comienza creando tu primera nota para organizar tus ideas y pensamientos.",
        emoji = "📝",
        modifier = modifier,
        action = onCreateNote?.let {
            {
                MemoraButton(
                    text = "Crear mi primera nota",
                    onClick = it
                )
            }
        }
    )
}

/**
 * Empty search results state
 */
@Composable
fun EmptySearchState(
    searchQuery: String,
    modifier: Modifier = Modifier,
    onClearSearch: (() -> Unit)? = null
) {
    EmptyState(
        title = "Sin resultados",
        description = "No se encontraron notas que coincidan con \"$searchQuery\". Intenta con otros términos.",
        emoji = "🔍",
        modifier = modifier,
        action = onClearSearch?.let {
            {
                MemoraButtonText(
                    text = "Limpiar búsqueda",
                    onClick = it
                )
            }
        }
    )
}

/**
 * Error state
 */
@Composable
fun ErrorState(
    title: String = "Algo salió mal",
    description: String = "Ocurrió un error inesperado. Por favor, inténtalo de nuevo.",
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    EmptyState(
        title = title,
        description = description,
        emoji = "⚠️",
        modifier = modifier,
        action = onRetry?.let {
            {
                MemoraButton(
                    text = "Reintentar",
                    onClick = it
                )
            }
        }
    )
}