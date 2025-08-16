package com.vicherarr.memora.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vicherarr.memora.domain.models.Note
import com.vicherarr.memora.domain.models.ArchivoAdjunto
import com.vicherarr.memora.domain.models.MediaFile
import com.vicherarr.memora.domain.usecases.SearchNotesUseCase
import com.vicherarr.memora.domain.usecases.UpdateNoteUseCase
import com.vicherarr.memora.domain.usecases.DeleteNoteUseCase
import com.vicherarr.memora.domain.usecases.GetCategoriesByUserUseCase
import com.vicherarr.memora.domain.usecases.CreateCategoryUseCase
import com.vicherarr.memora.domain.usecases.GetCategoriesByNoteIdUseCase
import com.vicherarr.memora.data.auth.CloudAuthProvider
import com.vicherarr.memora.domain.model.AuthState
import com.vicherarr.memora.domain.model.User
import com.vicherarr.memora.domain.models.Category
import com.vicherarr.memora.presentation.states.BaseUiState
import com.vicherarr.memora.presentation.states.ImageViewerState
import com.vicherarr.memora.presentation.states.VideoViewerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Note Detail UI State - Single Source of Truth
 * Immutable data class representing the complete state of the Note Detail screen
 */
data class NoteDetailUiState(
    val note: Note? = null,
    val isEditMode: Boolean = false,
    val editTitulo: String = "",
    val editContenido: String = "",
    val editAttachments: List<ArchivoAdjunto> = emptyList(),
    val selectedCategories: List<String> = emptyList(),
    val noteCategories: List<com.vicherarr.memora.domain.models.Category> = emptyList(), // ✅ Categorías completas de esta nota
    val availableCategories: List<com.vicherarr.memora.domain.models.Category> = emptyList(),
    val isShowingCategoryDropdown: Boolean = false,
    val isCreatingCategory: Boolean = false,
    val newCategoryName: String = "",
    val imageViewer: ImageViewerState = ImageViewerState(),
    val videoViewer: VideoViewerState = VideoViewerState(),
    val isNoteDeleted: Boolean = false,
    val isNoteSaved: Boolean = false,
    override val isLoading: Boolean = false,
    override val errorMessage: String? = null
) : BaseUiState

/**
 * ViewModel for Note Detail screen following JetBrains KMP patterns
 * Single Responsibility: Only handles note detail operations (view, edit, delete)
 */
