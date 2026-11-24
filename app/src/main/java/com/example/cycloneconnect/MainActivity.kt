package com.example.cycloneconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.cycloneconnect.ui.theme.CycloneConnectTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            var isDarkTheme by remember {
                mutableStateOf(false)
            }

            // Applies the selected theme to the entire app.
            CycloneConnectTheme(darkTheme = isDarkTheme) {

                AppNavigation(
                    isDarkTheme = isDarkTheme,
                    onThemeChange = { isDarkTheme = it }
                )
            }
        }
    }
}
