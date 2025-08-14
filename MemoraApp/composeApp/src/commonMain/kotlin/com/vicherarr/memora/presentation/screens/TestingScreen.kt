package com.vicherarr.memora.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.vicherarr.memora.presentation.viewmodels.SyncViewModel
import com.vicherarr.memora.sync.SyncState
import org.koin.compose.getKoin

/**
 * Testing Screen - Hidden debug/development features
 * 
 * Professional-grade testing interface with Material Design 3 styling.
 * Contains all testing and debugging features controlled by FeatureFlags.
 * 
 * Following MVVM pattern with clean separation of concerns.
 */
class TestingScreen : Screen {
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val koin = getKoin()
        val syncViewModel: SyncViewModel = remember { koin.get() }
        val syncState by syncViewModel.syncState.collectAsState()
        val scrollState = rememberScrollState()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Enhanced Top App Bar with gradient
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.05f)
                                )
                            )
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        Column(
                            modifier = Modifier.weight(1f).padding(start = 8.dp)
                        ) {
                            Text(
                                text = "🧪 Testing & Debug",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Funciones de desarrollo",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        // Status indicator
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = when (syncState) {
                                is SyncState.Syncing -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                is SyncState.Success -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                                is SyncState.Error -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ) {
                            Text(
                                text = when (syncState) {
                                    is SyncState.Syncing -> "⚡ Sync"
                                    is SyncState.Success -> "✅ OK"
                                    is SyncState.Error -> "❌ Error"
                                    else -> "💤 Idle"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                }
            }
            
            // Content with professional spacing
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Professional warning banner
                DevWarningBanner()
                
                // Smart Sync Testing Section
                TestingSection(
                    title = "🧠 Smart Sync Testing",
                    subtitle = "Sincronización inteligente con metadata",
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    SmartSyncContent(syncState, syncViewModel)
                }
                
                // Data Management Section
                TestingSection(
                    title = "🗃️ Data Management",
                    subtitle = "Gestión de datos y reseteo del sistema",
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                ) {
                    DataManagementContent(syncState, syncViewModel)
                }
                
                // Danger Zone
                TestingSection(
                    title = "⚠️ Danger Zone",
                    subtitle = "Operaciones destructivas - ¡Usar con precaución!",
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ) {
                    DangerZoneContent(syncState, syncViewModel)
                }
                
                // Bottom spacing
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun DevWarningBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.inverseSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.DeveloperMode,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.inverseOnSurface
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Modo Desarrollador",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.inverseOnSurface
                )
                Text(
                    text = "Estas funciones están ocultas en producción",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun TestingSection(
    title: String,
    subtitle: String,
    containerColor: Color,
    contentColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            // Section header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )
            
            content()
        }
    }
}

@Composable
private fun SmartSyncContent(
    syncState: SyncState,
    syncViewModel: SyncViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Info card
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "⚡ Optimización inteligente",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "• Skip sync si no hay cambios\n• Sync completo solo cuando es necesario",
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        
        ProfessionalButton(
            text = "🧠 Iniciar Smart Sync",
            icon = Icons.Default.AutoAwesome,
            onClick = { syncViewModel.iniciarSmartSync() },
            enabled = syncState !is SyncState.Syncing,
            isPrimary = true
        )
        
        ProfessionalButton(
            text = "📎 Test Sync + Attachments",
            icon = Icons.Default.CloudSync,
            onClick = { syncViewModel.iniciarSincronizacionManual() },
            enabled = syncState !is SyncState.Syncing,
            isPrimary = false
        )
    }
}

@Composable
private fun DataManagementContent(
    syncState: SyncState,
    syncViewModel: SyncViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfessionalButton(
                text = "☁️ Limpiar Remoto",
                icon = Icons.Default.CloudOff,
                onClick = { syncViewModel.forceDeleteRemoteData() },
                enabled = syncState !is SyncState.Syncing,
                modifier = Modifier.weight(1f),
                isDestructive = true
            )
            
            ProfessionalButton(
                text = "📱 Limpiar Local",
                icon = Icons.Default.DeleteForever,
                onClick = { syncViewModel.forceDeleteLocalData() },
                enabled = syncState !is SyncState.Syncing,
                modifier = Modifier.weight(1f),
                isDestructive = true
            )
        }
    }
}

@Composable
private fun DangerZoneContent(
    syncState: SyncState,
    syncViewModel: SyncViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Warning
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Esta operación eliminará TODOS los datos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        ProfessionalButton(
            text = "🚨 RESET COMPLETO",
            icon = Icons.Default.RestartAlt,
            onClick = { syncViewModel.forceCompleteReset() },
            enabled = syncState !is SyncState.Syncing,
            isDestructive = true,
            isDangerous = true
        )
    }
}

@Composable
private fun ProfessionalButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false,
    isDestructive: Boolean = false,
    isDangerous: Boolean = false
) {
    val colors = when {
        isDangerous -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
        )
        isDestructive -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
        isPrimary -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
        else -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
    
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = colors,
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isDangerous) 6.dp else 2.dp
        )
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isDangerous) FontWeight.Bold else FontWeight.Medium
        )
    }
}