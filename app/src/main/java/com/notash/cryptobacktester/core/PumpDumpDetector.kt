package com.notash.cryptobacktester.core

import kotlin.math.abs

/** Deterministic market classifier. Missing inputs reduce confidence rather than creating bullish evidence. */
object PumpDumpDetector {
    enum class Direction { PUMP, DUMP, NEUTRAL }

    data class Result(
        val direction: Direction,
        val score: Int,
        val confidence: Int,
        val reasons: List<String>,
        val highRisk: Boolean
    )

    fun detect(
        change24hPercent: Double,
        volumeRatio: Double?,
        momentumScore: Int,
        liquidityUsd: Double? = null
    ): Result {
        val volume = volumeRatio?.coerceAtLeast(0.0)
        val momentum = momentumScore.coerceIn(0, 100)
        val magnitude = abs(change24hPercent)
        val volumeScore = volume?.let { ((it.coerceIn(0.0, 5.0) / 5.0) * 100).toInt() }
        val directionalScore = when {
            change24hPercent >= 0 -> MarketSignals.pumpScore(change24hPercent, volume ?: 0.0, momentum)
            else -> MarketSignals.dumpScore(change24hPercent, volume ?: 0.0, momentum)
        }
        val evidence = listOfNotNull(volumeScore).size + 1
        val confidence = ((evidence / 2.0) * 100).toInt().coerceIn(0, 100)
        val threshold = when {
            magnitude >= 15.0 && (volume ?: 0.0) >= 2.0 -> 60
            magnitude >= 8.0 && (volume ?: 0.0) >= 1.5 -> 55
            else -> 70
        }
        val direction = when {
            change24hPercent > 0 && directionalScore >= threshold -> Direction.PUMP
            change24hPercent < 0 && directionalScore >= threshold -> Direction.DUMP
            else -> Direction.NEUTRAL
        }
        val reasons = buildList {
            if (magnitude >= 8.0) add("Large 24h price move: %.2f%%".format(change24hPercent))
            if (volume != null && volume >= 1.5) add("Volume is %.2fx baseline".format(volume))
            if (momentum >= 70) add("Momentum is elevated")
            if (liquidityUsd != null && liquidityUsd < 1_000_000.0) add("Low liquidity increases manipulation risk")
            if (volume == null) add("Volume baseline is unavailable; signal confidence is limited")
        }
        return Result(
            direction = direction,
            score = directionalScore,
            confidence = confidence,
            reasons = reasons,
            highRisk = liquidityUsd != null && liquidityUsd < 1_000_000.0 || volume == null
        )
    }
}
