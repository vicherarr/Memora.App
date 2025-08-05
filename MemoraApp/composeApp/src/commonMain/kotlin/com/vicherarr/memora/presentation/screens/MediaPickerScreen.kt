package com.vicherarr.memora.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.vicherarr.memora.domain.models.MediaResult
import com.vicherarr.memora.presentation.viewmodels.MediaViewModel
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
                                contentDescription = "Back"
                            )
                        }
                        
                        Text(
                            text = if (allowMultiple) "Select Media" else "Pick Media",
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
                                    contentDescription = "Confirm Selection"
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
                                if (allowMultiple) {
                                    mediaViewModel.pickMultipleImages(maxSelection)
                                } else {
                                    mediaViewModel.pickImage()
                                }
                            },
                            enabled = !uiState.isLoading,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (allowMultiple) "Pick Images" else "Pick Image"
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        // Pick Video Button
                        ElevatedButton(
                            onClick = {
                                mediaViewModel.pickVideo()
                            },
                            enabled = !uiState.isLoading && !allowMultiple, // Only single video for now
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Pick Video")
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
                                        Column(
                                            modifier = Modifier.padding(16.dp)
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
                                    text = "No media selected",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tap a button above to select media",
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