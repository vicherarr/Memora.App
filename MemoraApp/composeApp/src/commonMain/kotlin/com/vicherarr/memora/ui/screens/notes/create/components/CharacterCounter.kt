package com.vicherarr.memora.ui.screens.notes.create.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign

/**
 * Componente para mostrar contador de caracteres
 */
@Composable
fun CharacterCounter(
    count: Int,
    modifier: Modifier = Modifier
) {
    Text(
        text = "$count caracteres",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.End,
        modifier = modifier.fillMaxWidth()
    )
}