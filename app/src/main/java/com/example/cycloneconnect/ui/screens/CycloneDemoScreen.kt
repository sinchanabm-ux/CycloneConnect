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
import com.example.cycloneconnect.CycloneModel
import com.example.cycloneconnect.INTENSITY_SCALE
import com.example.cycloneconnect.intensityCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

// One model's outcome: either a wind speed or an error message.
private data class ModelResult(
    val model: CycloneModel,
    val knots: Float? = null,
    val error: String? = null
)

private data class EvaluationMetrics(
    val mae: String,
    val rmse: String
)

private fun getEvaluationMetrics(model: CycloneModel): EvaluationMetrics {
    return when (model) {
        CycloneModel.CNN -> EvaluationMetrics(
            mae = "12.8231 knots",
            rmse = "17.6671 knots"
        )

        CycloneModel.CONVLSTM -> EvaluationMetrics(
            mae = "12.5253 knots",
            rmse = "17.0156 knots"
        )

        CycloneModel.RESNET -> EvaluationMetrics(
            mae = "12.9393 knots",
            rmse = "16.3918 knots"
        )
    }
}



// DEMO-ONLY: delete this whole file after the demo.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CycloneDemoScreen(onBack: () -> Unit) {

    val context = LocalContext.current

    var previewImage by remember { mutableStateOf<ImageBitmap?>(null) }
    var imageLoadFailed by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<List<ModelResult>>(emptyList()) }
    var isRunning by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

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
                text = "Preview only. The models read the numerical data, not this picture.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = {
                    scope.launch {
                        isRunning = true
                        results = withContext(Dispatchers.Default) {
                            CycloneModel.entries.map { model ->
                                try {
                                    val predictor = CyclonePredictor(context, model)
                                    try {
                                        ModelResult(model, knots = predictor.predictSample())
                                    } finally {
                                        predictor.close()
                                    }
                                } catch (e: Exception) {
                                    ModelResult(
                                        model,
                                        error = e.message ?: e.javaClass.simpleName
                                    )
                                }
                            }
                        }
                        isRunning = false
                    }
                },
                enabled = !isRunning,
                modifier = Modifier.fillMaxWidth(),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 2.dp
                )
            ) {
                Text(if (isRunning) "Running all models..." else "Run all 3 models")
            }

            if (results.isNotEmpty()) {
                Text(
                    text = "PREDICTED WIND 6 HOURS AHEAD (EXPERIMENTAL)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                results.forEach { result -> ModelResultCard(result) }


                // Highlight every category that at least one model predicted.
                val activeCategories = results
                    .mapNotNull { it.knots }
                    .map { intensityCategory(it) }
                    .toSet()

                Text(
                    text = buildAnnotatedString {
                        INTENSITY_SCALE.forEachIndexed { index, (range, category) ->
                            val isCurrent = category in activeCategories
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

                            if (index < INTENSITY_SCALE.lastIndex) append("\n")
                        }
                    },
                    style = MaterialTheme.typography.labelSmall.copy(
                        lineHeight = MaterialTheme.typography.labelSmall.lineHeight * 1.35f
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// One card per model: name, what it reads, predicted wind and intensity label.

@Composable
private fun ModelResultCard(result: ModelResult) {

    val metrics = getEvaluationMetrics(result.model)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // MODEL NAME
            Text(
                text = result.model.displayName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Input: ${result.model.inputDescription}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider()

            // LIVE MODEL PREDICTION
            Text(
                text = "MODEL PREDICTION",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            val knots = result.knots

            if (knots != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ForecastMetric(
                        title = "Wind speed",
                        value = String.format(
                            Locale.US,
                            "%.1f knots",
                            knots
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    ForecastMetric(
                        title = "Intensity",
                        value = intensityCategory(knots),
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Text(
                    text = "Could not run this model: ${result.error}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            HorizontalDivider()

            // MODEL EVALUATION
            Text(
                text = "MODEL EVALUATION METRICS",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "MAE",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = metrics.mae,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = "Mean Absolute Error",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "RMSE",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = metrics.rmse,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = "Root Mean Squared Error",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = when (result.model) {
                    CycloneModel.CNN ->
                        "CNN: MAE of 12.8231 knots and RMSE of 17.6671 knots. " +
                                "Provides the baseline performance for comparison."

                    CycloneModel.CONVLSTM ->
                        "ConvLSTM: Achieves the lowest MAE (12.5253 knots), " +
                                "indicating the smallest average absolute prediction error."

                    CycloneModel.RESNET ->
                        "ResNet: Achieves the lowest RMSE (16.3918 knots), " +
                                "indicating better performance when penalising larger prediction errors."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


