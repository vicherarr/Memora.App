package com.vicherarr.memora.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.vicherarr.memora.domain.models.MediaFile
import com.vicherarr.memora.domain.models.MediaType
import com.vicherarr.memora.platform.camera.rememberCameraManager
import com.vicherarr.memora.platform.camera.rememberGalleryManager
import com.vicherarr.memora.platform.camera.rememberVideoPickerManager
import com.vicherarr.memora.platform.camera.rememberCameraCaptureManager
import com.vicherarr.memora.platform.camera.CameraCaptureMode
import com.vicherarr.memora.presentation.viewmodels.CreateNoteViewModel
import com.vicherarr.memora.presentation.viewmodels.MediaViewModel
import org.koin.compose.getKoin
import org.koin.compose.koinInject

class CreateNoteScreen : Screen {
    
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val koin = getKoin()
        
        // Use the same MediaViewModel instance that CreateNoteViewModel receives
        val mediaViewModel: MediaViewModel = remember { koin.get() }
        val createNoteViewModel: CreateNoteViewModel = remember { koin.get() }
        
        // Observe UI States from both ViewModels
        val noteUiState by createNoteViewModel.uiState.collectAsState()
        val mediaUiState by mediaViewModel.uiState.collectAsState()
        
        // Navigate back after creating note
        if (noteUiState.isNoteSaved) {
            navigator.pop()
            return
        }
        
        // State for camera capture mode dialog
        var showCameraCaptureDialog: Boolean by remember { mutableStateOf(false) }
        
        // Media managers for direct integration
        val cameraManager = rememberCameraManager { mediaFile ->
            mediaFile?.let { mediaViewModel.addToSelectedMedia(it) }
        }
        
        val cameraCaptureManager = rememberCameraCaptureManager { mediaFile ->
            mediaFile?.let { mediaViewModel.addToSelectedMedia(it) }
        }
        
        val galleryManager = rememberGalleryManager { mediaFile ->
            mediaFile?.let { mediaViewModel.addToSelectedMedia(it) }
        }
        
