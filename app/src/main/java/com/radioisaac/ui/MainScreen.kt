package com.radioisaac.ui

import android.content.res.Configuration
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.radioisaac.ui.theme.*
import com.radioisaac.viewmodel.RadioUiState
import com.radioisaac.viewmodel.RadioViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RadioScreen(vm: RadioViewModel = viewModel()) {
    val uiState by vm.uiState.collectAsState()
    val config = LocalConfiguration.current
    val isLandscape = config.orientation == Configuration.ORIENTATION_LANDSCAPE

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlack)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .border(2.dp, BorderBlue)
        ) {
            TefHeaderRow(uiState)
            HorizontalDivider(color = BorderBlue, thickness = 1.dp)
            if (isLandscape) {
                LandscapeContent(
                    uiState = uiState,
                    onPrev = vm::prevStation,
                    onNext = vm::nextStation,
                    onPlayStop = vm::togglePlayback,
                    modifier = Modifier.weight(1f)
                )
            } else {
                PortraitContent(
                    uiState = uiState,
                    onPrev = vm::prevStation,
                    onNext = vm::nextStation,
                    onPlayStop = vm::togglePlayback,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        FloatingActionButton(
            onClick = vm::openStationList,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .size(48.dp),
            containerColor = Color(0xFF000033),
            contentColor = FrequencyYellow,
            shape = RoundedCornerShape(4.dp),
            elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .border(1.dp, BorderBlue, RoundedCornerShape(4.dp))
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.List, contentDescription = "Estações", modifier = Modifier.size(20.dp))
                Text("LIST", fontSize = 14.sp, fontFamily = FontFamily.Monospace, color = FrequencyYellow)
            }
        }

        uiState.errorMessage?.let { msg ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp, start = 12.dp, end = 12.dp),
                action = {
                    TextButton(onClick = vm::clearError) {
                        Text("OK", color = FrequencyYellow, fontFamily = FontFamily.Monospace, fontSize = 15.sp)
                    }
                },
                containerColor = Color(0xFF220000),
                contentColor = SignalRed
            ) {
                Text(msg, fontFamily = FontFamily.Monospace, fontSize = 15.sp, color = SignalRed)
            }
        }
    }

    if (uiState.showStationList) {
        StationListSheet(
            uiState = uiState,
            onDismiss = vm::closeStationList,
            onSelect = vm::selectStation,
            onSearch = vm::search,
            onLoadTop = vm::loadTopStations,
            onLoadRegion = vm::loadByRegion
        )
    }
}

// ─── PORTRAIT: stacked column ─────────────────────────────────────────────────

@Composable
private fun PortraitContent(
    uiState: RadioUiState,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onPlayStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        TefMainDisplay(
            uiState = uiState,
            onPrev = onPrev,
            onNext = onNext,
            onPlayStop = onPlayStop,
            modifier = Modifier.weight(1f)
        )
        HorizontalDivider(color = BorderBlue, thickness = 1.dp)
        TefInfoBoxesRow(uiState)
        HorizontalDivider(color = BorderBlue, thickness = 1.dp)
        TefMetersRow(uiState)
        HorizontalDivider(color = BorderBlue, thickness = 1.dp)
        TefPtyRow(uiState)
        HorizontalDivider(color = BorderBlue, thickness = 1.dp)
        TefPsRow(uiState)
        HorizontalDivider(color = BorderBlue, thickness = 1.dp)
        TefRtFooter(uiState)
    }
}

// ─── LANDSCAPE: left (station) + right (meters/data) ─────────────────────────

@Composable
private fun LandscapeContent(
    uiState: RadioUiState,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onPlayStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxSize()) {
        // Left panel: station logo + name + info boxes
        Column(modifier = Modifier.weight(1.4f).fillMaxHeight()) {
            TefMainDisplay(
                uiState = uiState,
                onPrev = onPrev,
                onNext = onNext,
                onPlayStop = onPlayStop,
                modifier = Modifier.weight(1f)
            )
            HorizontalDivider(color = BorderBlue, thickness = 1.dp)
            TefInfoBoxesRow(uiState)
        }
        // Vertical separator
        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(BorderBlue)
        )
        // Right panel: meters + data rows
        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
            TefMetersRow(uiState)
            HorizontalDivider(color = BorderBlue, thickness = 1.dp)
            TefPtyRow(uiState)
            HorizontalDivider(color = BorderBlue, thickness = 1.dp)
            TefPsRow(uiState)
            HorizontalDivider(color = BorderBlue, thickness = 1.dp)
            TefRtFooter(uiState)
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

