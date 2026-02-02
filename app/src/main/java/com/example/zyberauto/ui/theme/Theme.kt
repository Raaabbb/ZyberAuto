package com.example.zyberauto.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryRed,
    onPrimary = Color.White,
    secondary = PrimaryRed, // Using Primary for secondary as no distinct secondary was requested, or could use accents? Sticking to red theme.
    onSecondary = Color.White,
    background = BackgroundDark,
    surface = SurfaceDarkAlternative, // Using the warmer dark check
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryRed,
    onPrimary = Color.White,
    secondary = PrimaryRed,
    onSecondary = Color.White,
    background = BackgroundLight,
    surface = BackgroundLight, // No distinct surface color mentioned, matching background or could be slightly different? User said "Backgrounds: #f8f5f5". 
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    primaryContainer = BackgroundLight, // Often useful
    onPrimaryContainer = TextPrimaryLight
    
    /* Other default colors to override
    error = ErrorBgLight,
    */
)

@Composable
fun ZyberAutoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}