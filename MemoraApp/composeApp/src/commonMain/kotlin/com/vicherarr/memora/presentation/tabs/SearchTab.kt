package com.vicherarr.memora.presentation.tabs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.vicherarr.memora.presentation.screens.SearchScreen

/**
 * Search Tab - Following Voyager 2025 nested navigation best practices
 * Each tab has its own Navigator for deep navigation to avoid ClassCastException
 */
object SearchTab : Tab {
    
    override val options: TabOptions
        @Composable
        get() {
            val title = "Búsqueda"
            val icon = rememberVectorPainter(Icons.Default.Search)
            
            return remember {
                TabOptions(
                    index = 1u,
                    title = title,
                    icon = icon
                )
            }
        }
    
    @Composable
    override fun Content() {
        // Nested Navigation Pattern (Voyager 2025 Best Practice)
        // Each tab has its own Navigator for deep navigation
        Navigator(SearchScreen())
    }
}