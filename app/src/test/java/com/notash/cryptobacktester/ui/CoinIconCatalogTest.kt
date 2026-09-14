package com.notash.cryptobacktester.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CoinIconCatalogTest {
    @Test
    fun normalizes_futures_symbols_to_asset_keys() {
        assertEquals("BTC", CoinIconCatalog.assetKey("BTCUSDT"))
        assertEquals("ETH", CoinIconCatalog.assetKey("ethusdt"))
        assertEquals("SOL", CoinIconCatalog.assetKey("SOLUSDC"))
    }

    @Test
    fun known_assets_have_real_provider_icon_urls() {
        val url = CoinIconCatalog.iconUrl("BTCUSDT")
        assertNotNull(url)
        assertEquals("https://assets.coinex.com/coins/btc.png", url)
        assertTrue(url.endsWith(".png"))
    }

    @Test
    fun unknown_assets_do_not_get_a_fabricated_icon() {
        assertFalse(CoinIconCatalog.isKnown("NOTAREALCOINUSDT"))
        assertNull(CoinIconCatalog.iconUrl("NOTAREALCOINUSDT"))
    }
}
