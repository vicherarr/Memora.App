package com.vicherarr.memora

import androidx.compose.runtime.Composable
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import cafe.adriel.voyager.navigator.Navigator

import com.vicherarr.memora.ui.theme.MemoraTheme
import com.vicherarr.memora.di.appModule
import com.vicherarr.memora.di.databaseModule
import com.vicherarr.memora.presentation.screens.WelcomeScreen

@Composable
@Preview
fun App() {
    KoinApplication(application = {
        modules(
            appModule,
            databaseModule,
            getPlatformDatabaseModule()
        )
    }) {
        MemoraTheme {
            Navigator(WelcomeScreen())
        }
    }
}

// Platform-specific function to get database module
expect fun getPlatformDatabaseModule(): org.koin.core.module.Module