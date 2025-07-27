package com.vicherarr.memora.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Light Color Scheme
private val LightColorScheme = lightColorScheme(
    primary = MemoraColors.Primary,
    onPrimary = MemoraColors.OnPrimary,
    primaryContainer = MemoraColors.PrimaryContainer,
    onPrimaryContainer = MemoraColors.OnPrimaryContainer,
    secondary = MemoraColors.Secondary,
    onSecondary = MemoraColors.OnSecondary,
    secondaryContainer = MemoraColors.SecondaryContainer,
    onSecondaryContainer = MemoraColors.OnSecondaryContainer,
    tertiary = MemoraColors.Tertiary,
    onTertiary = MemoraColors.OnTertiary,
    tertiaryContainer = MemoraColors.TertiaryContainer,
    onTertiaryContainer = MemoraColors.OnTertiaryContainer,
    error = MemoraColors.Error,
    onError = MemoraColors.OnError,
    errorContainer = MemoraColors.ErrorContainer,
    onErrorContainer = MemoraColors.OnErrorContainer,
    background = MemoraColors.Background,
    onBackground = MemoraColors.OnBackground,
    surface = MemoraColors.Surface,
    onSurface = MemoraColors.OnSurface,
    surfaceVariant = MemoraColors.SurfaceVariant,
    onSurfaceVariant = MemoraColors.OnSurfaceVariant,
    outline = MemoraColors.Outline,
    outlineVariant = MemoraColors.OutlineVariant,
    surfaceTint = MemoraColors.SurfaceTint
)

// Dark Color Scheme
private val DarkColorScheme = darkColorScheme(
    primary = MemoraColors.PrimaryDark,
    onPrimary = MemoraColors.OnPrimaryDark,
    primaryContainer = MemoraColors.PrimaryContainerDark,
    onPrimaryContainer = MemoraColors.OnPrimaryContainerDark,
    secondary = MemoraColors.SecondaryDark,
    onSecondary = MemoraColors.OnSecondaryDark,
    secondaryContainer = MemoraColors.SecondaryContainerDark,
    onSecondaryContainer = MemoraColors.OnSecondaryContainerDark,
    tertiary = MemoraColors.TertiaryDark,
    onTertiary = MemoraColors.OnTertiaryDark,
    tertiaryContainer = MemoraColors.TertiaryContainerDark,
    onTertiaryContainer = MemoraColors.OnTertiaryContainerDark,
    error = MemoraColors.ErrorDark,
    onError = MemoraColors.OnErrorDark,
    errorContainer = MemoraColors.ErrorContainerDark,
    onErrorContainer = MemoraColors.OnErrorContainerDark,
    background = MemoraColors.BackgroundDark,
    onBackground = MemoraColors.OnBackgroundDark,
    surface = MemoraColors.SurfaceDark,
    onSurface = MemoraColors.OnSurfaceDark,
    surfaceVariant = MemoraColors.SurfaceVariantDark,
    onSurfaceVariant = MemoraColors.OnSurfaceVariantDark,
    outline = MemoraColors.OutlineDark,
    outlineVariant = MemoraColors.OutlineVariantDark,
    surfaceTint = MemoraColors.SurfaceTintDark
)

@Composable
fun MemoraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MemoraTypography,
        shapes = MemoraShapes,
        content = content
    )
}