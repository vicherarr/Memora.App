package com.vicherarr.memora.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vicherarr.memora.ui.screens.DebugScreen

/**
 * Navegación principal de la aplicación (post-autenticación)
 * Maneja todas las pantallas principales de la app
 */
@Composable
fun MainNavigation(
    navController: NavHostController = rememberNavController(),
    onLogout: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = MainRoute.Notes
    ) {
        // Pantalla principal de notas (temporal: DebugScreen)
        composable<MainRoute.Notes> {
            DebugScreen(
                onLogout = onLogout
            )
        }
        
        // TODO: Implementar en Fase 5 - Gestión de Notas
        /*
        composable<MainRoute.NoteDetail> { backStackEntry ->
            val noteDetail = backStackEntry.toRoute<MainRoute.NoteDetail>()
            NoteDetailScreen(
                noteId = noteDetail.noteId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { noteId ->
                    navController.navigate(MainRoute.NoteEdit(noteId))
                }
            )
        }
        
        composable<MainRoute.NoteEdit> { backStackEntry ->
            val noteEdit = backStackEntry.toRoute<MainRoute.NoteEdit>()
            NoteEditScreen(
                noteId = noteEdit.noteId, // null para nueva nota
                onNavigateBack = { navController.popBackStack() },
                onSaveComplete = { noteId ->
                    navController.navigate(MainRoute.NoteDetail(noteId)) {
                        popUpTo<MainRoute.Notes>
                    }
                }
            )
        }
        
        composable<MainRoute.Search> {
            SearchNotesScreen(
                onNavigateBack = { navController.popBackStack() },
                onNoteSelected = { noteId ->
                    navController.navigate(MainRoute.NoteDetail(noteId))
                }
            )
        }
        
        composable<MainRoute.Profile> {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSettings = {
                    navController.navigate(MainRoute.Settings)
                },
                onLogout = onLogout
            )
        }
        
        composable<MainRoute.Settings> {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        */
    }
}

/**
 * Extensiones type-safe para navegación principal
 */
fun NavHostController.navigateToNoteDetail(noteId: String) {
    navigate(MainRoute.NoteDetail(noteId))
}

fun NavHostController.navigateToNoteEdit(noteId: String? = null) {
    navigate(MainRoute.NoteEdit(noteId))
}

fun NavHostController.navigateToSearch() {
    navigate(MainRoute.Search)
}

fun NavHostController.navigateToProfile() {
    navigate(MainRoute.Profile)
}

fun NavHostController.navigateToSettings() {
    navigate(MainRoute.Settings)
}

fun NavHostController.navigateToNotes() {
    navigate(MainRoute.Notes) {
        popUpTo<MainRoute.Notes> { inclusive = true }
        launchSingleTop = true
    }
}