// ─── HEADER — stereo dots + RDS box + kbps + FM + AUTO BW/iMS/EQ ─────────────

@Composable
private fun TefHeaderRow(uiState: RadioUiState) {
    val infiniteTransition = rememberInfiniteTransition(label = "rds")
    val rdsAlpha by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "rdsAlpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .background(Color(0xFF000008))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(if (uiState.isStereo) StereoRedColor else GreyOutColor))
        Spacer(Modifier.width(2.dp))
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(if (uiState.isStereo) StereoRedColor else GreyOutColor))
        Spacer(Modifier.width(6.dp))

        Box(
            modifier = Modifier
                .background(
                    if (uiState.hasRdsData) Color(0xFFCC0000) else Color(0xFF330000),
                    RoundedCornerShape(2.dp)
                )
                .padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
            Text(
                text = "RDS",
                color = if (uiState.hasRdsData) Color.White.copy(alpha = rdsAlpha) else GreyOutColor,
                fontSize = 15.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        Spacer(Modifier.width(8.dp))

        val bitrate = uiState.currentStation?.bitrate ?: 0
        Text(
            text = if (bitrate > 0) "$bitrate kbps" else "--- kbps",
            color = CyanColor,
            fontSize = 16.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.weight(1f))

        Text("FM", color = SkyBlueColor, fontSize = 15.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(10.dp))

        TefHeaderBtn("AUTO", uiState.isStreamActive)
        Spacer(Modifier.width(3.dp))
        TefHeaderBtn("iMS", uiState.isStereo)
        Spacer(Modifier.width(3.dp))
        TefHeaderBtn("EQ", uiState.isPlaying)
    }
}

