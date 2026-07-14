package com.radioisaac.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// TFT color palette — ported from TEF6686_ESP32 TFT_Colors.h
val BackgroundBlack = Color(0xFF000000)
val FrequencyYellow = Color(0xFFFFFF00)
val BorderBlue = Color(0xFF0000EE)
val SignalGreen = Color(0xFF00CC00)
val SignalRed = Color(0xFFEE2222)
val CyanColor = Color(0xFF00FFFF)
val SkyBlueColor = Color(0xFF5588BB)
val DarkGreyColor = Color(0xFF4A5060)
val GreyOutColor = Color(0xFF252830)
val OrangeColor = Color(0xFFFF8800)
val PurpleColor = Color(0xFFAA22DD)
val WhiteColor = Color(0xFFEEEEEE)
val StereoRedColor = Color(0xFFEE1111)
val SectionBg = Color(0xFF030508)

private val RadioColorScheme = darkColorScheme(
    background = BackgroundBlack,
    surface = BackgroundBlack,
    primary = FrequencyYellow,
    secondary = CyanColor,
    tertiary = SignalGreen,
    onBackground = WhiteColor,
    onSurface = WhiteColor,
    error = SignalRed
)

val RadioTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        color = FrequencyYellow,
        letterSpacing = 2.sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        color = FrequencyYellow,
        letterSpacing = 1.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = WhiteColor
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 12.sp,
        color = WhiteColor
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 10.sp,
        color = DarkGreyColor
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 10.sp,
        color = CyanColor
    )
)

@Composable
fun RadioIsaacTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = RadioColorScheme,
        typography = RadioTypography,
        content = content
    )
}
