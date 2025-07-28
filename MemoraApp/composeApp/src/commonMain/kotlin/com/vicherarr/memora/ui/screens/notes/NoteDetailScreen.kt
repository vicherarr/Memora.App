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
import com.vicherarr.memora.presentation.utils.SimpleUiState
import com.vicherarr.memora.presentation.viewmodels.NotesViewModel
import com.vicherarr.memora.ui.components.LoadingIndicator
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotesViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedNote by viewModel.selectedNote.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(noteId) {
        viewModel.loadNoteById(noteId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = selectedNote?.title?.takeIf { it.isNotBlank() } ?: "Detalle de Nota",
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
                    selectedNote?.let { note ->
                        IconButton(
                            onClick = { onNavigateToEdit(note.id) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar nota",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        IconButton(
                            onClick = { showDeleteDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar nota",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
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
                    LoadingIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                is SimpleUiState.Error -> {
                    ErrorContent(
                        message = (uiState as SimpleUiState.Error).message,
                        onRetry = { viewModel.loadNoteById(noteId) },
                        onBack = onNavigateBack,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                is SimpleUiState.Success -> {
                    selectedNote?.let { note ->
                        NoteDetailContent(
                            note = note,
                            modifier = Modifier.fillMaxSize()
                        )
                    } ?: run {
                        ErrorContent(
                            message = "Nota no encontrada",
                            onRetry = { viewModel.loadNoteById(noteId) },
                            onBack = onNavigateBack,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
    
    // Dialog de confirmación para eliminar
    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            onConfirm = {
                selectedNote?.let { note ->
                    viewModel.deleteNote(note.id)
                    onNavigateBack()
                }
                showDeleteDialog = false
            },
            onDismiss = {
                showDeleteDialog = false
            }
        )
    }
}

@Composable
private fun NoteDetailContent(
    note: Note,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Título
        if (!note.title.isNullOrBlank()) {
            Text(
                text = note.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        // Metadata Card
        NoteMetadataCard(note = note)
        
        // Contenido
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Contenido",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.4
                )
            }
        }
        
        // Archivos adjuntos (si los hay)
        if (note.attachments.isNotEmpty()) {
            AttachmentsSection(
                attachments = note.attachments,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun NoteMetadataCard(
    note: Note,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Información",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Divider(
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f),
                thickness = 1.dp
            )
            
            MetadataRow(
                label = "Creada:",
                value = formatDateTime(note.createdAt)
            )
            
            MetadataRow(
                label = "Modificada:",
                value = formatDateTime(note.modifiedAt)
            )
            
            if (note.attachments.isNotEmpty()) {
                MetadataRow(
                    label = "Archivos:",
                    value = "${note.attachments.size} archivo${if (note.attachments.size > 1) "s" else ""}"
                )
            }
            
            if (note.isLocalOnly) {
                MetadataRow(
                    label = "Estado:",
                    value = "Solo local (sin sincronizar)"
                )
            }
        }
    }
}

@Composable
private fun MetadataRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.weight(2f),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun AttachmentsSection(
    attachments: List<com.vicherarr.memora.domain.models.Attachment>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Archivos adjuntos (${attachments.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // Lista de archivos adjuntos
            attachments.forEach { attachment ->
                AttachmentItem(
                    attachment = attachment,
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (attachment != attachments.last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun AttachmentItem(
    attachment: com.vicherarr.memora.domain.models.Attachment,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icono del tipo de archivo
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = when (attachment.type) {
                        com.vicherarr.memora.domain.models.AttachmentType.IMAGE -> "📷"
                        com.vicherarr.memora.domain.models.AttachmentType.VIDEO -> "🎥"
                    },
                    modifier = Modifier.padding(8.dp)
                )
            }
            
            // Información del archivo
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = attachment.originalName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = formatFileSize(attachment.sizeBytes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
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
            text = "Error",
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
private fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Eliminar nota",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                text = "¿Estás seguro de que quieres eliminar esta nota? Esta acción no se puede deshacer.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
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
 */
private fun formatDateTime(instant: kotlinx.datetime.Instant): String {
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${localDateTime.dayOfMonth}/${localDateTime.monthNumber}/${localDateTime.year} ${
        String.format("%02d:%02d", localDateTime.hour, localDateTime.minute)
    }"
}

/**
 * Formatea el tamaño del archivo en formato legible (KB, MB, etc.)
 */
private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        else -> "${bytes / (1024 * 1024 * 1024)} GB"
    }
}