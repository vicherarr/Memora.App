package com.vicherarr.memora.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vicherarr.memora.presentation.viewmodels.AuthViewModel
import com.vicherarr.memora.presentation.utils.AsyncUiState
import com.vicherarr.memora.ui.components.MemoraButton
import com.vicherarr.memora.ui.components.MemoraTextField
import com.vicherarr.memora.ui.components.LoadingIndicator
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = koinViewModel()
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    // Error states
    var usernameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    val registerState by viewModel.registerState.collectAsState()

    // Handle register success
    LaunchedEffect(registerState) {
        if (registerState is AsyncUiState.Success) {
            onRegisterSuccess()
        }
    }

    // Validation functions
    fun validateUsername(): Boolean {
        usernameError = when {
            username.isBlank() -> "El nombre de usuario es requerido"
            username.length < 3 -> "El nombre debe tener al menos 3 caracteres"
            else -> null
        }
        return usernameError == null
    }

    fun validateEmail(): Boolean {
        emailError = when {
            email.isBlank() -> "El email es requerido"
            !email.contains("@") -> "Email inválido"
            else -> null
        }
        return emailError == null
    }

    fun validatePassword(): Boolean {
        passwordError = when {
            password.isBlank() -> "La contraseña es requerida"
            password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            else -> null
        }
        return passwordError == null
    }

    fun validateConfirmPassword(): Boolean {
        confirmPasswordError = when {
            confirmPassword.isBlank() -> "Confirma tu contraseña"
            confirmPassword != password -> "Las contraseñas no coinciden"
            else -> null
        }
        return confirmPasswordError == null
    }

    fun handleRegister() {
        val isUsernameValid = validateUsername()
        val isEmailValid = validateEmail()
        val isPasswordValid = validatePassword()
        val isConfirmPasswordValid = validateConfirmPassword()
        
        if (isUsernameValid && isEmailValid && isPasswordValid && isConfirmPasswordValid) {
            viewModel.register(username, email, password)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo/Title Section
        Text(
            text = "Memora",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "Crea tu cuenta",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Register Form
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Username Field
                MemoraTextField(
                    value = username,
                    onValueChange = { 
                        username = it
                        if (usernameError != null) usernameError = null
                    },
                    label = "Nombre de usuario",
                    isError = usernameError != null,
                    errorMessage = usernameError,
                    modifier = Modifier.fillMaxWidth()
                )

                // Email Field
                MemoraTextField(
                    value = email,
                    onValueChange = { 
                        email = it
                        if (emailError != null) emailError = null
                    },
                    label = "Email",
                    isError = emailError != null,
                    errorMessage = emailError,
                    modifier = Modifier.fillMaxWidth()
                )

                // Password Field
                MemoraTextField(
                    value = password,
                    onValueChange = { 
                        password = it
                        if (passwordError != null) passwordError = null
                        if (confirmPasswordError != null) confirmPasswordError = null
                    },
                    label = "Contraseña",
                    isError = passwordError != null,
                    errorMessage = passwordError,
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) {
                                    Icons.Default.Lock
                                } else {
                                    Icons.Default.Email
                                },
                                contentDescription = if (passwordVisible) {
                                    "Ocultar contraseña"
                                } else {
                                    "Mostrar contraseña"
                                }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Confirm Password Field
                MemoraTextField(
                    value = confirmPassword,
                    onValueChange = { 
                        confirmPassword = it
                        if (confirmPasswordError != null) confirmPasswordError = null
                    },
                    label = "Confirmar contraseña",
                    isError = confirmPasswordError != null,
                    errorMessage = confirmPasswordError,
                    visualTransformation = if (confirmPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) {
                                    Icons.Default.Lock
                                } else {
                                    Icons.Default.Email
                                },
                                contentDescription = if (confirmPasswordVisible) {
                                    "Ocultar contraseña"
                                } else {
                                    "Mostrar contraseña"
                                }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Error Message
                when (val state = registerState) {
                    is AsyncUiState.Error -> {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    else -> {}
                }

                // Register Button
                MemoraButton(
                    text = "Registrarse",
                    onClick = { handleRegister() },
                    enabled = registerState !is AsyncUiState.Loading,
                    modifier = Modifier.fillMaxWidth()
                )

                // Loading Indicator
                if (registerState is AsyncUiState.Loading) {
                    LoadingIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Login Link
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "¿Ya tienes una cuenta? ",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onNavigateToLogin) {
                Text(
                    text = "Inicia sesión",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}