package com.notash.cryptobacktester.notifications

import android.content.Context

/** Persisted user notification policy. No notification is emitted when the relevant switch is off. */
class NotificationPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("alvex_notifications", Context.MODE_PRIVATE)

    var whaleAlerts: Boolean
        get() = prefs.getBoolean("whale", true)
        set(value) = prefs.edit().putBoolean("whale", value).apply()

    var pumpDumpAlerts: Boolean
        get() = prefs.getBoolean("pump_dump", true)
        set(value) = prefs.edit().putBoolean("pump_dump", value).apply()

    var memeRiskAlerts: Boolean
        get() = prefs.getBoolean("meme_risk", true)
        set(value) = prefs.edit().putBoolean("meme_risk", value).apply()

    var newsAlerts: Boolean
        get() = prefs.getBoolean("news", true)
        set(value) = prefs.edit().putBoolean("news", value).apply()

    var minWhaleUsd: Double
        get() = prefs.getString("min_whale_usd", "100000")?.toDoubleOrNull() ?: 100000.0
        set(value) = prefs.edit().putString("min_whale_usd", value.coerceAtLeast(0.0).toString()).apply()
}