class NoteDetailViewModel(
    private val searchNotesUseCase: SearchNotesUseCase,
    private val updateNoteUseCase: UpdateNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val getCategoriesByUserUseCase: GetCategoriesByUserUseCase,
    private val createCategoryUseCase: CreateCategoryUseCase,
    private val getCategoriesByNoteIdUseCase: GetCategoriesByNoteIdUseCase,
    private val cloudAuthProvider: CloudAuthProvider,
    private val mediaViewModel: MediaViewModel
) : BaseViewModel<NoteDetailUiState>() {
    
    private val _uiState = MutableStateFlow(NoteDetailUiState())
    override val uiState: StateFlow<NoteDetailUiState> = _uiState.asStateFlow()
    
    override fun updateState(update: NoteDetailUiState.() -> NoteDetailUiState) {
        _uiState.value = _uiState.value.update()
    }
    
    /**
     * Load specific note by ID - Direct method call
     */
    fun loadNote(noteId: String) {
        viewModelScope.launch {
            setLoading(true)
            
            searchNotesUseCase.executeGetById(noteId)
                .onSuccess { note ->
                    updateState {
                        copy(
                            note = note,
                            editTitulo = note.titulo ?: "",
                            editContenido = note.contenido,
                            editAttachments = note.archivosAdjuntos,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                    
                    // Load categories for this note
                    loadNoteCategoriesForViewing(noteId)
                    // Also load available categories for editing
                    loadCategories()
                }
                .onFailure { exception ->
                    setError(exception.message ?: "Error al cargar la nota")
                }
        }
    }
    
    /**
     * Enter edit mode - Direct method call
     */
    fun enterEditMode() {
        val currentNote = _uiState.value.note ?: return
        
        _uiState.value = _uiState.value.copy(
            isEditMode = true,
            editTitulo = currentNote.titulo ?: "",
            editContenido = currentNote.contenido,
            editAttachments = currentNote.archivosAdjuntos,
            errorMessage = null
        )
        
        // Load categories assigned to this note
        loadNoteCategoriesForEditing(currentNote.id)
        
        // Load available categories
        loadCategories()
    }
    
    /**
     * Exit edit mode without saving - Direct method call
     */
    fun exitEditMode() {
        val currentNote = _uiState.value.note ?: return
        mediaViewModel.clearSelectedMedia() // Clear any unsaved selected media
        _uiState.value = _uiState.value.copy(
            isEditMode = false,
            editTitulo = currentNote.titulo ?: "",
            editContenido = currentNote.contenido,
            editAttachments = currentNote.archivosAdjuntos, // Restore original attachments
            errorMessage = null
        )
    }
    
    /**
     * Update titulo in edit mode - Direct method call
     */
    fun updateEditTitulo(titulo: String) {
        if (_uiState.value.isEditMode) {
            _uiState.value = _uiState.value.copy(editTitulo = titulo)
        }
    }
    
    /**
     * Update contenido in edit mode - Direct method call
     */
    fun updateEditContenido(contenido: String) {
        if (_uiState.value.isEditMode) {
            _uiState.value = _uiState.value.copy(editContenido = contenido)
        }
    }
    
    /**
     * Remove specific attachment from edit list
     */
    fun removeAttachment(attachmentId: String) {
        if (_uiState.value.isEditMode) {
            val currentAttachments = _uiState.value.editAttachments.toMutableList()
            currentAttachments.removeAll { it.id == attachmentId }
            _uiState.value = _uiState.value.copy(editAttachments = currentAttachments)
        }
    }
    
    /**
     * Add new media files from MediaViewModel - simplified approach
     * Media files are handled directly by MediaViewModel, no need to duplicate them
     */
    fun addMediaToNote() {
        // This function is no longer needed since we're showing selectedMedia directly in the UI
        // The media files will be retrieved from MediaViewModel when saving
    }
    
    /**
     * Save note changes - Direct method call
     */
    fun saveNote() {
        val currentState = _uiState.value
        val currentNote = currentState.note ?: return
        
        viewModelScope.launch {
            setLoading(true)
            
            // Note: Validation is now handled by the Use Case
            val newTitulo = if (currentState.editTitulo.isBlank()) null else currentState.editTitulo.trim()
            val newContenido = currentState.editContenido.trim()
            
            val result = updateNoteUseCase.executeWithAttachments(
                noteId = currentNote.id,
                titulo = newTitulo,
                contenido = newContenido,
                existingAttachments = currentState.editAttachments,
                newMediaFiles = mediaViewModel.uiState.value.selectedMedia,
                categoryIds = currentState.selectedCategories
            )
            
            result.onSuccess {
                loadNote(currentNote.id)
                mediaViewModel.clearSelectedMedia() // Clear selected media after saving
                updateState {
                    copy(
                        isEditMode = false,
                        isNoteSaved = true,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }.onFailure { exception ->
                setError(exception.message ?: "Error al guardar la nota")
            }
        }
    }
    
    /**
     * Delete current note - Direct method call
     */
    fun deleteNote() {
        val currentNote = _uiState.value.note ?: return
        
        viewModelScope.launch {
            setLoading(true)
            
            deleteNoteUseCase.execute(currentNote.id)
                .onSuccess {
                    updateState {
                        copy(
                            isLoading = false,
                            isNoteDeleted = true,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { exception ->
                    setError(exception.message ?: "Error al eliminar la nota")
                }
        }
    }
    
    /**
     * Clear success messages after handling - Direct method call
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            isNoteSaved = false
        )
    }
    
    /**
     * Show image viewer with specified image data
     * Following MVVM pattern - state management in ViewModel
     */
    fun showImageViewer(imageData: Any, imageName: String?) {
        _uiState.value = _uiState.value.copy(
            imageViewer = ImageViewerState(
                isVisible = true,
                imageData = imageData,
                imageName = imageName
            )
        )
    }
    
    /**
     * Hide image viewer
     * Following MVVM pattern - state management in ViewModel
     */
    fun hideImageViewer() {
        _uiState.value = _uiState.value.copy(
            imageViewer = ImageViewerState()
        )
    }
    
    /**
     * Show video viewer with specified video data
     * Following MVVM pattern - state management in ViewModel
     */
    fun showVideoViewer(videoData: Any, videoName: String?) {
        _uiState.value = _uiState.value.copy(
            videoViewer = VideoViewerState(
                isVisible = true,
                videoData = videoData,
                videoName = videoName
            )
        )
    }
    
    /**
     * Hide video viewer
     * Following MVVM pattern - state management in ViewModel
     */
    fun hideVideoViewer() {
        _uiState.value = _uiState.value.copy(
            videoViewer = VideoViewerState()
        )
    }
    
    /**
     * Universal media viewer - handles business logic for showing appropriate viewer
     * Following Clean Code principles - View doesn't know about media types
     */
    fun showMediaViewer(attachment: ArchivoAdjunto) {
        if (attachment.filePath.isNullOrBlank()) return
        
        when (attachment.tipoArchivo) {
            com.vicherarr.memora.domain.models.TipoDeArchivo.Imagen -> {
                showImageViewer(attachment.filePath!!, attachment.nombreOriginal ?: "Imagen")
            }
            com.vicherarr.memora.domain.models.TipoDeArchivo.Video -> {
                showVideoViewer(attachment.filePath!!, attachment.nombreOriginal ?: "Video")
            }
        }
    }
    
    /**
     * Universal media viewer for new media files (MediaFile type)
     * Following Clean Code principles - View doesn't know about media types
     */
    fun showMediaViewer(mediaFile: MediaFile) {
        when (mediaFile.type) {
            com.vicherarr.memora.domain.models.MediaType.IMAGE -> {
                showImageViewer(mediaFile.data, mediaFile.fileName)
            }
            com.vicherarr.memora.domain.models.MediaType.VIDEO -> {
                showVideoViewer(mediaFile.data, mediaFile.fileName)
            }
        }
    }
    
    /**
     * Check if note can be saved in current edit state
     */
    fun canSaveNote(): Boolean {
        val currentState = _uiState.value
        return currentState.isEditMode && 
               currentState.editContenido.isNotBlank() && 
               !currentState.isLoading
    }
    
    /**
     * Get validation hint for UI display
     */
    fun getValidationHint(contenido: String): String? {
        return if (contenido.isBlank()) {
            "El contenido es requerido"
        } else {
            null
        }
    }
    
    /**
     * Validate note input data
     * Single Responsibility: Only validates note editing data
     */
    private fun validateNoteInput(titulo: String, contenido: String): String? {
        return when {
            contenido.isBlank() -> "El contenido de la nota es requerido"
            contenido.length > 10000 -> "El contenido es demasiado largo (máximo 10,000 caracteres)"
            titulo.length > 200 -> "El título es demasiado largo (máximo 200 caracteres)"
            else -> null
        }
    }
    
    // === Categories Management - Following Single Responsibility Principle ===
    
    /**
     * Load available categories for user - Following Clean Architecture
     */
    private fun loadCategories() {
        getCurrentUserId()?.let { userId ->
            viewModelScope.launch {
                getCategoriesByUserUseCase.execute(userId).collect { categories ->
                    updateState { copy(availableCategories = categories) }
                }
            }
        }
    }
    
    /**
     * Load categories assigned to specific note for viewing - Following Clean Architecture
     */
    private fun loadNoteCategoriesForViewing(noteId: String) {
        viewModelScope.launch {
            getCategoriesByNoteIdUseCase.execute(noteId).collect { categories ->
                val categoryIds = categories.map { it.id }
                updateState { 
                    copy(
                        selectedCategories = categoryIds,
                        noteCategories = categories // ✅ Guardar categorías completas para mostrar
                    ) 
                }
            }
        }
    }
    
    /**
     * Load categories assigned to specific note for editing - Following Clean Architecture
     */
    private fun loadNoteCategoriesForEditing(noteId: String) {
        viewModelScope.launch {
            val categories = getCategoriesByNoteIdUseCase.executeOnce(noteId)
            val categoryIds = categories.map { it.id }
            updateState { 
                copy(
                    selectedCategories = categoryIds,
                    noteCategories = categories // ✅ Guardar categorías completas para mostrar
                ) 
            }
        }
    }
    
    /**
     * Get current user ID - Following Clean Architecture
     */
    private fun getCurrentUserId(): String? {
        return when (val authState = cloudAuthProvider.authState.value) {
            is AuthState.Authenticated -> authState.user.email // ✅ FIX: Usar email para consistencia con NotesRepository
            else -> null
        }
    }
    
    /**
     * Toggle category selection - Following Single Responsibility Principle
     */
    fun toggleCategory(categoryId: String) {
        val currentSelected = _uiState.value.selectedCategories
        val newSelected = if (categoryId in currentSelected) {
            currentSelected - categoryId
        } else {
            currentSelected + categoryId
        }
        updateState { copy(selectedCategories = newSelected, isShowingCategoryDropdown = false) }
    }
    
    /**
     * Show category dropdown - Following Single Responsibility Principle
     */
    fun showCategoryDropdown() {
        updateState { copy(isShowingCategoryDropdown = true) }
    }
    
    /**
     * Hide category dropdown - Following Single Responsibility Principle
     */
    fun hideCategoryDropdown() {
        updateState { 
            copy(
                isShowingCategoryDropdown = false,
                isCreatingCategory = false,
                newCategoryName = ""
            ) 
        }
    }
    
    /**
     * Show create category field - Following Single Responsibility Principle
     */
    fun showCreateCategory() {
        updateState { copy(isCreatingCategory = true, newCategoryName = "") }
    }
    
    /**
     * Hide create category field - Following Single Responsibility Principle
     */
    fun hideCreateCategory() {
        updateState { copy(isCreatingCategory = false, newCategoryName = "") }
    }
    
    /**
     * Update new category name - Following Single Responsibility Principle
     */
    fun updateNewCategoryName(name: String) {
        updateState { copy(newCategoryName = name) }
    }
    
    /**
     * Create new category - Following Clean Architecture
     */
    fun createNewCategory() {
        val name = _uiState.value.newCategoryName.trim()
        if (name.isEmpty()) return
        
        getCurrentUserId()?.let { userId ->
            viewModelScope.launch {
                createCategoryUseCase.execute(name, userId).fold(
                    onSuccess = { category ->
                        val newSelected = _uiState.value.selectedCategories + category.id
                        updateState { 
                            copy(
                                selectedCategories = newSelected,
                                isCreatingCategory = false,
                                newCategoryName = "",
                                isShowingCategoryDropdown = false
                            ) 
                        }
                    },
                    onFailure = { error ->
                        setError(error.message ?: "Error creating category")
                        updateState { copy(isCreatingCategory = false) }
                    }
                )
            }
        }
    }
}