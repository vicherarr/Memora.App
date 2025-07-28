package com.vicherarr.memora.ui.screens.notes.edit.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

/**
 * TopBar para la pantalla de editar nota
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteTopBar(
    onNavigateBack: () -> Unit,
    onSave: () -> Unit,
    isSaveEnabled: Boolean
) {
    TopAppBar(
        title = { Text("Editar Nota") },
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
                    contentDescription = "Guardar cambios",
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