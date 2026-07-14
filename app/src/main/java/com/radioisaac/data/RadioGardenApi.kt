package com.radioisaac.data

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

interface RadioGardenApi {
    @GET("api/ara/content/places")
    suspend fun getPlaces(): RgPlacesResponse

    @GET("api/ara/content/page/{id}/channels")
    suspend fun getChannels(@Path("id") placeId: String): RgChannelsResponse
}

data class RgPlacesResponse(val data: RgPlacesData?)
data class RgPlacesData(val list: List<RgPlace>?)
data class RgPlace(
    val id: String = "",
    val title: String = "",
    val size: Int = 0,
    val country: String = ""
)

data class RgChannelsResponse(val data: RgChannelsData?)
data class RgChannelsData(val content: List<RgContent>?)
data class RgContent(val items: List<RgItem>?)
data class RgItem(val page: RgPage?)
data class RgPage(
    val url: String = "",
    val title: String = "",
    val website: String = "",
    val secure: Boolean = false,
    val place: RgPlaceRef? = null,
    val country: RgCountryRef? = null
)
data class RgPlaceRef(val id: String = "", val title: String = "")
data class RgCountryRef(val id: String = "", val title: String = "")

object RadioGardenClient {
    private const val BASE_URL = "https://radio.garden/"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            chain.proceed(
                chain.request().newBuilder()
                    .addHeader("User-Agent", "Mozilla/5.0")
                    .addHeader("Origin", "https://radio.garden")
                    .addHeader("Referer", "https://radio.garden/")
                    .build()
            )
        }
        .build()

    val api: RadioGardenApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RadioGardenApi::class.java)
    }
}

fun RgPage.toRadioStation(): RadioStation? {
    if (title.isBlank()) return null
    val channelId = url.substringAfterLast("/").ifBlank { return null }
    val streamUrl = "https://radio.garden/api/ara/content/channel/$channelId/stream"
    return RadioStation(
        uuid = "rg_$channelId",
        name = title,
        streamUrl = streamUrl,
        streamUrlFallback = streamUrl,
        favicon = "",
        tags = "",
        country = country?.title ?: "Brazil",
        countryCode = "BR",
        codec = "MP3",
        bitrate = 0,
        votes = 0,
        language = "portuguese",
        isOnline = 1,
        clickCount = 0
    )
}
