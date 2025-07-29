package com.vicherarr.memora.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.vicherarr.memora.ui.screens.notes.NotesListScreen

/**
 * Pantalla principal simplificada - ahora solo muestra la lista de notas
 * La navegación compleja se maneja en MainNavigation.kt
 */
@Composable
fun MainScreen(
    onLogout: () -> Unit,
    onNavigateToNoteDetail: (String) -> Unit = {},
    onNavigateToNoteEdit: (String) -> Unit = {},
    onNavigateToNoteCreate: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    NotesListScreen(
        onNavigateToNoteDetail = onNavigateToNoteDetail,
        onNavigateToNoteEdit = { noteId ->
            // Lógica clara: si hay noteId editar, si no crear
            if (noteId != null) {
                onNavigateToNoteEdit(noteId) // Editar nota existente
            } else {
                onNavigateToNoteCreate() // Crear nueva nota
            }
        },
        modifier = modifier
    )
}