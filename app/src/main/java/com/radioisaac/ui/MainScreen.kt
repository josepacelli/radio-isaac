package com.radioisaac.ui

import android.content.res.Configuration
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.radioisaac.ui.theme.AccentTeal
import com.radioisaac.ui.theme.BackgroundBlack
import com.radioisaac.ui.theme.BorderColor
import com.radioisaac.ui.theme.CyanColor
import com.radioisaac.ui.theme.DarkGreyColor
import com.radioisaac.ui.theme.DimWhite
import com.radioisaac.ui.theme.FrequencyYellow
import com.radioisaac.ui.theme.GreyOutColor
import com.radioisaac.ui.theme.OrangeColor
import com.radioisaac.ui.theme.PanelBg
import com.radioisaac.ui.theme.PanelBg2
import com.radioisaac.ui.theme.SignalGreen
import com.radioisaac.ui.theme.SignalRed
import com.radioisaac.ui.theme.WhiteColor
import com.radioisaac.viewmodel.RadioUiState
import com.radioisaac.viewmodel.RadioViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        Column(modifier = Modifier.fillMaxSize()) {
            TefHeaderRow(uiState, vm::openStationList)
            if (isLandscape) {
                LandscapeContent(uiState, vm::prevStation, vm::nextStation, vm::togglePlayback, vm::openMetadataEditor, Modifier.weight(1f))
            } else {
                PortraitContent(uiState, vm::prevStation, vm::nextStation, vm::togglePlayback, vm::openMetadataEditor, Modifier.weight(1f))
            }
        }

        uiState.errorMessage?.let { msg ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp, start = 12.dp, end = 12.dp),
                action = {
                    TextButton(onClick = vm::clearError) {
                        Text("OK", color = AccentTeal, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    }
                },
                containerColor = Color(0xFF2A0A10),
                contentColor = SignalRed
            ) {
                Text(msg, fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = SignalRed)
            }
        }
    }

    val editStation = uiState.currentStation
    if (uiState.showMetadataEditor && editStation != null) {
        MetadataEditorDialog(
            station    = editStation,
            currentPs  = uiState.customPs,
            currentPty = uiState.customPty,
            currentRt  = uiState.customRt,
            onSave     = vm::saveStationMetadata,
            onClear    = vm::clearStationMetadata,
            onDismiss  = vm::closeMetadataEditor
        )
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

// ─── PORTRAIT ─────────────────────────────────────────────────────────────────

@Composable
private fun PortraitContent(
    uiState: RadioUiState,
    onPrev: () -> Unit, onNext: () -> Unit, onPlayStop: () -> Unit, onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        TefMainDisplay(uiState, onPrev, onNext, onPlayStop, Modifier.weight(1f), onEdit = onEdit)
        TefInfoBoxesRow(uiState)
        TefMetersRow(uiState)
        TefRdsDataRow(uiState)
        TefPsRow(uiState, onEdit)
        TefRtFooter(uiState, onEdit)
    }
}

// ─── LANDSCAPE ────────────────────────────────────────────────────────────────

@Composable
private fun LandscapeContent(
    uiState: RadioUiState,
    onPrev: () -> Unit, onNext: () -> Unit, onPlayStop: () -> Unit, onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.weight(1.4f).fillMaxHeight()) {
            TefMainDisplay(uiState, onPrev, onNext, onPlayStop, Modifier.weight(1f), onEdit = onEdit)
            TefInfoBoxesRow(uiState)
        }
        Box(Modifier.width(1.dp).fillMaxHeight().background(BorderColor))
        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
            TefMetersRow(uiState)
            TefRdsDataRow(uiState)
            TefPsRow(uiState, onEdit)
            TefRtFooter(uiState, onEdit)
            Spacer(Modifier.weight(1f))
        }
    }
}

// ─── HEADER ───────────────────────────────────────────────────────────────────

