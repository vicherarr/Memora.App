package com.vicherarr.memora.ui.screens.notes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vicherarr.memora.domain.models.Note
import com.vicherarr.memora.domain.models.Attachment
import com.vicherarr.memora.presentation.viewmodels.NoteDetailViewModel
import com.vicherarr.memora.presentation.viewmodels.NoteDetailUiState
import com.vicherarr.memora.ui.components.LoadingIndicator
import org.koin.compose.viewmodel.koinViewModel

/**
 * Pantalla de detalle de nota refactorizada
 * Aplicando principios SOLID y mejores prácticas de KMP
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NoteDetailViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val note by viewModel.note.collectAsState()
    val isDeleting by viewModel.isDeleting.collectAsState()
    
    // Estado para el diálogo de confirmación de eliminación
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Cargar nota cuando se crea la pantalla o cambia el ID
    LaunchedEffect(noteId) {
        viewModel.loadNote(noteId)
    }

    // Limpiar estado cuando se sale de la pantalla
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearState()
        }
    }

    Scaffold(
        topBar = {
            NoteDetailTopBar(
                title = note?.title,
                onNavigateBack = onNavigateBack,
                onEdit = { note?.let { onNavigateToEdit(it.id) } },
                onDelete = { showDeleteDialog = true },
                isLoading = uiState is NoteDetailUiState.Loading
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        NoteDetailContent(
            uiState = uiState,
            note = note,
            isDeleting = isDeleting,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
    
    // Diálogo de confirmación de eliminación
    if (showDeleteDialog) {
        note?.let { currentNote ->
            DeleteNoteDialog(
                note = currentNote,
                onConfirm = {
                    viewModel.deleteNote(onSuccess = onNavigateBack)
                    showDeleteDialog = false
                },
                onDismiss = {
                    showDeleteDialog = false
                }
            )
        }
    }
}

/**
 * TopBar específico para detalle de nota
 * Componente separado siguiendo principio SRP
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteDetailTopBar(
    title: String?,
    onNavigateBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    isLoading: Boolean
) {
    TopAppBar(
        title = { 
            Text(
                text = title?.takeIf { it.isNotBlank() } ?: "Detalle de Nota",
                style = MaterialTheme.typography.headlineSmall
            ) 
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver"
                )
            }
        },
        actions = {
            if (!isLoading) {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar nota",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar nota",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    )
}

/**
 * Contenido principal del detalle
 * Maneja todos los estados UI de forma profesional
 */
@Composable
private fun NoteDetailContent(
    uiState: NoteDetailUiState,
    note: Note?,
    isDeleting: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        when (uiState) {
            is NoteDetailUiState.Loading -> {
                LoadingIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            
            is NoteDetailUiState.Success -> {
                note?.let { currentNote ->
                    NoteDetailSuccess(
                        note = currentNote,
                        isDeleting = isDeleting,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            
            is NoteDetailUiState.NotFound -> {
                NoteNotFoundState(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            
            is NoteDetailUiState.Error -> {
                ErrorState(
                    message = uiState.message,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
        
        // Overlay de eliminación
        if (isDeleting) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Eliminando nota...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

/**
 * Contenido exitoso del detalle de nota
 * Layout profesional con toda la información
 */
@Composable
private fun NoteDetailSuccess(
    note: Note,
    isDeleting: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Título de la nota
        if (!note.title.isNullOrBlank()) {
            Text(
                text = note.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        // Contenido de la nota
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
        }
        
        // Metadata de la nota
        NoteMetadataCard(note = note)
        
        // Archivos adjuntos (si los hay)
        if (note.attachments.isNotEmpty()) {
            AttachmentsCard(attachments = note.attachments)
        }
    }
}

/**
 * Card con metadata de la nota
 * Información técnica y timestamps
 */
@OptIn(kotlin.time.ExperimentalTime::class)
@Composable
private fun NoteMetadataCard(
    note: Note,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Información",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            MetadataRow(
                label = "Creada:",
                value = formatDateTime(note.createdAt)
            )
            
            MetadataRow(
                label = "Modificada:",
                value = formatDateTime(note.modifiedAt)
            )
            
            if (note.isLocalOnly) {
                MetadataRow(
                    label = "Estado:",
                    value = "Solo local"
                )
            }
        }
    }
}

/**
 * Fila de metadata individual
 */
@Composable
private fun MetadataRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

/**
 * Card de archivos adjuntos
 * TODO: Implementar en fase de multimedia
 */
@Composable
private fun AttachmentsCard(
    attachments: List<Attachment>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Archivos adjuntos (${attachments.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Funcionalidad disponible en fase de multimedia",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Estado cuando la nota no existe
 */
@Composable
private fun NoteNotFoundState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "📄",
            style = MaterialTheme.typography.displayMedium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Nota no encontrada",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "La nota que buscas no existe o ha sido eliminada",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Estado de error general
 */
@Composable
private fun ErrorState(
    message: String,
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
            text = "Error",
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
    }
}

/**
 * Diálogo de confirmación de eliminación profesional
 */
@Composable
private fun DeleteNoteDialog(
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
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Esta acción no se puede deshacer y serás redirigido a la lista de notas.",
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

/**
 * Formatea la fecha y hora en formato legible
 * TODO: Implementar formateo profesional de fechas
 */
@OptIn(kotlin.time.ExperimentalTime::class)
private fun formatDateTime(instant: kotlin.time.Instant): String {
    // Usar solo la representación ISO básica hasta resolver las APIs
    val isoString = instant.toString()
    // Extraer fecha y hora básica: "2024-01-15T10:30:45Z" -> "2024-01-15 10:30"
    return try {
        val datePart = isoString.substringBefore('T')
        val timePart = isoString.substringAfter('T').substringBefore(':') + ":" + 
                      isoString.substringAfter('T').substringAfter(':').substringBefore(':')
        "$datePart $timePart"
    } catch (e: Exception) {
        isoString.substringBefore('T') // Solo mostrar fecha si falla
    }
}