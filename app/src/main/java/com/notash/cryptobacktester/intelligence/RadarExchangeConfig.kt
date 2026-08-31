package com.notash.cryptobacktester.intelligence

/** Supported public market-data venues for ALVEX AI Radar. */
enum class RadarExchange(val displayName: String) {
    COINEX("CoinEx"),
    BINANCE("Binance"),
    BYBIT("Bybit"),
    OKX("OKX")
}

data class RadarProviderHealth(
    val exchange: RadarExchange,
    val available: Boolean,
    val checkedAtMs: Long,
    val error: String? = null
)
