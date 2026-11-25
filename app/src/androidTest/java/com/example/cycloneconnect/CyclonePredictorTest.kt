package com.example.cycloneconnect

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CyclonePredictorTest {

    @Test
    fun testCNNPrediction() {

        val context = InstrumentationRegistry
            .getInstrumentation()
            .targetContext

        val predictor = CyclonePredictor(context)

        try {
            val predictedWindSpeed = predictor.predictSample()

            // Expected prediction from Colab
            val expectedWindSpeed = 20.333154f

            assertEquals(
                expectedWindSpeed,
                predictedWindSpeed,
                0.1f
            )

        } finally {
            predictor.close()
        }
    }
}