package com.vicherarr.memora.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.vicherarr.memora.domain.models.*
import com.vicherarr.memora.domain.models.displayName
import com.vicherarr.memora.domain.utils.DateTimeUtils
import com.vicherarr.memora.presentation.components.ActiveFiltersChips
import com.vicherarr.memora.presentation.components.FiltersSection
import com.vicherarr.memora.presentation.components.ImageFullScreenViewer
import com.vicherarr.memora.presentation.components.SyncStatusIndicator
import com.vicherarr.memora.presentation.components.VideoPlayerDialog
import com.vicherarr.memora.presentation.viewmodels.NotesViewModel
import com.vicherarr.memora.presentation.viewmodels.SyncViewModel
import org.koin.compose.getKoin
import kotlin.math.abs

class NotesListScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val koin = getKoin()
        val notesViewModel: NotesViewModel = remember { koin.get() }
        val syncViewModel: SyncViewModel = remember { koin.get() }
        val uiState by notesViewModel.uiState.collectAsState()
        val syncState by syncViewModel.syncState.collectAsState()
        val attachmentSyncState by syncViewModel.attachmentSyncState.collectAsState()
        

        // This state is purely for the UI (expanding/collapsing the filter section)
        var showFilters by rememberSaveable { mutableStateOf(false) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        val gradientColors = listOf(Color(0xFF1976D2), Color(0xFF00796B))
                        Text(
                            text = "Memora",
                            modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally),
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                brush = Brush.linearGradient(colors = gradientColors)
                            )
                        )
                    },
                    actions = {
                        SyncStatusIndicator(
                            syncState = syncState,
                            attachmentSyncState = attachmentSyncState,
                            modifier = Modifier.padding(end = 8.dp),
                            iconSize = 24.dp
                        )
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        navigator.push(CreateNoteScreen())
                    },
                    modifier = Modifier.padding(bottom = 72.dp),
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Agregar nota",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    SearchBarAndFilters(
                        searchQuery = uiState.searchQuery,
                        onSearchQueryChange = notesViewModel::onSearchQueryChanged,
                        showFilters = showFilters,
                        onShowFiltersChange = { showFilters = it },
                        selectedDateFilter = uiState.dateFilter,
                        onDateFilterChanged = { dateFilter ->
                            notesViewModel.onDateFilterChanged(dateFilter)
                            if (dateFilter != DateFilter.CUSTOM_RANGE) {
                                showFilters = false
                            }
                        },
                        selectedFileType = uiState.fileTypeFilter,
                        onFileTypeChanged = { fileType ->
                            notesViewModel.onFileTypeChanged(fileType)
                            showFilters = false
                        },
                        customDateRange = uiState.customDateRange,
                        onCustomDateRangeChanged = { range ->
                            notesViewModel.onCustomDateRangeChanged(range)
                            if (range != null) {
                                showFilters = false
                            }
                        },
                        selectedCategoryFilter = uiState.categoryFilter,
                        onCategoryFilterChanged = { categoryFilter ->
                            notesViewModel.onCategoryFilterChanged(categoryFilter)
                            if (categoryFilter != CategoryFilter.SPECIFIC_CATEGORY) {
                                showFilters = false
                            }
                        },
                        selectedCategoryId = uiState.selectedCategoryId,
                        onSelectedCategoryChanged = { categoryId ->
                            notesViewModel.onSelectedCategoryChanged(categoryId)
                            if (categoryId != null) {
                                showFilters = false
                            }
                        },
                        availableCategories = uiState.availableCategories
                    )

                    ActiveFiltersChips(
                        searchQuery = uiState.searchQuery,
                        selectedDateFilter = uiState.dateFilter,
                        customDateRange = uiState.customDateRange,
                        selectedFileType = uiState.fileTypeFilter,
                        selectedCategoryFilter = uiState.categoryFilter,
                        selectedCategoryId = uiState.selectedCategoryId,
                        availableCategories = uiState.availableCategories,
                        onClearSearch = { notesViewModel.onSearchQueryChanged("") },
                        onClearDateFilter = { notesViewModel.onDateFilterChanged(DateFilter.ALL) },
                        onClearFileTypeFilter = { notesViewModel.onFileTypeChanged(FileTypeFilter.ALL) },
                        onClearCategoryFilter = { notesViewModel.onCategoryFilterChanged(CategoryFilter.ALL) },
                        onClearAll = { notesViewModel.onClearAllFilters() }
                    )

                    when {
                        uiState.isLoading -> {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 32.dp))
                        }
                        uiState.errorMessage != null -> {
                            ErrorState(message = uiState.errorMessage ?: "Error desconocido")
                        }
                        // Case 1: No notes match the current filters, but there are notes in general
                        uiState.filteredNotes.isEmpty() && uiState.allNotes.isNotEmpty() -> {
                            EmptyState(isFiltering = true)
                        }
                        // Case 2: The user has no notes at all
                        uiState.allNotes.isEmpty() -> {
                            EmptyState(isFiltering = false)
                        }
                        // Case 3: Display the filtered notes
                        else -> {
                            NotesGrid(
                                notes = uiState.filteredNotes,
                                onNoteClick = {
                                    navigator.push(NoteDetailScreen(it))
                                },
                                onMediaClick = notesViewModel::showMediaViewer
                            )
                        }
                    }
                }
            }
        }

        // Media viewers state is also driven by the ViewModel
        uiState.imageViewer.imageData?.let { imageData ->
            ImageFullScreenViewer(
                imageData = imageData,
                fileName = uiState.imageViewer.imageName,
                isVisible = uiState.imageViewer.isVisible,
                onDismiss = notesViewModel::hideImageViewer
            )
        }

        uiState.videoViewer.videoData?.let { videoData ->
            VideoPlayerDialog(
                videoData = videoData,
                fileName = uiState.videoViewer.videoName,
                isVisible = uiState.videoViewer.isVisible,
                onDismiss = notesViewModel::hideVideoViewer
            )
        }
    }
}

