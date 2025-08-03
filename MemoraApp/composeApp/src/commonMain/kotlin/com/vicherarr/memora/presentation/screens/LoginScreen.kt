package com.vicherarr.memora.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.koin.compose.getKoin
import com.vicherarr.memora.ui.components.MemoraButton
import com.vicherarr.memora.ui.components.MemoraTextField
import com.vicherarr.memora.ui.components.MemoraPasswordField
import com.vicherarr.memora.presentation.viewmodels.LoginViewModel

class LoginScreen : Screen {
    
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val koin = getKoin()
        val loginViewModel: LoginViewModel = remember { koin.get() }
        
        // Single Source of Truth - Observe UI State from ViewModel
        val uiState by loginViewModel.uiState.collectAsState()
        
        // Simple navigation - when logged in, navigate to MainScreen
        if (uiState.isLoggedIn) {
            navigator.replace(MainScreen())
            return
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Iniciar Sesión",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Email field - Direct method call (JetBrains KMP style)
            MemoraTextField(
                value = uiState.email,
                onValueChange = loginViewModel::updateEmail,
                label = "Correo electrónico",
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Password field - Direct method call (JetBrains KMP style)
            MemoraPasswordField(
                value = uiState.password,
                onValueChange = loginViewModel::updatePassword,
                label = "Contraseña",
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Error message - Displayed based on UI State
            uiState.errorMessage?.let { errorMessage ->
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            
            // Login button - Direct method call (JetBrains KMP style)
            MemoraButton(
                text = if (uiState.isLoading) "Cargando..." else "Iniciar Sesión",
                onClick = loginViewModel::login,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            MemoraButton(
                text = "Volver",
                onClick = {
                    navigator.pop()
                }
            )
        }
    }
}