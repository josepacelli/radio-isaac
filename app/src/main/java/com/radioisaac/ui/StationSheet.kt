package com.radioisaac.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.radioisaac.data.BrazilRegion
import com.radioisaac.data.RadioStation
import com.radioisaac.ui.theme.*
import com.radioisaac.viewmodel.RadioUiState

private val BRAZIL_REGIONS = listOf(
    BrazilRegion("Fortaleza",          "Ceará",               "CE", "Fortaleza"),
    BrazilRegion("São Paulo",          "São Paulo",            "SP", "São Paulo"),
    BrazilRegion("Campinas",           "São Paulo",            "SP", "Campinas"),
    BrazilRegion("Rio de Janeiro",     "Rio de Janeiro",       "RJ", "Rio de Janeiro"),
    BrazilRegion("Minas Gerais",       "Minas Gerais",         "MG", "Belo Horizonte"),
    BrazilRegion("Bahia",              "Bahia",                "BA", "Salvador"),
    BrazilRegion("Pernambuco",         "Pernambuco",           "PE", "Recife"),
    BrazilRegion("Porto Alegre",       "Rio Grande do Sul",    "RS", "Porto Alegre"),
    BrazilRegion("Curitiba",           "Paraná",               "PR", "Curitiba"),
    BrazilRegion("Florianópolis",      "Santa Catarina",       "SC", "Florianópolis"),
    BrazilRegion("Goiânia",            "Goiás",                "GO", "Goiânia"),
    BrazilRegion("São Luís",           "Maranhão",             "MA", "São Luís"),
    BrazilRegion("Belém",              "Pará",                 "PA", "Belém"),
    BrazilRegion("Manaus",             "Amazonas",             "AM", "Manaus"),
    BrazilRegion("Vitória",            "Espírito Santo",       "ES", "Vitória"),
    BrazilRegion("Natal",              "Rio Grande do Norte",  "RN", "Natal"),
    BrazilRegion("Campina Grande",     "Paraíba",              "PB", "Campina Grande"),
    BrazilRegion("Maceió",             "Alagoas",              "AL", "Maceió"),
    BrazilRegion("Aracaju",            "Sergipe",              "SE", "Aracaju"),
    BrazilRegion("Campo Grande",       "Mato Grosso do Sul",   "MS", "Campo Grande"),
    BrazilRegion("Mato Grosso",        "Mato Grosso",          "MT", "Cuiabá"),
    BrazilRegion("Mato Grosso do Sul", "Mato Grosso do Sul",   "MS", "Campo Grande"),
    BrazilRegion("Tocantins",          "Tocantins",            "TO", "Palmas"),
    BrazilRegion("Acre",               "Acre",                 "AC", "Rio Branco"),
    BrazilRegion("Brasília",           "Distrito Federal",     "DF", "Brasília")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationListSheet(
    uiState: RadioUiState,
    onDismiss: () -> Unit,
    onSelect: (RadioStation) -> Unit,
    onSearch: (String) -> Unit,
    onLoadTop: () -> Unit,
    onLoadRegion: (BrazilRegion) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = BackgroundBlack,
        scrimColor = Color.Black.copy(alpha = 0.7f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .size(36.dp, 3.dp)
                        .background(BorderBlue, RoundedCornerShape(2.dp))
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.93f)
                .border(1.dp, BorderBlue)
        ) {
            // Header bar (like "PRESS MODE TO EXIT" header in original)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF000022))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STATION SELECTOR",
                    color = SkyBlueColor,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${uiState.stations.size} estacoes",
                    color = DarkGreyColor,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            HorizontalDivider(color = BorderBlue, thickness = 1.dp)

            // Search field
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearch,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                placeholder = {
                    Text("Buscar estacoes...", color = GreyOutColor, fontFamily = FontFamily.Monospace, fontSize = 15.sp)
                },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = SkyBlueColor, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearch("") }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Clear, null, tint = DarkGreyColor, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BorderBlue,
                    unfocusedBorderColor = DarkGreyColor,
                    cursorColor = FrequencyYellow,
                    focusedTextColor = WhiteColor,
                    unfocusedTextColor = WhiteColor
                ),
                singleLine = true,
                textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 16.sp)
            )

            // Brazilian state chips — only when not searching
            if (uiState.searchQuery.isEmpty()) {
                // "BRASIL" top chip
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    item {
                        val selected = uiState.selectedCategory == "BR"
                        FilterChip(
                            selected = selected,
                            onClick = onLoadTop,
                            label = { Text("BRASIL", fontSize = 15.sp, fontFamily = FontFamily.Monospace) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BorderBlue,
                                selectedLabelColor = FrequencyYellow,
                                containerColor = BackgroundBlack,
                                labelColor = DarkGreyColor
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                selectedBorderColor = BorderBlue,
                                borderColor = DarkGreyColor,
                                borderWidth = 1.dp,
                                selectedBorderWidth = 1.dp
                            )
                        )
                    }
                    items(BRAZIL_REGIONS.filter { it.display !in uiState.emptyStates }) { region ->
                        val selected = uiState.selectedCategory == region.display
                        FilterChip(
                            selected = selected,
                            onClick = { onLoadRegion(region) },
                            label = { Text(region.display, fontSize = 15.sp, fontFamily = FontFamily.Monospace) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BorderBlue,
                                selectedLabelColor = FrequencyYellow,
                                containerColor = BackgroundBlack,
                                labelColor = DarkGreyColor
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                selectedBorderColor = BorderBlue,
                                borderColor = DarkGreyColor,
                                borderWidth = 1.dp,
                                selectedBorderWidth = 1.dp
                            )
                        )
                    }
                }
            }

            HorizontalDivider(color = BorderBlue, thickness = 1.dp)

            val displayStations = if (uiState.searchQuery.length >= 2) uiState.searchResults else uiState.stations

            if (uiState.isLoading) {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        CircularProgressIndicator(color = FrequencyYellow, strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
                        Text("CARREGANDO...", color = CyanColor, fontFamily = FontFamily.Monospace, fontSize = 14.sp, letterSpacing = 2.sp)
                    }
                }
            } else if (displayStations.isEmpty()) {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        if (uiState.searchQuery.length >= 2) "Nenhuma estacao encontrada" else "Nenhuma estacao",
                        color = DarkGreyColor,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 15.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(displayStations, key = { it.uuid }) { station ->
                        StationItem(
                            station = station,
                            isCurrent = station.uuid == uiState.currentStation?.uuid,
                            onClick = { onSelect(station) }
                        )
                        HorizontalDivider(color = GreyOutColor, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun StationItem(station: RadioStation, isCurrent: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isCurrent) Color(0xFF001500) else BackgroundBlack)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Favicon box (like PS display box in original)
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(GreyOutColor)
                .border(1.dp, if (isCurrent) SignalGreen else DarkGreyColor, RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (station.favicon.isNotBlank()) {
                AsyncImage(
                    model = station.favicon,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Fit
                )
            } else {
                Icon(Icons.Default.Radio, null, tint = SkyBlueColor, modifier = Modifier.size(20.dp))
            }
        }

        Spacer(Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = station.name,
                color = if (isCurrent) SignalGreen else FrequencyYellow,
                fontSize = 16.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row {
                Text(
                    text = "${station.displayCodec} ${station.displayBitrate}k",
                    color = SkyBlueColor,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Monospace
                )
                if (station.displayTags.isNotBlank()) {
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = station.displayTags.take(24),
                        color = DarkGreyColor,
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(Modifier.width(6.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "▲${station.votes}",
                color = CyanColor,
                fontSize = 16.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = station.displayCountry,
                color = WhiteColor,
                fontSize = 16.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
