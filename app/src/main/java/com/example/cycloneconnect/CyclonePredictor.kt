package com.example.cycloneconnect

import android.content.Context
import org.json.JSONObject
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

// Everything that differs between the three models.
enum class CycloneModel(
    val displayName: String,
    val inputDescription: String,
    val tfliteAsset: String,
    val preprocessingAsset: String,
    val sampleAsset: String,
    val frames: Int
) {
    CNN(
        "CNN", "1 satellite image",
        "cyclone_cnn.tflite", "preprocessing.json", "sample_channel1.csv", 1
    ),
    RESNET(
        "ResNet", "1 satellite image, residual blocks",
        "cyclone_resnet.tflite", "resnet_preprocessing.json", "sample_channel1.csv", 1
    ),
    CONVLSTM(
        "ConvLSTM", "4 satellite images, 3 hours apart",
        "cyclone_convlstm.tflite", "convlstm_preprocessing.json", "sample_convlstm.csv", 4
    )
}

class CyclonePredictor(
    private val context: Context,
    private val model: CycloneModel = CycloneModel.CNN
) {

    private companion object {
        const val INPUT_SIZE = 64
    }

    private val interpreter: Interpreter
    private val pixelMean: Float
    private val pixelStd: Float

    // CNN/ResNet: 1x64x64x1. ConvLSTM: 1x4x64x64x1.
    private val expectedInputShape: IntArray =
        if (model.frames == 1) intArrayOf(1, INPUT_SIZE, INPUT_SIZE, 1)
        else intArrayOf(1, model.frames, INPUT_SIZE, INPUT_SIZE, 1)

    init {
        val preprocessing = JSONObject(
            context.assets.open(model.preprocessingAsset)
                .bufferedReader().use { it.readText() }
        )
        pixelMean = preprocessing.getDouble("pixel_mean").toFloat()
        pixelStd = preprocessing.getDouble("pixel_std").toFloat()

        check(pixelMean.isFinite() && pixelStd.isFinite() && pixelStd > 0f) {
            "Invalid preprocessing values: mean=$pixelMean, std=$pixelStd"
        }

        val modelBytes = context.assets.open(model.tfliteAsset).readBytes()
        val modelBuffer = ByteBuffer.allocateDirect(modelBytes.size)
            .order(ByteOrder.nativeOrder())
        modelBuffer.put(modelBytes)
        modelBuffer.rewind()

        interpreter = Interpreter(modelBuffer)

        try {
            val input = interpreter.getInputTensor(0)
            val output = interpreter.getOutputTensor(0)

            check(
                input.dataType() == DataType.FLOAT32 &&
                        input.shape().contentEquals(expectedInputShape)
            ) {
                "${model.displayName}: unexpected input " +
                        "${input.shape().joinToString("x")} ${input.dataType()}"
            }
            check(
                output.dataType() == DataType.FLOAT32 &&
                        output.shape().contentEquals(intArrayOf(1, 1))
            ) {
                "${model.displayName}: unexpected output " +
                        "${output.shape().joinToString("x")} ${output.dataType()}"
            }
        } catch (e: Exception) {
            interpreter.close()
            throw e
        }
    }

    // Reads the sample CSV: frames * 64 rows, 64 comma-separated values per row.
    // For ConvLSTM the 4 frames are stacked oldest first.
    private fun loadSampleData(): Array<FloatArray> {
        val csvText = context.assets.open(model.sampleAsset)
            .bufferedReader().use { it.readText() }

        val rows = csvText
            .trim()
            .lines()
            .filter { it.isNotBlank() }
            .map { row ->
                row.split(",").map { value ->
                    value.trim().toFloatOrNull()?.takeIf { it.isFinite() } ?: pixelMean
                }.toFloatArray()
            }

        val expectedRows = model.frames * INPUT_SIZE
        require(rows.size == expectedRows) {
            "${model.sampleAsset} must have $expectedRows rows but has ${rows.size}"
        }
        rows.forEachIndexed { index, row ->
            require(row.size == INPUT_SIZE) {
                "${model.sampleAsset} row ${index + 1} must have $INPUT_SIZE values but has ${row.size}"
            }
        }
        return rows.toTypedArray()
    }

    // Returns the predicted wind speed (knots) six hours ahead.
    fun predictSample(): Float {
        val imageData = loadSampleData()

        val inputBuffer = ByteBuffer.allocateDirect(
            model.frames * INPUT_SIZE * INPUT_SIZE * 4
        ).order(ByteOrder.nativeOrder())

        for (row in imageData) {
            for (pixel in row) {
                inputBuffer.putFloat((pixel - pixelMean) / pixelStd)
            }
        }
        inputBuffer.rewind()

        val output = Array(1) { FloatArray(1) }
        interpreter.run(inputBuffer, output)
        return output[0][0]
    }

    fun close() {
        interpreter.close()
    }
}