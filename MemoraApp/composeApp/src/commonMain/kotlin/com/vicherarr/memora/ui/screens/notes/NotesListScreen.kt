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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vicherarr.memora.domain.models.Note
import com.vicherarr.memora.presentation.viewmodels.NotesListViewModel
import com.vicherarr.memora.presentation.viewmodels.NotesListUiState
import com.vicherarr.memora.ui.components.EmptyState
import com.vicherarr.memora.ui.components.LoadingIndicator
import com.vicherarr.memora.ui.screens.notes.components.NoteCard
import org.koin.compose.viewmodel.koinViewModel

/**
 * Pantalla de lista de notas refactorizada
 * Aplicando principios SOLID y mejores prácticas de KMP
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    onNavigateToNoteDetail: (String) -> Unit,
    onNavigateToNoteEdit: (String?) -> Unit, // null para crear nueva nota
    modifier: Modifier = Modifier,
    viewModel: NotesListViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val notes by viewModel.notes.collectAsState()
    
    // Estado para el diálogo de confirmación de eliminación
    var noteToDelete by remember { mutableStateOf<Note?>(null) }

    // Auto-load notes when screen is created
    LaunchedEffect(Unit) {
        viewModel.loadNotes()
    }

    Scaffold(
        topBar = {
            NotesListTopBar()
        },
        floatingActionButton = {
            CreateNoteFab(
                onCreateNote = { onNavigateToNoteEdit(null) }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        NotesListContent(
            uiState = uiState,
            notes = notes,
            onNoteClick = onNavigateToNoteDetail,
            onEditNote = onNavigateToNoteEdit,
            onDeleteNote = { note -> noteToDelete = note },
            onRefresh = viewModel::refreshNotes,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
    
    // Diálogo de confirmación de eliminación profesional
    noteToDelete?.let { note ->
        DeleteConfirmationDialog(
            note = note,
            onConfirm = {
                viewModel.deleteNote(note.id)
                noteToDelete = null
            },
            onDismiss = {
                noteToDelete = null
            }
        )
    }
}

/**
 * TopBar específico para lista de notas
 * Componente separado siguiendo principio SRP
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotesListTopBar() {
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
}

/**
 * FAB para crear nueva nota
 * Componente separado siguiendo principio SRP
 */
@Composable
private fun CreateNoteFab(
    onCreateNote: () -> Unit
) {
    FloatingActionButton(
        onClick = onCreateNote,
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Crear nueva nota"
        )
    }
}

/**
 * Contenido principal de la lista
 * Maneja todos los estados UI de forma profesional
 */
@Composable
private fun NotesListContent(
    uiState: NotesListUiState,
    notes: List<Note>,
    onNoteClick: (String) -> Unit,
    onEditNote: (String?) -> Unit,
    onDeleteNote: (Note) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        when (uiState) {
            is NotesListUiState.Loading -> {
                LoadingIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            
            is NotesListUiState.Refreshing -> {
                // Mostrar contenido con indicador de refresh en la parte superior
                Column {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                    NotesListSuccess(
                        notes = notes,
                        onNoteClick = onNoteClick,
                        onEditNote = onEditNote,
                        onDeleteNote = onDeleteNote,
                        onRefresh = onRefresh,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            
            is NotesListUiState.Success -> {
                NotesListSuccess(
                    notes = notes,
                    onNoteClick = onNoteClick,
                    onEditNote = onEditNote,
                    onDeleteNote = onDeleteNote,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            is NotesListUiState.Empty -> {
                EmptyNotesState(
                    onCreateNote = { onEditNote(null) },
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            
            is NotesListUiState.EmptySearch -> {
                EmptySearchState(
                    query = uiState.query,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            
            is NotesListUiState.Error -> {
                ErrorState(
                    message = uiState.message,
                    onRetry = onRefresh,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

/**
 * Lista de notas exitosa con pull-to-refresh
 * Implementa UX patterns profesionales
 */
@Composable
private fun NotesListSuccess(
    notes: List<Note>,
    onNoteClick: (String) -> Unit,
    onEditNote: (String?) -> Unit,
    onDeleteNote: (Note) -> Unit,
    onRefresh: () -> Unit,
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
                onDelete = { onDeleteNote(note) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Estado vacío cuando no hay notas
 * UX mejorado con call-to-action
 */
@Composable
private fun EmptyNotesState(
    onCreateNote: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "📝",
            style = MaterialTheme.typography.displayLarge
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No tienes notas aún",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "¡Crea tu primera nota para comenzar!",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onCreateNote,
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Crear mi primera nota")
        }
    }
}

/**
 * Estado vacío para búsquedas sin resultados
 */
@Composable
private fun EmptySearchState(
    query: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🔍",
            style = MaterialTheme.typography.displayMedium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Sin resultados",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "No encontramos notas con \"$query\"",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Estado de error con retry
 * Error handling profesional
 */
@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "⚠️",
            style = MaterialTheme.typography.displayMedium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Error al cargar",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedButton(
            onClick = onRetry
        ) {
            Text("Reintentar")
        }
    }
}

/**
 * Diálogo de confirmación de eliminación profesional
 * UX clara y accesible con información de la nota
 */
@Composable
private fun DeleteConfirmationDialog(
    note: Note,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Eliminar nota")
        },
        text = {
            Column {
                Text("¿Estás seguro de que quieres eliminar esta nota?")
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Mostrar preview de la nota a eliminar
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        if (!note.title.isNullOrBlank()) {
                            Text(
                                text = note.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        
                        Text(
                            text = note.content,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Esta acción no se puede deshacer.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}