package com.vicherarr.memora.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vicherarr.memora.ui.screens.auth.LoginScreen
import com.vicherarr.memora.ui.screens.auth.RegisterScreen

/**
 * Navegación específica para el flujo de autenticación
 * Usa rutas type-safe con @Serializable para mayor seguridad
 */
@Composable
fun AuthNavigation(
    navController: NavHostController = rememberNavController(),
    onAuthSuccess: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = AuthRoute.Login
    ) {
        composable<AuthRoute.Login> {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(AuthRoute.Register) {
                        // Evitar que se acumulen múltiples pantallas de login
                        popUpTo<AuthRoute.Login> { inclusive = false }
                    }
                },
                onLoginSuccess = onAuthSuccess
            )
        }
        
        composable<AuthRoute.Register> {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.navigate(AuthRoute.Login) {
                        // Limpiar el stack y volver al login
                        popUpTo<AuthRoute.Login> { inclusive = true }
                    }
                },
                onRegisterSuccess = onAuthSuccess
            )
        }
    }
}

/**
 * Extensiones type-safe para simplificar la navegación de autenticación
 */
fun NavHostController.navigateToLogin() {
    navigate(AuthRoute.Login) {
        popUpTo(graph.startDestinationId) { inclusive = true }
        launchSingleTop = true
    }
}

fun NavHostController.navigateToRegister() {
    navigate(AuthRoute.Register) {
        launchSingleTop = true
    }
}