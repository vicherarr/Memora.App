package com.vicherarr.memora.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vicherarr.memora.presentation.viewmodels.AuthViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Navegación principal de la aplicación
 * Maneja el routing de alto nivel entre autenticación y funcionalidades principales
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = koinViewModel()
) {
    // Observar el estado de autenticación
    val currentUser by authViewModel.currentUser.collectAsState()
    val isAuthenticated = currentUser != null
    
    // Determinar la ruta inicial basada en el estado de autenticación
    val startDestination = if (isAuthenticated) AppRoute.Main else AppRoute.Auth
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Flujo de autenticación
        composable<AppRoute.Auth> {
            AuthNavigation(
                onAuthSuccess = {
                    // Navegar a la aplicación principal después de autenticación exitosa
                    navController.navigate(AppRoute.Main) {
                        // Limpiar el stack de autenticación
                        popUpTo<AppRoute.Auth> { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        
        // Aplicación principal (post-autenticación)
        composable<AppRoute.Main> {
            MainNavigation(
                onLogout = {
                    // Ejecutar logout en el ViewModel
                    authViewModel.logout()
                    
                    // Navegar de vuelta a autenticación
                    navController.navigate(AppRoute.Auth) {
                        // Limpiar completamente el stack
                        popUpTo(navController.graph.startDestinationId) { 
                            inclusive = true 
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

/**
 * Extensiones para navegación de alto nivel
 */
fun NavHostController.navigateToAuth() {
    navigate(AppRoute.Auth) {
        popUpTo(graph.startDestinationId) { inclusive = true }
        launchSingleTop = true
    }
}

fun NavHostController.navigateToMain() {
    navigate(AppRoute.Main) {
        popUpTo<AppRoute.Auth> { inclusive = true }
        launchSingleTop = true
    }
}