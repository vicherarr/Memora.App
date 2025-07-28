package com.vicherarr.memora.ui.screens.notes.edit

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.vicherarr.memora.presentation.viewmodels.NotesViewModel
import com.vicherarr.memora.ui.screens.notes.edit.components.EditNoteContent
import com.vicherarr.memora.ui.screens.notes.edit.components.EditNoteTopBar
import org.koin.compose.viewmodel.koinViewModel

/**
 * Pantalla para editar nota existente
 * Fase 1: Funcionalidad básica
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteScreen(
    noteId: String,
    onNavigateBack: () -> Unit,
    onNoteSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotesViewModel = koinViewModel()
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var isLoaded by remember { mutableStateOf(false) }
    val notes by viewModel.notes.collectAsState()
    
    // Cargar datos de la nota existente
    if (!isLoaded) {
        val note = notes.find { it.id == noteId }
        if (note != null) {
            title = note.title ?: ""
            content = note.content
            isLoaded = true
        }
    }
    
    Scaffold(
        topBar = {
            EditNoteTopBar(
                onNavigateBack = onNavigateBack,
                onSave = {
                    println("Actualizando nota: id='$noteId', title='$title', content='$content'")
                    viewModel.updateNote(
                        noteId = noteId,
                        title = if (title.isBlank()) null else title,
                        content = content
                    )
                    onNoteSaved()
                },
                isSaveEnabled = content.isNotBlank()
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        EditNoteContent(
            title = title,
            content = content,
            onTitleChange = { title = it },
            onContentChange = { content = it },
            modifier = Modifier.padding(paddingValues)
        )
    }
}