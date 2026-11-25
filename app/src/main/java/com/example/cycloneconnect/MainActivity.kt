
package com.example.cycloneconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.cycloneconnect.ui.theme.CycloneConnectTheme
import com.example.cycloneconnect.ui.theme.darkBackground
import com.example.cycloneconnect.ui.theme.deepSpaceBlue
import com.example.cycloneconnect.ui.theme.lightSurface
import com.example.cycloneconnect.ui.theme.lightBackground

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            var isDarkTheme by remember {
                mutableStateOf(false)
            }

            // Apply theme to the entire application.
            CycloneConnectTheme(darkTheme = isDarkTheme) {

                SideEffect {

                    val window = this@MainActivity.window

                    // Allow the app to control system bar appearance.
                    WindowCompat.setDecorFitsSystemWindows(
                        window,
                        true
                    )

                    // Match the status bar with the app toolbar.
                    window.statusBarColor = if (isDarkTheme) {
                        deepSpaceBlue.toArgb()
                    } else {
                        lightSurface.toArgb()
                    }

                    // Match the bottom navigation bar with the background.
                    window.navigationBarColor = if (isDarkTheme) {
                        darkBackground.toArgb()
                    } else {
                        lightBackground.toArgb()
                    }

                    // Configure system icon contrast.
                    val controller = WindowInsetsControllerCompat(
                        window,
                        window.decorView
                    )

                    controller.isAppearanceLightStatusBars = !isDarkTheme
                    controller.isAppearanceLightNavigationBars = !isDarkTheme

                    // Prevent Android from forcing a contrasting navigation bar.
                    if (android.os.Build.VERSION.SDK_INT >=
                        android.os.Build.VERSION_CODES.Q
                    ) {
                        window.isNavigationBarContrastEnforced = false
                        window.isStatusBarContrastEnforced = false
                    }
                }

                AppNavigation(
                    isDarkTheme = isDarkTheme,
                    onThemeChange = { isDarkTheme = it }
                )
            }
        }
    }
}
