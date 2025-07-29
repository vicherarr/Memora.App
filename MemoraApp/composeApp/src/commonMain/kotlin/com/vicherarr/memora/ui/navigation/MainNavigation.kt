package com.vicherarr.memora.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.vicherarr.memora.ui.screens.MainScreen
import com.vicherarr.memora.ui.screens.notes.NoteDetailScreen
import com.vicherarr.memora.ui.screens.notes.edit.EditNoteScreen
import com.vicherarr.memora.ui.screens.notes.create.CreateNoteScreen

/**
 * Navegación principal de la aplicación (post-autenticación)
 * Maneja todas las pantallas principales con navegación type-safe
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
        // Pantalla principal con lista de notas
        composable<MainRoute.Notes> {
            MainScreen(
                onLogout = onLogout,
                onNavigateToNoteDetail = { noteId ->
                    navController.navigate(MainRoute.NoteDetail(noteId))
                },
                onNavigateToNoteEdit = { noteId ->
                    navController.navigate(MainRoute.NoteEdit(noteId))
                },
                onNavigateToNoteCreate = {
                    navController.navigate(MainRoute.NoteCreate)
                }
            )
        }
        
        // Detalle de nota
        composable<MainRoute.NoteDetail> { backStackEntry ->
            val route: MainRoute.NoteDetail = backStackEntry.toRoute()
            NoteDetailScreen(
                noteId = route.noteId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { 
                    navController.navigate(MainRoute.NoteEdit(route.noteId))
                }
            )
        }
        
        // Editar nota existente - siempre tiene ID
        composable<MainRoute.NoteEdit> { backStackEntry ->
            val route: MainRoute.NoteEdit = backStackEntry.toRoute()
            EditNoteScreen(
                noteId = route.noteId, // Siempre non-null
                onNavigateBack = { navController.popBackStack() },
                onNoteSaved = { navController.popBackStack() }
            )
        }
        
        // Crear nueva nota - responsabilidad única
        composable<MainRoute.NoteCreate> {
            CreateNoteScreen(
                onNavigateBack = { navController.popBackStack() },
                onNoteSaved = { navController.popBackStack() }
            )
        }
    }
}

/**
 * Extensiones type-safe para navegación principal
 * Cada función tiene una responsabilidad clara y explícita
 */
fun NavHostController.navigateToNoteDetail(noteId: String) {
    navigate(MainRoute.NoteDetail(noteId))
}

fun NavHostController.navigateToNoteEdit(noteId: String) {
    navigate(MainRoute.NoteEdit(noteId)) // Siempre requiere ID
}

fun NavHostController.navigateToNoteCreate() {
    navigate(MainRoute.NoteCreate) // Crear nueva - explícito
}

fun NavHostController.navigateToNotes() {
    navigate(MainRoute.Notes) {
        popUpTo<MainRoute.Notes> { inclusive = true }
        launchSingleTop = true
    }
}