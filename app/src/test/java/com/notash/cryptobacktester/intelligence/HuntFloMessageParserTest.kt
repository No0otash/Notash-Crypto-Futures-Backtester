package com.notash.cryptobacktester.intelligence

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HuntFloMessageParserTest {
    @Test
    fun `parses whale transfer and ignores price-only message`() {
        val parser = HuntFloMessageParser()
        val whale = parser.parse("🐋 BTC whale transfer 1250 BTC $120000000 from unknown wallet to Binance")
        val price = parser.parse("BTC price 120000")

        assertEquals("BTC", whale.asset)
        assertEquals(1250.0, whale.amount)
        assertEquals(120_000_000.0, whale.usdValue)
        assertEquals(WhaleDirection.INFLOW, whale.direction)
        assertTrue(price == null)
    }

    @Test
    fun `classifies exchange outflow as accumulation signal`() {
        val result = HuntFloMessageParser().parse("ETH whale moved $15000000 from Coinbase to unknown wallet")
        assertEquals("ETH", result?.asset)
        assertEquals(WhaleDirection.OUTFLOW, result?.direction)
    }
}
