package com.vicherarr.memora.ui.screens.notes.edit

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vicherarr.memora.presentation.viewmodels.NoteEditViewModel
import com.vicherarr.memora.presentation.viewmodels.NoteEditUiState
import com.vicherarr.memora.presentation.viewmodels.NoteFormState
import com.vicherarr.memora.ui.components.*
import org.koin.compose.viewmodel.koinViewModel

/**
 * Pantalla para editar nota existente refactorizada
 * Aplicando principios SOLID y mejores prácticas de KMP
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteScreen(
    noteId: String,
    onNavigateBack: () -> Unit,
    onNoteSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NoteEditViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val formState by viewModel.formState.collectAsState()
    
    // Inicializar para editar nota existente
    LaunchedEffect(noteId) {
        viewModel.initializeForEdit(noteId)
    }
    
    Scaffold(
        topBar = {
            EditNoteTopBar(
                title = formState.title,
                isSaveEnabled = formState.isSaveEnabled,
                isSaving = uiState is NoteEditUiState.Saving,
                isLoading = uiState is NoteEditUiState.Loading,
                onNavigateBack = onNavigateBack,
                onSave = {
                    viewModel.saveNote(onSuccess = onNoteSaved)
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        EditNoteContent(
            uiState = uiState,
            formState = formState,
            onTitleChange = viewModel::updateTitle,
            onContentChange = viewModel::updateContent,
            onClearError = viewModel::clearError,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}

/**
 * TopBar específico para editar nota
 * Componente separado siguiendo principio SRP
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditNoteTopBar(
    title: String,
    isSaveEnabled: Boolean,
    isSaving: Boolean,
    isLoading: Boolean,
    onNavigateBack: () -> Unit,
    onSave: () -> Unit
) {
    TopAppBar(
        title = { 
            Text(
                text = if (isLoading) "Cargando..." else "Editar Nota",
                style = MaterialTheme.typography.headlineSmall
            ) 
        },
        navigationIcon = {
            IconButton(
                onClick = onNavigateBack,
                enabled = !isSaving && !isLoading
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver"
                )
            }
        },
        actions = {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
                isSaving -> {
                    Box(
                        modifier = Modifier.padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
                else -> {
                    TextButton(
                        onClick = onSave,
                        enabled = isSaveEnabled
                    ) {
                        Text(
                            text = "Guardar",
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    )
}

/**
 * Contenido principal para editar nota
 * Maneja todos los estados UI de forma profesional
 */
@Composable
private fun EditNoteContent(
    uiState: NoteEditUiState,
    formState: NoteFormState,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        when (uiState) {
            is NoteEditUiState.Loading -> {
                LoadingIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            
            is NoteEditUiState.Idle, 
            is NoteEditUiState.Saving,
            is NoteEditUiState.ValidationError,
            is NoteEditUiState.Error -> {
                EditFormContent(
                    uiState = uiState,
                    formState = formState,
                    onTitleChange = onTitleChange,
                    onContentChange = onContentChange,
                    onClearError = onClearError,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            is NoteEditUiState.Saved -> {
                // Este estado se maneja en el LaunchedEffect del callback
            }
        }
    }
}

/**
 * Formulario de edición
 * Componente reutilizable para diferentes estados
 */
@Composable
private fun EditFormContent(
    uiState: NoteEditUiState,
    formState: NoteFormState,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Campo de título con validación
        ValidatedTextField(
            value = formState.title,
            onValueChange = onTitleChange,
            label = "Título",
            validation = { NoteValidations.titleValidation(it) },
            placeholder = "Escribe un título...",
            singleLine = true,
            enabled = uiState !is NoteEditUiState.Saving,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Contador de caracteres para título
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            CharacterCounter(
                current = formState.title.length,
                max = 200
            )
        }
        
        // Campo de contenido con validación
        ValidatedTextField(
            value = formState.content,
            onValueChange = onContentChange,
            label = "Contenido",
            validation = { NoteValidations.contentValidation(it) },
            placeholder = "Escribe el contenido de tu nota...",
            singleLine = false,
            minLines = 10,
            enabled = uiState !is NoteEditUiState.Saving,
            isRequired = true,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
        
        // Contador de caracteres para contenido
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            CharacterCounter(
                current = formState.content.length,
                max = 10000
            )
        }
        
        // Mostrar información sobre cambios
        if (formState.hasChanges && uiState !is NoteEditUiState.Saving) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "✏️ Tienes cambios sin guardar",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
        
        // Mostrar error de validación si existe
        if (uiState is NoteEditUiState.ValidationError) {
            ErrorDisplay(
                errorInfo = uiState.errorInfo,
                onDismiss = onClearError,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Mostrar error general si existe
        if (uiState is NoteEditUiState.Error) {
            ErrorDisplay(
                errorInfo = uiState.errorInfo,
                onRetry = if (uiState.errorInfo.isRetryable) onClearError else null,
                onDismiss = onClearError,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}