@Composable
private fun TefHeaderBtn(label: String, active: Boolean) {
    Box(
        modifier = Modifier
            .border(1.dp, if (active) CyanColor.copy(alpha = 0.8f) else GreyOutColor, RoundedCornerShape(2.dp))
            .padding(horizontal = 4.dp, vertical = 1.dp)
    ) {
        Text(
            text = label,
            color = if (active) CyanColor else GreyOutColor,
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

// ─── MAIN DISPLAY — favicon + large station name + dBuV ──────────────────────

@Composable
private fun TefMainDisplay(
    uiState: RadioUiState,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onPlayStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val signalDb = uiState.signalLevel * 95f
    val dbColor = when {
        signalDb > 65 -> SignalGreen
        signalDb > 40 -> FrequencyYellow
        signalDb > 20 -> OrangeColor
        else -> SignalRed
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SectionBg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Station favicon box (like the logo box in webserver UI)
        Box(
            modifier = Modifier
                .padding(start = 8.dp)
                .size(54.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF000022))
                .border(1.dp, BorderBlue.copy(alpha = 0.6f), RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            val favicon = uiState.currentStation?.favicon
            if (!favicon.isNullOrBlank()) {
                AsyncImage(
                    model = favicon,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Fit
                )
            } else {
                Icon(
                    Icons.Default.Radio,
                    contentDescription = null,
                    tint = SkyBlueColor.copy(alpha = 0.4f),
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Prev arrow
        IconButton(onClick = onPrev, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.SkipPrevious, "Anterior", tint = PurpleColor, modifier = Modifier.size(24.dp))
        }

        // Center: frequency + PS name + play button
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val station = uiState.currentStation
            val freq = station?.extractedFrequency
            val psName = station?.psName ?: "--------"
            val fullName = station?.name ?: "---"

            // Frequency line — big if available, else "INTERNET"
            if (freq != null) {
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Center) {
                    Text(
                        text = freq,
                        color = CyanColor,
                        fontSize = 34.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text = "FM",
                        color = CyanColor.copy(alpha = 0.7f),
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            } else {
                Text(
                    text = "INTERNET",
                    color = DarkGreyColor,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 3.sp
                )
            }

            // PS name (8 chars, yellow — same as RDS PS field)
            Text(
                text = psName,
                color = if (station != null) FrequencyYellow else GreyOutColor,
                fontSize = 34.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Full name subtitle (grey, smaller)
            if (fullName.length > 8 && station != null) {
                Text(
                    text = fullName,
                    color = DarkGreyColor,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            Spacer(Modifier.height(4.dp))

            // Play/stop button + buffering
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (uiState.isPlaying) Color(0xFF001500) else Color(0xFF120800))
                        .border(1.dp, if (uiState.isPlaying) SignalGreen else OrangeColor, CircleShape)
                        .clickable(onClick = onPlayStop),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (uiState.isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = if (uiState.isPlaying) "Stop" else "Play",
                        tint = if (uiState.isPlaying) SignalGreen else OrangeColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
                if (uiState.isBuffering) {
                    Spacer(Modifier.width(4.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        color = CyanColor,
                        strokeWidth = 1.5.dp
                    )
                }
            }
        }

        // Next arrow
        IconButton(onClick = onNext, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.SkipNext, "Próxima", tint = PurpleColor, modifier = Modifier.size(24.dp))
        }

        // Signal dBuV — right column
        Column(
            modifier = Modifier.padding(end = 8.dp).width(50.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "%4.1f".format(signalDb),
                color = dbColor,
                fontSize = 34.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "dBuV",
                color = WhiteColor,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─── INFO BOXES — PI CODE | BITRATE | SIGNAL (webserver dashboard style) ─────

@Composable
private fun TefInfoBoxesRow(uiState: RadioUiState) {
    val signalDb = uiState.signalLevel * 95f
    val dbColor = when {
        signalDb > 65 -> SignalGreen
        signalDb > 40 -> FrequencyYellow
        signalDb > 20 -> OrangeColor
        else -> SignalRed
    }
    val bitrate = uiState.currentStation?.bitrate ?: 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF000008))
            .padding(horizontal = 6.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        TefInfoBox(
            label = "PI CODE",
            value = uiState.currentStation?.piCode ?: "----",
            valueColor = FrequencyYellow,
            modifier = Modifier.weight(1f)
        )
        TefInfoBox(
            label = "BITRATE",
            value = if (bitrate > 0) "${bitrate}k" else "---",
            valueColor = CyanColor,
            modifier = Modifier.weight(1f)
        )
        TefInfoBox(
            label = "SIGNAL",
            value = "%4.1f".format(signalDb),
            valueColor = dbColor,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TefInfoBox(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .border(1.dp, BorderBlue.copy(alpha = 0.4f), RoundedCornerShape(3.dp))
            .background(Color(0xFF000015), RoundedCornerShape(3.dp))
            .padding(horizontal = 4.dp, vertical = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = DarkGreyColor,
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.5.sp
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = 16.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

// ─── METERS — S bar (scale: 1 3 5 7 9 +10 +30) + M bar (0–120%) ──────────────

@Composable
private fun TefMetersRow(uiState: RadioUiState) {
    val animSignal by animateFloatAsState(
        targetValue = uiState.signalLevel,
        animationSpec = tween(280),
        label = "signal"
    )
    val animQuality by animateFloatAsState(
        targetValue = uiState.qualityLevel,
        animationSpec = tween(280),
        label = "quality"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF000005))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        TefSMeter(value = animSignal)
        Spacer(Modifier.height(4.dp))
        TefMMeter(value = animQuality, bitrate = uiState.currentStation?.bitrate ?: 0)
    }
}

@Composable
private fun TefSMeter(value: Float) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("1", "3", "5", "7", "9", "+10", "+30").forEach { tick ->
                Text(
                    text = tick,
                    color = if (tick.startsWith("+")) SignalRed else SignalGreen,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("S", color = WhiteColor, fontSize = 16.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.width(14.dp))
            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(vertical = 1.dp)
            ) {
                val total = 47
                val segW = (size.width - (total - 1) * 0.8f) / total
                val filled = (value * total).toInt().coerceIn(0, total)
                val greenCount = (total * 0.57f).toInt()

                for (i in 0 until total) {
                    val col = when {
                        i >= filled -> GreyOutColor
                        i < greenCount -> SignalGreen
                        else -> SignalRed
                    }
                    drawRect(
                        color = col,
                        topLeft = Offset(i * (segW + 0.8f), 0f),
                        size = Size(segW, size.height)
                    )
                }
            }
        }
    }
}

@Composable
private fun TefMMeter(value: Float, bitrate: Int) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("M", color = WhiteColor, fontSize = 16.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.width(14.dp))
            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(vertical = 1.dp)
            ) {
                val total = 47
                val segW = (size.width - (total - 1) * 0.8f) / total
                val filled = (value * total).toInt().coerceIn(0, total)
                val greenCount = (total * 0.68f).toInt()

                for (i in 0 until total) {
                    val col = when {
                        i >= filled -> GreyOutColor
                        i < greenCount -> SignalGreen
                        else -> SignalRed
                    }
                    drawRect(
                        color = col,
                        topLeft = Offset(i * (segW + 0.8f), 0f),
                        size = Size(segW, size.height)
                    )
                }
            }
            Text(
                text = if (bitrate > 0) "${bitrate}k" else "---",
                color = CyanColor,
                fontSize = 15.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.width(28.dp),
                textAlign = TextAlign.End
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 28.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("0", "20", "40", "60", "80", "100", "120%").forEach { tick ->
                Text(
                    text = tick,
                    color = if (tick == "120%") SignalRed else SignalGreen,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

// ─── PTY ROW — genre + clock + SQ/OFF + S/N dB ───────────────────────────────

@Composable
private fun TefPtyRow(uiState: RadioUiState) {
    var clockStr by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        while (true) {
            clockStr = sdf.format(Date())
            delay(10000)
        }
    }

    val snr = (uiState.signalLevel * 68f).toInt()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
            .background(Color(0xFF000003))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("PTY:", color = WhiteColor, fontSize = 16.sp, fontFamily = FontFamily.Monospace)
        Spacer(Modifier.width(3.dp))
        Text(
            text = uiState.currentStation?.displayTags?.ifBlank { "---" } ?: "---",
            color = DarkGreyColor,
            fontSize = 16.sp,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (uiState.rtArtist.isNotBlank()) {
            Spacer(Modifier.width(6.dp))
            Text(
                text = "| ${uiState.rtArtist}",
                color = FrequencyYellow,
                fontSize = 16.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
        Text(text = clockStr, color = CyanColor, fontSize = 16.sp, fontFamily = FontFamily.Monospace)
        Spacer(Modifier.width(8.dp))
        Text("SQ:", color = WhiteColor, fontSize = 16.sp, fontFamily = FontFamily.Monospace)
        Spacer(Modifier.width(2.dp))
        Text(
            text = if (uiState.isStreamActive) "ON " else "OFF",
            color = if (uiState.isStreamActive) SignalGreen else OrangeColor,
            fontSize = 16.sp,
            fontFamily = FontFamily.Monospace
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "S/N %2d dB".format(snr),
            color = CyanColor,
            fontSize = 16.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

// ─── PS ROW — PS: name + PI: [uuid short] ────────────────────────────────────

@Composable
private fun TefPsRow(uiState: RadioUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .background(Color(0xFF000008))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("PS:", color = WhiteColor, fontSize = 16.sp, fontFamily = FontFamily.Monospace)
        Spacer(Modifier.width(3.dp))
        Text(
            text = uiState.currentStation?.name?.take(8)?.uppercase() ?: "--------",
            color = FrequencyYellow,
            fontSize = 15.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text("PI:", color = WhiteColor, fontSize = 16.sp, fontFamily = FontFamily.Monospace)
        Spacer(Modifier.width(3.dp))
        Box(
            modifier = Modifier
                .border(1.dp, BorderBlue, RoundedCornerShape(2.dp))
                .background(Color(0xFF000022))
                .padding(horizontal = 5.dp, vertical = 1.dp)
        ) {
            Text(
                text = uiState.currentStation?.piCode ?: "----",
                color = FrequencyYellow,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ─── RT FOOTER — scrolling now-playing text ───────────────────────────────────

@Composable
private fun TefRtFooter(uiState: RadioUiState) {
    val rtText = when {
        uiState.rtTitle.isNotBlank() -> uiState.rtTitle.uppercase()
        uiState.nowPlaying.isNotBlank() -> uiState.nowPlaying.uppercase()
        uiState.currentStation != null -> uiState.currentStation.displayTags.uppercase().ifBlank { "RADIO ONLINE" }
        else -> "AGUARDANDO ESTACAO..."
    }

    val infiniteTransition = rememberInfiniteTransition(label = "scroll")
    val offsetAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(rtText.length * 180, easing = LinearEasing),
            RepeatMode.Restart
        ),
        label = "scrollOffset"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
            .background(Color(0xFF000033)),
        contentAlignment = Alignment.CenterStart
    ) {
        val padStart = (1f - offsetAnim) * 300f
        Text(
            text = "  $rtText  •  $rtText  ",
            color = CyanColor,
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier
                .padding(start = padStart.dp)
                .fillMaxWidth(),
            overflow = TextOverflow.Clip
        )
    }
}
