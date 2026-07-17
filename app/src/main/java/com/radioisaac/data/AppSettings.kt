package com.radioisaac.data

import android.content.Context
import com.radioisaac.BuildConfig

object AppSettings {
    private const val PREF_NAME = "app_settings"

    fun isFingerprintEnabled(context: Context): Boolean =
        prefs(context).getBoolean("fingerprint_enabled", true)

    fun setFingerprintEnabled(context: Context, enabled: Boolean) =
        prefs(context).edit().putBoolean("fingerprint_enabled", enabled).apply()

    fun getAuddToken(context: Context): String =
        prefs(context).getString("audd_token", BuildConfig.AUDD_TOKEN) ?: BuildConfig.AUDD_TOKEN

    fun setAuddToken(context: Context, token: String) =
        prefs(context).edit().putString("audd_token", token).apply()

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
}
