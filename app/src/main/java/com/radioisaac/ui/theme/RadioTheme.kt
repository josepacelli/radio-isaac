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

// fm-dx-webserver inspired palette
val BackgroundBlack  = Color(0xFF16181E)   // deep charcoal
val PanelBg          = Color(0xFF1E2028)   // panel background
val PanelBg2         = Color(0xFF23262F)   // slightly lighter panel
val BorderColor      = Color(0xFF2E3340)   // subtle panel border
val AccentTeal       = Color(0xFF58DBAB)   // primary accent — teal
val AccentTealDim    = Color(0xFF2A6B55)   // dim teal for inactive
val FrequencyYellow  = Color(0xFFFFD166)   // frequency / PS name
val SignalGreen      = Color(0xFF06D6A0)   // signal green
val SignalRed        = Color(0xFFFF5776)   // error / red accent
val CyanColor        = Color(0xFF48CAE4)   // secondary cyan
val SkyBlueColor     = Color(0xFF6A8EAE)   // muted blue labels
val DarkGreyColor    = Color(0xFF4A5568)   // dim labels
val GreyOutColor     = Color(0xFF2D3142)   // inactive segments
val OrangeColor      = Color(0xFFFFB347)   // buffering / warn
val WhiteColor       = Color(0xFFECEFF4)   // primary text
val DimWhite         = Color(0xFF8892A0)   // secondary text

private val RadioColorScheme = darkColorScheme(
    background = BackgroundBlack,
    surface    = PanelBg,
    primary    = AccentTeal,
    secondary  = CyanColor,
    tertiary   = SignalGreen,
    onBackground = WhiteColor,
    onSurface    = WhiteColor,
    error        = SignalRed
)

val RadioTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        color = FrequencyYellow,
        letterSpacing = 2.sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        color = AccentTeal,
        letterSpacing = 1.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        color = WhiteColor
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 13.sp,
        color = WhiteColor
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 11.sp,
        color = DarkGreyColor
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 11.sp,
        color = CyanColor
    )
)

@Composable
fun RadioIsaacTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = RadioColorScheme,
        typography  = RadioTypography,
        content     = content
    )
}
