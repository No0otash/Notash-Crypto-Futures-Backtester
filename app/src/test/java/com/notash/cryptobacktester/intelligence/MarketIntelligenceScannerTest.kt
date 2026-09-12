package com.notash.cryptobacktester.intelligence

import com.notash.cryptobacktester.data.FuturesMarketDescriptor
import com.notash.cryptobacktester.data.FuturesTickerSnapshot
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarketIntelligenceScannerTest {
    private class FakeSource : MarketIntelligenceDataSource {
        override suspend fun loadMarkets() = listOf(
            FuturesMarketDescriptor("PUMPUSDT", "PUMP", "USDT", true),
            FuturesMarketDescriptor("WATCHUSDT", "WATCH", "USDT", true),
            FuturesMarketDescriptor("OFFUSDT", "OFF", "USDT", false)
        )

        override suspend fun loadTickers() = listOf(
            FuturesTickerSnapshot("PUMPUSDT", 120.0, 100.0, 125.0, 95.0, 1_000_000.0, 10_000_000.0, 700_000.0, 300_000.0, null, 1L),
            FuturesTickerSnapshot("WATCHUSDT", 101.0, 100.0, 102.0, 99.0, 100_000.0, 500_000.0, null, null, null, 1L)
        )

        override suspend fun loadCandles(market: String, period: String, limit: Int) = emptyList<com.notash.cryptobacktester.core.Candle>()
    }

    @Test
    fun scansAllTradingMarketsAndRanksStrongSignal() = runBlocking {
        val report = MarketIntelligenceScanner(FakeSource()).scanPumpDump()

        assertEquals(2, report.scannedMarkets)
        assertEquals("PUMPUSDT", report.signals.first().symbol)
        assertTrue(report.signals.first().score > report.signals.last().score)
    }

    @Test
    fun unavailableEvidenceIsReportedWithoutFabricatingData() = runBlocking {
        val report = MarketIntelligenceScanner(FakeSource()).scanPumpDump()

        assertTrue(report.signals.any { "VOLUME_BASELINE" in it.dataGaps })
        assertEquals(1, report.unavailableMarkets)
    }
}
