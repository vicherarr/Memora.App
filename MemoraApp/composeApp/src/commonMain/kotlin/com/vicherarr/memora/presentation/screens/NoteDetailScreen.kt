package com.vicherarr.memora.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.vicherarr.memora.domain.models.TipoDeArchivo
import com.vicherarr.memora.platform.camera.rememberCameraManager
import com.vicherarr.memora.platform.camera.rememberGalleryManager
import com.vicherarr.memora.platform.camera.rememberVideoPickerManager
import com.vicherarr.memora.platform.camera.rememberCameraCaptureManager
import com.vicherarr.memora.platform.camera.CameraCaptureMode
import com.vicherarr.memora.presentation.viewmodels.NoteDetailViewModel
import com.vicherarr.memora.presentation.viewmodels.MediaViewModel
import com.vicherarr.memora.presentation.components.ImageFullScreenViewer
import com.vicherarr.memora.presentation.components.VideoPlayerDialog
import com.vicherarr.memora.ui.components.MemoraTextField
import org.koin.compose.getKoin

/**
 * Note Detail Screen - Displays a specific note with view/edit capabilities
 * Follows Material Design 3 guidelines with safe areas for iOS compatibility
 */
data class NoteDetailScreen(private val noteId: String) : Screen {
    
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val koin = getKoin()
        val viewModel: NoteDetailViewModel = remember { koin.get() }
        val mediaViewModel: MediaViewModel = remember { koin.get() }
        val uiState by viewModel.uiState.collectAsState()
        val mediaUiState by mediaViewModel.uiState.collectAsState()
        
        // State for camera capture mode dialog
        var showCameraCaptureDialog: Boolean by remember { mutableStateOf(false) }
        
        // Media managers for multimedia functionality
        val cameraManager = rememberCameraManager { mediaFile ->
            mediaFile?.let { 
                mediaViewModel.addToSelectedMedia(it)
                // Automatically add to note when media is captured
                viewModel.addMediaToNote()
            }
        }
        
        val cameraCaptureManager = rememberCameraCaptureManager { mediaFile ->
            mediaFile?.let { 
                mediaViewModel.addToSelectedMedia(it)
                // Automatically add to note when media is captured
                viewModel.addMediaToNote()
            }
        }
        
        val galleryManager = rememberGalleryManager { mediaFile ->
            mediaFile?.let { 
                mediaViewModel.addToSelectedMedia(it)
                // Automatically add to note when media is selected
                viewModel.addMediaToNote()
            }
        }
        
        val videoPickerManager = rememberVideoPickerManager { mediaFile ->
            mediaFile?.let { 
                mediaViewModel.addToSelectedMedia(it)
                // Automatically add to note when video is selected
                viewModel.addMediaToNote()
            }
        }
        
        // Load note when screen appears
        LaunchedEffect(noteId) {
            viewModel.loadNote(noteId)
        }
        
        // Handle navigation after delete
        LaunchedEffect(uiState.isNoteDeleted) {
            if (uiState.isNoteDeleted) {
                navigator.pop()
            }
        }
        
