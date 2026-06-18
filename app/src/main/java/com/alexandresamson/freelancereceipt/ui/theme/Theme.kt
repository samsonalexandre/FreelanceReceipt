package com.alexandresamson.freelancereceipt.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary            = BrandBlue,
    onPrimary          = NeutralSurface,
    primaryContainer   = BrandSky,
    onPrimaryContainer = NeutralOnBackground,

    secondary            = AccentMint,
    onSecondary          = NeutralSurface,
    secondaryContainer   = BrandSkyDeep,
    onSecondaryContainer = NeutralOnBackground,

    tertiary            = AccentGold,
    onTertiary          = NeutralOnBackground,

    background    = NeutralBackground,
    onBackground  = NeutralOnBackground,
    surface       = NeutralSurface,
    onSurface     = NeutralOnSurface,
    surfaceVariant   = NeutralSurfaceVar,
    onSurfaceVariant = NeutralOnSurface,

    outline = NeutralOutline,
    error   = SemanticError,
    onError = NeutralSurface
)

private val DarkColors = darkColorScheme(
    primary            = BrandBlueLight,
    onPrimary          = DarkBackground,
    primaryContainer   = BrandBlueDark,
    onPrimaryContainer = DarkOnBackground,

    secondary            = AccentMint,
    onSecondary          = DarkBackground,
    secondaryContainer   = DarkSurfaceVar,
    onSecondaryContainer = DarkOnBackground,

    tertiary            = AccentGold,
    onTertiary          = DarkBackground,

    background    = DarkBackground,
    onBackground  = DarkOnBackground,
    surface       = DarkSurface,
    onSurface     = DarkOnSurface,
    surfaceVariant   = DarkSurfaceVar,
    onSurfaceVariant = DarkOnSurface,

    outline = DarkOutline,
    error   = SemanticError,
    onError = DarkBackground
)

@Composable
fun FreelanceReceiptTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