        val videoPickerManager = rememberVideoPickerManager { mediaFile ->
            mediaFile?.let { mediaViewModel.addToSelectedMedia(it) }
        }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Professional TopAppBar with media count indicator
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver"
                            )
                        }
                        
                        Column(
                            modifier = Modifier.weight(1f).padding(start = 16.dp)
                        ) {
                            Text(
                                text = "Nueva Nota",
                                style = MaterialTheme.typography.titleLarge
                            )
                            
                            // Media count indicator
                            if (mediaUiState.selectedMedia.isNotEmpty()) {
                                Text(
                                    text = "${mediaUiState.selectedMedia.size} archivo(s) adjunto(s)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        // Clear media button
                        if (mediaUiState.selectedMedia.isNotEmpty()) {
                            IconButton(
                                onClick = { mediaViewModel.clearSelectedMedia() }
                            ) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Limpiar multimedia",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
                
                // Content with scrollable layout
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Titulo field
                    OutlinedTextField(
                        value = noteUiState.titulo,
                        onValueChange = createNoteViewModel::updateTitulo,
                        label = { Text("Título (opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !noteUiState.isLoading && !mediaUiState.isLoading
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Media attachment section with professional design
                    if (mediaUiState.selectedMedia.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Multimedia Adjunta",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    
                                    Text(
                                        text = "${mediaUiState.selectedMedia.size}/10",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Media thumbnails grid
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(mediaUiState.selectedMedia) { mediaFile ->
                                        MediaThumbnail(
                                            mediaFile = mediaFile,
                                            onRemove = { mediaViewModel.removeFromSelectedMedia(mediaFile) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // Multimedia Actions with professional card design
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Agregar Multimedia",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Camera Button - Muestra diálogo para elegir foto/video
                                FilledTonalButton(
                                    onClick = { showCameraCaptureDialog = true },
                                    enabled = !noteUiState.isLoading && !mediaUiState.isLoading,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        Icons.Default.CameraAlt,
                                        contentDescription = "Tomar foto o video",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                
                                // Gallery Button - Solo ícono
                                FilledTonalButton(
                                    onClick = { galleryManager.launch() },
                                    enabled = !noteUiState.isLoading && !mediaUiState.isLoading,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        Icons.Default.PhotoLibrary,
                                        contentDescription = "Seleccionar imagen",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                
                                // Video Button - Solo ícono
                                FilledTonalButton(
                                    onClick = { videoPickerManager.launch() },
                                    enabled = !noteUiState.isLoading && !mediaUiState.isLoading,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        Icons.Default.Videocam,
                                        contentDescription = "Seleccionar video",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            
                            // Labels centrados debajo de cada botón
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Cámara",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Text(
                                    text = "Galería",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Text(
                                    text = "Videos",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                    
                    // Contenido field with better spacing
                    OutlinedTextField(
                        value = noteUiState.contenido,
                        onValueChange = createNoteViewModel::updateContenido,
                        label = { Text("Contenido") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        minLines = 8,
                        maxLines = 12,
                        placeholder = { Text("Escribe tu nota aquí...") },
                        enabled = !noteUiState.isLoading && !mediaUiState.isLoading
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Error messages with better styling
                    noteUiState.errorMessage?.let { errorMessage ->
                        if (errorMessage.isNotEmpty()) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = errorMessage,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                    
                    mediaUiState.errorMessage?.let { errorMessage ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                    
                    // Validation hint with better styling
                    createNoteViewModel.getValidationHint(noteUiState.contenido)?.let { hint ->
                        Text(
                            text = hint,
                            color = MaterialTheme.colorScheme.outline,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    
                    // Bottom spacing for FAB
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
            
            // Extended FAB with loading state
            ExtendedFloatingActionButton(
                onClick = createNoteViewModel::createNote,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 88.dp, end = 16.dp), // Extra padding para evitar que lo tape la barra de tabs
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                if (noteUiState.isLoading || mediaUiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Guardando...",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Guardar Nota",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
        
        // Camera Capture Mode Selection Dialog
        if (showCameraCaptureDialog) {
            CameraCaptureDialog(
                onDismiss = { showCameraCaptureDialog = false },
                onPhotoSelected = { 
                    showCameraCaptureDialog = false
                    cameraCaptureManager.launch(CameraCaptureMode.PHOTO)
                },
                onVideoSelected = { 
                    showCameraCaptureDialog = false
                    cameraCaptureManager.launch(CameraCaptureMode.VIDEO)
                }
            )
        }
    }
}

@Composable
private fun MediaThumbnail(
    mediaFile: MediaFile,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.size(80.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            when (mediaFile.type) {
                MediaType.IMAGE -> {
                    AsyncImage(
                        model = mediaFile.data,
                        contentDescription = mediaFile.fileName,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                MediaType.VIDEO -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.8f),
                                RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Video",
                            modifier = Modifier.size(32.dp),
                            tint = androidx.compose.ui.graphics.Color.White
                        )
                        // Mostrar nombre del archivo en la parte inferior
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .background(
                                    androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.6f),
                                    RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                                )
                                .padding(4.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = mediaFile.fileName ?: "Video",
                                style = MaterialTheme.typography.labelSmall,
                                color = androidx.compose.ui.graphics.Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
            
            // Remove button
            Surface(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(20.dp),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.9f)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Eliminar",
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onError
                    )
                }
            }
            
            // Media type indicator
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(4.dp),
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
            ) {
                Text(
                    text = when (mediaFile.type) {
                        MediaType.IMAGE -> "IMG"
                        MediaType.VIDEO -> "VID"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Dialog for selecting camera capture mode (Photo or Video)
 * Following Material Design 3 guidelines
 */
@Composable
private fun CameraCaptureDialog(
    onDismiss: () -> Unit,
    onPhotoSelected: () -> Unit,
    onVideoSelected: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Capturar con Cámara",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                text = "¿Qué deseas capturar?",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Photo option
                FilledTonalButton(
                    onClick = onPhotoSelected,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.PhotoCamera,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Foto")
                }
                
                // Video option
                FilledTonalButton(
                    onClick = onVideoSelected,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Videocam,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Video")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}