package com.example.monthlyfinancetracker.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = DarkGreenPrimary,
    secondary = MediumGreenPrimary,
    tertiary = MintGreenAccent,
    background = AppBackground,
    surface = CardBackground,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF1A211E),
    onSurface = Color(0xFF1A211E)
)

private val DarkColorScheme = darkColorScheme(
    primary = MintGreenAccent,
    secondary = MediumGreenPrimary,
    tertiary = DarkGreenPrimary,
    background = Color(0xFF121413),
    surface = Color(0xFF1E2220),
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = Color(0xFFE2E3E2),
    onSurface = Color(0xFFE2E3E2)
)

@Composable
fun MonthlyFinanceTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
