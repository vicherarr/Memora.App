package com.vicherarr.memora

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.vicherarr.memora.ui.theme.MemoraTheme
import com.vicherarr.memora.ui.navigation.AppNavigation

@Composable
@Preview
fun App() {
    MemoraTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .safeContentPadding(),
            color = MaterialTheme.colorScheme.background
        ) {
            // Navegación principal con arquitectura escalable
            AppNavigation()
        }
    }
}

