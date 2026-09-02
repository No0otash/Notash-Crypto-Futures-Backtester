package com.notash.cryptobacktester.intelligence

/**
 * Bridges existing multi-exchange market snapshots into PumpDumpRadarEngine.
 * External intelligence is optional and remains explicitly absent when unavailable.
 */
class PumpDumpRadarOrchestrator(private val engine: PumpDumpRadarEngine = PumpDumpRadarEngine()) {
    data class Result(val signals: List<RadarPumpDumpSignal>, val scannedAtMs: Long)

    fun analyze(
        snapshots: List<RadarMarketSnapshot>,
        previousQuoteVolumeByKey: Map<String, Double> = emptyMap(),
        volatilityByKey: Map<String, Double> = emptyMap(),
        spreadByKey: Map<String, Double> = emptyMap(),
        openInterestChangeByKey: Map<String, Double> = emptyMap(),
        intelligenceBySymbol: Map<String, PumpDumpIntelligence> = emptyMap()
    ): Result {
        val signals: List<RadarPumpDumpSignal> = snapshots.map { snapshot ->
            val key = "${snapshot.exchange.uppercase()}:${snapshot.symbol.uppercase()}"
            engine.analyze(
                PumpDumpMarketEvidence(
                    snapshot = snapshot,
                    previousQuoteVolume24h = previousQuoteVolumeByKey[key],
                    volatilityPercent = volatilityByKey[key],
                    spreadPercent = spreadByKey[key],
                    openInterestChangePercent = openInterestChangeByKey[key]
                ),
                intelligenceBySymbol[snapshot.symbol.uppercase()] ?: PumpDumpIntelligence()
            )
        }
        return Result(
            signals = signals.groupBy { it.symbol }
                .values
                .map { rows -> combine(rows) }
                .sortedByDescending { it.score },
            scannedAtMs = System.currentTimeMillis()
        )
    }

    private fun combine(rows: List<RadarPumpDumpSignal>): RadarPumpDumpSignal {
        if (rows.size == 1) return rows.single()
        val best = rows.maxBy { it.score }
        // Independent exchange observations increase confidence without fabricating new evidence.
        val corroborationBonus = ((rows.size - 1) * 15).coerceAtMost(20)
        val averageConfidence = rows.map { it.confidence }.average().roundToInt()
        val combinedConfidence = (averageConfidence + corroborationBonus).coerceIn(15, 95)
        return best.copy(
            confidence = combinedConfidence,
            reasons = rows.flatMap { it.reasons }.distinct().take(8),
            dataGaps = rows.flatMap { it.dataGaps }.distinct()
        )
    }

    private fun Double.roundToInt() = kotlin.math.round(this).toInt()
}