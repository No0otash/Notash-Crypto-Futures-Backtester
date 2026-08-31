package com.notash.cryptobacktester.intelligence

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class HuntFloMessageClassifierTest {
    @Test
    fun parsesExchangeInflowWhaleMessage() {
        val event = HuntFloMessageClassifier.parse(
            "🐋 Whale transferred 1,250,000 USDT to Binance inflow",
            1234L
        )
        assertNotNull(event)
        assertEquals(1250000.0, event.amount)
        assertEquals("USDT", event.asset)
        assertEquals(WhaleFlow.EXCHANGE_INFLOW, event.flow)
        assertEquals("Binance", event.exchange)
        assertEquals("HuntFlo", event.source)
    }

    @Test
    fun ignoresPriceOnlyMessage() {
        val event = HuntFloMessageClassifier.parse(
            "BTC price is trading at 78200 USD, 24h +1.2%",
            1234L
        )
        assertNull(event)
    }

    @Test
    fun parsesWalletToWalletWithoutFabricatingAmount() {
        val event = HuntFloMessageClassifier.parse(
            "Whale wallet-to-wallet transfer detected for ETH",
            1234L
        )
        assertNotNull(event)
        assertEquals(WhaleFlow.WALLET_TO_WALLET, event.flow)
        assertEquals(null, event.amount)
        assertEquals("ETH", event.asset)
    }
}
