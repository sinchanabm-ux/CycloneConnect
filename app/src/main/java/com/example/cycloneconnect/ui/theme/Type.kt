package com.example.cycloneconnect.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.cycloneconnect.R

val AppFont = FontFamily(
    Font(R.font.atkinson_hyperlegible, FontWeight.Normal),
    Font(R.font.atkinson_hyperlegible_bold, FontWeight.Bold),
    Font(R.font.atkinson_hyperlegible_italic, FontWeight.ExtraLight),
    Font(R.font.atkinson_hyperlegible_bold_italic, FontWeight.Light)

)

private val Base = Typography()
val Typography = Typography(
    displayLarge = Base.displayLarge.copy(fontFamily = AppFont),
    displayMedium = Base.displayMedium.copy(fontFamily = AppFont),
    displaySmall = Base.displaySmall.copy(fontFamily = AppFont),
    headlineLarge = Base.headlineLarge.copy(fontFamily = AppFont),
    headlineMedium = Base.headlineMedium.copy(fontFamily = AppFont),
    headlineSmall = Base.headlineSmall.copy(fontFamily = AppFont),
    titleLarge = Base.titleLarge.copy(fontFamily = AppFont),
    titleMedium = Base.titleMedium.copy(fontFamily = AppFont),
    titleSmall = Base.titleSmall.copy(fontFamily = AppFont),
    bodyLarge = Base.bodyLarge.copy(fontFamily = AppFont),
    bodyMedium = Base.bodyMedium.copy(fontFamily = AppFont),
    bodySmall = Base.bodySmall.copy(fontFamily = AppFont),
    labelLarge = Base.labelLarge.copy(fontFamily = AppFont),
    labelMedium = Base.labelMedium.copy(fontFamily = AppFont),
    labelSmall = Base.labelSmall.copy(fontFamily = AppFont)
)