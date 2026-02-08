package com.example.plantpal.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ========================================
// Nature-Inspired Dark Color Scheme
// ========================================
private val DarkColorScheme = darkColorScheme(
    // Primary colors
    primary = MintGreen,
    onPrimary = DeepForest,
    primaryContainer = ForestGreen,
    onPrimaryContainer = TeaGreen,
    
    // Secondary colors
    secondary = LimeGreen,
    onSecondary = DeepForest,
    secondaryContainer = MossGreen,
    onSecondaryContainer = LightSage,
    
    // Tertiary/accent colors
    tertiary = GoldenSun,
    onTertiary = DarkBrown,
    tertiaryContainer = EarthBrown,
    onTertiaryContainer = LightCream,
    
    // Background and surface
    background = DeepForest,
    onBackground = OffWhite,
    surface = DarkMoss,
    onSurface = OffWhite,
    surfaceVariant = NightGreen,
    onSurfaceVariant = LightCream,
    
    // Outline and dividers
    outline = SageGreen,
    outlineVariant = MossGreen,
    
    // Error colors
    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

// ========================================
// Nature-Inspired Light Color Scheme
// ========================================
private val LightColorScheme = lightColorScheme(
    // Primary colors - Deep greens
    primary = LeafGreen,
    onPrimary = Color.White,
    primaryContainer = PaleGreen,
    onPrimaryContainer = ForestGreen,
    
    // Secondary colors - Fresh greens
    secondary = SageGreen,
    onSecondary = Color.White,
    secondaryContainer = LightSage,
    onSecondaryContainer = MossGreen,
    
    // Tertiary/accent colors - Warm earth tones
    tertiary = EarthBrown,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE0B2),
    onTertiaryContainer = EarthBrown,
    
    // Background and surface - Soft natural tones
    background = SoftGreen,
    onBackground = CharcoalGreen,
    surface = CreamWhite,
    onSurface = CharcoalGreen,
    surfaceVariant = PaleGreen,
    onSurfaceVariant = MossGreen,
    
    // Outline and dividers
    outline = SageGreen,
    outlineVariant = TeaGreen,
    
    // Error colors
    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A)
)

@Composable
fun PlantPalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
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
    
    // Update status bar color to match the theme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primaryContainer.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}