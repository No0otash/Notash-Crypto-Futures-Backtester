package com.notash.cryptobacktester.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OnChainAnalyzerTest {
    @Test
    fun `missing on-chain evidence stays unknown`() {
        val result = OnChainAnalyzer.analyze(
            MarketSignalSnapshot(
                symbol = "TEST",
                price = 1.0,
                change24hPercent = 1.0,
                volume24h = 1000.0
            )
        )
        assertEquals(0, result.score)
        assertEquals(0, result.confidence)
        assertEquals("Unknown", result.whaleSignal)
        assertTrue(result.risks.isNotEmpty())
    }

    @Test
    fun `outflow with whale accumulation produces accumulation signal`() {
        val result = OnChainAnalyzer.analyze(
            MarketSignalSnapshot(
                symbol = "TEST",
                price = 1.0,
                change24hPercent = 2.0,
                volume24h = 2000.0,
                holderGrowthPercent = 5.0,
                whaleAccumulationScore = 90,
                exchangeInflowUsd = 100_000.0,
                exchangeOutflowUsd = 300_000.0
            )
        )
        assertEquals("Strong accumulation", result.whaleSignal)
        assertEquals("Accumulation / net outflow", result.exchangeFlowSignal)
        assertTrue(result.score >= 70)
        assertTrue(result.confidence >= 70)
    }

    @Test
    fun `net inflow is treated as distribution risk`() {
        val result = OnChainAnalyzer.analyze(
            MarketSignalSnapshot(
                symbol = "TEST",
                price = 1.0,
                change24hPercent = -1.0,
                volume24h = 2000.0,
                whaleAccumulationScore = 20,
                exchangeInflowUsd = 300_000.0,
                exchangeOutflowUsd = 100_000.0
            )
        )
        assertEquals("Distribution / weak accumulation", result.whaleSignal)
        assertEquals("Distribution / net inflow", result.exchangeFlowSignal)
        assertTrue(result.risks.any { it.contains("inflow") })
    }
}
