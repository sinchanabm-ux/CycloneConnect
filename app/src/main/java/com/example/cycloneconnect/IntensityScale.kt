package com.example.cycloneconnect

// The models predict wind speed (knots) only.
// The intensity label is derived from that wind speed using the IMD table.
val INTENSITY_SCALE = listOf(
    "< 17 kt" to "Below Depression threshold",
    "17 – 27 kt" to "Depression (D)",
    "28 – 33 kt" to "Deep Depression (DD)",
    "34 – 47 kt" to "Cyclonic Storm (CS)",
    "48 – 63 kt" to "Severe Cyclonic Storm (SCS)",
    "64 – 89 kt" to "Very Severe Cyclonic Storm (VSCS)",
    "90 – 119 kt" to "Extremely Severe Cyclonic Storm (ESCS)",
    "≥ 120 kt" to "Super Cyclonic Storm (SuCS)"
)

fun intensityCategory(knots: Float): String = when {
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