package com.notash.cryptobacktester.core

import kotlinx.serialization.Serializable

@Serializable
data class TokenSupply(
    val maxSupply: Double? = null,
    val totalSupply: Double? = null,
    val circulatingSupply: Double? = null,
    val unlockedSupply: Double? = null,
    val teamPercent: Double? = null,
    val investorPercent: Double? = null,
    val treasuryPercent: Double? = null,
    val ecosystemPercent: Double? = null,
    val foundationPercent: Double? = null,
    val marketingPercent: Double? = null,
    val airdropPercent: Double? = null,
    val stakingPercent: Double? = null
)

@Serializable
data class UnlockEvent(
    val timestamp: Long,
    val amount: Double,
    val allocation: String,
    val percentOfSupply: Double,
    val source: String? = null
)

@Serializable
data class TokenIntelligence(
    val symbol: String,
    val project: String? = null,
    val problemSolved: String? = null,
    val product: String? = null,
    val utility: String? = null,
    val users: Long? = null,
    val competitors: List<String> = emptyList(),
    val roadmapStatus: String? = null,
    val completedMilestones: List<String> = emptyList(),
    val delayedMilestones: List<String> = emptyList(),
    val founders: List<String> = emptyList(),
    val coreTeam: List<String> = emptyList(),
    val advisors: List<String> = emptyList(),
    val investors: List<String> = emptyList(),
    val fundingUsd: Double? = null,
    val tokenSupply: TokenSupply = TokenSupply(),
    val unlocks: List<UnlockEvent> = emptyList(),
    val burnPercentAnnual: Double? = null,
    val emissionPercentAnnual: Double? = null,
    val holderCount: Long? = null,
    val holderGrowthPercent: Double? = null,
    val topHolderPercent: Double? = null,
    val whaleCount: Int? = null,
    val dataConfidence: Int = 0,
    val highRisk: Boolean = true
)

@Serializable
data class MarketSnapshot(
    val price: Double,
    val change24hPercent: Double,
    val volume24h: Double,
    val volumeBaseline24h: Double? = null,
    val liquidityUsd: Double? = null,
    val openInterestUsd: Double? = null,
    val fundingPercent: Double? = null,
    val longShortRatio: Double? = null,
    val btcTrendScore: Int = 0,
    val btcDominance: Double? = null,
    val sectorScore: Int = 0,
    val momentumScore: Int = 0,
    val relativeStrengthScore: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class GrowthScore(
    val productUtility: Int,
    val team: Int,
    val investors: Int,
    val roadmap: Int,
    val tokenomics: Int,
    val unlockRisk: Int,
    val burnMechanism: Int,
    val onChain: Int,
    val marketTrend: Int,
    val liquidity: Int,
    val narrative: Int,
    val dataConfidence: Int
) {
    val total: Int get() = (productUtility + team + investors + roadmap + tokenomics + unlockRisk + burnMechanism + onChain + marketTrend + liquidity + narrative) * 100 / 110
}

@Serializable
data class GrowthCandidate(
    val symbol: String,
    val score: GrowthScore,
    val reasons: List<String> = emptyList(),
    val risks: List<String> = emptyList(),
    val bullScenario: String? = null,
    val baseScenario: String? = null,
    val bearScenario: String? = null,
    val earlySignals: List<String> = emptyList()
)