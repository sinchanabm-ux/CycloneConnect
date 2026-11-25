package com.example.cycloneconnect.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.cycloneconnect.data.CoastalCities
import com.example.cycloneconnect.data.CoastalCity

// Compose version of AutoCompleteTextView.
// Suggests only coastal Indian places and only accepts a valid one on submit.
@Composable
fun CoastalCitySearch(
    onCitySelected: (CoastalCity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val cities = remember { CoastalCities.load(context) }

    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<CoastalCity?>(null) }
    var showError by remember { mutableStateOf(false) }

    // No suggestions once a suggestion has been picked.
    val suggestions = remember(query, selected) {
        if (selected != null) emptyList() else CoastalCities.suggest(cities, query)
    }

    fun submit() {
        val match = selected ?: CoastalCities.find(cities, query)
        if (match != null) {
            focusManager.clearFocus()
            onCitySelected(match)
        } else {
            showError = true
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {

        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                selected = null
                showError = false
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search other coastal areas") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search")
            },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = {
                            query = ""
                            selected = null
                            showError = false
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                    FilledIconButton(
                        onClick = { submit() },
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Submit search"
                        )
                    }
                }
            },
            isError = showError,
            supportingText = if (showError) {
                { Text("Please choose a coastal city from the suggestions") }
            } else null,
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(onSearch = { submit() })
        )

        // The dropdown list of suggestions.
        AnimatedVisibility(visible = suggestions.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column {
                    suggestions.forEachIndexed { index, city ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selected = city
                                    query = city.label
                                    showError = false
                                    focusManager.clearFocus()
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    text = city.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = city.state,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (index < suggestions.lastIndex) HorizontalDivider()
                    }
                }
            }
        }
    }
}
