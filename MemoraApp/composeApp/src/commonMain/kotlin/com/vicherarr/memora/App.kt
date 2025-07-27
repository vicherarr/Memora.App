package com.vicherarr.memora

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.vicherarr.memora.ui.theme.MemoraTheme
import com.vicherarr.memora.ui.navigation.AuthNavigation
import com.vicherarr.memora.ui.screens.DebugScreen
import com.vicherarr.memora.presentation.viewmodels.AuthViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun App() {
    MemoraTheme {
        val authViewModel: AuthViewModel = koinViewModel()
        var isAuthenticated by remember { mutableStateOf(false) }
        
        // Observar el estado de autenticación
        val currentUser by authViewModel.currentUser.collectAsState()
        
        LaunchedEffect(currentUser) {
            isAuthenticated = currentUser != null
        }
        
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .safeContentPadding(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (isAuthenticated) {
                // Pantalla de debug para testing (modo desarrollo)
                DebugScreen(
                    onLogout = {
                        isAuthenticated = false
                    }
                )
            } else {
                // Flujo de autenticación
                AuthNavigation(
                    onAuthSuccess = {
                        isAuthenticated = true
                    }
                )
            }
        }
    }
}

