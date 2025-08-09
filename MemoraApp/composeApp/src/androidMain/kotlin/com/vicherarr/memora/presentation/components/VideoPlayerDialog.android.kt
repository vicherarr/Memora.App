package com.vicherarr.memora.presentation.components

import android.media.MediaPlayer
import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import java.io.File
import java.io.FileOutputStream

/**
 * Android implementation of VideoPlayerView using VideoView
 * Handles its own state internally, only communicates tap events
 * No LaunchedEffect usage for better stability in KMP context
 */
@Composable
actual fun VideoPlayerView(
    videoData: Any,
    onTap: () -> Unit,
    modifier: Modifier
) {
    val context = LocalContext.current
    
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surface)
    ) {
        AndroidView(
            factory = { ctx ->
                VideoView(ctx).apply {
                    // Set up video URI based on data type
                    val videoUri = when (videoData) {
                        is String -> {
                            // File path
                            Uri.parse(videoData)
                        }
                        is ByteArray -> {
                            // Convert byte array to temporary file
                            try {
                                val tempFile = File.createTempFile("video_", ".mp4", ctx.cacheDir)
                                tempFile.deleteOnExit() // Clean up when app exits
                                val fos = FileOutputStream(tempFile)
                                fos.write(videoData)
                                fos.close()
                                Uri.fromFile(tempFile)
                            } catch (e: Exception) {
                                println("VideoPlayerView Android: Error creating video file: ${e.message}")
                                null
                            }
                        }
                        else -> null
                    }
                    
                    // Configure video view if URI is valid
                    videoUri?.let { uri ->
                        setVideoURI(uri)
                        
                        // Set up media player listeners
                        setOnPreparedListener { mediaPlayer ->
                            // Configure media player
                            mediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
                            mediaPlayer.isLooping = false
                            
                            // Auto-start video
                            start()
                        }
                        
                        setOnCompletionListener {
                            // Video completed - can restart if needed
                            println("VideoPlayerView Android: Video playback completed")
                        }
                        
                        setOnErrorListener { _, what, extra ->
                            println("VideoPlayerView Android: Error occurred: what=$what, extra=$extra")
                            true // Return true if error was handled
                        }
                        
                        // Handle tap events
                        setOnClickListener {
                            onTap()
                        }
                        
                        // Use built-in media controller for platform-native experience
                        setMediaController(android.widget.MediaController(ctx).apply {
                            setAnchorView(this@apply)
                        })
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}