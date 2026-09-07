package com.notash.cryptobacktester.intelligence

import com.notash.cryptobacktester.core.Candle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoinIntelligenceV2Test {
    private fun candles() = (1L..30L).map { i ->
        Candle(i, 100.0, 102.0, 98.0, 100.0 + i * 0.2, 1_000_000.0 + i * 10_000.0)
    }

    @Test
    fun complete_verified_inputs_produce_detailed_breakdown_without_data_gaps() {
        val source = SourceRef("official", "https://example.org", status = SourceStatus.CONFIRMED)
        val research = ResearchSummary(
            profile = ProjectProfile(name = "Alpha", symbol = "ALPHA", chain = "Ethereum", website = "https://example.org", github = "https://github.com/example/alpha", source = source),
            product = ProductAnalysis(utility = "Protocol utility", liveProduct = true, score = 82.0, confidence = 90.0, sources = listOf(source)),
            development = DevelopmentActivity(latestCommit = "2026-08-30", score = 78.0, sources = listOf(source)),
            strengths = listOf("LIVE_PRODUCT"), weaknesses = emptyList(), risks = emptyList(), dataGaps = emptyList(),
            overallScore = 80.0, confidence = 88.0, sources = listOf(source), timestamp = "2026-09-01T00:00:00Z"
        )
        val tokenomics = TokenomicsAssessment(
            supply = TokenSupply(circulating = 800.0, total = 1_000.0, max = 1_000.0, source = source),
            allocations = emptyList(), allocationTotal = 100.0, lowCirculation = false, largeFutureUnlock = false,
            unlimitedOrUnknownSupply = false, concentrationRisk = 20.0, inflationRisk = 15.0, dilutionRisk = 20.0,
            utilityRisk = 20.0, score = 81.0, riskScore = 19.0, confidence = 90.0, findings = emptyList()
        )
        val onChain = OnChainAnalysis(
            snapshot = HolderSnapshot(holderCount = 100_000, top10Percent = 22.0, largestHolderPercent = 6.0, source = source),
            concentrationRisk = 22.0, distributionScore = 78.0, whaleRisk = 6.0, holderGrowthScore = 80.0,
            overallScore = 79.0, confidence = 85.0, findings = emptyList()
        )
        val unlocks = UnlockAnalysis(
            events = emptyList(), nextUnlock = null, futureUnlocks = emptyList(), burnEvents = emptyList(), emission = null,
            supplyPressure = 5.0, dilutionRisk = 10.0, emissionRisk = 20.0, burnOffsetRisk = 30.0,
            overallScore = 82.0, risks = emptyList()
        )
        val team = TeamIntelligence(members = listOf(TeamMember("Founder", "CEO", source = source)), anonymous = false, transparencyScore = 90.0, teamRisk = 10.0, confidence = 90.0, sources = listOf(source))
        val investors = InvestorAssessment(investors = listOf(Investor("Fund", status = SourceStatus.CONFIRMED, source = source)), qualityScore = 85.0, fundingStrength = 80.0, reputationScore = 88.0, confidence = 90.0, sources = listOf(source))
        val report = CoinIntelligenceEngine().analyze(
            CoinIntelligenceInput(
                snapshot = MemeCoinSnapshot("ALPHA", "ALPHAUSDT", 20_000_000.0, 500_000_000.0, 1200, holderConcentrationPercent = 22.0, contractVerified = true),
                candles = candles(), research = research, tokenomics = tokenomics, onChain = onChain, unlocks = unlocks,
                team = team, investors = investors
            )
        )
        assertTrue(report.dataGaps.isEmpty())
        assertTrue(report.projectQualityScore >= 70.0)
        assertTrue(report.tokenomicsRiskScore <= 30.0)
        assertTrue(report.holderRiskScore <= 30.0)
        assertTrue(report.unlockRiskScore <= 30.0)
        assertTrue(report.confidenceScore >= 75.0)
        assertTrue(report.sourceCount >= 1)
    }

    @Test
    fun missing_intelligence_is_explicit_and_reduces_confidence_instead_of_being_fabricated() {
        val report = CoinIntelligenceEngine().analyze(
            CoinIntelligenceInput(
                snapshot = MemeCoinSnapshot("UNKNOWN", "UNKNOWNUSDT", 1_000_000.0, 10_000_000.0, 100),
                candles = candles()
            )
        )
        assertTrue(report.dataGaps.contains("PROJECT_RESEARCH"))
        assertTrue(report.dataGaps.contains("TOKENOMICS"))
        assertTrue(report.dataGaps.contains("ONCHAIN"))
        assertTrue(report.dataGaps.contains("TEAM"))
        assertTrue(report.dataGaps.contains("INVESTORS"))
        assertTrue(report.confidenceScore < 75.0)
        assertEquals(0, report.sourceCount)
    }

    @Test
    fun concentrated_holders_and_large_unlocks_raise_risk_breakdown() {
        val source = SourceRef("chain", status = SourceStatus.CONFIRMED)
        val onChain = OnChainAnalysis(
            snapshot = HolderSnapshot(holderCount = 400, top10Percent = 82.0, largestHolderPercent = 35.0, source = source),
            concentrationRisk = 82.0, distributionScore = 18.0, whaleRisk = 35.0, holderGrowthScore = 25.0,
            overallScore = 20.0, confidence = 90.0, findings = listOf("HIGH_HOLDER_CONCENTRATION")
        )
        val unlocks = UnlockAnalysis(
            events = listOf(UnlockEvent("2026-09-15", percentage = 35.0, source = source)), nextUnlock = UnlockEvent("2026-09-15", percentage = 35.0, source = source),
            futureUnlocks = emptyList(), burnEvents = emptyList(), emission = null, supplyPressure = 35.0, dilutionRisk = 80.0,
            emissionRisk = 50.0, burnOffsetRisk = 70.0, overallScore = 25.0, risks = listOf("HIGH_UNLOCK_PRESSURE")
        )
        val report = CoinIntelligenceEngine().analyze(
            CoinIntelligenceInput(
                snapshot = MemeCoinSnapshot("RISK", "RISKUSDT", 3_000_000.0, 20_000_000.0, 200, holderConcentrationPercent = 82.0),
                candles = candles(), onChain = onChain, unlocks = unlocks
            )
        )
        assertTrue(report.holderRiskScore >= 70.0)
        assertTrue(report.unlockRiskScore >= 70.0)
        assertTrue(report.riskScore >= 60.0)
        assertTrue(report.warnings.contains("HIGH_HOLDER_CONCENTRATION"))
        assertTrue(report.warnings.contains("HIGH_UNLOCK_PRESSURE"))
    }
}
