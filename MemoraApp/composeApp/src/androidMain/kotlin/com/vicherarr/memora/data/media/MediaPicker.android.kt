package com.vicherarr.memora.data.media

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.vicherarr.memora.domain.models.MediaFile
import com.vicherarr.memora.domain.models.MediaResult
import com.vicherarr.memora.domain.models.MediaType
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Android implementation of MediaPicker using Intent system
 */
actual class MediaPicker(
    private val context: Context,
    private val activity: AppCompatActivity
) {
    private var imagePickerLauncher: ActivityResultLauncher<Intent>? = null
    private var videoPickerLauncher: ActivityResultLauncher<Intent>? = null
    private var multipleImagePickerLauncher: ActivityResultLauncher<Intent>? = null
    
    init {
        setupLaunchers()
    }
    
    private fun setupLaunchers() {
        // Single image picker
        imagePickerLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            handleSingleMediaResult(result.data?.data, MediaType.IMAGE)
        }
        
        // Video picker
        videoPickerLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            handleSingleMediaResult(result.data?.data, MediaType.VIDEO)
        }
        
        // Multiple images picker
        multipleImagePickerLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            handleMultipleImageResult(result.data)
        }
    }
    
    private var currentImageContinuation: kotlin.coroutines.Continuation<MediaResult>? = null
    private var currentVideoContinuation: kotlin.coroutines.Continuation<MediaResult>? = null
    private var currentMultipleContinuation: kotlin.coroutines.Continuation<List<MediaResult>>? = null
    
    actual suspend fun pickImage(): MediaResult = suspendCancellableCoroutine { continuation ->
        currentImageContinuation = continuation
        
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
        }
        
        try {
            imagePickerLauncher?.launch(intent)
        } catch (e: Exception) {
            continuation.resume(MediaResult.Error("Failed to launch image picker: ${e.message}"))
        }
    }
    
    actual suspend fun pickVideo(): MediaResult = suspendCancellableCoroutine { continuation ->
        currentVideoContinuation = continuation
        
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI).apply {
            type = "video/*"
        }
        
        try {
            videoPickerLauncher?.launch(intent)
        } catch (e: Exception) {
            continuation.resume(MediaResult.Error("Failed to launch video picker: ${e.message}"))
        }
    }
    
    actual suspend fun pickMultipleImages(maxSelection: Int): List<MediaResult> = 
        suspendCancellableCoroutine { continuation ->
            currentMultipleContinuation = continuation
            
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            
            try {
                multipleImagePickerLauncher?.launch(intent)
            } catch (e: Exception) {
                continuation.resume(listOf(MediaResult.Error("Failed to launch multiple image picker: ${e.message}")))
            }
        }
    
    private fun handleSingleMediaResult(uri: Uri?, mediaType: MediaType) {
        val continuation = when (mediaType) {
            MediaType.IMAGE -> currentImageContinuation
            MediaType.VIDEO -> currentVideoContinuation
        }
        
        if (uri == null) {
            continuation?.resume(MediaResult.Cancelled)
            return
        }
        
        try {
            val mediaFile = createMediaFileFromUri(uri, mediaType)
            continuation?.resume(MediaResult.Success(mediaFile))
        } catch (e: Exception) {
            continuation?.resume(MediaResult.Error("Failed to process selected media: ${e.message}"))
        } finally {
            when (mediaType) {
                MediaType.IMAGE -> currentImageContinuation = null
                MediaType.VIDEO -> currentVideoContinuation = null
            }
        }
    }
    
    private fun handleMultipleImageResult(intent: Intent?) {
        val continuation = currentMultipleContinuation
        currentMultipleContinuation = null
        
        if (intent == null) {
            continuation?.resume(listOf(MediaResult.Cancelled))
            return
        }
        
        val results = mutableListOf<MediaResult>()
        
        // Handle multiple selection
        intent.clipData?.let { clipData ->
            for (i in 0 until clipData.itemCount) {
                val uri = clipData.getItemAt(i).uri
                try {
                    val mediaFile = createMediaFileFromUri(uri, MediaType.IMAGE)
                    results.add(MediaResult.Success(mediaFile))
                } catch (e: Exception) {
                    results.add(MediaResult.Error("Failed to process image $i: ${e.message}"))
                }
            }
        } ?: run {
            // Handle single selection
            intent.data?.let { uri ->
                try {
                    val mediaFile = createMediaFileFromUri(uri, MediaType.IMAGE)
                    results.add(MediaResult.Success(mediaFile))
                } catch (e: Exception) {
                    results.add(MediaResult.Error("Failed to process selected image: ${e.message}"))
                }
            } ?: results.add(MediaResult.Cancelled)
        }
        
        continuation?.resume(results)
    }
    
    private fun createMediaFileFromUri(uri: Uri, mediaType: MediaType): MediaFile {
        val contentResolver = context.contentResolver
        val inputStream = contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Cannot open input stream for URI: $uri")
        
        val data = inputStream.use { it.readBytes() }
        
        // Get file name and MIME type
        val cursor = contentResolver.query(uri, null, null, null, null)
        val fileName = cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                if (nameIndex >= 0) it.getString(nameIndex) else null
            } else null
        } ?: "media_${System.currentTimeMillis()}.${if (mediaType == MediaType.IMAGE) "jpg" else "mp4"}"
        
        val mimeType = contentResolver.getType(uri) ?: when (mediaType) {
            MediaType.IMAGE -> "image/jpeg"
            MediaType.VIDEO -> "video/mp4"
        }
        
        return MediaFile(
            data = data,
            fileName = fileName,
            mimeType = mimeType,
            type = mediaType,
            sizeBytes = data.size.toLong()
        )
    }
}