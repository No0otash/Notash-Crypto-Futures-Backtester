package com.notash.cryptobacktester.core

/** Combines market signals with token intelligence without treating missing data as positive evidence. */
class MarketRadarService {
    fun evaluate(
        intelligence: TokenIntelligence,
        market: MarketSnapshot,
        narrativeScore: Int = 0,
        onChainScore: Int = 0,
        earlySignals: List<String> = emptyList()
    ): GrowthCandidate {
        val early = MarketSignals.earlyGrowthScore(
            priceChangePercent = market.change24hPercent,
            volumeRatio = if (market.volume24h <= 0.0) 0.0 else 1.0,
            holderGrowthPercent = 0.0,
            whaleAccumulationScore = onChainScore,
            sectorScore = market.sectorScore,
            unlockRiskScore = 100,
            fundingPercent = market.fundingPercent ?: 0.0
        )
        val signals = if (earlySignals.isEmpty() && early >= 70) {
            listOf("Early market conditions are strengthening")
        } else earlySignals
        return GrowthScoringEngine.score(
            intelligence = intelligence,
            market = market,
            narrativeScore = narrativeScore,
            onChainScore = onChainScore,
            earlySignals = signals
        )
    }
}
