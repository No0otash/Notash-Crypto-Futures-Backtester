package com.notash.cryptobacktester.ads

/** Store-agnostic monetization state. Billing integration can be added later. */
data class MonetizationState(
    val premium: Boolean = false,
    val adsEnabled: Boolean = true
) {
    fun effectiveAdsEnabled(): Boolean = adsEnabled && !premium
}

class MonetizationController(
    initialState: MonetizationState = MonetizationState()
) {
    var state: MonetizationState = initialState
        private set

    fun setPremium(enabled: Boolean) {
        state = state.copy(premium = enabled)
    }

    fun setAdsEnabled(enabled: Boolean) {
        state = state.copy(adsEnabled = enabled)
    }
}
