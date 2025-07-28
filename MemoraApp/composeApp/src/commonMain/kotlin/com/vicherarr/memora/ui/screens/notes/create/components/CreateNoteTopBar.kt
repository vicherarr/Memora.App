package com.vicherarr.memora.ui.screens.notes.create.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * TopBar para la pantalla de crear nota
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateNoteTopBar(
    onNavigateBack: () -> Unit,
    onSave: () -> Unit,
    isSaveEnabled: Boolean
) {
    TopAppBar(
        title = { Text("Nueva Nota") },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver"
                )
            }
        },
        actions = {
            IconButton(
                onClick = onSave,
                enabled = isSaveEnabled
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Guardar nota",
                    tint = if (isSaveEnabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    }
                )
            }
        }
    )
}