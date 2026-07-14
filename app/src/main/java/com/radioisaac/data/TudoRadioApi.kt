package com.radioisaac.data

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

// ── GraphQL request/response models ──────────────────────────────────────────

data class TrRequest(val query: String)

data class TrResponse(val data: TrData?)
data class TrData(val radios: List<TrStation>?)

data class TrStation(
    val id: String = "",
    val nome: String? = null,
    val streaming: String? = null,
    @SerializedName("streaming_android") val streamingAndroid: String? = null,
    val views: Int? = null,
    @SerializedName("logo_nova") val logoNova: TrLogoNova? = null,
    val idcidades: TrCity? = null,
    val idestados: TrState? = null
)

data class TrLogoNova(val id: String? = null)
data class TrCity(val nome: String? = null)
data class TrState(val nome: String? = null, val sigla: String? = null)

fun TrStation.toRadioStation(): RadioStation? {
    val url = (streamingAndroid ?: streaming)?.takeIf { it.isNotBlank() } ?: return null
    val name = nome?.takeIf { it.isNotBlank() } ?: return null
    return RadioStation(
        uuid        = "tr-$id",
        name        = name,
        streamUrl   = url,
        favicon     = logoNova?.id?.let { "https://api.tudoradio.com/assets/$it" } ?: "",
        tags        = buildString {
            idestados?.sigla?.let { append(it.lowercase()) }
            idcidades?.nome?.let { append(",${it.lowercase()}") }
        },
        country     = "Brazil",
        countryCode = "BR",
        codec       = "",
        bitrate     = 0,
        votes       = views ?: 0,
        language    = "portuguese"
    )
}

// ── Retrofit interface ────────────────────────────────────────────────────────

interface TudoRadioApi {
    @POST("graphql")
    suspend fun query(@Body request: TrRequest): TrResponse
}

// ── Client ───────────────────────────────────────────────────────────────────

object TudoRadioClient {
    val api: TudoRadioApi by lazy {
        val http = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
        Retrofit.Builder()
            .baseUrl("https://api.tudoradio.com/")
            .client(http)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TudoRadioApi::class.java)
    }

    private fun buildQuery(stateFilter: String = "") = TrRequest("""
        { radios(limit: 500, sort: ["-views"],
          filter: { status: { _eq: "published" }, streaming: { _nnull: true }
                    $stateFilter }) {
            id nome streaming streaming_android views
            logo_nova { id }
            idcidades { nome }
            idestados { nome sigla }
        }}
    """.trimIndent())

    suspend fun fetchAll(): List<RadioStation> = runCatching {
        api.query(buildQuery()).data?.radios
            ?.mapNotNull { it.toRadioStation() } ?: emptyList()
    }.getOrDefault(emptyList())

    suspend fun fetchByState(sigla: String): List<RadioStation> = runCatching {
        val filter = ", idestados: { sigla: { _eq: \"$sigla\" } }"
        api.query(buildQuery(filter)).data?.radios
            ?.mapNotNull { it.toRadioStation() } ?: emptyList()
    }.getOrDefault(emptyList())
}
