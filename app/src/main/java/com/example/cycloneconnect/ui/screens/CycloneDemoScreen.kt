package com.example.cycloneconnect.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.cycloneconnect.CyclonePredictor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

// DEMO-ONLY: delete this whole file after the demo.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CycloneDemoScreen(onBack: () -> Unit) {

    val context = LocalContext.current

    // Holds the loaded preview image (null until it is loaded).
    var previewImage by remember { mutableStateOf<ImageBitmap?>(null) }
    var imageLoadFailed by remember { mutableStateOf(false) }
    // DEMO-ONLY: prediction state.
    var predictedKnots by remember { mutableStateOf<Float?>(null) }
    var isRunning by remember { mutableStateOf(false) }
    var predictionError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Loads the PNG from the assets folder on a background thread.
    LaunchedEffect(Unit) {
        try {
            previewImage = withContext(Dispatchers.IO) {
                context.assets.open("sample_satellite.png").use { stream ->
                    BitmapFactory.decodeStream(stream)?.asImageBitmap()
                }
            }
            if (previewImage == null) imageLoadFailed = true
        } catch (e: Exception) {
            imageLoadFailed = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cyclone Intelligence") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text(
                text = "SAMPLE SATELLITE IMAGE",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                val image = previewImage
                when {
                    image != null -> Image(
                        bitmap = image,
                        contentDescription = "Sample satellite image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                    imageLoadFailed -> Text("Could not load the sample image.")
                    else -> CircularProgressIndicator()
                }
            }

            Text(
                text = "Preview only. The model reads the numerical data, not this picture.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = {
                    scope.launch {
                        isRunning = true
                        predictionError = null
                        try {
                            predictedKnots = withContext(Dispatchers.Default) {
                                val predictor = CyclonePredictor(context)
                                try {
                                    predictor.predictSample()
                                } finally {
                                    predictor.close()
                                }
                            }
                        } catch (e: Exception) {
                            predictedKnots = null
                            predictionError = "Prediction failed: ${e.message}"
                        } finally {
                            isRunning = false
                        }
                    }
                },
                enabled = !isRunning,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isRunning) "Running..." else "Run prediction")
            }

            predictedKnots?.let { knots ->
                val activeCategory = intensityCategory(knots)

                ForecastMetric(
                    title = "Model prediction: wind speed 6 hours ahead (experimental)",
                    value = String.format(Locale.US, "%.1f knots", knots),
                    modifier = Modifier.fillMaxWidth()
                )
                ForecastMetric(
                    title = "Intensity category (derived from predicted wind, IMD table)",
                    value = activeCategory,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Demo output on a bundled historical sample. " +
                            "Not an official forecast. Follow IMD bulletins for real warnings.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val scaleItems = listOf(
                    "< 17 kt" to "Below Depression threshold",
                    "17 – 27 kt" to "Depression (D)",
                    "28 – 33 kt" to "Deep Depression (DD)",
                    "34 – 47 kt" to "Cyclonic Storm (CS)",
                    "48 – 63 kt" to "Severe Cyclonic Storm (SCS)",
                    "64 – 89 kt" to "Very Severe Cyclonic Storm (VSCS)",
                    "90 – 119 kt" to "Extremely Severe Cyclonic Storm (ESCS)",
                    "≥ 120 kt" to "Super Cyclonic Storm (SuCS)"
                )

                Text(
                    text = buildAnnotatedString {
                        scaleItems.forEachIndexed { index, (range, category) ->
                            val isCurrent = category == activeCategory
                            val rowColor = if (isCurrent) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }

                            withStyle(
                                style = MaterialTheme.typography.labelSmall.toSpanStyle().copy(
                                    color = rowColor,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold
                                )
                            ) {
                                append(range.padEnd(13))
                            }
                            withStyle(
                                style = MaterialTheme.typography.labelSmall.toSpanStyle().copy(
                                    color = rowColor,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                )
                            ) {
                                append(category)
                            }

                            if (index < scaleItems.lastIndex) {
                                append("\n")
                            }
                        }
                    },
                    style = MaterialTheme.typography.labelSmall.copy(
                        lineHeight = MaterialTheme.typography.labelSmall.lineHeight * 1.35f
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            predictionError?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// DEMO-ONLY: the CNN outputs only wind speed (knots). The category is
// derived from that prediction using the IMD table, not predicted by the CNN.
// Compares the unrounded value against the lower edge of each band.
private fun intensityCategory(knots: Float): String = when {
    !knots.isFinite() -> "Unavailable"
    knots < 17f -> "Below Depression threshold"
    knots < 28f -> "Depression (D)"
    knots < 34f -> "Deep Depression (DD)"
    knots < 48f -> "Cyclonic Storm (CS)"
    knots < 64f -> "Severe Cyclonic Storm (SCS)"
    knots < 90f -> "Very Severe Cyclonic Storm (VSCS)"
    knots < 120f -> "Extremely Severe Cyclonic Storm (ESCS)"
    else -> "Super Cyclonic Storm (SuCS)"
}