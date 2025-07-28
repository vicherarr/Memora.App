package com.vicherarr.memora.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.vicherarr.memora.ui.screens.DebugScreen
import com.vicherarr.memora.ui.screens.notes.NotesListScreen
import com.vicherarr.memora.ui.screens.notes.NoteDetailScreen
import com.vicherarr.memora.ui.screens.notes.NoteEditScreen

/**
 * Navegación principal de la aplicación (post-autenticación)
 * Maneja todas las pantallas principales de la app
 */
@Composable
fun MainNavigation(
    navController: NavHostController = rememberNavController(),
    onLogout: () -> Unit
) {
    com.vicherarr.memora.ui.screens.MainScreen(
        onLogout = onLogout
    )
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