package com.vicherarr.memora.ui.screens.notes.create.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Campo de texto reutilizable para notas
 */
@Composable
fun NoteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    singleLine: Boolean,
    modifier: Modifier = Modifier,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else minLines,
        modifier = modifier.fillMaxWidth()
    )
}