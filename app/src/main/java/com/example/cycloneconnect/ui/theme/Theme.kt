package com.example.cycloneconnect.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = dustyDenim,
    onPrimary = inkBlack,
    secondary = blueState,
    onSecondary = eggShell,
    tertiary = eggShell,
    background = inkBlack,
    onBackground = eggShell,
    surface = deepSpaceBlue,
    onSurface = eggShell
)

private val LightColorScheme = lightColorScheme(
    primary = deepSpaceBlue,
    onPrimary = eggShell,
    secondary = blueState,
    onSecondary = eggShell,
    tertiary = dustyDenim,
    background = eggShell,
    onBackground = inkBlack,
    surface = eggShell,
    onSurface = inkBlack
)

@Composable
fun CycloneConnectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Changed to false so your custom palette is used
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
