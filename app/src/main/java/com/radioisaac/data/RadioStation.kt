package com.radioisaac.data

import com.google.gson.annotations.SerializedName

data class RadioStation(
    @SerializedName("stationuuid") val uuid: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("url_resolved") val streamUrl: String = "",
    @SerializedName("url") val streamUrlFallback: String = "",
    @SerializedName("favicon") val favicon: String = "",
    @SerializedName("tags") val tags: String = "",
    @SerializedName("country") val country: String = "",
    @SerializedName("countrycode") val countryCode: String = "",
    @SerializedName("codec") val codec: String = "",
    @SerializedName("bitrate") val bitrate: Int = 0,
    @SerializedName("votes") val votes: Int = 0,
    @SerializedName("language") val language: String = "",
    @SerializedName("lastcheckok") val isOnline: Int = 0,
    @SerializedName("clickcount") val clickCount: Int = 0
) {
    val effectiveStreamUrl: String get() = streamUrl.ifBlank { streamUrlFallback }
    val displayName: String get() = name.trim().take(24)
    val displayTags: String get() = tags.split(",").take(5).joinToString(", ") { it.trim().lowercase() }.trim(',', ' ')
    val displayCodec: String get() = codec.uppercase().take(4).ifBlank { "???" }
    val displayCountry: String get() = countryCode.uppercase().take(2).ifBlank { "--" }
    val displayBitrate: String get() = if (bitrate > 0) "$bitrate" else "---"
    val uuidShort: String get() = uuid.take(8).uppercase()
    val piCode: String get() = uuid.hashCode().and(0xFFFF).toString(16).uppercase().padStart(4, '0')
    val extractedFrequency: String? get() {
        val m = Regex("""(\d{2,3}[.,]\d)\s*(?:FM|AM|MHz|KHz)?""", RegexOption.IGNORE_CASE).find(name)
        return m?.groupValues?.get(1)?.replace(',', '.')
    }
    val psName: String get() = name.trim().take(8).uppercase()
}
