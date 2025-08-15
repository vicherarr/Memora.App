package com.vicherarr.memora.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.vicherarr.memora.presentation.viewmodels.SplashNavigationState
import com.vicherarr.memora.presentation.viewmodels.SplashViewModel
import org.koin.compose.koinInject

/**
 * Splash Screen
 * 
 * Initial screen that verifies authentication and navigates accordingly.
 * Following MVVM pattern with Clean Architecture.
 */
class SplashScreen : Screen {
    
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel: SplashViewModel = koinInject()
        
        val navigationState by viewModel.navigationState.collectAsState()
        
        // Handle navigation based on authentication state
        LaunchedEffect(navigationState) {
            when (navigationState) {
                is SplashNavigationState.NavigateToMain -> {
                    navigator.replace(MainScreen())
                }
                is SplashNavigationState.NavigateToWelcome -> {
                    navigator.replace(WelcomeScreen())
                }
                is SplashNavigationState.Loading -> {
                    // Stay on splash screen
                }
            }
        }
        
        // Splash screen UI
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Memora",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}