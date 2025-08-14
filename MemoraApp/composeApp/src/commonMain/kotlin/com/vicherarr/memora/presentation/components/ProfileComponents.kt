package com.vicherarr.memora.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vicherarr.memora.domain.models.AppInfo
import com.vicherarr.memora.domain.models.UserProfile
import com.vicherarr.memora.domain.models.UserStatistics
import kotlinx.datetime.LocalDate

/**
 * User Profile Header Component
 * 
 * Displays user information with avatar, name, and membership details.
 * Following Single Responsibility Principle - only handles user display.
 * Following Open/Closed Principle - easy to extend with new fields.
 */
@Composable
fun UserProfileHeader(
    userProfile: UserProfile,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar placeholder (will be replaced with Google avatar)
                UserAvatar(
                    avatarUrl = userProfile.avatarUrl,
                    displayName = userProfile.displayName,
                    size = 80.dp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // User name
                Text(
                    text = userProfile.displayName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // User email
                Text(
                    text = userProfile.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Member since
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "Miembro desde ${formatMemberSince(userProfile.memberSince)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

/**
 * User Avatar Component
 * 
 * Displays user avatar with fallback to initials.
 * Following Single Responsibility Principle - only handles avatar display.
 */
@Composable
fun UserAvatar(
    avatarUrl: String?,
    displayName: String,
    size: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = 4.dp
    ) {
        if (avatarUrl != null) {
            // TODO: Implement AsyncImage for Google avatar
            // AsyncImage(model = avatarUrl, ...)
            // For now, show initials
            UserInitials(displayName = displayName, size = size)
        } else {
            UserInitials(displayName = displayName, size = size)
        }
    }
}

/**
 * User Initials Component
 * 
 * Shows user initials when no avatar is available.
 * Following Single Responsibility Principle.
 */
@Composable
private fun UserInitials(
    displayName: String,
    size: androidx.compose.ui.unit.Dp
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = getInitials(displayName),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = (size.value * 0.4f).sp
        )
    }
}

/**
 * Statistics Section Component
 * 
 * Displays user statistics in a grid layout.
 * Following Single Responsibility Principle - only handles statistics display.
 */
@Composable
fun StatisticsSection(
    statistics: UserStatistics,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section title
        Text(
            text = "📊 Estadísticas",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        
        // Statistics grid
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: Notes and Attachments
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatisticCard(
                    icon = Icons.Default.Note,
                    title = "Notas",
                    value = statistics.totalNotes.toString(),
                    subtitle = "Total creadas",
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
                
                StatisticCard(
                    icon = Icons.Default.Attachment,
                    title = "Archivos",
                    value = statistics.totalAttachments.toString(),
                    subtitle = "Adjuntos",
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            }
            
            // Row 2: Storage metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatisticCard(
                    icon = Icons.Default.Storage,
                    title = "Local",
                    value = statistics.getFormattedLocalStorage(),
                    subtitle = "Almacenado",
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
                
                StatisticCard(
                    icon = Icons.Default.Cloud,
                    title = "Google Drive",
                    value = statistics.getFormattedRemoteStorage(),
                    subtitle = "Sincronizado",
                    modifier = Modifier.weight(1f),
                    containerColor = Color(0xFF4285F4).copy(alpha = 0.1f) // Google Blue
                )
            }
            
            // Row 3: Activity
            StatisticCard(
                icon = Icons.Default.TrendingUp,
                title = "Este mes",
                value = statistics.notesThisMonth.toString(),
                subtitle = "notas creadas",
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

/**
 * Individual Statistic Card Component
 * 
 * Reusable card for displaying a single statistic.
 * Following Single Responsibility and DRY principles.
 */
@Composable
fun StatisticCard(
    icon: ImageVector,
    title: String,
    value: String,
    subtitle: String,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Settings Section Component
 * 
 * Displays app information and settings options.
 * Following Single Responsibility Principle.
 */
@Composable
fun SettingsSection(
    appInfo: AppInfo,
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onSupportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "ℹ️ Información de la App",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // App version
            SettingsItem(
                icon = Icons.Default.Info,
                title = "Versión",
                subtitle = "${appInfo.versionName} (${appInfo.versionCode}) - ${appInfo.buildType}",
                onClick = { /* No action for version */ }
            )
            
            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
            
            // Terms and Privacy
            SettingsItem(
                icon = Icons.Default.Gavel,
                title = "Términos y Condiciones",
                subtitle = "Política de uso",
                onClick = onTermsClick
            )
            
            SettingsItem(
                icon = Icons.Default.PrivacyTip,
                title = "Política de Privacidad",
                subtitle = "Protección de datos",
                onClick = onPrivacyClick
            )
            
            SettingsItem(
                icon = Icons.Default.Support,
                title = "Soporte",
                subtitle = appInfo.supportEmail ?: "Contactar equipo",
                onClick = onSupportClick
            )
        }
    }
}

/**
 * Individual Settings Item Component
 * 
 * Reusable item for settings list.
 * Following Single Responsibility and DRY principles.
 */
@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Ver más",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Account Management Section Component
 * 
 * Handles logout and account-related actions.
 * Following Single Responsibility Principle.
 */
@Composable
fun AccountManagementSection(
    onLogout: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🚪 Gestión de Cuenta",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Cerrar sesión de Google y salir de la aplicación",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onLogout,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onError
                    )
                } else {
                    Icon(
                        Icons.Default.Logout,
                        contentDescription = "Cerrar sesión",
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (isLoading) "Cerrando sesión..." else "Cerrar Sesión",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// Utility functions following Single Responsibility Principle

/**
 * Format member since date for display
 */
private fun formatMemberSince(date: LocalDate): String {
    val months = listOf(
        "enero", "febrero", "marzo", "abril", "mayo", "junio",
        "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
    )
    return "${months[date.monthNumber - 1]} ${date.year}"
}

/**
 * Get user initials from display name
 */
private fun getInitials(displayName: String): String {
    return displayName
        .split(" ")
        .take(2)
        .map { it.firstOrNull()?.uppercaseChar() ?: "" }
        .joinToString("")
        .take(2)
        .ifEmpty { "U" }
}