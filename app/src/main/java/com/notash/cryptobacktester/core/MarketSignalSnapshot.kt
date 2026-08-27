package com.notash.cryptobacktester.core

import kotlinx.serialization.Serializable

@Serializable
data class MarketSignalSnapshot(
    val symbol: String,
    val price: Double,
    val change24hPercent: Double,
    val volume24h: Double,
    val volumeRatio: Double = 1.0,
    val holderGrowthPercent: Double? = null,
    val whaleAccumulationScore: Int? = null,
    val exchangeInflowUsd: Double? = null,
    val exchangeOutflowUsd: Double? = null,
    val openInterestUsd: Double? = null,
    val fundingPercent: Double? = null,
    val longShortRatio: Double? = null,
    val btcTrendScore: Int = 0,
    val btcDominance: Double? = null,
    val sectorScore: Int = 0,
    val momentumScore: Int = 0,
    val relativeStrengthScore: Int = 0,
    val liquidityUsd: Double? = null,
    val updatedAt: Long = 0L
)

object MarketSignalAnalyzer {
    fun analyze(s: MarketSignalSnapshot): Pair<Int, List<String>> {
        val early = MarketSignals.earlyGrowthScore(
            priceChangePercent = s.change24hPercent,
            volumeRatio = s.volumeRatio,
            holderGrowthPercent = s.holderGrowthPercent ?: 0.0,
            whaleAccumulationScore = s.whaleAccumulationScore ?: 0,
            sectorScore = s.sectorScore,
            unlockRiskScore = 75,
            fundingPercent = s.fundingPercent ?: 0.0
        )
        val reasons = buildList {
            if (s.volumeRatio >= 1.5) add("Unusual volume expansion")
            if ((s.holderGrowthPercent ?: 0.0) >= 3.0) add("Holder growth is accelerating")
            if ((s.whaleAccumulationScore ?: 0) >= 70) add("Whale accumulation signal")
            if (s.sectorScore >= 70) add("Sector strength is positive")
            if (s.relativeStrengthScore >= 70) add("Relative strength versus BTC/sector is positive")
            if (s.change24hPercent in -2.0..6.0 && early >= 70) add("Price has not yet entered an extreme move")
        }
        return early to reasons
    }
}
