package com.radioisaac.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.radioisaac.data.RadioStation
import com.radioisaac.ui.theme.AccentTeal
import com.radioisaac.ui.theme.BorderColor
import com.radioisaac.ui.theme.DarkGreyColor
import com.radioisaac.ui.theme.DimWhite
import com.radioisaac.ui.theme.FrequencyYellow
import com.radioisaac.ui.theme.PanelBg
import com.radioisaac.ui.theme.PanelBg2
import com.radioisaac.ui.theme.SignalRed
import com.radioisaac.ui.theme.WhiteColor

// Standard RDS PTY codes (European — used worldwide)
val RDS_PTY = listOf(
     0 to "0 – Nenhum",
     1 to "1 – Notícias",
     2 to "2 – Assuntos Atuais",
     3 to "3 – Informação",
     4 to "4 – Esporte",
     5 to "5 – Educação",
     6 to "6 – Drama",
     7 to "7 – Cultura",
     8 to "8 – Ciência",
     9 to "9 – Variedades",
    10 to "10 – Pop",
    11 to "11 – Rock",
    12 to "12 – Easy Listening",
    13 to "13 – Clássico Leve",
    14 to "14 – Clássico Sério",
    15 to "15 – Outra Música",
    16 to "16 – Clima/Meteorologia",
    17 to "17 – Finanças",
    18 to "18 – Infantil",
    19 to "19 – Assuntos Sociais",
    20 to "20 – Religião",
    21 to "21 – Phone-In",
    22 to "22 – Viagem & Turismo",
    23 to "23 – Lazer & Hobby",
    24 to "24 – Jazz",
    25 to "25 – Country",
    26 to "26 – Música Nacional",
    27 to "27 – Oldies",
    28 to "28 – Folk",
    29 to "29 – Documentário",
    30 to "30 – Teste de Alarme",
    31 to "31 – Alarme"
)

@Composable
fun MetadataEditorDialog(
    station: RadioStation,
    currentPs: String,
    currentPty: String,
    currentRt: String,
    onSave: (ps: String, pty: String, rt: String) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    var ps  by remember { mutableStateOf(currentPs) }
    var pty by remember { mutableStateOf(currentPty) }
    var rt  by remember { mutableStateOf(currentRt) }
    var showPtyPicker by remember { mutableStateOf(false) }

    if (showPtyPicker) {
        PtyPickerDialog(
            current = pty,
            onPick = { pty = it; showPtyPicker = false },
            onDismiss = { showPtyPicker = false }
        )
        return
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(PanelBg)
                .border(1.dp, AccentTeal.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Title
            Column {
                Text("EDITOR RDS", color = AccentTeal, fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Text(station.name, color = DimWhite, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            }

            HorizontalDivider(color = BorderColor)

            // PS field — 255 chars, scrolls in display when > 8
            MetaField(
                label = "PS  (Programme Service — exibe rolando se > 8 chars)",
                value = ps,
                onValueChange = { if (it.length <= 255) ps = it.uppercase() },
                hint = "Ex: JOVEM PAN FM 98.5",
                maxLen = 255
            )

            // PTY — dropdown selector
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("PTY  (Programme Type — padrão RDS mundial)",
                    color = DarkGreyColor, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(PanelBg2)
                        .border(1.dp, AccentTeal.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .clickable { showPtyPicker = true }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = pty.ifBlank { "Selecionar PTY..." },
                            color = if (pty.isBlank()) DarkGreyColor else FrequencyYellow,
                            fontSize = 14.sp, fontFamily = FontFamily.Monospace,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.ArrowDropDown, null, tint = AccentTeal, modifier = Modifier.size(20.dp))
                    }
                }
            }

            // RT field — 255 chars, scrolls in display
            MetaField(
                label = "RT  (Radio Text — artista · música · informação)",
                value = rt,
                onValueChange = { if (it.length <= 255) rt = it },
                hint = "Ex: Artista - Nome da Música",
                maxLen = 255,
                singleLine = false
            )

            HorizontalDivider(color = BorderColor)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onClear, modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SignalRed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SignalRed.copy(alpha = 0.5f))
                ) { Text("LIMPAR", fontSize = 11.sp, fontFamily = FontFamily.Monospace) }

                OutlinedButton(
                    onClick = onDismiss, modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DimWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
                ) { Text("CANCELAR", fontSize = 11.sp, fontFamily = FontFamily.Monospace) }

                Button(
                    onClick = { onSave(ps.trim(), pty.trim(), rt.trim()) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentTeal.copy(alpha = 0.2f), contentColor = AccentTeal),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("SALVAR", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
private fun PtyPickerDialog(current: String, onPick: (String) -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .clip(RoundedCornerShape(14.dp))
                .background(PanelBg)
                .border(1.dp, AccentTeal.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().background(PanelBg2).padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text("SELECIONAR PTY", color = AccentTeal, fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            }
            HorizontalDivider(color = BorderColor)
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(RDS_PTY) { (_, label) ->
                    val selected = label == current || label.substringAfter("– ") == current
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (selected) AccentTeal.copy(alpha = 0.1f) else PanelBg)
                            .clickable { onPick(label.substringAfter("– ")) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label,
                            color = if (selected) AccentTeal else WhiteColor,
                            fontSize = 14.sp, fontFamily = FontFamily.Monospace,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                    HorizontalDivider(color = BorderColor, thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
private fun MetaField(
    label: String, value: String, onValueChange: (String) -> Unit,
    hint: String, maxLen: Int = 255, singleLine: Boolean = true
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = DarkGreyColor, fontSize = 10.sp, fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(1f))
            Text("${value.length}/$maxLen",
                color = if (value.length >= maxLen) SignalRed else DarkGreyColor,
                fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(hint, color = DarkGreyColor, fontFamily = FontFamily.Monospace, fontSize = 13.sp) },
            singleLine = singleLine,
            minLines = if (singleLine) 1 else 2,
            maxLines = if (singleLine) 1 else 4,
            textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 14.sp, color = FrequencyYellow),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentTeal,
                unfocusedBorderColor = BorderColor,
                cursorColor = AccentTeal,
                focusedTextColor = FrequencyYellow,
                unfocusedTextColor = FrequencyYellow
            )
        )
    }
}
