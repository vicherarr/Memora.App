package com.vicherarr.memora.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.vicherarr.memora.domain.models.MediaOperationType
import com.vicherarr.memora.platform.camera.rememberCameraManager
import com.vicherarr.memora.presentation.states.MediaUiState
import com.vicherarr.memora.presentation.viewmodels.MediaViewModel
import org.koin.compose.koinInject

/**
 * Camera screen for capturing photos and videos
 * Follows Material Design 3 guidelines with safe areas support
 */
class CameraScreen : Screen {
    
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val mediaViewModel: MediaViewModel = koinInject()
        val uiState by mediaViewModel.uiState.collectAsState(initial = MediaUiState())
        
        // Setup camera manager with callback
        val cameraManager = rememberCameraManager { mediaFile ->
            mediaViewModel.onCameraResult(mediaFile)
            // Navigate back to previous screen when photo is captured
            if (mediaFile != null) {
                navigator.pop()
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .background(Color.Black)
        ) {
            // Camera Preview Area
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Black.copy(alpha = 0.7f)
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
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        
                        Text(
                            text = "Camera",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                        
                        // Placeholder for balance
                        Spacer(modifier = Modifier.width(48.dp))
                    }
                }
                
                // Camera Preview Placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.Gray),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = "Camera Preview",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White
                        )
                    }
                }
                
                // Controls Area
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Black.copy(alpha = 0.7f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Video Record Button
                        IconButton(
                            onClick = {
                                mediaViewModel.recordVideo()
                            },
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    if (uiState.operationType == MediaOperationType.VIDEO_RECORDING) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.secondary
                                    }
                                ),
                            enabled = !uiState.isLoading
                        ) {
                            Icon(
                                imageVector = Icons.Default.VideoCall,
                                contentDescription = "Record Video",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        
                        // Photo Capture Button
                        IconButton(
                            onClick = {
                                mediaViewModel.setLoading(true)
                                cameraManager.launch()
                            },
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            enabled = !uiState.isLoading
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Take Photo",
                                tint = Color.Black,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        
                        // Placeholder for balance
                        Spacer(modifier = Modifier.size(64.dp))
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
}