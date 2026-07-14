package com.radioisaac.data

import android.content.Context
import com.google.gson.Gson

data class StationMetadata(
    val ps: String = "",   // max 8 chars — Programme Service name
    val pty: String = "",  // Programme Type / genre
    val rt: String = ""    // Radio Text — artist/title hint
)

object StationMetadataStore {
    private const val PREF_NAME = "station_metadata_v1"
    private val gson = Gson()

    fun save(context: Context, uuid: String, meta: StationMetadata) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putString(uuid, gson.toJson(meta)).apply()
    }

    fun load(context: Context, uuid: String): StationMetadata? {
        val json = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(uuid, null) ?: return null
        return try { gson.fromJson(json, StationMetadata::class.java) } catch (e: Exception) { null }
    }

    fun delete(context: Context, uuid: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().remove(uuid).apply()
    }

    fun loadAll(context: Context): Map<String, StationMetadata> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.all.mapNotNull { (k, v) ->
            try { k to gson.fromJson(v as String, StationMetadata::class.java) } catch (e: Exception) { null }
        }.toMap()
    }
}
