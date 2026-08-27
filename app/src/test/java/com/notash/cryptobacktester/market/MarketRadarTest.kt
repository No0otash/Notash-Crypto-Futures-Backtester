package com.notash.cryptobacktester.market

import com.notash.cryptobacktester.core.MarketTicker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarketRadarTest {
    @Test
    fun `strong positive momentum receives high growth score`() {
        val ticker = MarketTicker("MOONUSDT", 1.25, 18.0, 5_000_000.0, 25_000_000.0)
        val score = MarketRadar.score(ticker, maxValue24h = 25_000_000.0)
        assertTrue(score >= 75)
        assertEquals("HIGH", MarketRadar.riskBand(score))
    }

    @Test
    fun `negative momentum is classified as dump`() {
        val ticker = MarketTicker("DROPUSDT", 0.9, -12.0, 2_000_000.0, 10_000_000.0)
        assertTrue(MarketRadar.isDump(ticker))
        assertTrue(!MarketRadar.isPump(ticker))
    }

    @Test
    fun `growth candidates are ranked and capped`() {
        val tickers = listOf(
            MarketTicker("AUSDT", 1.0, 20.0, 1.0, 30.0),
            MarketTicker("BUSDT", 1.0, 8.0, 1.0, 20.0),
            MarketTicker("CUSDT", 1.0, 3.0, 1.0, 10.0),
            MarketTicker("DUSDT", 1.0, -4.0, 1.0, 5.0)
        )
        val candidates = MarketRadar.rankGrowthCandidates(tickers, limit = 3)
        assertEquals(3, candidates.size)
        assertEquals("AUSDT", candidates.first().market)
        assertTrue(candidates.all { it.score >= 60 })
    }
}
