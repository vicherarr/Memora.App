package com.vicherarr.memora.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.vicherarr.memora.config.FeatureFlags
import com.vicherarr.memora.presentation.components.*
import com.vicherarr.memora.presentation.viewmodels.ProfileViewModel
import com.vicherarr.memora.presentation.viewmodels.SyncViewModel
import com.vicherarr.memora.sync.SyncState
import org.koin.compose.getKoin

/**
 * Profile Screen - Redesigned with MVVM and Clean Architecture
 * 
 * Features:
 * - User profile with Google Auth integration
 * - Advanced statistics (notes, files, storage local/remote)
 * - App information and settings
 * - Account management with logout
 * - Developer testing section (conditional)
 * 
 * Following MVVM pattern with reactive state management.
 */
class ProfileScreen : Screen {
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val uriHandler = LocalUriHandler.current
        val koin = getKoin()
        
        // ViewModels following Dependency Injection
        val profileViewModel: ProfileViewModel = remember { koin.get() }
        val syncViewModel: SyncViewModel = remember { koin.get() }
        
        // State observation following MVVM pattern
        val profileUiState by profileViewModel.uiState.collectAsState()
        val syncState by syncViewModel.syncState.collectAsState()
        
        val scrollState = rememberScrollState()
        
        // Error handling with SnackBar
        val snackbarHostState = remember { SnackbarHostState() }
        
        // Handle errors from ProfileViewModel
        LaunchedEffect(profileUiState.errorMessage) {
            profileUiState.errorMessage?.let { error ->
                snackbarHostState.showSnackbar(
                    message = error,
                    actionLabel = "Reintentar",
                    duration = SnackbarDuration.Long
                )
                profileViewModel.clearError()
            }
        }
        
        // Logout handled in ViewModel - App will exit automatically
        
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .verticalScroll(scrollState)
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                    
                    // Loading indicator
                    if (profileUiState.isLoading) {
                        ProfileLoadingState()
                        return@Column
                    }
                    
                    // User Profile Header with integrated logout
                    profileUiState.userProfile?.let { userProfile ->
                        UserProfileHeader(
                            userProfile = userProfile,
                            onLogout = { profileViewModel.logout() },
                            isLoading = profileUiState.isLoading,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    // User Statistics
                    profileUiState.userStatistics?.let { statistics ->
                        StatisticsSection(
                            statistics = statistics,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    // Smart Sync Button
                    Button(
                        onClick = { syncViewModel.iniciarSmartSync() },
                        enabled = syncState !is SyncState.Syncing,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (syncState is SyncState.Syncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sincronizando...")
                        } else {
                            Icon(
                                Icons.Default.CloudSync,
                                contentDescription = "Sincronizar",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sincronizar")
                        }
                    }
                    
                    // App Settings and Information
                    SettingsSection(
                        appInfo = profileUiState.appInfo,
                        onTermsClick = { 
                            profileUiState.appInfo.termsUrl?.let { 
                                uriHandler.openUri(it) 
                            }
                        },
                        onPrivacyClick = { 
                            profileUiState.appInfo.privacyUrl?.let { 
                                uriHandler.openUri(it) 
                            }
                        },
                        onSupportClick = { 
                            profileUiState.appInfo.supportEmail?.let { 
                                uriHandler.openUri("mailto:$it") 
                            }
                        }
                    )
                    
                    // Developer Testing Section (conditional)
                    if (FeatureFlags.isTestingEnabled) {
                        DeveloperTestingSection(
                            onTestingClick = { navigator.push(TestingScreen()) }
                        )
                    }
                    
                // Bottom spacing for navigation
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

/**
 * Profile Loading State Component
 * 
 * Shows skeleton loading indicators while data is being fetched.
 * Following Single Responsibility Principle.
 */
@Composable
private fun ProfileLoadingState() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header skeleton
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) { }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(24.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) { }
            }
        }
        
        // Statistics skeleton
        repeat(2) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) { }
        }
    }
}

/**
 * Cloud Sync Section Component
 * 
 * Maintains existing sync functionality in modular component.
 * Following Single Responsibility Principle.
 */
@Composable
private fun CloudSyncSection(
    syncState: SyncState,
    onSyncClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🌥️ Sincronización Cloud",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Sync status indicator
            when (syncState) {
                is SyncState.Idle -> {
                    Text(
                        text = "Listo para sincronizar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                is SyncState.Syncing -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sincronizando...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                is SyncState.Success -> {
                    Text(
                        text = "✅ ${syncState.message}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF4CAF50)
                    )
                }
                is SyncState.Error -> {
                    Text(
                        text = "❌ ${syncState.error}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Manual sync button
            Button(
                onClick = onSyncClick,
                enabled = syncState !is SyncState.Syncing,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    Icons.Default.CloudSync,
                    contentDescription = "Sincronizar",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sincronizar")
            }
        }
    }
}

/**
 * Developer Testing Section Component
 * 
 * Conditional testing section for development builds.
 * Following Single Responsibility and Open/Closed principles.
 */
@Composable
private fun DeveloperTestingSection(
    onTestingClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🧪 Desarrollo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Acceso a funciones de testing y debug",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onTestingClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    Icons.Default.Science,
                    contentDescription = "Testing",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Testing")
            }
        }
    }
}