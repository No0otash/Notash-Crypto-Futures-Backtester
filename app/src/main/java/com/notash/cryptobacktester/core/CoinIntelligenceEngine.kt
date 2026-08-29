package com.notash.cryptobacktester.core

import kotlinx.serialization.Serializable

/** Unified evidence-based dossier for a coin. Missing evidence lowers confidence and never becomes a positive score. */
class CoinIntelligenceEngine {
    fun analyze(
        intelligence: TokenIntelligence,
        market: MarketSnapshot,
        narrativeScore: Int = 0,
        onChainScore: Int = 0
    ): CoinIntelligenceReport {
        val token = TokenomicsAnalyzer.analyze(intelligence, market.timestamp)
        val early = MarketSignals.earlyGrowthScore(
            priceChangePercent = market.change24hPercent,
            volumeRatio = market.volumeBaseline24h?.takeIf { it > 0.0 }?.let { market.volume24h / it } ?: 0.0,
            holderGrowthPercent = intelligence.holderGrowthPercent ?: 0.0,
            whaleAccumulationScore = onChainScore,
            sectorScore = market.sectorScore,
            unlockRiskScore = token.unlockRiskScore,
            fundingPercent = market.fundingPercent ?: 0.0
        )
        val growth = GrowthScoringEngine.score(
            intelligence = intelligence,
            market = market,
            narrativeScore = narrativeScore,
            onChainScore = onChainScore,
            earlySignals = if (early >= 70) listOf("Early-growth conditions detected") else emptyList()
        )
        val evidence = EvidenceSummary(
            project = !intelligence.project.isNullOrBlank(),
            product = !intelligence.product.isNullOrBlank(),
            utility = !intelligence.utility.isNullOrBlank(),
            users = intelligence.users != null,
            roadmap = !intelligence.roadmapStatus.isNullOrBlank(),
            team = intelligence.founders.isNotEmpty() || intelligence.coreTeam.isNotEmpty(),
            investors = intelligence.investors.isNotEmpty(),
            tokenomics = intelligence.tokenSupply.maxSupply != null || intelligence.tokenSupply.circulatingSupply != null,
            unlockSchedule = intelligence.unlocks.isNotEmpty(),
            onChain = intelligence.holderCount != null || intelligence.topHolderPercent != null || intelligence.whaleCount != null,
            market = market.volume24h > 0.0 || market.liquidityUsd != null
        )
        val evidenceConfidence = (evidence.knownCount * 100) / evidence.totalCount
        val confidence = minOf(intelligence.dataConfidence.coerceIn(0, 100), evidenceConfidence)
        val risks = buildList {
            if (confidence < 60) add("Low data confidence")
            if (intelligence.highRisk) add("High-risk classification")
            if ((intelligence.topHolderPercent ?: 0.0) >= 30.0) add("High top-holder concentration")
            if (token.unlockRiskScore < 60) add("Elevated near-term unlock pressure")
            if ((intelligence.emissionPercentAnnual ?: 0.0) > (intelligence.burnPercentAnnual ?: 0.0)) add("Net annual supply expansion")
            if ((market.liquidityUsd ?: 0.0) > 0.0 && market.liquidityUsd!! < 100_000.0) add("Low liquidity")
            if (!evidence.unlockSchedule) add("No verified unlock schedule")
            if (!evidence.onChain) add("On-chain evidence is incomplete")
            if (!evidence.team) add("Team identity evidence is incomplete")
            if (!evidence.product) add("No verified product evidence")
        }.distinct()
        val dossierNotes = buildList {
            addAll(token.notes)
            if (evidence.product && evidence.users) add("Product and user evidence are available")
            if (evidence.roadmap && intelligence.delayedMilestones.isNotEmpty()) add("Roadmap contains delayed milestones")
            if (intelligence.fundingUsd != null) add("Funding evidence is available")
            if (early >= 70) add("Market conditions qualify as an early-growth setup")
        }.distinct().take(12)
        return CoinIntelligenceReport(
            symbol = intelligence.symbol,
            project = intelligence.project,
            problemSolved = intelligence.problemSolved,
            product = intelligence.product,
            utility = intelligence.utility,
            roadmap = RoadmapSummary(intelligence.roadmapStatus, intelligence.completedMilestones, intelligence.delayedMilestones),
            team = TeamSummary(intelligence.founders, intelligence.coreTeam, intelligence.advisors),
            investors = intelligence.investors,
            fundingUsd = intelligence.fundingUsd,
            tokenomics = TokenomicsSummary(intelligence.tokenSupply, intelligence.burnPercentAnnual, intelligence.emissionPercentAnnual, token.unlockRiskScore),
            onChain = OnChainSummary(intelligence.holderCount, intelligence.holderGrowthPercent, intelligence.topHolderPercent, intelligence.whaleCount, onChainScore),
            market = market,
            growthScore = growth.score,
            earlyGrowthScore = early,
            dataConfidence = confidence,
            evidence = evidence,
            notes = dossierNotes,
            risks = risks,
            bullScenario = growth.bullScenario,
            baseScenario = growth.baseScenario,
            bearScenario = growth.bearScenario
        )
    }
}

@Serializable
data class EvidenceSummary(
    val project: Boolean,
    val product: Boolean,
    val utility: Boolean,
    val users: Boolean,
    val roadmap: Boolean,
    val team: Boolean,
    val investors: Boolean,
    val tokenomics: Boolean,
    val unlockSchedule: Boolean,
    val onChain: Boolean,
    val market: Boolean
) {
    val totalCount: Int get() = 11
    val knownCount: Int get() = listOf(project, product, utility, users, roadmap, team, investors, tokenomics, unlockSchedule, onChain, market).count { it }
}

@Serializable
data class RoadmapSummary(
    val status: String?,
    val completed: List<String>,
    val delayed: List<String>
)

@Serializable
data class TeamSummary(
    val founders: List<String>,
    val coreTeam: List<String>,
    val advisors: List<String>
)

@Serializable
data class TokenomicsSummary(
    val supply: TokenSupply,
    val burnPercentAnnual: Double?,
    val emissionPercentAnnual: Double?,
    val unlockRiskScore: Int
)

@Serializable
data class OnChainSummary(
    val holderCount: Long?,
    val holderGrowthPercent: Double?,
    val topHolderPercent: Double?,
    val whaleCount: Int?,
    val score: Int
)

@Serializable
data class CoinIntelligenceReport(
    val symbol: String,
    val project: String?,
    val problemSolved: String?,
    val product: String?,
    val utility: String?,
    val roadmap: RoadmapSummary,
    val team: TeamSummary,
    val investors: List<String>,
    val fundingUsd: Double?,
    val tokenomics: TokenomicsSummary,
    val onChain: OnChainSummary,
    val market: MarketSnapshot,
    val growthScore: GrowthScore,
    val earlyGrowthScore: Int,
    val dataConfidence: Int,
    val evidence: EvidenceSummary,
    val notes: List<String> = emptyList(),
    val risks: List<String>,
    val bullScenario: String?,
    val baseScenario: String?,
    val bearScenario: String?
)