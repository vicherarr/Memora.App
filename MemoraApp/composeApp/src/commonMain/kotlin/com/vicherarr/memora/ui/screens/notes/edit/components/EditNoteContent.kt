package com.vicherarr.memora.ui.screens.notes.edit.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vicherarr.memora.ui.screens.notes.create.components.AttachFileButton
import com.vicherarr.memora.ui.screens.notes.create.components.CharacterCounter
import com.vicherarr.memora.ui.screens.notes.create.components.NoteTextField

/**
 * Contenido principal de la pantalla de editar nota
 * Reutiliza componentes de create para mantener consistencia
 */
@Composable
fun EditNoteContent(
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
                println("Botón adjuntar archivo pulsado en edición - TODO: implementar")
            }
        )
    }
}