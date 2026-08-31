package com.notash.cryptobacktester.notifications

import com.notash.cryptobacktester.intelligence.WhaleEvent

/** Pure policy layer: UI/notification transport can consume these decisions deterministically. */
class AlvexNotificationPolicy(private val preferences: NotificationPreferences) {
    fun shouldNotifyWhale(event: WhaleEvent): Boolean {
        if (!preferences.whaleAlerts) return false
        val amount = event.amount ?: return false
        return amount >= preferences.minWhaleUsd
    }

    fun shouldNotifyNews() = preferences.newsAlerts
    fun shouldNotifyPumpDump() = preferences.pumpDumpAlerts
    fun shouldNotifyMemeRisk() = preferences.memeRiskAlerts
}
