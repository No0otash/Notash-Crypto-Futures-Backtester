package com.notash.cryptobacktester.core

import kotlin.math.abs

object MarketSignals {
    fun pumpScore(change24h: Double, volumeRatio: Double, momentum: Int): Int =
        weighted(change24hMagnitude = change24h.coerceAtLeast(0.0), volumeRatio = volumeRatio, momentum = momentum)

    fun dumpScore(change24h: Double, volumeRatio: Double, momentum: Int): Int =
        weighted(change24hMagnitude = (-change24h).coerceAtLeast(0.0), volumeRatio = volumeRatio, momentum = momentum)

    fun earlyGrowthScore(
        priceChangePercent: Double,
        volumeRatio: Double,
        holderGrowthPercent: Double,
        whaleAccumulationScore: Int,
        sectorScore: Int,
        unlockRiskScore: Int,
        fundingPercent: Double
    ): Int {
        val price = if (priceChangePercent in -2.0..6.0) 90 else (100 - abs(priceChangePercent) * 3).toInt().coerceIn(0, 100)
        val volume = ((volumeRatio.coerceIn(0.0, 4.0) / 4.0) * 100).toInt()
        val holders = (holderGrowthPercent.coerceIn(0.0, 20.0) / 20.0 * 100).toInt()
        val funding = if (abs(fundingPercent) <= 0.05) 90 else if (abs(fundingPercent) <= 0.1) 70 else 40
        return weightedAverage(price, volume, holders, whaleAccumulationScore, sectorScore, unlockRiskScore, funding)
    }

    private fun weighted(change24hMagnitude: Double, volumeRatio: Double, momentum: Int): Int {
        val change = (change24hMagnitude.coerceIn(0.0, 30.0) / 30.0 * 100).toInt()
        val volume = (volumeRatio.coerceIn(0.0, 5.0) / 5.0 * 100).toInt()
        return weightedAverage(change, volume, momentum.coerceIn(0, 100))
    }

    private fun weightedAverage(vararg values: Int): Int =
        values.average().toInt().coerceIn(0, 100)
}
