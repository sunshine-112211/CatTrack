package com.cattrack.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = CatOrange,
    onPrimary = Color.White,
    primaryContainer = CatOrangeLight,
    onPrimaryContainer = CatOrangeDark,
    secondary = CatPurple,
    onSecondary = Color.White,
    secondaryContainer = CatPurpleLight,
    onSecondaryContainer = CatPurpleDark,
    tertiary = CatMint,
    onTertiary = Color.White,
    tertiaryContainer = CatMintLight,
    onTertiaryContainer = CatMintDark,
    background = BackgroundLight,
    onBackground = Color(0xFF1A1A1A),
    surface = SurfaceLight,
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFF0ECE8),
    onSurfaceVariant = Color(0xFF4A4545),
    outline = Color(0xFFCCC8C3),
    error = Color(0xFFF44336),
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = CatOrangeLight,
    onPrimary = Color(0xFF2A1500),
    primaryContainer = CatOrangeDark,
    onPrimaryContainer = CatOrangeLight,
    secondary = CatPurpleLight,
    onSecondary = Color(0xFF1A0A2E),
    secondaryContainer = CatPurpleDark,
    onSecondaryContainer = CatPurpleLight,
    tertiary = CatMintLight,
    onTertiary = Color(0xFF002020),
    tertiaryContainer = CatMintDark,
    onTertiaryContainer = CatMintLight,
    background = BackgroundDark,
    onBackground = Color(0xFFECE8E2),
    surface = SurfaceDark,
    onSurface = Color(0xFFECE8E2),
    surfaceVariant = Color(0xFF3A3550),
    onSurfaceVariant = Color(0xFFCCC8D0),
    outline = Color(0xFF5A5568),
    error = Color(0xFFCF6679),
    onError = Color.White
)

@Composable
fun CatTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            // Dynamic color not used - use custom palette for brand identity
            if (darkTheme) DarkColorScheme else LightColorScheme
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            // 安全地获取 Activity，避免 ClassCastException
            val activity = view.context as? Activity ?: return@SideEffect
            activity.window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(activity.window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
