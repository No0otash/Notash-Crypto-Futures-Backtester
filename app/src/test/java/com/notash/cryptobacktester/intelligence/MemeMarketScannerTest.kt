package com.notash.cryptobacktester.intelligence

import com.notash.cryptobacktester.core.Candle
import com.notash.cryptobacktester.data.FuturesMarketDescriptor
import com.notash.cryptobacktester.data.FuturesTickerSnapshot
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MemeMarketScannerTest {
    private class FakeSource : MarketIntelligenceDataSource {
        override suspend fun loadMarkets() = listOf(FuturesMarketDescriptor("PEPEUSDT", "PEPE", "USDT", true))
        override suspend fun loadTickers() = listOf(FuturesTickerSnapshot("PEPEUSDT", 0.00002, 0.00001, 0.00003, 0.000009, 2_000_000.0, 10_000_000.0, null, null, null, 1L))
        override suspend fun loadCandles(market: String, period: String, limit: Int) = listOf(
            Candle(1L, 0.00001, 0.000011, 0.000009, 0.000012, 100.0, 1000.0),
            Candle(2L, 0.000012, 0.00002, 0.000011, 0.00002, 1000.0, 10000.0)
        )
    }

    @Test
    fun unavailableLiquidityIsDataGapNotFalseLowLiquidity() = runBlocking {
        val result = MemeMarketScanner(FakeSource()).scan().single()

        assertFalse(result.dataComplete)
        assertTrue(result.flags.contains("LIQUIDITY_DATA_UNAVAILABLE"))
        assertFalse(result.flags.contains("LOW_LIQUIDITY"))
    }
}
