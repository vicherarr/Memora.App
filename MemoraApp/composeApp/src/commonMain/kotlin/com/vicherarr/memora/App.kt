package com.vicherarr.memora

import androidx.compose.runtime.Composable
import org.jetbrains.compose.ui.tooling.preview.Preview
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior

import com.vicherarr.memora.ui.theme.MemoraTheme
import com.vicherarr.memora.presentation.screens.SplashScreen

@Composable
@Preview
fun App() {
    MemoraTheme {
        Navigator(
            screen = SplashScreen(),
            disposeBehavior = NavigatorDisposeBehavior(
                disposeNestedNavigators = false,
                disposeSteps = true
            )
        )
    }
}