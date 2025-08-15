package com.vicherarr.memora.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vicherarr.memora.domain.models.*
import com.vicherarr.memora.domain.repository.NotesRepository
import com.vicherarr.memora.domain.utils.DateTimeUtils
import com.vicherarr.memora.presentation.states.BaseUiState
import com.vicherarr.memora.presentation.states.ImageViewerState
import com.vicherarr.memora.presentation.states.VideoViewerState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.vicherarr.memora.domain.usecase.GetNotesUseCase

/**
 * Notes UI State - Single Source of Truth
 * Immutable data class representing the complete state of the Notes List screen
 */
data class NotesUiState(
    // Raw, unfiltered list from the repository
    val allNotes: List<Note> = emptyList(),
    // The final list displayed to the user after applying filters
    val filteredNotes: List<Note> = emptyList(),
    val selectedNote: Note? = null,
    
    // Filter states now live here
    val searchQuery: String = "",
    val dateFilter: DateFilter = DateFilter.ALL,
    val fileTypeFilter: FileTypeFilter = FileTypeFilter.ALL,
    val customDateRange: DateRange? = null,

    val imageViewer: ImageViewerState = ImageViewerState(),
    val videoViewer: VideoViewerState = VideoViewerState(),
    override val isLoading: Boolean = false,
    override val errorMessage: String? = null
) : BaseUiState

/**
 * ViewModel for notes operations following JetBrains KMP patterns
 * Single Responsibility: Handles notes list and filtering logic
 */
class NotesViewModel(
    private val notesRepository: NotesRepository, // Still needed for CUD operations
    private val getNotesUseCase: GetNotesUseCase // Injected Use Case for reading
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    // Private state flows for each filter criterion
    private val _searchQuery = MutableStateFlow("")
    private val _dateFilter = MutableStateFlow(DateFilter.ALL)
    private val _fileTypeFilter = MutableStateFlow(FileTypeFilter.ALL)
    private val _customDateRange = MutableStateFlow<DateRange?>(null)

    init {
        // The ViewModel now delegates the complex flow combination to the Use Case
        viewModelScope.launch {
            getNotesUseCase.execute(
                searchQuery = _searchQuery,
                dateFilter = _dateFilter,
                fileTypeFilter = _fileTypeFilter,
                customDateRange = _customDateRange
            )
            .onStart {
                _uiState.value = _uiState.value.copy(isLoading = true)
            }
            .catch { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "Error loading notes"
                )
            }
            .collect { (allNotes, filteredNotes) ->
                _uiState.value = _uiState.value.copy(
                    allNotes = allNotes,
                    filteredNotes = filteredNotes,
                    isLoading = false,
                    searchQuery = _searchQuery.value,
                    dateFilter = _dateFilter.value,
                    fileTypeFilter = _fileTypeFilter.value,
                    customDateRange = _customDateRange.value
                )
            }
        }
    }

    // --- Event Handlers for UI to call ---

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onDateFilterChanged(filter: DateFilter) {
        _dateFilter.value = filter
        // Custom range is mutually exclusive with other date filters
        if (filter != DateFilter.CUSTOM_RANGE) {
            _customDateRange.value = null
        }
    }

    fun onFileTypeChanged(filter: FileTypeFilter) {
        _fileTypeFilter.value = filter
    }

    fun onCustomDateRangeChanged(range: DateRange?) {
        _customDateRange.value = range
        // If a custom range is set, ensure the date filter reflects this
        if (range != null) {
            _dateFilter.value = DateFilter.CUSTOM_RANGE
        }
    }
    
    fun onClearAllFilters() {
        _searchQuery.value = ""
        _dateFilter.value = DateFilter.ALL
        _fileTypeFilter.value = FileTypeFilter.ALL
        _customDateRange.value = null
    }


    // --- Other ViewModel functions remain the same ---

    fun selectNote(noteId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            notesRepository.getNoteById(noteId)
                .onSuccess { note ->
                    _uiState.value = _uiState.value.copy(
                        selectedNote = note,
                        isLoading = false
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Error loading note"
                    )
                }
        }
    }

    fun createNote(titulo: String?, contenido: String) {
        viewModelScope.launch {
            notesRepository.createNote(titulo, contenido)
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = exception.message ?: "Error creating note"
                    )
                }
        }
    }

    fun updateNote(id: String, titulo: String?, contenido: String) {
        viewModelScope.launch {
            notesRepository.updateNote(id, titulo, contenido)
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = exception.message ?: "Error updating note"
                    )
                }
        }
    }

    fun deleteNote(id: String) {
        viewModelScope.launch {
            notesRepository.deleteNote(id)
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = exception.message ?: "Error deleting note"
                    )
                }
        }
    }

    fun clearSelectedNote() {
        _uiState.value = _uiState.value.copy(selectedNote = null)
    }

    // MARK: - Media Viewer Methods
    
    private fun showImageViewer(imageData: Any, imageName: String?) {
        _uiState.value = _uiState.value.copy(
            imageViewer = ImageViewerState(
                isVisible = true,
                imageData = imageData,
                imageName = imageName
            )
        )
    }
    
    fun hideImageViewer() {
        _uiState.value = _uiState.value.copy(
            imageViewer = ImageViewerState()
        )
    }
    
    private fun showVideoViewer(videoData: Any, videoName: String?) {
        _uiState.value = _uiState.value.copy(
            videoViewer = VideoViewerState(
                isVisible = true,
                videoData = videoData,
                videoName = videoName
            )
        )
    }
    
    fun hideVideoViewer() {
        _uiState.value = _uiState.value.copy(
            videoViewer = VideoViewerState()
        )
    }
    
    fun showMediaViewer(attachment: ArchivoAdjunto) {
        if (attachment.filePath.isNullOrBlank()) return

        when (attachment.tipoArchivo) {
            TipoDeArchivo.Imagen -> {
                showImageViewer(attachment.filePath!!, attachment.nombreOriginal ?: "Imagen")
            }
            TipoDeArchivo.Video -> {
                showVideoViewer(attachment.filePath!!, attachment.nombreOriginal ?: "Video")
            }
        }
    }
}