        // Show snackbar for save success
        if (uiState.isNoteSaved) {
            LaunchedEffect(Unit) {
                // Clear message after showing
                viewModel.clearMessages()
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Custom Top Bar
                NoteDetailTopBar(
                    isEditMode = uiState.isEditMode,
                    canSave = viewModel.canSaveNote(),
                    onBackClick = { navigator.pop() },
                    onEditClick = { viewModel.enterEditMode() },
                    onSaveClick = { viewModel.saveNote() },
                    onCancelClick = { viewModel.exitEditMode() },
                    onDeleteClick = { viewModel.deleteNote() }
                )
                
                // Content
                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    uiState.errorMessage != null -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
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
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    
                    uiState.note != null -> {
                        if (uiState.isEditMode) {
                            EditNoteContent(
                                titulo = uiState.editTitulo,
                                contenido = uiState.editContenido,
                                editAttachments = uiState.editAttachments,
                                newlySelectedMedia = mediaUiState.selectedMedia,
                                onTituloChange = viewModel::updateEditTitulo,
                                onContenidoChange = viewModel::updateEditContenido,
                                onRemoveAttachment = viewModel::removeAttachment,
                                onRemoveNewMedia = mediaViewModel::removeFromSelectedMedia,
                                onAttachmentClick = { attachment -> viewModel.showMediaViewer(attachment) },
                                onMediaFileClick = { mediaFile -> viewModel.showMediaViewer(mediaFile) },
                                onCameraClick = { showCameraCaptureDialog = true },
                                onGalleryClick = { galleryManager.launch() },
                                onVideoPickerClick = { videoPickerManager.launch() },
                                validationHint = viewModel.getValidationHint(uiState.editContenido)
                            )
                        } else {
                            ViewNoteContent(
                                note = uiState.note!!,
                                onMediaClick = { attachment -> viewModel.showMediaViewer(attachment) }
                            )
                        }
                    }
                }
            }
        }

        // Full screen image viewer - Following MVVM pattern
        uiState.imageViewer.imageData?.let { imageData ->
            ImageFullScreenViewer(
                imageData = imageData,
                fileName = uiState.imageViewer.imageName,
                isVisible = uiState.imageViewer.isVisible,
                onDismiss = viewModel::hideImageViewer
            )
        }
        
        // Video player dialog - Following MVVM pattern
        uiState.videoViewer.videoData?.let { videoData ->
            VideoPlayerDialog(
                videoData = videoData,
                fileName = uiState.videoViewer.videoName,
                isVisible = uiState.videoViewer.isVisible,
                onDismiss = viewModel::hideVideoViewer
            )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteDetailTopBar(
    isEditMode: Boolean,
    canSave: Boolean,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Actions
            Row {
                if (isEditMode) {
                    // Cancel edit
                    TextButton(onClick = onCancelClick) {
                        Text("Cancelar")
                    }
                    
                    // Save note
                    Button(
                        onClick = onSaveClick,
                        enabled = canSave
                    ) {
                        Text("Guardar")
                    }
                } else {
                    // Edit note
                    IconButton(onClick = onEditClick) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    // Delete note
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar nota") },
            text = { Text("¿Estás seguro de que quieres eliminar esta nota? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteClick()
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun EditNoteContent(
    titulo: String,
    contenido: String,
    editAttachments: List<com.vicherarr.memora.domain.models.ArchivoAdjunto>,
    newlySelectedMedia: List<com.vicherarr.memora.domain.models.MediaFile>,
    onTituloChange: (String) -> Unit,
    onContenidoChange: (String) -> Unit,
    onRemoveAttachment: (String) -> Unit,
    onRemoveNewMedia: (com.vicherarr.memora.domain.models.MediaFile) -> Unit,
    onAttachmentClick: (com.vicherarr.memora.domain.models.ArchivoAdjunto) -> Unit,
    onMediaFileClick: (com.vicherarr.memora.domain.models.MediaFile) -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onVideoPickerClick: () -> Unit,
    validationHint: String?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Título field
        MemoraTextField(
            value = titulo,
            onValueChange = onTituloChange,
            label = "Título (opcional)",
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Attachments section in edit mode - show both existing and newly selected
        if (editAttachments.isNotEmpty() || newlySelectedMedia.isNotEmpty()) {
            Text(
                text = "Archivos adjuntos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                modifier = Modifier.heightIn(max = 300.dp) // Limit height for edit mode
            ) {
                // Show existing attachments (can be edited/deleted)
                items(editAttachments) { attachment ->
                    EditableAttachmentItem(
                        attachment = attachment,
                        onRemove = { onRemoveAttachment(attachment.id) },
                        onMediaClick = { onAttachmentClick(attachment) }
                    )
                }
                
                // Show newly selected media (can be removed before saving)
                items(newlySelectedMedia) { mediaFile ->
                    NewMediaThumbnail(
                        mediaFile = mediaFile,
                        onRemove = { onRemoveNewMedia(mediaFile) },
                        onMediaClick = { onMediaFileClick(mediaFile) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Multimedia Actions section
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
                    // Camera Button
                    FilledTonalButton(
                        onClick = onCameraClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Tomar foto",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    // Gallery Button
                    FilledTonalButton(
                        onClick = onGalleryClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.PhotoLibrary,
                            contentDescription = "Seleccionar imagen",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    // Video Button
                    FilledTonalButton(
                        onClick = onVideoPickerClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.Videocam,
                            contentDescription = "Seleccionar video",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                // Labels below buttons
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
        
        // Contenido field
        MemoraTextField(
            value = contenido,
            onValueChange = onContenidoChange,
            label = "Contenido",
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp),
            maxLines = Int.MAX_VALUE,
            isError = validationHint != null,
            supportingText = validationHint
        )
    }
}

@Composable
private fun ViewNoteContent(
    note: com.vicherarr.memora.domain.models.Note,
    onMediaClick: (com.vicherarr.memora.domain.models.ArchivoAdjunto) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Título
        if (!note.titulo.isNullOrBlank()) {
            item {
                Text(
                    text = note.titulo,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        // Fecha de creación
        item {
            Text(
                text = formatRelativeTime(note.fechaCreacion),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Contenido
        item {
            Text(
                text = note.contenido,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2
            )
        }
        
        // Attachments
        if (note.archivosAdjuntos.isNotEmpty()) {
            item {
                Text(
                    text = "Archivos adjuntos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            item {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    modifier = Modifier.heightIn(max = 400.dp) // Limit height for large collections
                ) {
                    items(note.archivosAdjuntos) { attachment ->
                        AttachmentItem(
                            attachment = attachment,
                            onMediaClick = { onMediaClick(attachment) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AttachmentItem(
    attachment: com.vicherarr.memora.domain.models.ArchivoAdjunto,
    onMediaClick: () -> Unit // Simple callback - no business logic
) {
    // Debug logging
    println("AttachmentItem: id=${attachment.id}, tipo=${attachment.tipoArchivo}, path=${attachment.filePath}")
    
    Card(
        modifier = Modifier
            .size(120.dp)
            .clickable { onMediaClick() }, // Clean - no business logic
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (attachment.tipoArchivo) {
                TipoDeArchivo.Imagen -> {
                    // Show image thumbnail - Coil handles file paths directly
                    if (!attachment.filePath.isNullOrBlank()) {
                        AsyncImage(
                            model = attachment.filePath,
                            contentDescription = "Imagen adjunta",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            onError = { error ->
                                println("AsyncImage Error: ${error.result.throwable}")
                            },
                            onSuccess = {
                                println("AsyncImage Success: Image loaded successfully")
                            }
                        )
                    } else {
                        // Placeholder when no data
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.BrokenImage,
                                contentDescription = "Sin imagen",
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                TipoDeArchivo.Video -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.8f),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Video",
                            modifier = Modifier.size(48.dp),
                            tint = androidx.compose.ui.graphics.Color.White
                        )
                        // Mostrar nombre del archivo en la parte inferior
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .background(
                                    androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.6f),
                                    RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                                )
                                .padding(4.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = attachment.nombreOriginal ?: "Video",
                                style = MaterialTheme.typography.labelSmall,
                                color = androidx.compose.ui.graphics.Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
            
            // File type indicator
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = if (attachment.tipoArchivo == TipoDeArchivo.Imagen) "IMG" else "VID",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun EditableAttachmentItem(
    attachment: com.vicherarr.memora.domain.models.ArchivoAdjunto,
    onRemove: () -> Unit,
    onMediaClick: () -> Unit // Simple callback - no business logic
) {
    Card(
        modifier = Modifier
            .size(120.dp)
            .clickable { onMediaClick() }, // Clean - no business logic
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (attachment.tipoArchivo) {
                TipoDeArchivo.Imagen -> {
                    if (!attachment.filePath.isNullOrBlank()) {
                        AsyncImage(
                            model = attachment.filePath,
                            contentDescription = "Imagen adjunta",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.BrokenImage,
                                contentDescription = "Sin imagen",
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                TipoDeArchivo.Video -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.8f),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Video",
                            modifier = Modifier.size(48.dp),
                            tint = androidx.compose.ui.graphics.Color.White
                        )
                        // Mostrar nombre del archivo en la parte inferior
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .background(
                                    androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.6f),
                                    RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                                )
                                .padding(4.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = attachment.nombreOriginal ?: "Video",
                                style = MaterialTheme.typography.labelSmall,
                                color = androidx.compose.ui.graphics.Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
            
            // Delete button - prominent and easy to tap
            Surface(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(32.dp),
                shape = androidx.compose.foundation.shape.CircleShape,
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.9f),
                shadowElevation = 2.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Eliminar archivo",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onError
                    )
                }
            }
            
            // File type indicator
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = if (attachment.tipoArchivo == TipoDeArchivo.Imagen) "IMG" else "VID",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun NewMediaThumbnail(
    mediaFile: com.vicherarr.memora.domain.models.MediaFile,
    onRemove: () -> Unit,
    onMediaClick: () -> Unit, // Simple callback - no business logic
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .size(120.dp)
            .clickable { onMediaClick() }, // Clean - no business logic
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (mediaFile.type) {
                com.vicherarr.memora.domain.models.MediaType.IMAGE -> {
                    AsyncImage(
                        model = mediaFile.data,
                        contentDescription = mediaFile.fileName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                com.vicherarr.memora.domain.models.MediaType.VIDEO -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.8f),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Video",
                            modifier = Modifier.size(48.dp),
                            tint = androidx.compose.ui.graphics.Color.White
                        )
                        // Mostrar nombre del archivo en la parte inferior
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .background(
                                    androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.6f),
                                    RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
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
            
            // Delete button - prominent and easy to tap
            Surface(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(32.dp),
                shape = androidx.compose.foundation.shape.CircleShape,
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.9f),
                shadowElevation = 2.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Eliminar archivo",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onError
                    )
                }
            }
            
            // "NEW" badge to distinguish from existing attachments
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp),
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "NUEVO",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            
            // File type indicator
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = if (mediaFile.type == com.vicherarr.memora.domain.models.MediaType.IMAGE) "IMG" else "VID",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

// Utility function to format relative time (reusing from NotesTab)
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