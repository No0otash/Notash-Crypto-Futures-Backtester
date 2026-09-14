package com.notash.cryptobacktester.ui

/**
 * Provider-neutral catalog for real market asset icons.
 * Unknown assets intentionally return null instead of showing fabricated identity data.
 */
internal object CoinIconCatalog {
    private const val ICON_BASE = "https://assets.coinex.com/coins/"

    private val known = setOf(
        "BTC", "ETH", "SOL", "XRP", "DOGE", "PEPE", "USDT", "USDC", "BNB", "ADA",
        "AVAX", "LINK", "DOT", "TRX", "LTC", "BCH", "SUI", "TON", "SHIB", "UNI"
    )

    fun assetKey(symbol: String): String = symbol
        .uppercase()
        .removeSuffix("USDT")
        .removeSuffix("USDC")
        .removeSuffix("USD")
        .trim()

    fun iconUrl(symbol: String): String? {
        val key = assetKey(symbol)
        return if (key in known) "$ICON_BASE${key.lowercase()}.png" else null
    }

    fun isKnown(symbol: String): Boolean = assetKey(symbol) in known
}
