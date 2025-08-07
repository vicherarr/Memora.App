package com.vicherarr.memora.presentation.tabs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import coil3.compose.AsyncImage
import com.vicherarr.memora.domain.models.ArchivoAdjunto
import com.vicherarr.memora.domain.models.MediaType
import com.vicherarr.memora.domain.models.Note
import com.vicherarr.memora.domain.models.TipoDeArchivo
import com.vicherarr.memora.presentation.viewmodels.NotesViewModel
import org.koin.compose.getKoin

object NotesTab : Tab {
    
    override val options: TabOptions
        @Composable
        get() {
            val title = "Notas"
            val icon = rememberVectorPainter(Icons.Default.Home)
            
            return remember {
                TabOptions(
                    index = 0u,
                    title = title,
                    icon = icon
                )
            }
        }
    
    @Composable
    override fun Content() {
        val koin = getKoin()
        val notesViewModel: NotesViewModel = remember { koin.get() }
        
        val notes by notesViewModel.notes.collectAsState()
        val isLoading by notesViewModel.isLoading.collectAsState()
        val error by notesViewModel.error.collectAsState()
        
        // Debug logging for notes data
        println("NotesTab: Total notes loaded: ${notes.size}")
        notes.forEachIndexed { index, note ->
            println("NotesTab: Note $index - id: ${note.id}, title: '${note.titulo}', attachments: ${note.archivosAdjuntos.size}")
        }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    
                    error != null && error!!.isNotEmpty() -> {
                        Text(
                            text = error!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    
                    notes.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "¡No tienes notas aún!",
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
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
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(notes) { note ->
                                println("NotesTab: Rendering note ${note.id} - attachments: ${note.archivosAdjuntos.size}")
                                EnhancedNoteCard(
                                    note = note,
                                    onClick = {
                                        // TODO: Navegar a detalle de nota
                                    }
                                )
                            }
                        }
                    }
            }
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
            println("EnhancedNoteCard: Note ${note.id} has ${note.archivosAdjuntos.size} attachments")
            
            // Multimedia Preview Section (if attachments exist)
            if (note.archivosAdjuntos.isNotEmpty()) {
                println("EnhancedNoteCard: Showing multimedia preview for note ${note.id}")
                MultimediaPreviewSection(
                    attachments = note.archivosAdjuntos,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                println("EnhancedNoteCard: No attachments for note ${note.id}")
            }
            
            // Content Section
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Title and Multimedia Indicators Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Title Column
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        if (!note.titulo.isNullOrBlank()) {
                            Text(
                                text = note.titulo,
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        
                        Text(
                            text = note.contenido,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = if (note.archivosAdjuntos.isNotEmpty()) 3 else 5,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                        )
                    }
                    
                    // Multimedia Indicators (compact version for cards with preview)
                    if (note.archivosAdjuntos.isNotEmpty()) {
                        MultimediaIndicators(
                            attachments = note.archivosAdjuntos,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Bottom Row: Date and Attachment Summary
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = formatRelativeTime(note.fechaModificacion),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    
                    // Attachment Summary or Note Type indicator
                    if (note.archivosAdjuntos.isNotEmpty()) {
                        AttachmentSummary(
                            attachments = note.archivosAdjuntos
                        )
                    } else {
                        // Visual indicator for text-only notes
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Article,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Nota",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MultimediaPreviewSection(
    attachments: List<ArchivoAdjunto>,
    modifier: Modifier = Modifier
) {
    // Debug logging
    println("MultimediaPreviewSection: Total attachments: ${attachments.size}")
    attachments.forEachIndexed { index, attachment ->
        println("MultimediaPreviewSection: Attachment $index - type: ${attachment.tipoArchivo}, name: ${attachment.nombreOriginal}, size: ${attachment.tamanoBytes}")
    }
    
    // Show first 3 images as a preview grid, or video preview if no images
    val imageAttachments = attachments.filter { it.tipoArchivo == TipoDeArchivo.Imagen }.take(3)
    val videoAttachments = attachments.filter { it.tipoArchivo == TipoDeArchivo.Video }
    
    println("MultimediaPreviewSection: Image attachments: ${imageAttachments.size}, Video attachments: ${videoAttachments.size}")
    
    if (imageAttachments.isNotEmpty()) {
        // Show image previews
        Box(
            modifier = modifier
                .height(120.dp)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
        ) {
            when (imageAttachments.size) {
                1 -> {
                    // Single large image with debug logging
                    println("MultimediaPreviewSection: Loading single image - size: ${imageAttachments[0].tamanoBytes} bytes")
                    AsyncImage(
                        model = imageAttachments[0].datosArchivo,
                        contentDescription = "Imagen adjunta",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        onSuccess = {
                            println("MultimediaPreviewSection: Image loaded successfully")
                        },
                        onError = { error ->
                            println("MultimediaPreviewSection: Error loading image: ${error.result.throwable?.message}")
                        },
                        onLoading = {
                            println("MultimediaPreviewSection: Loading image...")
                        }
                    )
                }
                2 -> {
                    // Two images side by side
                    Row {
                        AsyncImage(
                            model = imageAttachments[0].datosArchivo,
                            contentDescription = "Imagen 1",
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(1.dp))
                        AsyncImage(
                            model = imageAttachments[1].datosArchivo,
                            contentDescription = "Imagen 2",
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                else -> {
                    // Three images: one large + two small
                    Row {
                        AsyncImage(
                            model = imageAttachments[0].datosArchivo,
                            contentDescription = "Imagen principal",
                            modifier = Modifier.weight(2f).fillMaxHeight(),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(1.dp))
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            AsyncImage(
                                model = imageAttachments[1].datosArchivo,
                                contentDescription = "Imagen 2",
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                contentScale = ContentScale.Crop
                            )
                            AsyncImage(
                                model = imageAttachments[2].datosArchivo,
                                contentDescription = "Imagen 3",
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
            
            // Video overlay if there are videos mixed with images
            val videoCount = attachments.count { it.tipoArchivo == TipoDeArchivo.Video }
            if (videoCount > 0) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSecondary
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = if (videoCount == 1) "Video" else "${videoCount} Videos",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                }
            }
            
            // Overlay for additional files indicator
            if (attachments.size > 3) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                ) {
                    Text(
                        text = "+${attachments.size - 3}",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    } else if (videoAttachments.isNotEmpty()) {
        // Show video preview when no images are present
        Box(
            modifier = modifier
                .height(120.dp)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.PlayCircleFilled,
                            contentDescription = "Video",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (videoAttachments.size == 1) "Video" else "${videoAttachments.size} Videos",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = videoAttachments.firstOrNull()?.nombreOriginal ?: "Video adjunto",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MultimediaIndicators(
    attachments: List<ArchivoAdjunto>,
    modifier: Modifier = Modifier
) {
    val imageCount = attachments.count { it.tipoArchivo == TipoDeArchivo.Imagen }
    val videoCount = attachments.count { it.tipoArchivo == TipoDeArchivo.Video }
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (imageCount > 0) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = imageCount.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        if (videoCount > 0) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Videocam,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = videoCount.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
private fun AttachmentSummary(
    attachments: List<ArchivoAdjunto>,
    modifier: Modifier = Modifier
) {
    val imageCount = attachments.count { it.tipoArchivo == TipoDeArchivo.Imagen }
    val videoCount = attachments.count { it.tipoArchivo == TipoDeArchivo.Video }
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (imageCount > 0) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Image,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = imageCount.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        if (videoCount > 0) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Videocam,
                    contentDescription = null,
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
        diff < 60_000 -> "Hace un momento" // Less than 1 minute
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