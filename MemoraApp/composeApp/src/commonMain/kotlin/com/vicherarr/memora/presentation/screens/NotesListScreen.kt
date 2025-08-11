package com.vicherarr.memora.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Arrangement
import kotlin.math.ceil
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.vicherarr.memora.domain.models.ArchivoAdjunto
import com.vicherarr.memora.domain.models.Note
import com.vicherarr.memora.domain.models.TipoDeArchivo
import com.vicherarr.memora.presentation.screens.CreateNoteScreen
import com.vicherarr.memora.presentation.viewmodels.NotesViewModel
import com.vicherarr.memora.presentation.viewmodels.SyncViewModel
import com.vicherarr.memora.presentation.components.SyncStatusIndicator
import org.koin.compose.getKoin

/**
 * Notes List Screen - Shows list of notes with navigation to detail
 * Following Voyager nested navigation best practices 2025
 */
class NotesListScreen : Screen {
    
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val koin = getKoin()
        val notesViewModel: NotesViewModel = remember { koin.get() }
        val syncViewModel: SyncViewModel = remember { koin.get() }
        
        val uiState by notesViewModel.uiState.collectAsState()
        val syncState by syncViewModel.syncState.collectAsState()
        val attachmentSyncState by syncViewModel.attachmentSyncState.collectAsState()
        
        // ✅ NUEVO: Estado para búsqueda local
        var searchQuery by rememberSaveable { mutableStateOf("") }
        
        // ✅ NUEVO: Filtrar notas basado en query de búsqueda
        val filteredNotes = remember(uiState.notes, searchQuery) {
            if (searchQuery.isBlank()) {
                uiState.notes
            } else {
                uiState.notes.filter { note ->
                    note.titulo?.contains(searchQuery, ignoreCase = true) == true ||
                    note.contenido.contains(searchQuery, ignoreCase = true)
                }
            }
        }
        
        // Debug logging for notes data
        println("NotesListScreen: Total notes loaded: ${uiState.notes.size}")
        println("NotesListScreen: Filtered notes: ${filteredNotes.size} (query: '$searchQuery')")
        filteredNotes.forEachIndexed { index, note ->
            println("NotesListScreen: Note $index - id: ${note.id}, title: '${note.titulo}', attachments: ${note.archivosAdjuntos.size}")
        }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                uiState.errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
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
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error
                        )
                        
                        
                    }
                }
                
                uiState.notes.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Assignment,
                            contentDescription = "Sin notas",
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No tienes notas aún",
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Toca el botón + para crear tu primera nota",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
                
                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // ✅ NUEVO: Search Bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            placeholder = { 
                                Text("Buscar en notas...") 
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Buscar"
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = "Limpiar búsqueda"
                                        )
                                    }
                                }
                            },
                            singleLine = true
                        )
                        
                        // Lista de notas filtradas
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 4.dp,
                                bottom = 80.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredNotes) { note ->
                                println("NotesListScreen: Rendering note ${note.id} - attachments: ${note.archivosAdjuntos.size}")
                                EnhancedNoteCard(
                                    note = note,
                                    onClick = {
                                        navigator.push(NoteDetailScreen(note.id))
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            // FAB moved to here from MainScreen (proper nested navigation pattern)
            FloatingActionButton(
                onClick = {
                    navigator.push(CreateNoteScreen())
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 88.dp, end = 16.dp), // Extra padding para evitar que lo tape la barra de tabs
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    Icons.Default.Add, 
                    contentDescription = "Agregar nota",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            
            // Sync Status Indicator - más pequeño y con espacio para SearchBar
            SyncStatusIndicator(
                syncState = syncState,
                attachmentSyncState = attachmentSyncState,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 68.dp, end = 20.dp), // ✅ Más abajo para no interferir con SearchBar
                iconSize = 16.dp // ✅ Más pequeño
            )
        }
    }
}

@Composable
private fun EnhancedNoteCard(
    note: Note,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            // Debug logging for note card
            println("NoteCard: Rendering note ${note.id} with ${note.archivosAdjuntos.size} attachments")
            
            // Multimedia preview section
            if (note.archivosAdjuntos.isNotEmpty()) {
                AttachmentsGrid(
                    attachments = note.archivosAdjuntos,
                    modifier = Modifier.padding(top = 12.dp, start = 16.dp, end = 16.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Note content section
            Column(
                modifier = Modifier.padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = if (note.archivosAdjuntos.isEmpty()) 16.dp else 8.dp,
                    bottom = 16.dp
                )
            ) {
                // Title (if exists)
                if (!note.titulo.isNullOrBlank()) {
                    Text(
                        text = note.titulo,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                // Content preview
                Text(
                    text = note.contenido,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                // Footer with date and attachment summary
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Date
                    Text(
                        text = formatRelativeTime(note.fechaCreacion),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Attachment summary
                    if (note.archivosAdjuntos.isNotEmpty()) {
                        AttachmentSummary(attachments = note.archivosAdjuntos)
                    }
                }
            }
        }
    }
}

@Composable
private fun AttachmentsGrid(
    attachments: List<ArchivoAdjunto>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        val chunkedAttachments = attachments.chunked(3) // Divide en grupos de 3
        
        chunkedAttachments.forEach { rowAttachments ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                rowAttachments.forEach { attachment ->
                    Box(modifier = Modifier.weight(1f)) {
                        CompactAttachmentPreview(attachment = attachment)
                    }
                }
                
                // Fill remaining spaces in the row with empty boxes
                repeat(3 - rowAttachments.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CompactAttachmentPreview(attachment: ArchivoAdjunto) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f), // Square aspect ratio
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (attachment.tipoArchivo) {
                TipoDeArchivo.Imagen -> {
                    AsyncImage(
                        model = attachment.filePath,
                        contentDescription = "Imagen adjunta",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                TipoDeArchivo.Video -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Video thumbnail placeholder
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {}
                        
                        // Play button overlay - smaller for compact view
                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "Video",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(6.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
            
            // File type indicator - smaller for compact view
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                shape = RoundedCornerShape(3.dp)
            ) {
                Text(
                    text = if (attachment.tipoArchivo == TipoDeArchivo.Imagen) "IMG" else "VID",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                )
            }
        }
    }
}

@Composable
private fun AttachmentPreview(attachment: ArchivoAdjunto) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .height(200.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (attachment.tipoArchivo) {
                TipoDeArchivo.Imagen -> {
                    AsyncImage(
                        model = attachment.filePath,
                        contentDescription = "Imagen adjunta",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                TipoDeArchivo.Video -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Video thumbnail placeholder
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {}
                        
                        // Play button overlay
                        Surface(
                            modifier = Modifier.size(56.dp),
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "Video",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
            
            // File type indicator
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = if (attachment.tipoArchivo == TipoDeArchivo.Imagen) "IMG" else "VID",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun AttachmentSummary(attachments: List<ArchivoAdjunto>) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val imageCount = attachments.count { it.tipoArchivo == TipoDeArchivo.Imagen }
        val videoCount = attachments.count { it.tipoArchivo == TipoDeArchivo.Video }
        
        if (imageCount > 0) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Image,
                    contentDescription = "Imágenes",
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = imageCount.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
        
        if (videoCount > 0) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Videocam,
                    contentDescription = "Videos",
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = videoCount.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

// Utility function to format relative time
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