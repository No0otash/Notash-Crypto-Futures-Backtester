package com.notash.cryptobacktester.intelligence

import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.round

data class RadarMarketSnapshot(
    val exchange: String,
    val symbol: String,
    val lastPrice: Double,
    val open24h: Double,
    val high24h: Double,
    val low24h: Double,
    val volume24h: Double,
    val quoteVolume24h: Double,
    val buyVolume24h: Double? = null,
    val sellVolume24h: Double? = null,
    val openInterest: Double? = null,
    val timestampMs: Long
)

data class RadarSignal(
    val symbol: String,
    val exchanges: List<String>,
    val pumpPotential: Int,
    val dumpRisk: Int,
    val confidence: Int,
    val momentum: Int,
    val volumePressure: Int,
    val liquidityScore: Int,
    val whalePressure: Int? = null,
    val reasons: List<String>,
    val dataQuality: Double
)

/** Provider-neutral scoring engine. Missing external intelligence is never fabricated. */
class AiRadarEngine {
    fun score(
        snapshots: List<RadarMarketSnapshot>,
        whalePressure: Map<String, Int> = emptyMap(),
        newsImpact: Map<String, Int> = emptyMap(),
        tokenomicsRisk: Map<String, Int> = emptyMap()
    ): List<RadarSignal> = snapshots.groupBy { it.symbol.uppercase() }.mapNotNull { (symbol, rows) ->
        val valid = rows.filter { it.lastPrice > 0 && it.open24h > 0 && it.volume24h >= 0 }
        if (valid.isEmpty()) return@mapNotNull null
        val momentum = valid.map { pct(it.lastPrice, it.open24h) }.average()
        val pressure = valid.map { buySellPressure(it) }.average()
        val liquidity = liquidityScore(valid.map { it.quoteVolume24h })
        val whale = whalePressure[symbol]
        val news = newsImpact[symbol] ?: 0
        val tokenRisk = tokenomicsRisk[symbol] ?: 0
        val pump = clamp(50.0 + momentum * 2.5 + pressure * .25 + (whale ?: 0) * .15 + news * .10 - tokenRisk * .15)
        val dump = clamp(50.0 - momentum * 2.5 - pressure * .25 - (whale ?: 0) * .15 - news * .10 + tokenRisk * .15)
        val quality = valid.size.toDouble() / rows.size.coerceAtLeast(1)
        val confidence = (45 + quality * 35 + valid.size.coerceAtMost(3) * 5 + if (rows.size > 1) 5 else 0).toInt().coerceIn(0, 95)
        RadarSignal(symbol, valid.map { it.exchange }.distinct(), pump, dump, confidence,
            clamp(momentum * 4 + 50.0), clamp(pressure + 50.0), liquidity, whale,
            buildReasons(momentum, pressure, liquidity, whale, news, tokenRisk), quality)
    }.sortedByDescending { maxOf(it.pumpPotential, it.dumpRisk) }

    private fun buildReasons(momentum: Double, pressure: Double, liquidity: Int, whale: Int?, news: Int, tokenRisk: Int) = buildList {
        if (momentum >= 3) add("Positive momentum") else if (momentum <= -3) add("Negative momentum")
        if (pressure >= 20) add("Buy-side volume pressure") else if (pressure <= -20) add("Sell-side volume pressure")
        if (liquidity < 35) add("Low liquidity risk")
        if (whale != null && abs(whale) >= 30) add("Whale activity detected")
        if (news >= 30) add("Positive news impact") else if (news <= -30) add("Negative news impact")
        if (tokenRisk >= 50) add("Elevated tokenomics risk")
        if (isEmpty()) add("No dominant anomaly; monitoring")
    }

    private fun buySellPressure(s: RadarMarketSnapshot): Double {
        val buy = s.buyVolume24h ?: return pct(s.lastPrice, s.open24h).coerceIn(-100.0, 100.0)
        val sell = s.sellVolume24h ?: return pct(s.lastPrice, s.open24h).coerceIn(-100.0, 100.0)
        val total = buy + sell
        return if (total <= 0) 0.0 else (buy - sell) / total * 100.0
    }

    private fun liquidityScore(volumes: List<Double>): Int {
        val v = volumes.filter { it > 0 }.average()
        if (v <= 0) return 0
        return (ln(v + 1.0) / ln(1_000_000_000.0) * 100.0).toInt().coerceIn(0, 100)
    }

    private fun pct(a: Double, b: Double) = (a - b) / b * 100.0
    private fun clamp(v: Double) = round(v).toInt().coerceIn(0, 100)
}
