package com.vicherarr.memora.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vicherarr.memora.presentation.utils.ErrorHandler

/**
 * Componente estándar para mostrar errores
 * Diseño consistente y profesional
 */
@Composable
fun ErrorDisplay(
    errorInfo: ErrorHandler.ErrorInfo,
    onRetry: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icono y título
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = getErrorIcon(errorInfo.category),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(24.dp)
                )
                
                Text(
                    text = errorInfo.userMessage,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            
            // Mensaje de acción si existe
            errorInfo.actionMessage?.let { actionMessage ->
                Text(
                    text = actionMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                )
            }
            
            // Botones de acción
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (errorInfo.isRetryable && onRetry != null) {
                    OutlinedButton(
                        onClick = onRetry,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reintentar")
                    }
                }
                
                onDismiss?.let { dismiss ->
                    TextButton(
                        onClick = dismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Text("Cerrar")
                    }
                }
            }
        }
    }
}

/**
 * Banner de error compacto para mostrar en la parte superior
 */
@Composable
fun ErrorBanner(
    errorInfo: ErrorHandler.ErrorInfo,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = getErrorIcon(errorInfo.category),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(20.dp)
                )
                
                Text(
                    text = errorInfo.userMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            
            TextButton(onClick = onDismiss) {
                Text(
                    text = "✕",
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

/**
 * Estado de error para pantallas completas
 */
@Composable
fun FullScreenError(
    errorInfo: ErrorHandler.ErrorInfo,
    onRetry: (() -> Unit)? = null,
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icono grande
        Icon(
            imageVector = getErrorIcon(errorInfo.category),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Mensaje principal
        Text(
            text = errorInfo.userMessage,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Mensaje de acción
        errorInfo.actionMessage?.let { actionMessage ->
            Text(
                text = actionMessage,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        // Botones de acción
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            onNavigateBack?.let { navigateBack ->
                OutlinedButton(onClick = navigateBack) {
                    Text("Volver")
                }
            }
            
            if (errorInfo.isRetryable && onRetry != null) {
                Button(onClick = onRetry) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reintentar")
                }
            }
        }
    }
}

/**
 * Snackbar personalizado para errores
 */
@Composable
fun ErrorSnackbar(
    errorInfo: ErrorHandler.ErrorInfo,
    onActionClick: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    Snackbar(
        action = if (errorInfo.isRetryable && onActionClick != null) {
            {
                TextButton(onClick = onActionClick) {
                    Text("REINTENTAR")
                }
            }
        } else null,
        dismissAction = {
            TextButton(onClick = onDismiss) {
                Text("CERRAR")
            }
        },
        containerColor = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = getErrorIcon(errorInfo.category),
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            
            Text(
                text = errorInfo.userMessage,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * Obtiene el icono apropiado según la categoría de error
 */
private fun getErrorIcon(category: ErrorHandler.ErrorCategory): ImageVector {
    return when (category) {
        is ErrorHandler.ErrorCategory.Network -> Icons.Default.Warning
        is ErrorHandler.ErrorCategory.Authentication -> Icons.Default.Warning
        is ErrorHandler.ErrorCategory.NotFound -> Icons.Default.Error
        is ErrorHandler.ErrorCategory.Permission -> Icons.Default.Warning
        else -> Icons.Default.Error
    }
}