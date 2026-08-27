package com.notash.cryptobacktester.core

import kotlin.test.Test
import kotlin.test.assertEquals

class TokenomicsAnalyzerTest {
    @Test
    fun calculatesCirculatingUnlockAndNetSupply() {
        val now = 1_000_000L
        val token = TokenIntelligence(
            symbol = "TEST",
            dataConfidence = 90,
            tokenSupply = TokenSupply(maxSupply = 1_000_000.0, circulatingSupply = 320_000.0),
            unlocks = listOf(
                UnlockEvent(now + 30L * 24 * 60 * 60 * 1000, 50_000.0, "VC", 5.0),
                UnlockEvent(now + 120L * 24 * 60 * 60 * 1000, 100_000.0, "Ecosystem", 10.0)
            ),
            emissionPercentAnnual = 8.0,
            burnPercentAnnual = 1.0
        )
        val result = TokenomicsAnalyzer.analyze(token, now)
        assertEquals(32.0, result.circulatingPercent)
        assertEquals(7.0, result.netSupplyChangePercent)
        assertEquals(5.0, result.next90DayUnlockPercent)
        assertEquals(60, result.unlockRiskScore)
    }
}
