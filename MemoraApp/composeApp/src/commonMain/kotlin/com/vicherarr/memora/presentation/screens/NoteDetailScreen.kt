package com.vicherarr.memora.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.vicherarr.memora.domain.models.TipoDeArchivo
import com.vicherarr.memora.presentation.viewmodels.NoteDetailViewModel
import com.vicherarr.memora.ui.components.MemoraTextField
import org.koin.compose.getKoin

/**
 * Note Detail Screen - Displays a specific note with view/edit capabilities
 * Follows Material Design 3 guidelines with safe areas for iOS compatibility
 */
data class NoteDetailScreen(private val noteId: String) : Screen {
    
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val koin = getKoin()
        val viewModel: NoteDetailViewModel = remember { koin.get() }
        val uiState by viewModel.uiState.collectAsState()
        
        // Load note when screen appears
        LaunchedEffect(noteId) {
            viewModel.loadNote(noteId)
        }
        
        // Handle navigation after delete
        LaunchedEffect(uiState.isNoteDeleted) {
            if (uiState.isNoteDeleted) {
                navigator.pop()
            }
        }
        
        // Show snackbar for save success
        if (uiState.isNoteSaved) {
            LaunchedEffect(Unit) {
                // Clear message after showing
                viewModel.clearMessages()
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Custom Top Bar
                NoteDetailTopBar(
                    isEditMode = uiState.isEditMode,
                    canSave = viewModel.canSaveNote(),
                    onBackClick = { navigator.pop() },
                    onEditClick = { viewModel.enterEditMode() },
                    onSaveClick = { viewModel.saveNote() },
                    onCancelClick = { viewModel.exitEditMode() },
                    onDeleteClick = { viewModel.deleteNote() }
                )
                
                // Content
                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    uiState.errorMessage != null -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.ErrorOutline,
                                contentDescription = "Error",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = uiState.errorMessage ?: "Error desconocido",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    
                    uiState.note != null -> {
                        if (uiState.isEditMode) {
                            EditNoteContent(
                                titulo = uiState.editTitulo,
                                contenido = uiState.editContenido,
                                onTituloChange = viewModel::updateEditTitulo,
                                onContenidoChange = viewModel::updateEditContenido,
                                validationHint = viewModel.getValidationHint(uiState.editContenido)
                            )
                        } else {
                            ViewNoteContent(
                                note = uiState.note!!
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteDetailTopBar(
    isEditMode: Boolean,
    canSave: Boolean,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Actions
            Row {
                if (isEditMode) {
                    // Cancel edit
                    TextButton(onClick = onCancelClick) {
                        Text("Cancelar")
                    }
                    
                    // Save note
                    Button(
                        onClick = onSaveClick,
                        enabled = canSave
                    ) {
                        Text("Guardar")
                    }
                } else {
                    // Edit note
                    IconButton(onClick = onEditClick) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    // Delete note
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar nota") },
            text = { Text("¿Estás seguro de que quieres eliminar esta nota? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteClick()
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun EditNoteContent(
    titulo: String,
    contenido: String,
    onTituloChange: (String) -> Unit,
    onContenidoChange: (String) -> Unit,
    validationHint: String?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Título field
        MemoraTextField(
            value = titulo,
            onValueChange = onTituloChange,
            label = "Título (opcional)",
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Contenido field
        MemoraTextField(
            value = contenido,
            onValueChange = onContenidoChange,
            label = "Contenido",
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp),
            maxLines = Int.MAX_VALUE,
            isError = validationHint != null,
            supportingText = validationHint
        )
    }
}

@Composable
private fun ViewNoteContent(
    note: com.vicherarr.memora.domain.models.Note
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Título
        if (!note.titulo.isNullOrBlank()) {
            item {
                Text(
                    text = note.titulo,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        // Fecha de creación
        item {
            Text(
                text = formatRelativeTime(note.fechaCreacion),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Contenido
        item {
            Text(
                text = note.contenido,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2
            )
        }
        
        // Attachments
        if (note.archivosAdjuntos.isNotEmpty()) {
            item {
                Text(
                    text = "Archivos adjuntos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(note.archivosAdjuntos) { attachment ->
                        AttachmentItem(attachment = attachment)
                    }
                }
            }
        }
    }
}

@Composable
private fun AttachmentItem(
    attachment: com.vicherarr.memora.domain.models.ArchivoAdjunto
) {
    Card(
        modifier = Modifier.size(120.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (attachment.tipoArchivo) {
                TipoDeArchivo.Imagen -> {
                    // Show image thumbnail
                    AsyncImage(
                        model = attachment.datosArchivo, // This might need adjustment based on how images are handled
                        contentDescription = "Imagen adjunta",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                TipoDeArchivo.Video -> {
                    // Show video thumbnail with play icon
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Video",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // File type indicator
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = if (attachment.tipoArchivo == TipoDeArchivo.Imagen) "IMG" else "VID",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

// Utility function to format relative time (reusing from NotesTab)
private fun formatRelativeTime(timestamp: Long): String {
    val now = com.vicherarr.memora.data.database.getCurrentTimestamp()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "Hace un momento"
        diff < 3_600_000 -> {
            val minutes = diff / 60_000
            "Hace ${minutes}min"
        }
        diff < 86_400_000 -> {
            val hours = diff / 3_600_000
            "Hace ${hours}h"
        }
        diff < 604_800_000 -> {
            val days = diff / 86_400_000
            "Hace ${days}d"
        }
        else -> {
            val weeks = diff / 604_800_000
            "Hace ${weeks}sem"
        }
    }
}