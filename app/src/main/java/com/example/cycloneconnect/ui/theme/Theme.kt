package com.example.cycloneconnect.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(

    primary = deepSpaceBlue,
    onPrimary = eggShell,

    secondary = blueState,
    onSecondary = Color(0xFFFFFFFF),

    tertiary = dustyDenim,
    onTertiary = inkBlack,

    background = lightBackground,
    onBackground = inkBlack,

    surface = lightSurface,
    onSurface = inkBlack,

    surfaceVariant = lightBlueContainer,
    onSurfaceVariant = blueState,

    secondaryContainer = lightBlueContainer,
    onSecondaryContainer = lightOnBlueContainer,

    error = emergencyRed,
    onError = onEmergencyRed
)

private val DarkColorScheme = darkColorScheme(

    primary = dustyDenim,
    onPrimary = inkBlack,

    secondary = blueState,
    onSecondary = eggShell,

    tertiary = mistBlue,
    onTertiary = inkBlack,

    background = darkBackground,
    onBackground = eggShell,

    surface = darkSurface,
    onSurface = eggShell,

    surfaceVariant = darkBlueContainer,
    onSurfaceVariant = Color(0xFFD0D9E5),

    secondaryContainer = darkBlueContainer,
    onSecondaryContainer = darkOnBlueContainer,

    error = emergencyRedDark,
    onError = onEmergencyRedDark
)

@Composable
fun CycloneConnectTheme(
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
        typography = Typography,
        content = content
    )
}
