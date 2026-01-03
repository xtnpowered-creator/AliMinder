package com.aliminder.app.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Using available colors from Color.kt
private val DarkColorScheme = darkColorScheme(
    primary = VinylPurple,
    onPrimary = TextPrimary,
    primaryContainer = VinylPurpleVariant,
    onPrimaryContainer = TextPrimary,
    
    secondary = TealAccent,
    onSecondary = BackgroundDark,
    secondaryContainer = TealAccent, // Fallback since TealAccent is the only secondary
    onSecondaryContainer = BackgroundDark,
    
    tertiary = WearyOrange, // Using WearyOrange as tertiary
    onTertiary = BackgroundDark,
    
    error = GraveRed,
    onError = TextPrimary,
    errorContainer = GraveRed,
    onErrorContainer = TextPrimary,
    
    background = BackgroundDark,
    onBackground = TextPrimary,
    
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,
    
    outline = BorderDark,
    outlineVariant = SurfaceVariant
)

private val LightColorScheme = lightColorScheme(
    primary = VinylPurple,
    onPrimary = TextPrimary,
    primaryContainer = VinylPurpleVariant,
    onPrimaryContainer = TextPrimary,
    
    secondary = TealAccent,
    onSecondary = BackgroundDark,
    
    background = TextPrimary, 
    onBackground = BackgroundDark,
    
    surface = TextPrimary,
    onSurface = BackgroundDark
)

/**
 * AliMinder theme with persona-based color system.
 * Defaults to dark theme (vigilance aesthetic).
 */
@Composable
fun AliMinderTheme(
    darkTheme: Boolean = true, // Force dark theme by default
    dynamicColor: Boolean = false, // Disable dynamic color to preserve persona colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AliMinderTypography, // Updated to use the renamed variable
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class) // Acknowledge experimental API usage
@Composable
fun aliMinderTopAppBarColors() = TopAppBarDefaults.centerAlignedTopAppBarColors(
    containerColor = MaterialTheme.colorScheme.background
)
