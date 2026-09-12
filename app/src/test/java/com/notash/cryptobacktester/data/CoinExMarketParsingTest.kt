package com.notash.cryptobacktester.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CoinExMarketParsingTest {
    @Test
    fun parsesTradingAndUnavailableFuturesMarkets() {
        val body = """
            {"code":0,"data":[
              {"market":"BTCUSDT","base_ccy":"BTC","quote_ccy":"USDT","status":"online","is_market_available":true},
              {"market":"PEPEUSDT","base_ccy":"PEPE","quote_ccy":"USDT","status":"online","is_market_available":true},
              {"market":"OLDUSDT","base_ccy":"OLD","quote_ccy":"USDT","status":"counting_down","is_market_available":false}
            ],"message":"OK"}
        """.trimIndent()

        val result = CoinExApi.parseFuturesMarkets(body)

        assertEquals(3, result.size)
        assertEquals("BTCUSDT", result[0].market)
        assertEquals("BTC", result[0].baseAsset)
        assertEquals("USDT", result[0].quoteAsset)
        assertTrue(result[0].isTrading)
        assertEquals("OLDUSDT", result[2].market)
        assertFalse(result[2].isTrading)
    }
}
