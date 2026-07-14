package com.radioisaac.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.text.Normalizer

// Estações adicionadas manualmente (não estão no RadioBrowser)
val PINNED_STATIONS = listOf(
    RadioStation(
        uuid        = "pinned-forbes-sp-001",
        name        = "Forbes Radio SP 106.1 FM",
        streamUrl   = "https://9176.brasilstream.com.br/stream",
        favicon     = "https://forbes.com.br/wp-content/uploads/2020/08/forbes-icon.png",
        tags        = "negocios,economia,noticias",
        country     = "Brazil",
        countryCode = "BR",
        codec       = "AAC",
        bitrate     = 64,
        votes       = 9999,
        language    = "portuguese"
    )
)

class RadioRepository {
    private val api = RadioApiClient.api
    private val rgApi = RadioGardenClient.api

    private var rgBrazilPlaces: List<RgPlace>? = null

    private suspend fun getBrazilPlaces(): List<RgPlace> {
        rgBrazilPlaces?.let { return it }
        return try {
            val places = rgApi.getPlaces().data?.list
                ?.filter { it.country == "Brazil" && it.size > 0 }
                ?: emptyList()
            rgBrazilPlaces = places
            places
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun fetchRgStations(places: List<RgPlace>): List<RadioStation> {
        if (places.isEmpty()) return emptyList()
        return coroutineScope {
            places
                .sortedByDescending { it.size }
                .take(50)
                .map { place ->
                    async(Dispatchers.IO) {
                        try {
                            rgApi.getChannels(place.id)
                                .data?.content?.firstOrNull()?.items
                                ?.mapNotNull { it.page?.toRadioStation() }
                                ?: emptyList()
                        } catch (e: Exception) {
                            emptyList<RadioStation>()
                        }
                    }
                }
                .awaitAll()
                .flatten()
        }
    }

    suspend fun getByCountry(code: String): Result<List<RadioStation>> = withContext(Dispatchers.IO) {
        runCatching {
            coroutineScope {
                // Different orderings expose different stations after dedup
                val byVotes   = async { runCatching { api.getByCountry(code) }.getOrDefault(emptyList()) }
                val byClicks  = async { runCatching { api.getByCountryOrder(code, order = "clickcount") }.getOrDefault(emptyList()) }
                val byTrend   = async { runCatching { api.getByCountryOrder(code, order = "clicktrend") }.getOrDefault(emptyList()) }
                val byLang    = async { runCatching { api.getByLanguage("portuguese") }.getOrDefault(emptyList()) }
                val byName    = async { runCatching { api.searchByCountryName("Brazil") }.getOrDefault(emptyList()) }
                val byTagBr   = async { runCatching { api.getByTag("brasil") }.getOrDefault(emptyList()) }
                val byTagEn   = async { runCatching { api.getByTag("brazil") }.getOrDefault(emptyList()) }
                val rgTop     = async {
                    try { fetchRgStations(getBrazilPlaces().sortedByDescending { it.size }.take(10)) }
                    catch (e: Exception) { emptyList() }
                }
                val trAll     = async { TudoRadioClient.fetchAll() }
                val pinnedIds = PINNED_STATIONS.map { it.uuid }.toSet()
                (PINNED_STATIONS +
                 (byVotes.await() + byClicks.await() + byTrend.await() + byLang.await() +
                  byName.await() + byTagBr.await() + byTagEn.await() + rgTop.await() + trAll.await())
                    .filter { it.effectiveStreamUrl.isNotBlank() && it.uuid !in pinnedIds }
                    .distinctBy { it.uuid }
                    .sortedByDescending { it.votes })
            }
        }
    }

    suspend fun searchStations(query: String): Result<List<RadioStation>> = withContext(Dispatchers.IO) {
        runCatching { api.searchStations(query).filter { it.effectiveStreamUrl.isNotBlank() } }
    }

    suspend fun getByRegion(region: BrazilRegion): Result<List<RadioStation>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val stripped = region.stateName.removeAccents()
                coroutineScope {
                    // RadioBrowser: bystate with accent + no-accent in parallel
                    val rbAccented = async {
                        runCatching {
                            api.getByState(state = region.stateName)
                                .filter { it.effectiveStreamUrl.isNotBlank() }
                        }.getOrDefault(emptyList())
                    }
                    val rbStripped = async {
                        if (stripped == region.stateName) emptyList()
                        else runCatching {
                            api.getByState(state = stripped)
                                .filter { it.effectiveStreamUrl.isNotBlank() }
                        }.getOrDefault(emptyList())
                    }

                    // RadioBrowser: /search?state= (partial match, catches different spellings)
                    val rbSearch = async {
                        runCatching {
                            api.searchByStateAndCountry(state = region.stateName)
                                .filter { it.effectiveStreamUrl.isNotBlank() }
                        }.getOrDefault(emptyList())
                    }

                    // RadioBrowser: search by city name (some stations store city in state field)
                    val rbCity = async {
                        val city = region.rgCity ?: return@async emptyList()
                        val byStatePath = runCatching {
                            api.getByState(state = city).filter { it.effectiveStreamUrl.isNotBlank() }
                        }.getOrDefault(emptyList())
                        val bySearch = runCatching {
                            api.searchByStateAndCountry(state = city).filter { it.effectiveStreamUrl.isNotBlank() }
                        }.getOrDefault(emptyList())
                        (byStatePath + bySearch).distinctBy { it.uuid }
                    }

                    // radio.garden: by state code suffix + city name (both)
                    val rgAsync = async {
                        try {
                            val places = getBrazilPlaces()
                            val byCode = places.filter { p ->
                                p.title.endsWith(" ${region.stateCode}") ||
                                p.title.contains(", ${region.stateCode}")
                            }
                            val byCity = region.rgCity?.let { city ->
                                val normalizedCity = city.removeAccents().lowercase()
                                places.filter { p ->
                                    p.title.removeAccents().lowercase().contains(normalizedCity)
                                }
                            } ?: emptyList()
                            val statePlaces = (byCode + byCity).distinctBy { it.id }
                            fetchRgStations(statePlaces)
                        } catch (e: Exception) {
                            emptyList()
                        }
                    }

                    val trState = async { TudoRadioClient.fetchByState(region.stateCode) }
                    val rb = (rbAccented.await() + rbStripped.await() + rbSearch.await() + rbCity.await()).distinctBy { it.uuid }
                    val rg = rgAsync.await()
                    val tr = trState.await()
                    (rb + rg + tr).distinctBy { it.uuid }.sortedByDescending { it.votes }
                }
            }
        }

    private fun String.removeAccents(): String =
        Normalizer.normalize(this, Normalizer.Form.NFD)
            .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
}
