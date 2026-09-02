package com.notash.cryptobacktester.intelligence

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PumpDumpRadarEngineTest {
    private fun snapshot(price: Double = 110.0, open: Double = 100.0, volume: Double = 20_000_000.0) =
        RadarMarketSnapshot("Test", "ABCUSDT", price, open, 115.0, 95.0, volume, volume, null, null, null, 1L)

    @Test fun strongPositiveMoveIsPumpCandidate() {
        val result = PumpDumpRadarEngine().analyze(
            PumpDumpMarketEvidence(snapshot(), previousQuoteVolume24h = 2_000_000.0, volatilityPercent = 4.0),
            PumpDumpIntelligence(whalePressure = 70, newsImpact = 50)
        )
        assertEquals(RadarPumpDumpDirection.PUMP, result.direction)
        assertTrue(result.score >= 60)
        assertTrue(result.confidence > 50)
        assertTrue(result.reasons.any { it.contains("Positive momentum") })
    }

    @Test fun negativeMoveWithRiskEvidenceIsDumpCandidate() {
        val result = PumpDumpRadarEngine().analyze(
            PumpDumpMarketEvidence(snapshot(price = 80.0), previousQuoteVolume24h = 2_000_000.0, volatilityPercent = 10.0),
            PumpDumpIntelligence(whalePressure = -80, newsImpact = -50, tokenomicsRisk = 90, holderConcentrationRisk = 80, projectRisk = 70)
        )
        assertEquals(RadarPumpDumpDirection.DUMP, result.direction)
        assertTrue(result.score >= 60)
        assertTrue(result.reasons.any { it.contains("Tokenomics") })
    }

    @Test fun missingEvidenceIsReportedAndNeverFabricated() {
        val result = PumpDumpRadarEngine().analyze(PumpDumpMarketEvidence(snapshot()))
        assertTrue("VOLUME_BASELINE" in result.dataGaps)
        assertTrue("WHALE_INTELLIGENCE" in result.dataGaps)
        assertTrue(result.confidence < 80)
    }

    @Test fun lowLiquidityRaisesRisk() {
        val result = PumpDumpRadarEngine().analyze(
            PumpDumpMarketEvidence(snapshot(volume = 50_000.0), spreadPercent = 2.5)
        )
        assertEquals(85, result.liquidityRisk)
    }
}