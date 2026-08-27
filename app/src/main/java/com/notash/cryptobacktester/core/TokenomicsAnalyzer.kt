package com.notash.cryptobacktester.core

import kotlin.math.max

object TokenomicsAnalyzer {
    data class Result(
        val circulatingPercent: Double?,
        val netSupplyChangePercent: Double?,
        val next90DayUnlockPercent: Double,
        val unlockRiskScore: Int,
        val notes: List<String>
    )

    fun analyze(intelligence: TokenIntelligence, nowMillis: Long): Result {
        val supply = intelligence.tokenSupply
        val circulatingPercent = supply.maxSupply?.takeIf { it > 0 }
            ?.let { ((supply.circulatingSupply ?: 0.0) / it * 100.0).coerceIn(0.0, 100.0) }
        val ninetyDays = nowMillis + 90L * 24L * 60L * 60L * 1000L
        val nextUnlock = intelligence.unlocks
            .filter { it.timestamp in (nowMillis + 1)..ninetyDays }
            .sumOf { max(0.0, it.percentOfSupply) }
        val burn = intelligence.burnPercentAnnual ?: 0.0
        val emission = intelligence.emissionPercentAnnual ?: 0.0
        val net = if (intelligence.burnPercentAnnual != null || intelligence.emissionPercentAnnual != null) emission - burn else null
        val risk = when {
            nextUnlock >= 20 -> 15
            nextUnlock >= 10 -> 35
            nextUnlock >= 5 -> 60
            nextUnlock > 0 -> 80
            else -> if (intelligence.dataConfidence >= 70) 90 else 0
        }
        val notes = buildList {
            if (circulatingPercent != null) add("Circulating supply: %.2f%% of max supply".format(circulatingPercent))
            if (nextUnlock > 0) add("%.2f%% of supply is scheduled to unlock within 90 days".format(nextUnlock))
            if (net != null) add("Net supply change: %.2f%% annually".format(net))
            if (intelligence.unlocks.isEmpty()) add("No verified unlock schedule supplied")
            if (intelligence.topHolderPercent != null) add("Top-holder concentration: %.2f%%".format(intelligence.topHolderPercent))
        }
        return Result(circulatingPercent, net, nextUnlock, risk, notes)
    }
}
