package com.notash.cryptobacktester.data

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CoinExMarketParsingTest {
    @Test
    fun parsesTradingAndUnavailableFuturesMarkets() {
        val body = JSONObject()
            .put(
                "data",
                JSONArray()
                    .put(JSONObject().put("market", "BTCUSDT").put("base_ccy", "BTC").put("quote_ccy", "USDT").put("status", "online"))
                    .put(JSONObject().put("market", "PEPEUSDT").put("base_ccy", "PEPE").put("quote_ccy", "USDT").put("status", "online"))
                    .put(JSONObject().put("market", "OLDUSDT").put("base_ccy", "OLD").put("quote_ccy", "USDT").put("status", "counting_down"))
            )
            .toString()

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
