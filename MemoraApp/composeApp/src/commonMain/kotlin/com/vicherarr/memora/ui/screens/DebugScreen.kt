package com.vicherarr.memora.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vicherarr.memora.data.repository.AuthRepositoryMock
import com.vicherarr.memora.domain.repository.AuthRepository
import com.vicherarr.memora.presentation.viewmodels.AuthViewModel
import com.vicherarr.memora.ui.components.MemoraButton
import com.vicherarr.memora.ui.components.MemoraCard
import org.koin.compose.viewmodel.koinViewModel
import org.koin.compose.koinInject

/**
 * Pantalla de debug para testing durante desarrollo
 * Solo visible cuando se usa AuthRepositoryMock
 */
@Composable
fun DebugScreen(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = koinViewModel(),
    authRepository: AuthRepository = koinInject()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "🧪 Modo Desarrollo - Debug",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        // Usuario actual
        MemoraCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "👤 Usuario Autenticado",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                if (currentUser != null) {
                    Text("ID: ${currentUser!!.id}")
                    Text("Username: ${currentUser!!.username}")
                    Text("Email: ${currentUser!!.email}")
                    Text("Creado: ${currentUser!!.createdAt}")
                } else {
                    Text(
                        text = "No hay usuario autenticado",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        
        // Información del repositorio
        if (authRepository is AuthRepositoryMock) {
            MemoraCard {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🔧 Repositorio Mock",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text("Usando AuthRepositoryMock para testing")
                    
                    Text(
                        text = "Usuarios de prueba disponibles:",
                        fontWeight = FontWeight.Medium
                    )
                    
                    val mockUsers = authRepository.getMockUsers()
                    mockUsers.forEach { (email, info) ->
                        Text("• $email", style = MaterialTheme.typography.bodySmall)
                        Text("  $info", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        
        // Credenciales de prueba
        MemoraCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🔑 Credenciales de Prueba",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                TestCredential(
                    title = "Usuario Test",
                    email = "test@example.com",
                    password = "123456"
                )
                
                TestCredential(
                    title = "Admin",
                    email = "admin@memora.com", 
                    password = "admin123"
                )
                
                Text(
                    text = "También puedes registrar un nuevo usuario",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Estado del token
        LaunchedEffect(Unit) {
            val isAuthenticated = authRepository.isUserAuthenticated()
            val token = authRepository.getAuthToken()
        }
        
        MemoraCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🎯 Estado de Autenticación",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                var isAuthenticated by remember { mutableStateOf(false) }
                var token by remember { mutableStateOf<String?>(null) }
                
                LaunchedEffect(currentUser) {
                    isAuthenticated = authRepository.isUserAuthenticated()
                    token = authRepository.getAuthToken()
                }
                
                Text("Autenticado: ${if (isAuthenticated) "✅ Sí" else "❌ No"}")
                Text("Token: ${token?.take(20) ?: "Sin token"}...")
            }
        }
        
        // Acciones
        MemoraCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "⚡ Acciones",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MemoraButton(
                        text = "Logout",
                        onClick = {
                            authViewModel.logout()
                            onLogout()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        
        // Notas importantes
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "ℹ️ Notas de Desarrollo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = "• Esta pantalla solo aparece en modo desarrollo\n" +
                            "• Las credenciales son simuladas (no reales)\n" +
                            "• Los datos se pierden al reiniciar la app\n" +
                            "• Próximo paso: conectar con backend real",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun TestCredential(
    title: String,
    email: String,
    password: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Email: $email",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Password: $password",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}