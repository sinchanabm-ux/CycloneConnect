package com.example.cycloneconnect.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProfileScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "My Profile",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Create an account to save your contacts " +
                    "and access them across devices."
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onLoginClick,
            modifier = Modifier.width(220.dp)
        ) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onRegisterClick,
            modifier = Modifier.width(220.dp)
        ) {
            Text("Create Account")
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = onBackClick,
            modifier = Modifier.width(220.dp)
        ) {
            Text("Back")
        }
    }
}