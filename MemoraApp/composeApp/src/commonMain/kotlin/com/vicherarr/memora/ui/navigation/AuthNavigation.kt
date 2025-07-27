package com.vicherarr.memora.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vicherarr.memora.ui.screens.auth.LoginScreen
import com.vicherarr.memora.ui.screens.auth.RegisterScreen

/**
 * Rutas de navegación para autenticación
 */
object AuthDestinations {
    const val LOGIN = "login"
    const val REGISTER = "register"
}

/**
 * Navegación específica para el flujo de autenticación
 */
@Composable
fun AuthNavigation(
    navController: NavHostController = rememberNavController(),
    onAuthSuccess: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = AuthDestinations.LOGIN
    ) {
        composable(AuthDestinations.LOGIN) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(AuthDestinations.REGISTER) {
                        // Evitar que se acumulen múltiples pantallas de login
                        popUpTo(AuthDestinations.LOGIN) { inclusive = false }
                    }
                },
                onLoginSuccess = onAuthSuccess
            )
        }
        
        composable(AuthDestinations.REGISTER) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.navigate(AuthDestinations.LOGIN) {
                        // Limpiar el stack y volver al login
                        popUpTo(AuthDestinations.LOGIN) { inclusive = true }
                    }
                },
                onRegisterSuccess = onAuthSuccess
            )
        }
    }
}

/**
 * Extensiones para simplificar la navegación
 */
fun NavHostController.navigateToLogin() {
    navigate(AuthDestinations.LOGIN) {
        popUpTo(graph.startDestinationId) { inclusive = true }
        launchSingleTop = true
    }
}

fun NavHostController.navigateToRegister() {
    navigate(AuthDestinations.REGISTER) {
        launchSingleTop = true
    }
}