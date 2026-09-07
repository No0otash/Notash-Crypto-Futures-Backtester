package com.notash.cryptobacktester.intelligence

import com.notash.cryptobacktester.core.Candle
import kotlin.math.abs
import kotlin.math.max

data class CoinIntelligenceInput(
    val snapshot: MemeCoinSnapshot,
    val candles: List<Candle>,
    val whaleActivity: WhaleActivity? = null,
    val projectQualityScore: Double? = null,
    val tokenomicsScore: Double? = null,
    val research: ResearchSummary? = null,
    val roadmap: RoadmapAnalysis? = null,
    val onChain: OnChainAnalysis? = null,
    val unlocks: UnlockAnalysis? = null,
    val tokenomics: TokenomicsAssessment? = null,
    val team: TeamIntelligence? = null,
    val investors: InvestorAssessment? = null
)

data class IntelligenceComponent(
    val name: String,
    val score: Double,
    val weight: Double,
    val explanation: String
)

data class CoinIntelligenceReport(
    val symbol: String,
    val market: String,
    val overallScore: Double,
    val riskScore: Double,
    val opportunityScore: Double,
    val confidenceScore: Double,
    val components: List<IntelligenceComponent>,
    val warnings: List<String>,
    val dataComplete: Boolean,
    val summary: String,
    val projectQualityScore: Double = 0.0,
    val tokenomicsRiskScore: Double = 50.0,
    val holderRiskScore: Double = 50.0,
    val unlockRiskScore: Double = 50.0,
    val teamRiskScore: Double = 50.0,
    val investorRiskScore: Double = 50.0,
    val dataGaps: List<String> = emptyList(),
    val sourceCount: Int = 0,
    val verdict: String = "UNKNOWN"
)

/**
 * Unified Coin Intelligence engine for items 95-104.
 *
 * The engine aggregates only data supplied by verified/provider layers. Missing research,
 * tokenomics, team/investor and on-chain inputs are surfaced as data gaps and never fabricated.
 */
