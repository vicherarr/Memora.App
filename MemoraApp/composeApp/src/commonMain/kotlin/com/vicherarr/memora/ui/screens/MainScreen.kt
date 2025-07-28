package com.vicherarr.memora.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home  
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.vicherarr.memora.ui.navigation.MainRoute
import com.vicherarr.memora.ui.screens.notes.NotesListScreen
import com.vicherarr.memora.ui.screens.notes.NoteDetailScreen
import com.vicherarr.memora.ui.screens.notes.create.CreateNoteScreen
import com.vicherarr.memora.ui.screens.notes.edit.EditNoteScreen

/**
 * Pantalla principal con bottom navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Lista de pantallas que muestran bottom navigation
    val bottomNavScreens = setOf(
        "com.vicherarr.memora.ui.navigation.MainRoute.Notes",
        "com.vicherarr.memora.ui.navigation.MainRoute.Search", 
        "com.vicherarr.memora.ui.navigation.MainRoute.Profile"
    )
    
    val showBottomBar = currentRoute in bottomNavScreens

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                MainBottomBar(
                    navController = navController,
                    currentRoute = currentRoute
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        MainNavHost(
            navController = navController,
            onLogout = onLogout,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun MainBottomBar(
    navController: NavController,
    currentRoute: String?
) {
    NavigationBar {
        val items = listOf(
            BottomNavItem(
                route = "com.vicherarr.memora.ui.navigation.MainRoute.Notes",
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
                label = "Notas"
            ),
            BottomNavItem(
                route = "com.vicherarr.memora.ui.navigation.MainRoute.Search",
                selectedIcon = Icons.Filled.Search,
                unselectedIcon = Icons.Outlined.Search,
                label = "Buscar"
            ),
            BottomNavItem(
                route = "com.vicherarr.memora.ui.navigation.MainRoute.Profile",
                selectedIcon = Icons.Filled.Person,
                unselectedIcon = Icons.Outlined.Person,
                label = "Perfil"
            )
        )

        items.forEach { item ->
            NavigationBarItem(
                icon = { 
                    Icon(
                        imageVector = if (currentRoute == item.route) {
                            item.selectedIcon
                        } else {
                            item.unselectedIcon
                        },
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    when (item.route) {
                        "com.vicherarr.memora.ui.navigation.MainRoute.Notes" -> {
                            navController.navigate(MainRoute.Notes) {
                                popUpTo(MainRoute.Notes) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                        "com.vicherarr.memora.ui.navigation.MainRoute.Search" -> {
                            navController.navigate(MainRoute.Search) {
                                popUpTo(MainRoute.Notes)
                                launchSingleTop = true
                            }
                        }
                        "com.vicherarr.memora.ui.navigation.MainRoute.Profile" -> {
                            navController.navigate(MainRoute.Profile) {
                                popUpTo(MainRoute.Notes)
                                launchSingleTop = true
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun MainNavHost(
    navController: NavHostController,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = MainRoute.Notes,
        modifier = modifier
    ) {
        // Pantalla principal de notas
        composable<MainRoute.Notes> {
            NotesListScreen(
                onNavigateToNoteDetail = { noteId ->
                    navController.navigate(MainRoute.NoteDetail(noteId))
                },
                onNavigateToNoteEdit = { noteId ->
                    println("MainScreen: onNavigateToNoteEdit called with noteId = $noteId")
                    if (noteId != null) {
                        println("MainScreen: Navigating to NoteEdit")
                        navController.navigate(MainRoute.NoteEdit(noteId))
                    } else {
                        println("MainScreen: Navigating to NoteCreate")
                        navController.navigate(MainRoute.NoteCreate)
                    }
                }
            )
        }
        
        // Pantalla de detalle de nota
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
        
        // Pantalla de editar nota existente
        composable<MainRoute.NoteEdit> { backStackEntry ->
            val noteEdit = backStackEntry.toRoute<MainRoute.NoteEdit>()
            EditNoteScreen(
                noteId = noteEdit.noteId!!,
                onNavigateBack = { navController.popBackStack() },
                onNoteSaved = { navController.popBackStack() }
            )
        }
        
        // Pantalla de crear nueva nota
        composable<MainRoute.NoteCreate> {
            CreateNoteScreen(
                onNavigateBack = { navController.popBackStack() },
                onNoteSaved = { navController.popBackStack() }
            )
        }
        
        // Pantalla temporal de búsqueda - TODO: implementar en fase 6
        composable<MainRoute.Search> {
            SearchPlaceholderScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Pantalla temporal para profile/debug - mantener hasta implementar profile completo
        composable<MainRoute.Profile> {
            DebugScreen(
                onLogout = onLogout
            )
        }
    }
}


@Composable
private fun SearchPlaceholderScreen(
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🔍 Búsqueda de Notas",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Esta funcionalidad se implementará en la Fase 6",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private data class BottomNavItem(
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val label: String
)