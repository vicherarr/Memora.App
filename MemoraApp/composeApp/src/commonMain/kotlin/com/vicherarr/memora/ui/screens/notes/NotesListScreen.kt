package com.vicherarr.memora.ui.screens.notes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vicherarr.memora.domain.models.Note
import com.vicherarr.memora.presentation.utils.SimpleUiState
import com.vicherarr.memora.presentation.viewmodels.NotesViewModel
import com.vicherarr.memora.ui.components.EmptyState
import com.vicherarr.memora.ui.components.LoadingIndicator
import com.vicherarr.memora.ui.components.MemoraCard
import com.vicherarr.memora.ui.screens.notes.components.NoteCard
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    onNavigateToNoteDetail: (String) -> Unit,
    onNavigateToNoteEdit: (String?) -> Unit, // null para crear nueva nota
    modifier: Modifier = Modifier,
    viewModel: NotesViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val notes by viewModel.notes.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadNotes()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Mis Notas",
                        style = MaterialTheme.typography.headlineSmall
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    println("FAB clicked - navigating to create note")
                    onNavigateToNoteEdit(null) 
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Crear nueva nota"
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is SimpleUiState.Loading -> {
                    LoadingIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                is SimpleUiState.Error -> {
                    ErrorContent(
                        message = (uiState as SimpleUiState.Error).message,
                        onRetry = { viewModel.loadNotes() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                is SimpleUiState.Success -> {
                    if (notes.isEmpty()) {
                        EmptyState(
                            title = "No tienes notas aún",
                            description = "Toca el botón + para crear tu primera nota",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        NotesContent(
                            notes = notes,
                            onNoteClick = onNavigateToNoteDetail,
                            onEditNote = onNavigateToNoteEdit,
                            onDeleteNote = { noteId ->
                                viewModel.deleteNote(noteId)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotesContent(
    notes: List<Note>,
    onNoteClick: (String) -> Unit,
    onEditNote: (String) -> Unit,
    onDeleteNote: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = notes,
            key = { note -> note.id }
        ) { note ->
            NoteCard(
                note = note,
                onClick = { onNoteClick(note.id) },
                onEdit = { onEditNote(note.id) },
                onDelete = { onDeleteNote(note.id) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Error al cargar las notas",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Reintentar")
        }
    }
}