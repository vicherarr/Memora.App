package com.vicherarr.memora.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Video Player Dialog Component
 * 
 * Professional video player with native platform integration and elegant Material Design 3 styling
 * Supports both file paths and byte arrays for maximum compatibility
 * Follows the same design pattern as ImageFullScreenViewer for consistency
 * 
 * @param videoData The video data (file path or byte array)
 * @param fileName Optional file name for accessibility and display
 * @param isVisible Controls visibility of the player
 * @param onDismiss Callback when user closes the player
 */
@Composable
fun VideoPlayerDialog(
    videoData: Any, // Can be String (file path) or ByteArray
    fileName: String? = null,
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    if (!isVisible) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false, // Video should not dismiss on outside click during playback
            usePlatformDefaultWidth = false
        )
    ) {
        VideoPlayerContent(
            videoData = videoData,
            fileName = fileName,
            onDismiss = onDismiss
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VideoPlayerContent(
    videoData: Any,
    fileName: String?,
    onDismiss: () -> Unit
) {
    var isControlsVisible by remember { mutableStateOf(true) }
    
    // Auto-hide controls after 3 seconds
    LaunchedEffect(isControlsVisible) {
        if (isControlsVisible) {
            kotlinx.coroutines.delay(3000)
            isControlsVisible = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // Main video player area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    isControlsVisible = !isControlsVisible
                },
            contentAlignment = Alignment.Center
        ) {
            // Platform-specific video player - handles its own state internally
            VideoPlayerView(
                videoData = videoData,
                onTap = { isControlsVisible = !isControlsVisible },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Top controls (Back button and title)
        AnimatedVisibility(
            visible = isControlsVisible,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300)),
            exit = slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300)),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // File name or placeholder
                    Text(
                        text = fileName ?: "Video",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Back button (consistent with ImageFullScreenViewer)
                    Surface(
                        onClick = onDismiss,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

    }
}

/**
 * Platform-specific video player view
 * Each platform handles its own internal state and controls
 * Only communicates essential events back to common UI
 */
@Composable
expect fun VideoPlayerView(
    videoData: Any, // String (file path) or ByteArray
    onTap: () -> Unit, // Show/hide overlay controls
    modifier: Modifier = Modifier
)

