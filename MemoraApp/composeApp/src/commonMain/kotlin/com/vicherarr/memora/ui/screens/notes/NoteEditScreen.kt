package com.vicherarr.memora.ui.screens.notes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vicherarr.memora.presentation.utils.SimpleUiState
import com.vicherarr.memora.presentation.viewmodels.NotesViewModel
import com.vicherarr.memora.ui.components.LoadingIndicator
import com.vicherarr.memora.ui.components.MemoraTextField
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditScreen(
    noteId: String?, // null para crear nueva nota
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotesViewModel = koinViewModel()
) {
    println("NoteEditScreen: Starting with noteId = $noteId")
    val uiState by viewModel.uiState.collectAsState()
    val selectedNote by viewModel.selectedNote.collectAsState()
    
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var showUnsavedDialog by remember { mutableStateOf(false) }
    
    val isEditing = noteId != null
    val screenTitle = if (isEditing) "Editar Nota" else "Nueva Nota"
    
    // Calcular cambios no guardados de forma derivada
    val hasUnsavedChanges = remember(title, content, selectedNote) {
        if (selectedNote != null) {
            title != (selectedNote?.title ?: "") || content != selectedNote?.content
        } else if (!isEditing) {
            title.isNotBlank() || content.isNotBlank()
        } else {
            false
        }
    }
    
    val contentFocusRequester = remember { FocusRequester() }
    
    // Cargar nota existente si estamos editando
    LaunchedEffect(noteId) {
        if (noteId != null) {
            viewModel.loadNoteById(noteId)
        } else {
            // Para nuevas notas, hacer focus en el campo de contenido
            contentFocusRequester.requestFocus()
        }
    }
    
    // Actualizar campos cuando se carga la nota (solo una vez)
    LaunchedEffect(selectedNote) {
        selectedNote?.let { note ->
            title = note.title ?: ""
            content = note.content
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = screenTitle,
                        style = MaterialTheme.typography.headlineSmall
                    ) 
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (hasUnsavedChanges) {
                                showUnsavedDialog = true
                            } else {
                                onNavigateBack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (isEditing && noteId != null) {
                                viewModel.updateNote(
                                    noteId = noteId,
                                    title = title.takeIf { it.isNotBlank() },
                                    content = content
                                )
                            } else {
                                viewModel.createNote(
                                    title = title.takeIf { it.isNotBlank() },
                                    content = content
                                )
                            }
                        },
                        enabled = content.isNotBlank() && uiState !is SimpleUiState.Loading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Guardar nota",
                            tint = if (content.isNotBlank()) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
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
                    if (isEditing && selectedNote == null) {
                        // Mostrar loading solo cuando estamos cargando una nota existente
                        LoadingIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        // Mostrar formulario con overlay de loading para operaciones de guardado
                        EditContent(
                            title = title,
                            content = content,
                            onTitleChange = { title = it },
                            onContentChange = { content = it },
                            contentFocusRequester = contentFocusRequester,
                            modifier = Modifier.fillMaxSize()
                        )
                        
                        // Overlay de loading durante guardado
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Text(
                                        text = if (isEditing) "Actualizando..." else "Guardando...",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
                
                is SimpleUiState.Error -> {
                    if (isEditing && selectedNote == null) {
                        // Error cargando nota existente
                        ErrorContent(
                            message = (uiState as SimpleUiState.Error).message,
                            onRetry = { noteId?.let { viewModel.loadNoteById(it) } },
                            onBack = onNavigateBack,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        // Error guardando - mostrar formulario con snackbar
                        EditContent(
                            title = title,
                            content = content,
                            onTitleChange = { title = it },
                            onContentChange = { content = it },
                            contentFocusRequester = contentFocusRequester,
                            modifier = Modifier.fillMaxSize()
                        )
                        
                        // Snackbar de error
                        LaunchedEffect(uiState) {
                            // Aquí podrías mostrar un SnackbarHost si lo configuras
                        }
                    }
                }
                
                is SimpleUiState.Success -> {
                    // Navegar de vuelta cuando se guarda exitosamente
                    LaunchedEffect(uiState) {
                        onNavigateBack()
                    }
                    
                    EditContent(
                        title = title,
                        content = content,
                        onTitleChange = { title = it },
                        onContentChange = { content = it },
                        contentFocusRequester = contentFocusRequester,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
    
    // Dialog de cambios no guardados
    if (showUnsavedDialog) {
        UnsavedChangesDialog(
            onSave = {
                if (isEditing && noteId != null) {
                    viewModel.updateNote(
                        noteId = noteId,
                        title = title.takeIf { it.isNotBlank() },
                        content = content
                    )
                } else {
                    viewModel.createNote(
                        title = title.takeIf { it.isNotBlank() },
                        content = content
                    )
                }
                showUnsavedDialog = false
            },
            onDiscard = {
                showUnsavedDialog = false
                onNavigateBack()
            },
            onCancel = {
                showUnsavedDialog = false
            }
        )
    }
}

@Composable
private fun EditContent(
    title: String,
    content: String,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    contentFocusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Campo de título
        MemoraTextField(
            value = title,
            onValueChange = onTitleChange,
            label = "Título (opcional)",
            placeholder = "Título de tu nota...",
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Campo de contenido
        MemoraTextField(
            value = content,
            onValueChange = onContentChange,
            label = "Contenido",
            placeholder = "Escribe tu nota aquí...",
            singleLine = false,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .focusRequester(contentFocusRequester)
        )
        
        // Información adicional
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "💡 Consejos",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "• El contenido es obligatorio para guardar la nota\n" +
                          "• El título es opcional\n" +
                          "• Tus cambios se guardan automáticamente",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Contador de caracteres
        Text(
            text = "${content.length} caracteres",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Error al cargar la nota",
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
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = onBack) {
                Text("Volver")
            }
            
            Button(onClick = onRetry) {
                Text("Reintentar")
            }
        }
    }
}

@Composable
private fun UnsavedChangesDialog(
    onSave: () -> Unit,
    onDiscard: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(
                text = "Cambios no guardados",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                text = "Tienes cambios sin guardar. ¿Qué quieres hacer?",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onSave) {
                Text("Guardar")
            }
        },
        dismissButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = onDiscard,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Descartar")
                }
                
                TextButton(onClick = onCancel) {
                    Text("Cancelar")
                }
            }
        }
    )
}