class CoinIntelligenceEngine(private val memeScanner: MemeShitcoinScanner = MemeShitcoinScanner()) {
    fun analyze(input: CoinIntelligenceInput): CoinIntelligenceReport {
        val meme = memeScanner.scan(input.snapshot, input.candles)

        val projectQuality = (input.research?.overallScore ?: input.projectQualityScore ?: 0.0).coerceIn(0.0, 100.0)
        val tokenRisk = (input.tokenomics?.riskScore
            ?: input.tokenomicsScore?.let { 100.0 - it.coerceIn(0.0, 100.0) }
            ?: 50.0).coerceIn(0.0, 100.0)
        val holderRisk = (input.onChain?.concentrationRisk
            ?: input.snapshot.holderConcentrationPercent
            ?: 50.0).coerceIn(0.0, 100.0)
        val unlockRisk = input.unlocks?.let {
            max(100.0 - it.overallScore.coerceIn(0.0, 100.0), max(it.dilutionRisk, it.supplyPressure)).coerceIn(0.0, 100.0)
        } ?: 50.0
        val teamRisk = (input.team?.teamRisk ?: 50.0).coerceIn(0.0, 100.0)
        val investorRisk = input.investors?.let {
            val quality = listOf(it.qualityScore, it.fundingStrength, it.reputationScore)
                .map { value -> value.coerceIn(0.0, 100.0) }
                .average()
            (100.0 - quality).coerceIn(0.0, 100.0)
        } ?: 50.0

        val components = mutableListOf(
            IntelligenceComponent("MARKET", marketScore(input.candles), 0.20, "Price movement and volatility from supplied candles"),
            IntelligenceComponent("MEME_RISK", 100.0 - meme.riskScore, 0.15, "Liquidity, age, concentration, contract and market-behaviour risk"),
            IntelligenceComponent("MOMENTUM", meme.opportunityScore, 0.10, "Recent price/volume opportunity without guaranteed prediction")
        )
        input.whaleActivity?.takeIf { it.dataAvailable }?.let {
            components += IntelligenceComponent("WHALE", 100.0 - it.score.coerceIn(0.0, 100.0), 0.10, "Whale activity from supplied provider data")
        }
        if (input.research != null || input.projectQualityScore != null) {
            components += IntelligenceComponent("PROJECT", projectQuality, 0.10, "Project/product/development quality")
        }
        if (input.tokenomics != null || input.tokenomicsScore != null) {
            components += IntelligenceComponent("TOKENOMICS", 100.0 - tokenRisk, 0.10, "Supply, allocation, inflation and dilution risk")
        }
        input.roadmap?.let {
            components += IntelligenceComponent("ROADMAP", ((it.progressScore + it.credibilityScore) / 2.0).coerceIn(0.0, 100.0), 0.05, "Milestone progress and roadmap credibility")
        }
        input.onChain?.let {
            components += IntelligenceComponent("ONCHAIN", 100.0 - holderRisk, 0.075, "Holder distribution and concentration")
        }
        input.unlocks?.let {
            components += IntelligenceComponent("UNLOCKS", 100.0 - unlockRisk, 0.075, "Unlock, emission and burn pressure")
        }
        input.team?.let {
            components += IntelligenceComponent("TEAM", 100.0 - teamRisk, 0.05, "Team transparency and sourced team risk")
        }
        input.investors?.let {
            components += IntelligenceComponent("INVESTORS", 100.0 - investorRisk, 0.05, "Investor quality, funding strength and reputation")
        }

        val componentWeight = components.sumOf { it.weight }
        val overall = if (componentWeight > 0.0) {
            components.sumOf { it.score.coerceIn(0.0, 100.0) * it.weight } / componentWeight
        } else 0.0

        val riskParts = mutableListOf(meme.riskScore to 0.20)
        input.whaleActivity?.takeIf { it.dataAvailable }?.let { riskParts += it.score.coerceIn(0.0, 100.0) to 0.10 }
        if (input.tokenomics != null || input.tokenomicsScore != null) riskParts += tokenRisk to 0.15
        if (input.onChain != null || input.snapshot.holderConcentrationPercent != null) riskParts += holderRisk to 0.20
        if (input.unlocks != null) riskParts += unlockRisk to 0.15
        if (input.team != null) riskParts += teamRisk to 0.10
        if (input.investors != null) riskParts += investorRisk to 0.10
        val riskWeight = riskParts.sumOf { it.second }
        val weightedRisk = if (riskWeight > 0.0) riskParts.sumOf { it.first * it.second } / riskWeight else meme.riskScore
        val correlatedCriticalRisk = if (holderRisk >= 70.0 && unlockRisk >= 70.0 && input.unlocks != null) {
            ((holderRisk + unlockRisk) / 2.0 * 0.80).coerceIn(0.0, 100.0)
        } else 0.0
        val risk = max(weightedRisk, correlatedCriticalRisk).coerceIn(0.0, 100.0)

        val dataGaps = buildList {
            if (input.research == null && input.projectQualityScore == null) add("PROJECT_RESEARCH")
            if (input.tokenomics == null && input.tokenomicsScore == null) add("TOKENOMICS")
            if (input.onChain == null) add("ONCHAIN")
            if (input.team == null) add("TEAM")
            if (input.investors == null) add("INVESTORS")
        }

        val warnings = buildList {
            addAll(meme.flags)
            if (input.whaleActivity?.dataAvailable == false) add("WHALE_DATA_UNAVAILABLE")
            if (!meme.dataComplete) add("INCOMPLETE_TOKEN_DATA")
            addAll(input.research?.risks.orEmpty())
            addAll(input.roadmap?.riskFlags.orEmpty())
            addAll(input.tokenomics?.findings.orEmpty())
            addAll(input.onChain?.findings.orEmpty())
            addAll(input.unlocks?.risks.orEmpty())
            addAll(input.investors?.risks.orEmpty())
            if (input.team?.anonymous == true) add("ANONYMOUS_TEAM")
            dataGaps.forEach { add("${it}_UNAVAILABLE") }
        }.distinct()

        val sources = collectSources(input)
        val confidence = confidence(input, meme, dataGaps).coerceIn(0.0, 100.0)
        val verdict = when {
            confidence < 45.0 -> "INSUFFICIENT_DATA"
            risk >= 75.0 -> "HIGH_RISK"
            overall >= 75.0 && risk < 45.0 -> "STRONG_PROFILE"
            overall >= 55.0 -> "MIXED_POSITIVE"
            overall >= 40.0 -> "MIXED_CAUTION"
            else -> "WEAK_PROFILE"
        }
        val summary = when (verdict) {
            "INSUFFICIENT_DATA" -> "Low-confidence intelligence: verified research/on-chain data is still required"
            "HIGH_RISK" -> "High-risk coin profile; elevated holder/unlock/token or market risks require caution"
            "STRONG_PROFILE" -> "Strong supplied-data profile with comparatively controlled risk; not a trading guarantee"
            "MIXED_POSITIVE" -> "Generally constructive supplied-data profile with material risks still present"
            "MIXED_CAUTION" -> "Mixed supplied-data profile; review warnings and data gaps before acting"
            else -> "Weak supplied-data profile; elevated caution is appropriate"
        }

        return CoinIntelligenceReport(
            symbol = input.snapshot.symbol,
            market = input.snapshot.market,
            overallScore = overall,
            riskScore = risk,
            opportunityScore = meme.opportunityScore,
            confidenceScore = confidence,
            components = components,
            warnings = warnings,
            dataComplete = meme.dataComplete && dataGaps.isEmpty(),
            summary = summary,
            projectQualityScore = projectQuality,
            tokenomicsRiskScore = tokenRisk,
            holderRiskScore = holderRisk,
            unlockRiskScore = unlockRisk,
            teamRiskScore = teamRisk,
            investorRiskScore = investorRisk,
            dataGaps = dataGaps,
            sourceCount = sources.size,
            verdict = verdict
        )
    }