@Composable
private fun TefHeaderRow(uiState: RadioUiState, onOpenList: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "rds")
    val rdsAlpha by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "rdsAlpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PanelBg)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Stereo indicator
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Box(Modifier.size(9.dp).clip(CircleShape).background(if (uiState.isStereo) AccentTeal else GreyOutColor))
            Box(Modifier.size(9.dp).clip(CircleShape).background(if (uiState.isStereo) AccentTeal else GreyOutColor))
        }
        Text(
            text = if (uiState.isStereo) "ST" else "MO",
            color = if (uiState.isStereo) AccentTeal else DarkGreyColor,
            fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold
        )

        // RDS badge
        RdsPill(label = "RDS", active = uiState.hasRdsData, activeColor = AccentTeal, alpha = rdsAlpha)
        RdsPill(label = "TP",  active = false, activeColor = OrangeColor)
        RdsPill(label = "TA",  active = false, activeColor = SignalRed)

        Spacer(Modifier.weight(1f))

        // Bitrate
        val bitrate = uiState.currentStation?.bitrate ?: 0
        Text(
            text = if (bitrate > 0) "${bitrate}k" else "---",
            color = CyanColor, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.width(4.dp))
        // Codec
        Text(
            text = uiState.currentStation?.displayCodec ?: "···",
            color = DimWhite, fontSize = 11.sp, fontFamily = FontFamily.Monospace
        )
        Spacer(Modifier.width(4.dp))
        HeaderBtn("FM", true)
        Spacer(Modifier.width(2.dp))
        HeaderBtn("BW", uiState.isStreamActive)
        Spacer(Modifier.width(2.dp))
        HeaderBtn("AGC", uiState.isPlaying)
        Spacer(Modifier.width(6.dp))
        // Station list button — in header so it never overlaps content
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(AccentTeal.copy(alpha = 0.12f))
                .border(1.dp, AccentTeal.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                .clickable(onClick = onOpenList)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Icon(Icons.Default.List, null, tint = AccentTeal, modifier = Modifier.size(13.dp))
                Text("LIST", color = AccentTeal, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun RdsPill(label: String, active: Boolean, activeColor: Color, alpha: Float = 1f) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (active) activeColor.copy(alpha = 0.15f) else GreyOutColor.copy(alpha = 0.5f))
            .border(1.dp, if (active) activeColor.copy(alpha = alpha) else DarkGreyColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            color = if (active) activeColor.copy(alpha = alpha) else DarkGreyColor,
            fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun HeaderBtn(label: String, active: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (active) AccentTeal.copy(alpha = 0.12f) else Color.Transparent)
            .border(1.dp, if (active) AccentTeal.copy(alpha = 0.6f) else DarkGreyColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            color = if (active) AccentTeal else DarkGreyColor,
            fontSize = 10.sp, fontFamily = FontFamily.Monospace
        )
    }
}

// ─── MAIN DISPLAY ─────────────────────────────────────────────────────────────

