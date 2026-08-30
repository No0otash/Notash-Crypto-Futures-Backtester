package com.notash.cryptobacktester.intelligence

import com.notash.cryptobacktester.core.Candle
import kotlin.math.abs

/** Price/volume anomaly detector. It consumes existing Market/Candle data; it does not fetch data itself. */
class PumpDumpDetector(
    private val priceThresholdPercent: Double = 3.0,
    private val volumeRatioThreshold: Double = 2.0,
    private val extremePricePercent: Double = 8.0,
    private val extremeVolumeRatio: Double = 5.0,
    private val baselinePeriod: Int = 20,
    private val cooldownCandles: Int = 3
) {
    private var lastSignalIndex = Int.MIN_VALUE

    fun analyze(candles: List<Candle>): PumpDumpSignal? {
        if (candles.size < baselinePeriod + 1) return null
        val currentIndex = candles.lastIndex
        if (currentIndex - lastSignalIndex < cooldownCandles) return null
        val current = candles[currentIndex]
        val previous = candles[currentIndex - 1]
        if (previous.close <= 0.0) return null

        val priceChange = (current.close - previous.close) / previous.close * 100.0
        val baseline = candles.takeLast(baselinePeriod + 1).dropLast(1)
        val avgVolume = baseline.map { it.volume }.average()
        if (avgVolume <= 0.0) return null
        val volumeRatio = current.volume / avgVolume
        val priceScore = (abs(priceChange) / priceThresholdPercent).coerceAtMost(3.0)
        val volumeScore = (volumeRatio / volumeRatioThreshold).coerceAtMost(3.0)
        val score = ((priceScore * 0.6 + volumeScore * 0.4) / 3.0 * 100.0).coerceIn(0.0, 100.0)

        if (abs(priceChange) < priceThresholdPercent || volumeRatio < volumeRatioThreshold) return null
        lastSignalIndex = currentIndex
        val direction = if (priceChange > 0) PumpDumpDirection.PUMP else PumpDumpDirection.DUMP
        val severity = when {
            abs(priceChange) >= extremePricePercent || volumeRatio >= extremeVolumeRatio -> PumpDumpSeverity.EXTREME
            score >= 75.0 -> PumpDumpSeverity.HIGH
            score >= 50.0 -> PumpDumpSeverity.MEDIUM
            else -> PumpDumpSeverity.LOW
        }
        return PumpDumpSignal(
            timestamp = current.timestamp,
            direction = direction,
            severity = severity,
            score = score,
            priceBefore = previous.close,
            priceAfter = current.close,
            priceChangePercent = priceChange,
            volume = current.volume,
            averageVolume = avgVolume,
            volumeRatio = volumeRatio,
            reason = if (direction == PumpDumpDirection.PUMP) "Positive price spike confirmed by abnormal volume" else "Negative price spike confirmed by abnormal volume"
        )
    }

    fun reset() { lastSignalIndex = Int.MIN_VALUE }
}

enum class PumpDumpDirection { PUMP, DUMP }
enum class PumpDumpSeverity { LOW, MEDIUM, HIGH, EXTREME }

data class PumpDumpSignal(
    val timestamp: Long,
    val direction: PumpDumpDirection,
    val severity: PumpDumpSeverity,
    val score: Double,
    val priceBefore: Double,
    val priceAfter: Double,
    val priceChangePercent: Double,
    val volume: Double,
    val averageVolume: Double,
    val volumeRatio: Double,
    val reason: String
)
