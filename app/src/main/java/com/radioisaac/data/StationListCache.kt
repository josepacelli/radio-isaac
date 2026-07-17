package com.radioisaac.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

object StationListCache {
    private const val FILENAME = "station_list_cache.json"
    private val gson = Gson()
    private val type = object : TypeToken<List<RadioStation>>() {}.type

    fun save(context: Context, stations: List<RadioStation>) {
        try {
            File(context.filesDir, FILENAME).writeText(gson.toJson(stations))
        } catch (_: Exception) {}
    }

    fun load(context: Context): List<RadioStation>? {
        return try {
            val file = File(context.filesDir, FILENAME)
            if (!file.exists()) null else gson.fromJson(file.readText(), type)
        } catch (_: Exception) { null }
    }
}
