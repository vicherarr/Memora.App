package com.vicherarr.memora.ui.screens.notes.create

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vicherarr.memora.presentation.viewmodels.NotesViewModel
import com.vicherarr.memora.ui.screens.notes.create.components.CreateNoteContent
import com.vicherarr.memora.ui.screens.notes.create.components.CreateNoteTopBar
import org.koin.compose.viewmodel.koinViewModel

/**
 * Pantalla para crear nueva nota
 * Fase 1: Funcionalidad básica
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateNoteScreen(
    onNavigateBack: () -> Unit,
    onNoteSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotesViewModel = koinViewModel()
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            CreateNoteTopBar(
                onNavigateBack = onNavigateBack,
                onSave = {
                    println("Guardando nota: title='$title', content='$content'")
                    viewModel.createNote(
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
        CreateNoteContent(
            title = title,
            content = content,
            onTitleChange = { title = it },
            onContentChange = { content = it },
            modifier = Modifier.padding(paddingValues)
        )
    }
}