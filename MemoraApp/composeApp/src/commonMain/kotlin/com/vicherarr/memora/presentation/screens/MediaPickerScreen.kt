package com.vicherarr.memora.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.vicherarr.memora.domain.models.MediaResult
import com.vicherarr.memora.presentation.viewmodels.MediaViewModel
import com.vicherarr.memora.platform.camera.rememberGalleryManager
import org.koin.compose.koinInject

/**
 * Media picker screen for selecting images and videos from gallery
 * Supports single and multiple selection modes
 */
data class MediaPickerScreen(
    val allowMultiple: Boolean = false,
    val maxSelection: Int = 5
) : Screen {
    
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val mediaViewModel: MediaViewModel = koinInject()
        val uiState by mediaViewModel.uiState.collectAsState()
        
        // Gallery Manager para selección de imágenes
        val galleryManager = rememberGalleryManager { mediaFile ->
            println("MediaPickerScreen: Gallery callback received - mediaFile: ${mediaFile != null}")
            
            mediaViewModel.setLoading(false) // Finalizar loading
            println("MediaPickerScreen: Loading set to false")
            
            mediaFile?.let { 
                println("MediaPickerScreen: Processing mediaFile - size: ${it.sizeBytes} bytes")
                // Siempre usar selectedMedia para consistencia en la UI
                if (allowMultiple) {
                    mediaViewModel.addToSelectedMedia(it)
                    println("MediaPickerScreen: Added to selected media (multiple mode)")
                } else {
                    // Para modo simple, limpiar lista y agregar solo esta imagen
                    mediaViewModel.clearSelectedMedia()
                    mediaViewModel.addToSelectedMedia(it)
                    println("MediaPickerScreen: Added to selected media (single mode)")
                }
            } ?: run {
                // Si mediaFile es null, significa que se canceló o falló
                println("MediaPickerScreen: mediaFile is null - clearing error")
                mediaViewModel.clearError()
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { navigator.pop() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Volver"
                            )
                        }
                        
                        Text(
                            text = if (allowMultiple) "Seleccionar Multimedia" else "Elegir Multimedia",
                            style = MaterialTheme.typography.titleLarge
                        )
                        
                        if (allowMultiple && uiState.selectedMedia.isNotEmpty()) {
                            TextButton(
                                onClick = {
                                    // Confirm selection and navigate back
                                    navigator.pop()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Confirmar Selección"
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${uiState.selectedMedia.size}")
                            }
                        } else {
                            // Placeholder for balance
                            Spacer(modifier = Modifier.width(48.dp))
                        }
                    }
                }
                
                // Action Buttons
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Pick Image Button
                        ElevatedButton(
                            onClick = {
                                println("MediaPickerScreen: Pick Image button clicked")
                                mediaViewModel.setLoading(true)
                                println("MediaPickerScreen: Loading set to true, launching gallery")
                                galleryManager.launch()
                                println("MediaPickerScreen: Gallery launch called")
                            },
                            enabled = !uiState.isLoading,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = if (allowMultiple) "Elegir Imágenes" else "Elegir Imagen"
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        // Pick Video Button (temporalmente deshabilitado hasta implementar video picker)
                        ElevatedButton(
                            onClick = {
                                // TODO: Implementar video picker
                                mediaViewModel.clearError()
                            },
                            enabled = false, // Temporalmente deshabilitado
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Elegir Video")
                        }
                    }
                }
                
                // Content Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        uiState.isLoading -> {
                            CircularProgressIndicator()
                        }
                        
                        uiState.selectedMedia.isNotEmpty() -> {
                            // Show selected media preview
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(uiState.selectedMedia) { mediaFile ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Preview de la imagen usando Coil
                                            if (mediaFile.type.name == "IMAGE") {
                                                AsyncImage(
                                                    model = mediaFile.data,
                                                    contentDescription = mediaFile.fileName,
                                                    modifier = Modifier
                                                        .size(64.dp)
                                                        .clip(RoundedCornerShape(8.dp)),
                                                    contentScale = ContentScale.Crop,
                                                    error = null // Coil manejará automáticamente los errores
                                                )
                                            } else {
                                                // Placeholder para videos
                                                Surface(
                                                    modifier = Modifier
                                                        .size(64.dp)
                                                        .clip(RoundedCornerShape(8.dp)),
                                                    color = MaterialTheme.colorScheme.surfaceVariant
                                                ) {
                                                    Box(
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            "VID", 
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }
                                            
                                            Spacer(modifier = Modifier.width(16.dp))
                                            
                                            // Información del archivo
                                            Column(
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(
                                                    text = mediaFile.fileName,
                                                    style = MaterialTheme.typography.titleMedium
                                                )
                                                Text(
                                                    text = "${mediaFile.type.name} • ${formatFileSize(mediaFile.sizeBytes)}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            
                                            // Botón para eliminar si es modo múltiple
                                            if (allowMultiple) {
                                                IconButton(
                                                    onClick = {
                                                        mediaViewModel.removeFromSelectedMedia(mediaFile)
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Eliminar",
                                                        tint = MaterialTheme.colorScheme.error
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        else -> {
                            // Empty state
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Sin multimedia seleccionada",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Toca un botón arriba para seleccionar multimedia",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            
            // Error Display
            uiState.errorMessage?.let { error ->
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
    
    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            bytes >= 1024 -> "${bytes / 1024} KB"
            else -> "$bytes B"
        }
    }
}