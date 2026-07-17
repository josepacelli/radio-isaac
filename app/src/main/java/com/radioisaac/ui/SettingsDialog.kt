package com.radioisaac.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.radioisaac.ui.theme.AccentTeal
import com.radioisaac.ui.theme.BorderColor
import com.radioisaac.ui.theme.DarkGreyColor
import com.radioisaac.ui.theme.DimWhite
import com.radioisaac.ui.theme.FrequencyYellow
import com.radioisaac.ui.theme.PanelBg
import com.radioisaac.ui.theme.PanelBg2
import com.radioisaac.ui.theme.SignalRed
import com.radioisaac.ui.theme.WhiteColor

@Composable
fun SettingsDialog(
    fingerprintEnabled: Boolean,
    auddToken: String,
    onSave: (enabled: Boolean, token: String) -> Unit,
    onDismiss: () -> Unit
) {
    var enabled by remember { mutableStateOf(fingerprintEnabled) }
    var token by remember { mutableStateOf(auddToken) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(PanelBg)
                .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "CONFIGURAÇÕES",
                color = AccentTeal,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            HorizontalDivider(color = BorderColor)

            // Fingerprint toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Identificação de músicas",
                        color = WhiteColor,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Via AudD quando sem metadados ICY",
                        color = DimWhite,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = { enabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AccentTeal,
                        checkedTrackColor = AccentTeal.copy(alpha = 0.3f),
                        uncheckedThumbColor = DarkGreyColor,
                        uncheckedTrackColor = PanelBg2
                    )
                )
            }

            // AudD token field
            if (enabled) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "TOKEN AUDD.IO",
                        color = DimWhite,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    OutlinedTextField(
                        value = token,
                        onValueChange = { token = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text("test", color = DarkGreyColor, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        },
                        textStyle = TextStyle(
                            color = FrequencyYellow,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentTeal,
                            unfocusedBorderColor = BorderColor
                        ),
                        singleLine = true
                    )
                    Text(
                        "Token gratuito em audd.io (1.000 req/mês)",
                        color = DarkGreyColor,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(Modifier.height(4.dp))
            HorizontalDivider(color = BorderColor)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SignalRed)
                ) {
                    Text("CANCELAR", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
                Button(
                    onClick = { onSave(enabled, token.trim().ifBlank { "test" }) },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentTeal.copy(alpha = 0.2f), contentColor = AccentTeal)
                ) {
                    Text("SALVAR", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}
