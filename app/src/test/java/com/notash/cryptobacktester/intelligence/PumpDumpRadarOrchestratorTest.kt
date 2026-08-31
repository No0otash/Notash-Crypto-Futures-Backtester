package com.notash.cryptobacktester.intelligence

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PumpDumpRadarOrchestratorTest {
    private fun snapshot(exchange: String, price: Double) = RadarMarketSnapshot(
        exchange, "ABCUSDT", price, 100.0, 120.0, 80.0, 10_000_000.0, 10_000_000.0, null, null, null, 1L
    )

    @Test fun multiExchangeEvidenceIsCombinedBySymbol() {
        val result = PumpDumpRadarOrchestrator().analyze(
            listOf(snapshot("CoinEx", 110.0), snapshot("Binance", 111.0)),
            previousQuoteVolumeByKey = mapOf("COINEX:ABCUSDT" to 1_000_000.0, "BINANCE:ABCUSDT" to 1_000_000.0),
            volatilityByKey = mapOf("COINEX:ABCUSDT" to 3.0, "BINANCE:ABCUSDT" to 3.0)
        )
        assertEquals(1, result.signals.size)
        assertTrue(result.signals.single().confidence > 50)
    }
}
