
package com.example.cycloneconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.cycloneconnect.ui.screens.HomeScreen
import com.example.cycloneconnect.ui.theme.CycloneConnectTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Applies your existing light/dark theme and typography.
            CycloneConnectTheme {
                HomeScreen()
            }
        }
    }
}
