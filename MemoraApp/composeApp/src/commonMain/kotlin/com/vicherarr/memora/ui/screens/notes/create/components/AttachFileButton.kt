package com.vicherarr.memora.ui.screens.notes.create.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Botón para adjuntar archivos a la nota
 * Fase 6: Media Management
 */
@Composable
fun AttachFileButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Text("📎 Adjuntar Archivo")
    }
}