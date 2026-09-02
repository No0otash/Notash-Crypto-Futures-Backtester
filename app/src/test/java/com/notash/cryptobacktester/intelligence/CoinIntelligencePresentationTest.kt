package com.notash.cryptobacktester.intelligence

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoinIntelligencePresentationTest {
    @Test
    fun presentation_exposes_unavailable_intelligence_instead_of_fake_values() {
        val report = CoinIntelligenceReport(
            symbol = "BTC",
            market = "BTCUSDT",
            overallScore = 55.0,
            riskScore = 30.0,
            opportunityScore = 61.0,
            confidenceScore = 46.0,
            components = listOf(IntelligenceComponent("MARKET", 65.0, .2, "market")),
            warnings = listOf("TOKENOMICS_UNAVAILABLE"),
            dataComplete = false,
            summary = "Mixed",
            dataGaps = listOf("TOKENOMICS", "ONCHAIN"),
            sourceCount = 0,
            verdict = "MIXED_CAUTION"
        )
        val ui = CoinIntelligencePresenter.present(report, updatedAtMs = 1234L)
        assertEquals("MIXED_CAUTION", ui.verdict)
        assertTrue(ui.gapLabels.contains("TOKENOMICS: unavailable"))
        assertTrue(ui.gapLabels.contains("ONCHAIN: unavailable"))
        assertEquals("0 verified sources", ui.sourceLabel)
        assertEquals(1234L, ui.updatedAtMs)
    }
}
