package com.radioisaac.data

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface RadioApi {
    @GET("json/stations/topvote/{limit}")
    suspend fun getTopStations(
        @Path("limit") limit: Int = 120,
        @Query("hidebroken") hideBroken: Boolean = true
    ): List<RadioStation>

    @GET("json/stations/search")
    suspend fun searchStations(
        @Query("name") name: String,
        @Query("limit") limit: Int = 200,
        @Query("order") order: String = "votes",
        @Query("reverse") reverse: Boolean = true,
        @Query("hidebroken") hideBroken: Boolean = true
    ): List<RadioStation>

    @GET("json/stations/search")
    suspend fun searchByCountryName(
        @Query("country") country: String,
        @Query("limit") limit: Int = 2000,
        @Query("order") order: String = "votes",
        @Query("reverse") reverse: Boolean = true,
        @Query("hidebroken") hideBroken: Boolean = true
    ): List<RadioStation>

    @GET("json/stations/search")
    suspend fun searchByStateAndCountry(
        @Query("state") state: String,
        @Query("countrycode") countryCode: String = "BR",
        @Query("limit") limit: Int = 600,
        @Query("order") order: String = "votes",
        @Query("reverse") reverse: Boolean = true,
        @Query("hidebroken") hideBroken: Boolean = true
    ): List<RadioStation>

    @GET("json/stations/bycountrycode/{code}")
    suspend fun getByCountry(
        @Path("code") code: String,
        @Query("limit") limit: Int = 5000,
        @Query("order") order: String = "votes",
        @Query("reverse") reverse: Boolean = true,
        @Query("hidebroken") hideBroken: Boolean = true
    ): List<RadioStation>

    @GET("json/stations/bycountrycode/{code}")
    suspend fun getByCountryOrder(
        @Path("code") code: String,
        @Query("limit") limit: Int = 3000,
        @Query("order") order: String,
        @Query("reverse") reverse: Boolean = true,
        @Query("hidebroken") hideBroken: Boolean = true
    ): List<RadioStation>

    @GET("json/stations/bylanguage/{language}")
    suspend fun getByLanguage(
        @Path("language") language: String,
        @Query("countrycode") countryCode: String = "BR",
        @Query("limit") limit: Int = 1000,
        @Query("order") order: String = "votes",
        @Query("reverse") reverse: Boolean = true,
        @Query("hidebroken") hideBroken: Boolean = true
    ): List<RadioStation>

    @GET("json/stations/bytag/{tag}")
    suspend fun getByTag(
        @Path("tag") tag: String,
        @Query("limit") limit: Int = 200,
        @Query("order") order: String = "votes",
        @Query("reverse") reverse: Boolean = true,
        @Query("hidebroken") hideBroken: Boolean = true
    ): List<RadioStation>

    @GET("json/stations/bystate/{state}")
    suspend fun getByState(
        @Path("state") state: String,
        @Query("countrycode") countryCode: String = "BR",
        @Query("limit") limit: Int = 600,
        @Query("order") order: String = "votes",
        @Query("reverse") reverse: Boolean = true,
        @Query("hidebroken") hideBroken: Boolean = true
    ): List<RadioStation>
}

object RadioApiClient {
    private const val BASE_URL = "https://de1.api.radio-browser.info/"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val api: RadioApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RadioApi::class.java)
    }
}
