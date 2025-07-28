package com.vicherarr.memora.ui.screens.notes.create.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Contenido principal de la pantalla de crear nota
 */
@Composable
fun CreateNoteContent(
    title: String,
    content: String,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Campo de título
        NoteTextField(
            value = title,
            onValueChange = onTitleChange,
            label = "Título (opcional)",
            placeholder = "Título de tu nota...",
            singleLine = true
        )
        
        // Campo de contenido
        NoteTextField(
            value = content,
            onValueChange = onContentChange,
            label = "Contenido",
            placeholder = "Escribe tu nota aquí...",
            singleLine = false,
            minLines = 5,
            modifier = Modifier.weight(1f)
        )
        
        // Contador de caracteres
        CharacterCounter(
            count = content.length
        )
        
        // Botón adjuntar archivo (Fase 6)
        AttachFileButton(
            onClick = {
                println("Botón adjuntar archivo pulsado - TODO: implementar")
            }
        )
    }
}