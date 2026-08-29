package com.notash.cryptobacktester.core

import kotlin.math.abs

/** Deterministic on-chain interpretation layer. Missing fields remain unknown, not bullish. */
object OnChainAnalyzer {
    data class Result(
        val score: Int,
        val confidence: Int,
        val whaleSignal: String,
        val exchangeFlowSignal: String,
        val concentrationRisk: String?,
        val reasons: List<String>,
        val risks: List<String>
    )

    fun analyze(snapshot: MarketSignalSnapshot): Result {
        val evidence = mutableListOf<Int>()
        val reasons = mutableListOf<String>()
        val risks = mutableListOf<String>()

        snapshot.whaleAccumulationScore?.let {
            evidence += it.coerceIn(0, 100)
            if (it >= 70) reasons += "Whale accumulation is strong"
            if (it <= 30) risks += "Whale accumulation signal is weak"
        }

        snapshot.holderGrowthPercent?.let {
            val holderScore = ((it.coerceIn(-10.0, 20.0) + 10.0) / 30.0 * 100.0).toInt()
            evidence += holderScore
            if (it >= 3.0) reasons += "Holder count is expanding"
            if (it < 0.0) risks += "Holder count is declining"
        }

        val flow = exchangeFlow(snapshot.exchangeInflowUsd, snapshot.exchangeOutflowUsd)
        flow.score?.let { evidence += it }
        reasons += flow.reason?.let(::listOf).orEmpty()
        risks += flow.risk?.let(::listOf).orEmpty()

        val concentration = snapshot.liquidityUsd?.let { liquidity ->
            snapshot.whaleAccumulationScore?.let { whale ->
                if (whale >= 80 && liquidity < 1_000_000) {
                    risks += "Strong whale activity with low liquidity can amplify manipulation risk"
                }
            }
            snapshot.whaleAccumulationScore
        }

        val score = if (evidence.isEmpty()) 0 else evidence.average().toInt().coerceIn(0, 100)
        val confidence = when {
            evidence.size >= 3 -> 90
            evidence.size == 2 -> 70
            evidence.size == 1 -> 50
            else -> 0
        }

        return Result(
            score = score,
            confidence = confidence,
            whaleSignal = whaleLabel(snapshot.whaleAccumulationScore),
            exchangeFlowSignal = flow.label,
            concentrationRisk = concentrationRisk(snapshot.whaleAccumulationScore),
            reasons = reasons.distinct().take(6),
            risks = risks.distinct().take(6)
        )
    }

    private data class FlowResult(
        val score: Int?,
        val label: String,
        val reason: String?,
        val risk: String?
    )

    private fun exchangeFlow(inflow: Double?, outflow: Double?): FlowResult {
        if (inflow == null || outflow == null) {
            return FlowResult(null, "Unknown", null, "Exchange flow data is incomplete")
        }
        val total = inflow + outflow
        if (total <= 0.0) return FlowResult(null, "Unknown", null, "Exchange flow volume is unavailable")
        val outflowShare = (outflow / total).coerceIn(0.0, 1.0)
        val score = (outflowShare * 100.0).toInt()
        return when {
            outflowShare >= 0.65 -> FlowResult(score, "Accumulation / net outflow", "Net exchange outflow supports accumulation", null)
            outflowShare <= 0.35 -> FlowResult(score, "Distribution / net inflow", null, "Net exchange inflow can increase sell-side supply")
            else -> FlowResult(score, "Balanced", null, null)
        }
    }

    private fun whaleLabel(score: Int?): String = when {
        score == null -> "Unknown"
        score >= 80 -> "Strong accumulation"
        score >= 60 -> "Moderate accumulation"
        score >= 40 -> "Neutral"
        else -> "Distribution / weak accumulation"
    }

    private fun concentrationRisk(whaleScore: Int?): String? = when {
        whaleScore == null -> null
        whaleScore >= 80 -> "Whale concentration/activity requires monitoring"
        whaleScore <= 25 -> "Potential distribution pressure"
        else -> null
    }
}
