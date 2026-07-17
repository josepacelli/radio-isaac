package com.radioisaac.data

import android.util.Log
import com.radioisaac.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object AudDClient {
    private const val TAG = "AudDClient"
    private const val API_URL = "https://api.audd.io/"
    private const val CHUNK_BYTES = 500_000  // ~10s at 320kbps

    private val http = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun recognize(streamUrl: String, apiToken: String): Pair<String, String>? = withContext(Dispatchers.IO) {
        try {
            if (BuildConfig.DEBUG) Log.d(TAG, "Downloading stream chunk from $streamUrl")
            val chunk = downloadChunk(streamUrl) ?: run {
                if (BuildConfig.DEBUG) Log.w(TAG, "downloadChunk returned null")
                return@withContext null
            }
            if (BuildConfig.DEBUG) Log.d(TAG, "Chunk downloaded: ${chunk.size} bytes — posting to AudD")
            val result = postToAudD(chunk, apiToken)
            if (result != null) if (BuildConfig.DEBUG) Log.d(TAG, "AudD result: ${result.first} - ${result.second}")
            else if (BuildConfig.DEBUG) Log.w(TAG, "AudD returned no match")
            result
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.e(TAG, "recognize failed: ${e.javaClass.simpleName}: ${e.message}")
            null
        }
    }

    private fun downloadChunk(url: String): ByteArray? {
        val req = Request.Builder().url(url).header("Connection", "close").build()
        http.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) {
                if (BuildConfig.DEBUG) Log.w(TAG, "Stream HTTP ${resp.code}")
                return null
            }
            val stream = resp.body?.byteStream() ?: return null
            val buf = ByteArray(CHUNK_BYTES)
            var totalRead = 0
            while (totalRead < CHUNK_BYTES) {
                val read = stream.read(buf, totalRead, CHUNK_BYTES - totalRead)
                if (read == -1) break
                totalRead += read
            }
            return if (totalRead > 0) buf.copyOf(totalRead) else null
        }
    }

    private fun postToAudD(chunk: ByteArray, apiToken: String): Pair<String, String>? {
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("api_token", apiToken)
            .addFormDataPart("file", "audio.mp3", chunk.toRequestBody("audio/mpeg".toMediaType()))
            .build()
        val req = Request.Builder().url(API_URL).post(body).build()
        http.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) {
                if (BuildConfig.DEBUG) Log.w(TAG, "AudD HTTP ${resp.code}")
                return null
            }
            val json = resp.body?.string() ?: return null
            if (BuildConfig.DEBUG) Log.d(TAG, "AudD response: $json")
            return parseJson(json)
        }
    }

    private fun parseJson(json: String): Pair<String, String>? {
        if (!json.contains("\"status\":\"success\"")) return null
        val artist = json.substringAfter("\"artist\":\"", "").substringBefore("\"").trim()
        val title = json.substringAfter("\"title\":\"", "").substringBefore("\"").trim()
        return if (artist.isNotBlank() && title.isNotBlank()) artist to title else null
    }
}
