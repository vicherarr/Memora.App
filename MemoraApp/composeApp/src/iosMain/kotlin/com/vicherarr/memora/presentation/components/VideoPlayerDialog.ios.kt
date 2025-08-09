@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.vicherarr.memora.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.unit.dp
import kotlinx.cinterop.*
import platform.AVFoundation.*
import platform.AVKit.*
import platform.Foundation.*
import platform.UIKit.*

/**
 * iOS implementation of VideoPlayerView using AVPlayerViewController
 * Simplified version to avoid complex API issues
 */
@Composable
actual fun VideoPlayerView(
    videoData: Any,
    onTap: () -> Unit,
    modifier: Modifier
) {
    UIKitView(
        factory = {
            val playerViewController = AVPlayerViewController()
            
            // Set up video URL based on data type
            val videoURL = when (videoData) {
                is String -> {
                    // File path
                    NSURL.fileURLWithPath(videoData)
                }
                is ByteArray -> {
                    // For ByteArray, create a temporary file
                    try {
                        val tempDir = NSTemporaryDirectory()
                        val fileName = "video_${kotlin.random.Random.nextInt()}.mp4"
                        val filePath = "${tempDir}$fileName"
                        
                        // Convert ByteArray to NSData
                        val nsData = videoData.usePinned { pinned ->
                            NSData.create(
                                bytes = pinned.addressOf(0),
                                length = videoData.size.toULong()
                            )
                        }
                        
                        if (nsData.writeToFile(filePath, atomically = true)) {
                            NSURL.fileURLWithPath(filePath)
                        } else {
                            println("VideoPlayerView iOS: Error writing video data to file")
                            null
                        }
                    } catch (e: Exception) {
                        println("VideoPlayerView iOS: Error creating video file: ${e.message}")
                        null
                    }
                }
                else -> {
                    println("VideoPlayerView iOS: Unsupported video data type")
                    null
                }
            }
            
            // Configure player if URL is valid
            videoURL?.let { url ->
                val player = AVPlayer(uRL = url)
                playerViewController.player = player
                
                // Configure player view controller
                playerViewController.showsPlaybackControls = true
                playerViewController.allowsPictureInPicturePlayback = false
                
                // Auto-start video
                player.play()
            }
            
            playerViewController.view
        },
        modifier = modifier.fillMaxSize()
    )
}