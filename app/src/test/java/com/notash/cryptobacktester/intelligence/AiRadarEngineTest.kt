package com.notash.cryptobacktester.intelligence

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AiRadarEngineTest {
    private fun snapshot(symbol: String, price: Double, open: Double, buy: Double? = null, sell: Double? = null) =
        RadarMarketSnapshot("Test", symbol, price, open, price, open, 1000.0, 1_000_000.0, buy, sell, null, 1L)

    @Test fun positiveMomentumAndBuyPressureIncreasePumpScore() {
        val result = AiRadarEngine().score(listOf(snapshot("ABCUSDT", 120.0, 100.0, 800.0, 200.0))).single()
        assertTrue(result.pumpPotential > 50)
        assertTrue(result.dumpRisk < 50)
        assertTrue(result.reasons.any { it.contains("Positive momentum") })
    }

    @Test fun tokenomicsRiskRaisesDumpRisk() {
        val result = AiRadarEngine().score(
            listOf(snapshot("ABCUSDT", 100.0, 100.0)),
            tokenomicsRisk = mapOf("ABCUSDT" to 100)
        ).single()
        assertTrue(result.dumpRisk > result.pumpPotential)
    }

    @Test fun emptyInputProducesNoSignals() {
        assertEquals(emptyList<RadarSignal>(), AiRadarEngine().score(emptyList()))
    }
}
