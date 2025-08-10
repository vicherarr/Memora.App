package com.vicherarr.memora.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vicherarr.memora.sync.AttachmentSyncState
import com.vicherarr.memora.sync.SyncState

/**
 * Componente compacto tipo favicon para mostrar estado de sincronización
 * Solo un icono pequeño que no quita espacio al contenido
 */
@Composable
fun SyncStatusIndicator(
    syncState: SyncState,
    attachmentSyncState: AttachmentSyncState,
    modifier: Modifier = Modifier
) {
    // Determinar icono y color según el estado
    val (icon, color) = when {
        syncState is SyncState.Error -> Icons.Default.Error to MaterialTheme.colorScheme.error
        syncState is SyncState.Syncing -> Icons.Default.CloudSync to MaterialTheme.colorScheme.primary
        attachmentSyncState is AttachmentSyncState.Error -> Icons.Default.Error to MaterialTheme.colorScheme.error
        attachmentSyncState is AttachmentSyncState.Syncing -> Icons.Default.CloudSync to MaterialTheme.colorScheme.primary
        else -> Icons.Default.CloudDone to MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
    }
    
    // Animación de rotación para el estado de sincronización
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    // Icono flotante compacto
    Surface(
        modifier = modifier.size(32.dp),
        shape = CircleShape,
        color = color.copy(alpha = 0.1f),
        shadowElevation = 4.dp
    ) {
        Icon(
            imageVector = icon,
            contentDescription = when {
                syncState is SyncState.Error -> "Error de sincronización"
                syncState is SyncState.Syncing -> "Sincronizando..."
                else -> "Sincronizado"
            },
            tint = color,
            modifier = Modifier
                .size(20.dp)
                .let { modifier ->
                    if (syncState is SyncState.Syncing || attachmentSyncState is AttachmentSyncState.Syncing) 
                        modifier.rotate(rotation)
                    else 
                        modifier
                }
        )
    }
}