    private fun marketScore(candles: List<Candle>): Double {
        if (candles.size < 2) return 0.0
        val first = candles.first().close
        val last = candles.last().close
        if (first <= 0.0 || last <= 0.0) return 0.0
        val change = ((last / first) - 1.0) * 100.0
        val ranges = candles.takeLast(20).mapNotNull { candle ->
            if (candle.close > 0.0) abs(candle.high - candle.low) / candle.close * 100.0 else null
        }
        val volatility = ranges.average().takeIf { it.isFinite() } ?: 0.0
        return (50.0 + change.coerceIn(-50.0, 50.0) - volatility.coerceIn(0.0, 30.0)).coerceIn(0.0, 100.0)
    }

    private fun confidence(input: CoinIntelligenceInput, meme: MemeScanResult, dataGaps: List<String>): Double {
        var score = 0.0
        if (input.candles.size >= 20) score += 30.0
        if (meme.liquidityUsd > 0.0) score += 10.0
        if (meme.marketCapUsd > 0.0) score += 10.0
        if (input.snapshot.contractVerified != null) score += 5.0
        if (input.snapshot.holderConcentrationPercent != null) score += 5.0
        input.research?.let { score += (it.confidence.coerceIn(0.0, 100.0) / 100.0) * 10.0 }
        input.tokenomics?.let { score += (it.confidence.coerceIn(0.0, 100.0) / 100.0) * 10.0 }
            ?: input.tokenomicsScore?.let { score += 5.0 }
        input.onChain?.let { score += (it.confidence.coerceIn(0.0, 100.0) / 100.0) * 10.0 }
        input.team?.let { score += (it.confidence.coerceIn(0.0, 100.0) / 100.0) * 5.0 }
        input.investors?.let { score += (it.confidence.coerceIn(0.0, 100.0) / 100.0) * 5.0 }
        if (input.whaleActivity?.dataAvailable == true) score += 3.0
        if (input.roadmap?.available == true) score += 2.0
        if (input.unlocks != null) score += 5.0
        score -= dataGaps.size * 2.0
        return score
    }

    private fun collectSources(input: CoinIntelligenceInput): List<SourceRef> {
        val collected = buildList {
            addAll(input.research?.sources.orEmpty())
            addAll(input.team?.sources.orEmpty())
            addAll(input.investors?.sources.orEmpty())
            input.onChain?.snapshot?.source?.let(::add)
            input.tokenomics?.supply?.source?.let(::add)
            input.tokenomics?.allocations?.mapNotNull { it.source }?.let(::addAll)
            input.unlocks?.events?.mapNotNull { it.source }?.let(::addAll)
            input.unlocks?.burnEvents?.mapNotNull { it.source }?.let(::addAll)
            input.unlocks?.emission?.source?.let(::add)
            input.roadmap?.milestones?.mapNotNull { it.source }?.let(::addAll)
        }
        return collected.distinctBy { listOf(it.title, it.url.orEmpty(), it.timestamp.orEmpty()).joinToString("|") }
    }
}
