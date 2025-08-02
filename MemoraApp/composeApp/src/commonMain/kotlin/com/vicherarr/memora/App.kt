package com.vicherarr.memora

import androidx.compose.runtime.Composable
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import cafe.adriel.voyager.navigator.Navigator

import com.vicherarr.memora.ui.theme.MemoraTheme
import com.vicherarr.memora.di.appModule
import com.vicherarr.memora.presentation.screens.WelcomeScreen

@Composable
@Preview
fun App() {
    KoinApplication(application = {
        modules(appModule)
    }) {
        MemoraTheme {
            Navigator(WelcomeScreen())
        }
    }
}