@Composable
private fun TefMainDisplay(
    uiState: RadioUiState,
    onPrev: () -> Unit, onNext: () -> Unit, onPlayStop: () -> Unit,
    modifier: Modifier = Modifier,
    onEdit: (() -> Unit)? = null
) {
    val signalDb = uiState.signalLevel * 95f

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(PanelBg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Favicon
        Box(
            modifier = Modifier
                .padding(start = 10.dp)
                .size(56.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(PanelBg2)
                .border(1.dp, BorderColor, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            val favicon = uiState.currentStation?.favicon
            if (!favicon.isNullOrBlank()) {
                AsyncImage(
                    model = favicon, contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Fit
                )
            } else {
                Icon(Icons.Default.Radio, null, tint = AccentTeal.copy(alpha = 0.35f), modifier = Modifier.size(28.dp))
            }
        }

        // Prev
        IconButton(onClick = onPrev, modifier = Modifier.size(38.dp)) {
            Icon(Icons.Default.SkipPrevious, null, tint = AccentTeal.copy(alpha = 0.7f), modifier = Modifier.size(26.dp))
        }

        // Center: freq + PS + play
        Column(
            modifier = Modifier.weight(1f).padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val station = uiState.currentStation
            val freq = station?.extractedFrequency

            // Frequency
            if (freq != null) {
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Center) {
                    Text(
                        text = freq,
                        color = AccentTeal,
                        fontSize = 36.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "MHz",
                        color = AccentTeal.copy(alpha = 0.55f),
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            } else {
                Text(
                    text = station?.name?.take(14)?.uppercase() ?: "WEB RADIO",
                    color = AccentTeal.copy(alpha = 0.7f),
                    fontSize = if ((station?.name?.length ?: 0) > 8) 18.sp else 28.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(2.dp))
            }

            // PS name — custom overrides station name; padded to 8 chars, yellow, large
            val psRaw = (uiState.customPs.ifBlank { station?.name ?: "--------" }).take(8).uppercase().padEnd(8)
            Text(
                text = psRaw,
                color = if (station != null) FrequencyYellow else DarkGreyColor,
                fontSize = 22.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp,
                textAlign = TextAlign.Center
            )

            // Full name if longer
            if ((station?.name?.length ?: 0) > 8) {
                Text(
                    text = station!!.name,
                    color = DimWhite,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )
            }

            Spacer(Modifier.height(6.dp))

            // Play/stop + buffering + edit button
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (uiState.isPlaying) AccentTeal.copy(alpha = 0.15f) else OrangeColor.copy(alpha = 0.12f))
                        .border(1.5.dp, if (uiState.isPlaying) AccentTeal else OrangeColor, CircleShape)
                        .clickable(onClick = onPlayStop),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (uiState.isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = if (uiState.isPlaying) AccentTeal else OrangeColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                if (uiState.isBuffering) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        color = CyanColor,
                        strokeWidth = 1.5.dp
                    )
                }
                // Edit RDS metadata button
                if (uiState.currentStation != null && onEdit != null) {
                    val hasCustom = uiState.customPs.isNotBlank() || uiState.customPty.isNotBlank() || uiState.customRt.isNotBlank()
                    val btnColor = if (hasCustom) FrequencyYellow else AccentTeal
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(btnColor.copy(alpha = 0.12f))
                            .border(1.dp, btnColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .clickable(onClick = onEdit)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Edit, null, tint = btnColor, modifier = Modifier.size(14.dp))
                            Text("RDS", color = btnColor, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Next
        IconButton(onClick = onNext, modifier = Modifier.size(38.dp)) {
            Icon(Icons.Default.SkipNext, null, tint = AccentTeal.copy(alpha = 0.7f), modifier = Modifier.size(26.dp))
        }

        // dBuV column
        val dbColor = when {
            signalDb > 65 -> SignalGreen
            signalDb > 40 -> FrequencyYellow
            signalDb > 20 -> OrangeColor
            else -> SignalRed
        }
        Column(
            modifier = Modifier.padding(end = 10.dp).width(52.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "%4.1f".format(signalDb),
                color = dbColor,
                fontSize = 26.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text("dBµV", color = DimWhite, fontSize = 10.sp, fontFamily = FontFamily.Monospace, textAlign = TextAlign.Center)
        }
    }
}

// ─── INFO BOXES ───────────────────────────────────────────────────────────────

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
            .background(BackgroundBlack)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        InfoBox("PI CODE",  uiState.currentStation?.piCode ?: "----",  FrequencyYellow, Modifier.weight(1f))
        InfoBox("BITRATE",  if (bitrate > 0) "${bitrate}k" else "---", CyanColor,       Modifier.weight(1f))
        InfoBox("SIGNAL",   "%4.1f".format(signalDb),                  dbColor,         Modifier.weight(1f))
        InfoBox("COUNTRY",  uiState.currentStation?.displayCountry ?: "--", DimWhite,   Modifier.weight(1f))
    }
}

@Composable
private fun InfoBox(label: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(PanelBg)
            .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, color = DarkGreyColor, fontSize = 9.sp, fontFamily = FontFamily.Monospace, letterSpacing = 0.5.sp)
        Text(value, color = valueColor, fontSize = 15.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

// ─── SIGNAL METERS ────────────────────────────────────────────────────────────

@Composable
private fun TefMetersRow(uiState: RadioUiState) {
    val animSignal by animateFloatAsState(uiState.signalLevel, tween(280), label = "sig")
    val animQuality by animateFloatAsState(uiState.qualityLevel, tween(280), label = "qual")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PanelBg)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        MeterBar(label = "S", value = animSignal, ticks = listOf("1","3","5","7","9","+10","+30"), redFraction = 0.57f)
        MeterBar(label = "M", value = animQuality, ticks = listOf("0","20","40","60","80","100","120%"), redFraction = 0.68f, suffix = if ((uiState.currentStation?.bitrate ?: 0) > 0) "${uiState.currentStation!!.bitrate}k" else "")
    }
}

@Composable
private fun MeterBar(label: String, value: Float, ticks: List<String>, redFraction: Float, suffix: String = "") {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 18.dp, end = if (suffix.isNotBlank()) 32.dp else 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ticks.forEach { tick ->
                Text(tick, color = if (tick.startsWith("+") || tick == "120%") SignalRed else DimWhite, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            }
        }
        Row(modifier = Modifier.fillMaxWidth().height(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = WhiteColor, fontSize = 11.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.width(18.dp))
            Canvas(modifier = Modifier.weight(1f).fillMaxHeight().padding(vertical = 2.dp)) {
                val total = 50
                val segW = (size.width - (total - 1) * 1f) / total
                val filled = (value * total).toInt().coerceIn(0, total)
                val greenCount = (total * redFraction).toInt()
                for (i in 0 until total) {
                    val col = when {
                        i >= filled -> GreyOutColor
                        i < greenCount -> AccentTeal
                        else -> SignalRed
                    }
                    drawRect(col, Offset(i * (segW + 1f), 0f), Size(segW, size.height))
                }
            }
            if (suffix.isNotBlank()) {
                Text(suffix, color = CyanColor, fontSize = 10.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.width(30.dp), textAlign = TextAlign.End)
            }
        }
    }
}

// ─── RDS DATA ROW ─────────────────────────────────────────────────────────────

@Composable
private fun TefRdsDataRow(uiState: RadioUiState) {
    var clockStr by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        while (true) { clockStr = sdf.format(Date()); delay(1000) }
    }
    val snr = (uiState.signalLevel * 68f).toInt()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundBlack)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val ptyDisplay = uiState.customPty.ifBlank { uiState.currentStation?.displayTags?.take(12) ?: "" }.ifBlank { "---" }
        RdsField("PTY", ptyDisplay, FrequencyYellow, Modifier.weight(1f))
        RdsField("ART", uiState.rtArtist.take(16).ifBlank { "---" }, AccentTeal, Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End) {
            Text(clockStr, color = CyanColor, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
            Text("S/N %2ddB".format(snr), color = DimWhite, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
private fun RdsField(label: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(label, color = DarkGreyColor, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        Text(value, color = valueColor, fontSize = 12.sp, fontFamily = FontFamily.Monospace, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

// ─── PS ROW ───────────────────────────────────────────────────────────────────

@Composable
private fun TefPsRow(uiState: RadioUiState, onEdit: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PanelBg)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // PS label
        Text("PS", color = DarkGreyColor, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)

        // PS cells fill available space between "PS" label and PI box
        BoxWithConstraints(modifier = Modifier.weight(1f)) {
            val cellW = 18.dp
            val cellGap = 2.dp
            val cellWPx = with(LocalDensity.current) { cellW.toPx() }
            val cellGapPx = with(LocalDensity.current) { cellGap.toPx() }
            val numCells = ((constraints.maxWidth + cellGapPx) / (cellWPx + cellGapPx))
                .toInt().coerceAtLeast(1)

            val psSource = (uiState.customPs.ifBlank { uiState.currentStation?.name ?: "" }).uppercase()
            val shouldScroll = psSource.length > numCells
            // Pad with numCells spaces as gap; totalLen always > 0
            val scrollText = "$psSource" + " ".repeat(numCells.coerceAtLeast(8))
            val totalLen = scrollText.length

            val infiniteScroll = rememberInfiniteTransition(label = "ps_scroll")
            val rawOffset by infiniteScroll.animateFloat(
                initialValue = 0f,
                targetValue = totalLen.toFloat(),
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = totalLen * 280, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "ps_offset"
            )
            val offset = if (shouldScroll) rawOffset.toInt() % totalLen else 0

            val psChars = if (shouldScroll) {
                val doubled = scrollText + scrollText
                doubled.substring(offset, offset + numCells)
            } else {
                psSource.take(numCells).padEnd(numCells)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(cellGap)) {
                psChars.forEach { ch ->
                    Box(
                        modifier = Modifier
                            .size(width = cellW, height = 26.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(PanelBg2)
                            .border(1.dp, BorderColor, RoundedCornerShape(3.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = ch.toString(),
                            color = if (uiState.currentStation != null) FrequencyYellow else DarkGreyColor,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // PI code
        Text("PI", color = DarkGreyColor, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(5.dp))
                .background(PanelBg2)
                .border(1.dp, AccentTeal.copy(alpha = 0.4f), RoundedCornerShape(5.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = uiState.currentStation?.piCode ?: "----",
                color = AccentTeal,
                fontSize = 15.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ─── RT FOOTER ────────────────────────────────────────────────────────────────

@Composable
private fun TefRtFooter(uiState: RadioUiState, onEdit: () -> Unit) {
    val rtText = when {
        uiState.customRt.isNotBlank() -> uiState.customRt.uppercase()
        uiState.rtTitle.isNotBlank() -> uiState.rtTitle.uppercase()
        uiState.nowPlaying.isNotBlank() -> uiState.nowPlaying.uppercase()
        uiState.currentStation != null -> uiState.currentStation.displayTags.uppercase().ifBlank { "RADIO ONLINE" }
        else -> "AGUARDANDO ESTACAO..."
    }

    val infiniteTransition = rememberInfiniteTransition(label = "rt")
    val offsetAnim by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(rtText.length * 160, easing = LinearEasing), RepeatMode.Restart),
        label = "scroll"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(PanelBg2)
            .padding(vertical = 1.dp)
            .background(AccentTeal.copy(alpha = 0.06f))
            .height(30.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        val padStart = (1f - offsetAnim) * 320f
        Text(
            text = "  $rtText  ·  $rtText  ",
            color = AccentTeal,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier.padding(start = padStart.dp).fillMaxWidth(),
            overflow = TextOverflow.Clip
        )
    }
}
