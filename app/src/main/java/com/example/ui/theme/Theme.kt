package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DslrDarkColorScheme = darkColorScheme(
    primary = DslrGold,
    onPrimary = DslrBlack,
    primaryContainer = DslrSurfaceVariant,
    onPrimaryContainer = DslrGold,
    secondary = DslrCyan,
    onSecondary = DslrBlack,
    tertiary = DslrRed,
    background = DslrBlack,
    onBackground = TextPrimary,
    surface = DslrDarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DslrSurfaceVariant,
    onSurfaceVariant = TextSecondary
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = DslrBlack.toArgb()
                window.navigationBarColor = DslrBlack.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = DslrDarkColorScheme,
        typography = Typography,
        content = content
    )
}