@Composable
private fun SearchBarAndFilters(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    showFilters: Boolean,
    onShowFiltersChange: (Boolean) -> Unit,
    selectedDateFilter: DateFilter,
    onDateFilterChanged: (DateFilter) -> Unit,
    selectedFileType: FileTypeFilter,
    onFileTypeChanged: (FileTypeFilter) -> Unit,
    customDateRange: DateRange? = null,
    onCustomDateRangeChanged: (DateRange?) -> Unit = {},
    selectedCategoryFilter: CategoryFilter,
    onCategoryFilterChanged: (CategoryFilter) -> Unit,
    selectedCategoryId: String?,
    onSelectedCategoryChanged: (String?) -> Unit,
    availableCategories: List<Category>
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text("Buscar en notas...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
            trailingIcon = {
                Row {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar búsqueda")
                        }
                    }
                    IconButton(onClick = { onShowFiltersChange(!showFilters) }) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Filtros",
                            tint = if (selectedDateFilter != DateFilter.ALL || selectedFileType != FileTypeFilter.ALL || customDateRange != null || selectedCategoryFilter != CategoryFilter.ALL)
                                MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(32.dp)
        )
        if (showFilters) {
            FiltersSection(
                selectedDateFilter = selectedDateFilter,
                onDateFilterChanged = onDateFilterChanged,
                selectedFileType = selectedFileType,
                onFileTypeChanged = onFileTypeChanged,
                customDateRange = customDateRange,
                onCustomDateRangeChanged = onCustomDateRangeChanged,
                selectedCategoryFilter = selectedCategoryFilter,
                onCategoryFilterChanged = onCategoryFilterChanged,
                selectedCategoryId = selectedCategoryId,
                onSelectedCategoryChanged = onSelectedCategoryChanged,
                availableCategories = availableCategories,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun NotesGrid(
    notes: List<Note>,
    onNoteClick: (String) -> Unit,
    onMediaClick: (ArchivoAdjunto) -> Unit = {}
) {
    val noteColors = remember {
        listOf(
            Color(0xFFFFF0F0), Color(0xFFE6F4EA), Color(0xFFE0F7FA),
            Color(0xFFF3E5F5), Color(0xFFFFFDE7), Color(0xFFF5F5F5)
        )
    }

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalItemSpacing = 12.dp
    ) { 
        items(notes, key = { it.id }) { note ->
            val colorIndex = remember(note.id) { abs(note.id.hashCode()) % noteColors.size }
            RedesignedNoteCard(
                note = note,
                onClick = { onNoteClick(note.id) },
                backgroundColor = noteColors[colorIndex],
                onMediaClick = onMediaClick
            )
        }
    }
}

@Composable
internal fun RedesignedNoteCard(
    note: Note,
    onClick: () -> Unit,
    backgroundColor: Color,
    onMediaClick: (ArchivoAdjunto) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (note.archivosAdjuntos.isNotEmpty()) {
                AttachmentsGridPreview(
                    attachments = note.archivosAdjuntos.take(4),
                    onMediaClick = onMediaClick
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (!note.titulo.isNullOrBlank()) {
                Text(
                    text = note.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = note.contenido,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = if (note.archivosAdjuntos.isEmpty()) 8 else 4,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Show categories if any
            if (note.categories.isNotEmpty()) {
                NoteCategoriesRow(categories = note.categories)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = DateTimeUtils.formatRelativeTime(note.fechaCreacion),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                if (note.archivosAdjuntos.isNotEmpty()) {
                    AttachmentSummaryIcons(attachments = note.archivosAdjuntos)
                }
            }
        }
    }
}

@Composable
private fun AttachmentsGridPreview(
    attachments: List<ArchivoAdjunto>,
    onMediaClick: (ArchivoAdjunto) -> Unit = {}
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        attachments.forEach { attachment ->
            when (attachment.tipoArchivo) {
                TipoDeArchivo.Imagen -> {
                    AsyncImage(
                        model = attachment.filePath,
                        contentDescription = "Imagen adjunta",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onMediaClick(attachment) },
                        contentScale = ContentScale.Crop
                    )
                }
                TipoDeArchivo.Video -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Color.Black.copy(alpha = 0.8f),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onMediaClick(attachment) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Video adjunto",
                            modifier = Modifier.size(48.dp),
                            tint = Color.White
                        )
                        // Mostrar nombre del archivo en la parte inferior
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .background(
                                    Color.Black.copy(alpha = 0.6f),
                                    RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                                )
                                .padding(8.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = attachment.nombreOriginal ?: "Video",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AttachmentSummaryIcons(attachments: List<ArchivoAdjunto>) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (attachments.any { it.tipoArchivo == TipoDeArchivo.Imagen }) {
            Icon(
                Icons.Default.Image,
                contentDescription = "Imágenes",
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
        if (attachments.any { it.tipoArchivo == TipoDeArchivo.Video }) {
            Icon(
                Icons.Default.Videocam,
                contentDescription = "Videos",
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun EmptyState(isFiltering: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val message = if (isFiltering) "No se encontraron notas" else "Tu lienzo en blanco"
        val subMessage = if (isFiltering) "Prueba a cambiar los filtros o el texto de búsqueda." else "Toca el botón + para que tus ideas cobren vida."

        Icon(
            imageVector = if (isFiltering) Icons.Default.SearchOff else Icons.Default.Assignment,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subMessage,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )
    }
}

@Composable
private fun ErrorState(message: String) {
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
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun NoteCategoriesRow(categories: List<Category>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(categories.take(3)) { category ->
            AssistChip(
                onClick = { /* No action needed in list view */ },
                label = { 
                    Text(
                        text = category.displayName,
                        style = MaterialTheme.typography.labelSmall
                    ) 
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = Color(parseHexColor(category.color)).copy(alpha = 0.15f),
                    labelColor = Color(parseHexColor(category.color))
                ),
                border = null,
                modifier = Modifier.height(24.dp)
            )
        }
        if (categories.size > 3) {
            item {
                AssistChip(
                    onClick = { /* No action needed */ },
                    label = { 
                        Text(
                            text = "+${categories.size - 3}",
                            style = MaterialTheme.typography.labelSmall
                        ) 
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = null,
                    modifier = Modifier.height(24.dp)
                )
            }
        }
    }
}

// Helper function to parse hex color (copied from CategoryDomainMapper)
private fun parseHexColor(hexColor: String): Int {
    return try {
        val cleanHex = hexColor.removePrefix("#")
        when (cleanHex.length) {
            6 -> cleanHex.toLong(16).toInt() or 0xFF000000.toInt() // Add alpha
            8 -> cleanHex.toLong(16).toInt() // Already has alpha
            else -> 0xFF6750A4.toInt() // Default Material Design 3 Primary
        }
    } catch (e: NumberFormatException) {
        0xFF6750A4.toInt() // Default color on parse error
    }
}
