package com.example.cycloneconnect

import android.content.Context
import org.json.JSONObject
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

class CyclonePredictor(private val context: Context) {

    private companion object {
        const val INPUT_SIZE = 64
    }

    private val interpreter: Interpreter

    private val pixelMean: Float
    private val pixelStd: Float

    init {
        // Read the preprocessing parameters first and make sure they are usable
        val preprocessingText = context.assets
            .open("preprocessing.json")
            .bufferedReader()
            .use { it.readText() }

        val preprocessing = JSONObject(preprocessingText)

        pixelMean = preprocessing.getDouble("pixel_mean").toFloat()
        pixelStd = preprocessing.getDouble("pixel_std").toFloat()

        check(pixelMean.isFinite() && pixelStd.isFinite() && pixelStd > 0f) {
            "Invalid preprocessing values: mean=$pixelMean, std=$pixelStd"
        }

        // Load the trained CNN model
        val modelBytes = context.assets
            .open("cyclone_cnn.tflite")
            .readBytes()

        val modelBuffer = ByteBuffer.allocateDirect(modelBytes.size)
            .order(ByteOrder.nativeOrder())

        modelBuffer.put(modelBytes)
        modelBuffer.rewind()

        interpreter = Interpreter(modelBuffer)

        // Confirm the model has the input/output we expect
        try {
            val input = interpreter.getInputTensor(0)
            val output = interpreter.getOutputTensor(0)

            check(
                input.dataType() == DataType.FLOAT32 &&
                        input.shape().contentEquals(intArrayOf(1, INPUT_SIZE, INPUT_SIZE, 1))
            ) {
                "Unexpected model input: ${input.shape().joinToString("x")} ${input.dataType()}"
            }

            check(
                output.dataType() == DataType.FLOAT32 &&
                        output.shape().contentEquals(intArrayOf(1, 1))
            ) {
                "Unexpected model output: ${output.shape().joinToString("x")} ${output.dataType()}"
            }
        } catch (e: Exception) {
            interpreter.close()
            throw e
        }
    }

    // Load the 64x64 Channel 1 data from CSV (no header, comma-separated)
    private fun loadSampleData(): Array<FloatArray> {

        val csvText = context.assets
            .open("sample_channel1.csv")
            .bufferedReader()
            .use { it.readText() }

        val rows = csvText
            .trim()
            .lines()
            .filter { it.isNotBlank() }
            .map { row ->
                row.split(",").map { value ->
                    value.trim()
                        .toFloatOrNull()
                        ?.takeIf { it.isFinite() }
                        ?: pixelMean
                }.toFloatArray()
            }

        require(rows.size == INPUT_SIZE) {
            "Sample CSV must have $INPUT_SIZE rows but has ${rows.size}"
        }
        rows.forEachIndexed { index, row ->
            require(row.size == INPUT_SIZE) {
                "Sample CSV row ${index + 1} must have $INPUT_SIZE values but has ${row.size}"
            }
        }

        return rows.toTypedArray()
    }

    // Run the CNN prediction
    fun predictSample(): Float {

        val imageData = loadSampleData()

        // Create input buffer for 1x64x64x1 Float32 tensor
        val inputBuffer = ByteBuffer.allocateDirect(
            INPUT_SIZE * INPUT_SIZE * 4
        ).order(ByteOrder.nativeOrder())

        // Normalize the pixel values
        for (row in imageData) {
            for (pixel in row) {
                val normalizedPixel = (pixel - pixelMean) / pixelStd
                inputBuffer.putFloat(normalizedPixel)
            }
        }

        inputBuffer.rewind()

        // Output tensor containing predicted wind speed
        val output = Array(1) { FloatArray(1) }

        // Execute the model
        interpreter.run(inputBuffer, output)

        return output[0][0]
    }

    // Close the interpreter when finished
    fun close() {
        interpreter.close()
    }
}