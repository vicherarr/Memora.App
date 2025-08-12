package com.vicherarr.memora.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.vicherarr.memora.domain.models.ArchivoAdjunto
import com.vicherarr.memora.domain.models.Note
import com.vicherarr.memora.domain.models.TipoDeArchivo
import com.vicherarr.memora.presentation.viewmodels.NotesViewModel
import com.vicherarr.memora.presentation.viewmodels.SyncViewModel
import com.vicherarr.memora.presentation.components.SyncStatusIndicator
import com.vicherarr.memora.presentation.components.FiltersSection
import com.vicherarr.memora.domain.models.DateFilter
import com.vicherarr.memora.domain.models.FileTypeFilter
import com.vicherarr.memora.domain.utils.DateTimeUtils
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
        var searchQuery by rememberSaveable { mutableStateOf("") }
        var showFilters by rememberSaveable { mutableStateOf(false) }
        var selectedDateFilter by rememberSaveable { mutableStateOf(DateFilter.ALL) }
        var selectedFileType by rememberSaveable { mutableStateOf(FileTypeFilter.ALL) }

        val filteredNotes = remember(uiState.notes, searchQuery, selectedDateFilter, selectedFileType) {
            var notes = uiState.notes
            if (searchQuery.isNotBlank()) {
                notes = notes.filter { note ->
                    note.titulo?.contains(searchQuery, ignoreCase = true) == true ||
                            note.contenido.contains(searchQuery, ignoreCase = true)
                }
            }
            notes = when (selectedDateFilter) {
                DateFilter.TODAY -> notes.filter {
                    DateTimeUtils.isWithinTimeRange(it.fechaModificacion, DateTimeUtils.TimeRanges.ONE_DAY)
                }
                DateFilter.WEEK -> notes.filter {
                    DateTimeUtils.isWithinTimeRange(it.fechaModificacion, DateTimeUtils.TimeRanges.ONE_WEEK)
                }
                DateFilter.MONTH -> notes.filter {
                    DateTimeUtils.isWithinTimeRange(it.fechaModificacion, DateTimeUtils.TimeRanges.ONE_MONTH)
                }
                DateFilter.ALL -> notes
            }
            notes = when (selectedFileType) {
                FileTypeFilter.WITH_IMAGES -> notes.filter { note ->
                    note.archivosAdjuntos.any { it.tipoArchivo == TipoDeArchivo.Imagen }
                }
                FileTypeFilter.WITH_VIDEOS -> notes.filter { note ->
                    note.archivosAdjuntos.any { it.tipoArchivo == TipoDeArchivo.Video }
                }
                FileTypeFilter.WITH_ATTACHMENTS -> notes.filter { note ->
                    note.archivosAdjuntos.isNotEmpty()
                }
                FileTypeFilter.TEXT_ONLY -> notes.filter { note ->
                    note.archivosAdjuntos.isEmpty()
                }
                FileTypeFilter.ALL -> notes
            }
            notes
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Memora", fontWeight = FontWeight.Bold) },
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
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        showFilters = showFilters,
                        onShowFiltersChange = { showFilters = it },
                        selectedDateFilter = selectedDateFilter,
                        onDateFilterChanged = { selectedDateFilter = it },
                        selectedFileType = selectedFileType,
                        onFileTypeChanged = { selectedFileType = it }
                    )

                    when {
                        uiState.isLoading -> {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 32.dp))
                        }
                        uiState.errorMessage != null -> {
                            ErrorState(message = uiState.errorMessage ?: "Error desconocido")
                        }
                        filteredNotes.isEmpty() && uiState.notes.isNotEmpty() -> {
                            EmptyState(isFiltering = true)
                        }
                        uiState.notes.isEmpty() -> {
                            EmptyState(isFiltering = false)
                        }
                        else -> {
                            NotesGrid(
                                notes = filteredNotes,
                                onNoteClick = {
                                    navigator.push(NoteDetailScreen(it))
                                }
                            )
                        }
                    }
                }

                
            }
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
    onFileTypeChanged: (FileTypeFilter) -> Unit
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
                            if (showFilters) Icons.Default.FilterListOff else Icons.Default.FilterList,
                            contentDescription = "Filtros",
                            tint = if (selectedDateFilter != DateFilter.ALL || selectedFileType != FileTypeFilter.ALL)
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
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun NotesGrid(notes: List<Note>, onNoteClick: (String) -> Unit) {
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
                backgroundColor = noteColors[colorIndex]
            )
        }
    }
}

@Composable
internal fun RedesignedNoteCard(
    note: Note,
    onClick: () -> Unit,
    backgroundColor: Color
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
                    attachments = note.archivosAdjuntos.take(4)
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
private fun AttachmentsGridPreview(attachments: List<ArchivoAdjunto>) {
    // Each image takes full width, creating a larger preview while maintaining a square aspect ratio.
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        attachments.forEach { attachment ->
            AsyncImage(
                model = attachment.filePath,
                contentDescription = "Attachment",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f) // Maintain square aspect ratio
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
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
