package com.vicherarr.memora.presentation.screens

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.vicherarr.memora.presentation.screens.CreateNoteScreen
import com.vicherarr.memora.presentation.tabs.NotesTab
import com.vicherarr.memora.presentation.tabs.SearchTab
import com.vicherarr.memora.presentation.tabs.ProfileTab

class MainScreen : Screen {
    
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        
        TabNavigator(NotesTab) {
            val tabNavigator = LocalTabNavigator.current
            
            Scaffold(
                contentWindowInsets = WindowInsets.safeDrawing,
                bottomBar = {
                    NavigationBar {
                        TabNavigationItem(NotesTab)
                        TabNavigationItem(SearchTab)
                        TabNavigationItem(ProfileTab)
                    }
                }
                // FAB removed - now handled by each tab's own Navigator
            ) {
                CurrentTab()
            }
        }
    }
}

@Composable
private fun RowScope.TabNavigationItem(tab: Tab) {
    val tabNavigator = LocalTabNavigator.current
    
    NavigationBarItem(
        selected = tabNavigator.current == tab,
        onClick = { tabNavigator.current = tab },
        icon = { 
            tab.options.icon?.let { painter ->
                Icon(painter, contentDescription = tab.options.title)
            }
        },
        label = { Text(tab.options.title) }
    )
}