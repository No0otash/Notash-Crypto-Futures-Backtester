package com.notash.cryptobacktester.core

import org.junit.Assert.assertTrue
import org.junit.Test

class GrowthScoringEngineTest {
    private val market = MarketSnapshot(
        price = 1.0,
        change24hPercent = 3.0,
        volume24h = 2_000_000.0,
        liquidityUsd = 10_000_000.0,
        btcTrendScore = 80,
        sectorScore = 80,
        momentumScore = 80,
        relativeStrengthScore = 80
    )

    @Test
    fun missingFundamentals_neverReceivePositiveAssumptions() {
        val result = GrowthScoringEngine.score(
            intelligence = TokenIntelligence(symbol = "UNKNOWN", dataConfidence = 20),
            market = market,
            narrativeScore = 0,
            onChainScore = 0,
            earlySignals = emptyList()
        )

        assertTrue(result.score.productUtility == 0)
        assertTrue(result.score.team == 0)
        assertTrue(result.score.investors == 0)
        assertTrue(result.score.roadmap == 0)
        assertTrue(result.score.tokenomics == 0)
        assertTrue(result.risks.any { it.contains("Data Confidence") })
        assertTrue(result.risks.any { it.contains("High Risk") })
    }

    @Test
    fun measurableEarlySignalsProduceCandidateWithoutGuaranteeLanguage() {
        val result = GrowthScoringEngine.score(
            intelligence = TokenIntelligence(
                symbol = "TEST",
                product = "Live product",
                utility = "Used for network fees",
                founders = listOf("Founder"),
                investors = listOf("Investor"),
                completedMilestones = listOf("Mainnet"),
                dataConfidence = 85,
                highRisk = false,
                tokenSupply = TokenSupply(
                    maxSupply = 1_000_000.0,
                    circulatingSupply = 800_000.0
                ),
                burnPercentAnnual = 3.0,
                emissionPercentAnnual = 2.0
            ),
            market = market,
            narrativeScore = 80,
            onChainScore = 85,
            earlySignals = listOf("Holder growth", "Whale accumulation")
        )

        assertTrue(result.score.total >= 70)
        assertTrue(result.earlySignals.size == 2)
        assertTrue(result.bullScenario?.contains("sustained demand") == true)
        assertTrue(result.risks.none { it.contains("High Risk") })
    }
}
