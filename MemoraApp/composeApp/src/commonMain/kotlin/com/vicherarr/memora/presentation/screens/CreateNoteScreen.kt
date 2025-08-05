package com.vicherarr.memora.presentation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.koin.compose.getKoin
import com.vicherarr.memora.presentation.viewmodels.CreateNoteViewModel

class CreateNoteScreen : Screen {
    
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val koin = getKoin()
        val createNoteViewModel: CreateNoteViewModel = remember { koin.get() }
        
        // Single Source of Truth - Observe UI State from ViewModel
        val uiState by createNoteViewModel.uiState.collectAsState()
        
        // Simple navigation - when note is saved, navigate back
        if (uiState.isNoteSaved) {
            navigator.pop()
            return
        }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Custom TopAppBar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .padding(horizontal = 4.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                        Text(
                            text = "Nueva Nota",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
                
                // Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Titulo field - Direct method call (JetBrains KMP style)
                    OutlinedTextField(
                        value = uiState.titulo,
                        onValueChange = createNoteViewModel::updateTitulo,
                        label = { Text("Título (opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !uiState.isLoading
                    )
                    
                    // Contenido field - Direct method call (JetBrains KMP style)
                    OutlinedTextField(
                        value = uiState.contenido,
                        onValueChange = createNoteViewModel::updateContenido,
                        label = { Text("Contenido") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(top = 16.dp),
                        minLines = 5,
                        placeholder = { Text("Escribe tu nota aquí...") },
                        enabled = !uiState.isLoading
                    )
                    
                    // Multimedia Actions - Navigate to multimedia screens
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly
                    ) {
                        // Camera Button Section
                        Column(
                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                        ) {
                            IconButton(
                                onClick = { 
                                    navigator.push(CameraScreen())
                                },
                                enabled = !uiState.isLoading
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Cámara"
                                )
                            }
                            Text(
                                text = "Cámara",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        
                        // Gallery Button Section
                        Column(
                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                        ) {
                            IconButton(
                                onClick = { 
                                    navigator.push(MediaPickerScreen(allowMultiple = true, maxSelection = 5))
                                },
                                enabled = !uiState.isLoading
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = "Galería"
                                )
                            }
                            Text(
                                text = "Galería",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    
                    // Error message - Displayed based on UI State
                    uiState.errorMessage?.let { errorMessage ->
                        if (errorMessage.isNotEmpty()) {
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                    
                    // Validation hint - From ViewModel logic
                    createNoteViewModel.getValidationHint(uiState.contenido)?.let { hint ->
                        Text(
                            text = hint,
                            color = MaterialTheme.colorScheme.outline,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
            
            // FloatingActionButton - Direct method call (JetBrains KMP style)
            FloatingActionButton(
                onClick = createNoteViewModel::createNote,
                modifier = Modifier
                    .align(androidx.compose.ui.Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                if (uiState.isLoading) {
                    // Could add loading indicator here
                    Icon(Icons.Default.Check, contentDescription = "Guardando...")
                } else {
                    Icon(Icons.Default.Check, contentDescription = "Guardar nota")
                }
            }
        }
    }
}