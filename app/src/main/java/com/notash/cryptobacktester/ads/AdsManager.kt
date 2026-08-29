package com.notash.cryptobacktester.ads

/**
 * Store-safe abstraction for monetization placements.
 * The UI depends on this contract rather than a specific ad network SDK.
 */
interface AdsManager {
    fun isEnabled(): Boolean
    fun showBanner(placement: AdPlacement)
    fun showInterstitial(placement: AdPlacement): Boolean
    fun showRewarded(placement: AdPlacement, onReward: () -> Unit): Boolean
}

enum class AdPlacement {
    DASHBOARD,
    MARKET_RADAR,
    REPORT,
    AI_HUB,
    RESULTS
}

class NoOpAdsManager : AdsManager {
    override fun isEnabled(): Boolean = false
    override fun showBanner(placement: AdPlacement) = Unit
    override fun showInterstitial(placement: AdPlacement): Boolean = false
    override fun showRewarded(placement: AdPlacement, onReward: () -> Unit): Boolean = false
}
