package com.notash.cryptobacktester.core

import kotlin.math.abs

/** Deterministic on-chain scoring. Missing observations reduce confidence and never become positive evidence. */
object OnChainIntelligence {
    data class Snapshot(
        val holderCount: Long? = null,
        val holderGrowthPercent: Double? = null,
        val top10HolderPercent: Double? = null,
        val whaleCount: Int? = null,
        val whaleAccumulationPercent: Double? = null,
        val treasuryToExchangePercent: Double? = null,
        val teamToExchangePercent: Double? = null,
        val exchangeNetflowPercent: Double? = null,
        val smartMoneyAccumulationScore: Int? = null,
        val timestamp: Long = System.currentTimeMillis()
    )

    data class Assessment(
        val score: Int,
        val confidence: Int,
        val whaleSignal: String,
        val smartMoneySignal: String,
        val risks: List<String>,
        val reasons: List<String>
    )

    fun assess(snapshot: Snapshot): Assessment {
        val reasons = mutableListOf<String>()
        val risks = mutableListOf<String>()
        val scores = mutableListOf<Int>()

        snapshot.holderGrowthPercent?.let {
            scores += it.coerceIn(-20.0, 20.0).let { v -> ((v + 20.0) / 40.0 * 100).toInt() }
            if (it > 0) reasons += "Holder count is growing"
            if (it < -2) risks += "Holder count is declining"
        }

        snapshot.whaleAccumulationPercent?.let {
            scores += ((it.coerceIn(-20.0, 20.0) + 20.0) / 40.0 * 100).toInt()
            if (it > 0) reasons += "Whale accumulation detected"
            if (it < -2) risks += "Whale distribution detected"
        }

        snapshot.smartMoneyAccumulationScore?.let {
            scores += it.coerceIn(0, 100)
            if (it >= 70) reasons += "Smart Money accumulation is strong"
            if (it < 35) risks += "Smart Money accumulation is weak"
        }

        snapshot.exchangeNetflowPercent?.let {
            val score = ((-it.coerceIn(-20.0, 20.0) + 20.0) / 40.0 * 100).toInt()
            scores += score
            if (it > 2) risks += "Net exchange inflow may increase selling pressure"
            if (it < -2) reasons += "Net exchange outflow supports accumulation"
        }

        snapshot.treasuryToExchangePercent?.let {
            if (it > 2) risks += "Treasury transfers to exchanges are elevated"
        }
        snapshot.teamToExchangePercent?.let {
            if (it > 1) risks += "Team wallet transfers to exchanges detected"
        }
        snapshot.top10HolderPercent?.let {
            if (it >= 50) risks += "Top holders have high supply concentration"
            else if (it < 25) reasons += "Holder concentration is relatively distributed"
        }

        val confidence = listOf(
            snapshot.holderGrowthPercent,
            snapshot.top10HolderPercent,
            snapshot.whaleAccumulationPercent,
            snapshot.exchangeNetflowPercent,
            snapshot.smartMoneyAccumulationScore?.toDouble()
        ).count { it != null } * 20

        val score = if (scores.isEmpty()) 0 else scores.average().toInt().coerceIn(0, 100)
        val whaleSignal = when {
            snapshot.whaleAccumulationPercent == null -> "Unknown"
            snapshot.whaleAccumulationPercent > 2 -> "Accumulation"
            snapshot.whaleAccumulationPercent < -2 -> "Distribution"
            else -> "Neutral"
        }
        val smartMoneySignal = when {
            snapshot.smartMoneyAccumulationScore == null -> "Unknown"
            snapshot.smartMoneyAccumulationScore >= 70 -> "Accumulating"
            snapshot.smartMoneyAccumulationScore <= 30 -> "Distributing/weak"
            else -> "Neutral"
        }

        return Assessment(
            score = score,
            confidence = confidence.coerceIn(0, 100),
            whaleSignal = whaleSignal,
            smartMoneySignal = smartMoneySignal,
            risks = risks.distinct().take(8),
            reasons = reasons.distinct().take(8)
        )
    }
}
