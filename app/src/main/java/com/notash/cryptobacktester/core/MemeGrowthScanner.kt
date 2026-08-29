package com.notash.cryptobacktester.core

import kotlin.math.abs

/**
 * Conservative scanner for meme, micro-cap and early-growth candidates.
 * Missing data never becomes positive evidence.
 */
object MemeGrowthScanner {
    enum class Category { MEME, SHITCOIN, UTILITY, DEFI, AI, GAMING, UNKNOWN }

    data class Input(
        val symbol: String,
        val category: Category = Category.UNKNOWN,
        val market: MarketSnapshot,
        val intelligence: TokenIntelligence,
        val pumpScore: Int,
        val dumpScore: Int,
        val onChainScore: Int = 0,
        val narrativeScore: Int = 0,
        val contractRiskScore: Int = 0
    )

    data class Result(
        val symbol: String,
        val category: Category,
        val opportunityScore: Int,
        val earlyGrowth: Boolean,
        val highRisk: Boolean,
        val dataConfidence: Int,
        val reasons: List<String>,
        val risks: List<String>
    )

    fun scan(input: Input): Result {
        val confidence = input.intelligence.dataConfidence.coerceIn(0, 100)
        val liquidity = liquidityScore(input.market.liquidityUsd)
        val holderGrowth = input.intelligence.holderGrowthPercent ?: 0.0
        val volumeRatio = input.market.volumeBaseline24h?.takeIf { it > 0.0 }
            ?.let { input.market.volume24h / it } ?: 0.0
        val unlockRisk = TokenomicsAnalyzer.analyze(
            input.intelligence,
            input.market.timestamp
        ).unlockRiskScore

        val earlyScore = MarketSignals.earlyGrowthScore(
            input.market.change24hPercent,
            volumeRatio,
            holderGrowth,
            input.onChainScore,
            input.market.sectorScore,
            unlockRisk,
            input.market.fundingPercent ?: 0.0
        )

        val opportunity = listOf(
            earlyScore,
            input.pumpScore,
            input.onChainScore,
            input.narrativeScore,
            liquidity,
            confidence
        ).average().toInt().coerceIn(0, 100)

        val reasons = buildList {
            if (earlyScore >= 70) add("Early-growth market conditions are strengthening")
            if (volumeRatio >= 1.5) add("Volume is accelerating")
            if (holderGrowth > 0.0) add("Holder count is growing")
            if (input.onChainScore >= 70) add("On-chain accumulation signal is strong")
            if (input.narrativeScore >= 70) add("Narrative momentum is strong")
            if (liquidity >= 70) add("Liquidity is relatively healthy")
        }.take(6)

        val risks = buildList {
            if (confidence < 60) add("Data confidence is low")
            if (liquidity < 40) add("Liquidity is low")
            if (input.dumpScore >= 65) add("Dump pressure is elevated")
            if (input.contractRiskScore >= 60) add("Contract/security risk is elevated")
            if (unlockRisk < 60) add("Near-term unlock pressure is elevated")
            if ((input.intelligence.topHolderPercent ?: 0.0) >= 40.0) add("Top-holder concentration is high")
            if (input.category == Category.SHITCOIN) add("Speculative token category: extreme risk")
        }.distinct().take(8)

        val highRisk = confidence < 50 || liquidity < 25 || input.contractRiskScore >= 70 ||
            input.category == Category.SHITCOIN

        return Result(
            symbol = input.symbol,
            category = input.category,
            opportunityScore = opportunity,
            earlyGrowth = earlyScore >= 70 && confidence >= 50 && !highRisk,
            highRisk = highRisk,
            dataConfidence = confidence,
            reasons = reasons,
            risks = risks
        )
    }

    private fun liquidityScore(liquidityUsd: Double?): Int = when {
        liquidityUsd == null -> 0
        liquidityUsd <= 0.0 -> 0
        liquidityUsd < 25_000.0 -> 20
        liquidityUsd < 100_000.0 -> 45
        liquidityUsd < 500_000.0 -> 70
        else -> 90
    }
}
