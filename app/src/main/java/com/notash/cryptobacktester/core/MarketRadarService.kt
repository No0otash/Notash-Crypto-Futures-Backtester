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
        val volumeRatio = market.volumeBaseline24h?.takeIf { it > 0.0 }?.let { market.volume24h / it } ?: 0.0
        val holderGrowth = intelligence.holderGrowthPercent ?: 0.0
        val unlockRisk = TokenomicsAnalyzer.analyze(intelligence, System.currentTimeMillis()).unlockRiskScore
        val early = MarketSignals.earlyGrowthScore(
            priceChangePercent = market.change24hPercent,
            volumeRatio = volumeRatio,
            holderGrowthPercent = holderGrowth,
            whaleAccumulationScore = onChainScore,
            sectorScore = market.sectorScore,
            unlockRiskScore = unlockRisk,
            fundingPercent = market.fundingPercent ?: 0.0
        )
        val signals = buildList {
            addAll(earlySignals)
            if (market.change24hPercent in -2.0..6.0 && volumeRatio >= 1.5) {
                add("Price is still relatively early while volume is accelerating")
            }
            if (holderGrowth > 0.0) add("Holder growth: %.2f%%".format(holderGrowth))
            if (onChainScore >= 70) add("On-chain accumulation signal is strong")
            if (market.sectorScore >= 70) add("Sector strength is positive")
            if (unlockRisk < 60) add("Near-term unlock pressure is elevated")
            if (market.fundingPercent != null && kotlin.math.abs(market.fundingPercent) <= 0.05) {
                add("Funding is not showing severe crowding")
            }
        }.distinct().take(8)
        val finalSignals = if (signals.isEmpty() && early >= 70) {
            listOf("Early market conditions are strengthening")
        } else signals
        return GrowthScoringEngine.score(
            intelligence = intelligence,
            market = market,
            narrativeScore = narrativeScore,
            onChainScore = onChainScore,
            earlySignals = finalSignals
        )
    }
}
