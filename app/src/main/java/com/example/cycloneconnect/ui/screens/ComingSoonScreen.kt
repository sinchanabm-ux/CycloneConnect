
package com.example.cycloneconnect.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Reusable placeholder for features not implemented yet.
@Composable
fun ComingSoonScreen(
    title: String,
    onBackClick: () -> Unit
) {
    Scaffold { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),

            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Displays the selected screen name.
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium
            )

            // Explains that the feature is not ready.
            Text(
                text = "This feature is coming soon.",
                modifier = Modifier.padding(top = 12.dp),
                style = MaterialTheme.typography.bodyLarge
            )

            // Returns the user to the previous screen.
            Button(
                onClick = onBackClick,
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Text("Go Back")
            }
        }
    }
}
