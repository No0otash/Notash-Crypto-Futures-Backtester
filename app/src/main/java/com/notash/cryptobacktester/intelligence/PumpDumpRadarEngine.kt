package com.notash.cryptobacktester.intelligence

import kotlin.math.abs
import kotlin.math.round

/** Explainable pump/dump detector. Only supplied provider evidence is scored. */
data class PumpDumpMarketEvidence(
    val snapshot: RadarMarketSnapshot,
    val previousQuoteVolume24h: Double? = null,
    val previousPriceChangePercent: Double? = null,
    val volatilityPercent: Double? = null,
    val spreadPercent: Double? = null,
    val openInterestChangePercent: Double? = null
)

data class PumpDumpIntelligence(
    val whalePressure: Int? = null,
    val newsImpact: Int? = null,
    val tokenomicsRisk: Int? = null,
    val holderConcentrationRisk: Int? = null,
    val projectRisk: Int? = null
)

enum class RadarPumpDumpDirection { PUMP, DUMP, WATCH }

data class RadarPumpDumpSignal(
    val symbol: String,
    val direction: RadarPumpDumpDirection,
    val score: Int,
    val confidence: Int,
    val momentumScore: Int,
    val volumeAnomalyScore: Int,
    val volatilityRisk: Int,
    val liquidityRisk: Int,
    val whalePressure: Int?,
    val newsImpact: Int?,
    val tokenomicsRisk: Int?,
    val reasons: List<String>,
    val dataGaps: List<String>
)

class PumpDumpRadarEngine {
    fun analyze(evidence: PumpDumpMarketEvidence, intelligence: PumpDumpIntelligence = PumpDumpIntelligence()): RadarPumpDumpSignal {
        val s = evidence.snapshot
        require(s.lastPrice > 0.0 && s.open24h > 0.0) { "Invalid market price data" }
        val change = (s.lastPrice / s.open24h - 1.0) * 100.0
        val momentum = (abs(change) / 12.0 * 100.0).coerceIn(0.0, 100.0).toScore()
        val volumeAnomaly = evidence.previousQuoteVolume24h?.takeIf { it > 0.0 }?.let {
            (((s.quoteVolume24h / it) - 1.0) * 100.0 / 5.0).coerceIn(0.0, 100.0).toScore()
        } ?: 0
        val volatility = (evidence.volatilityPercent ?: abs(s.high24h - s.low24h) / s.lastPrice * 100.0).coerceIn(0.0, 100.0)
        val volatilityRisk = (volatility * 6.0).coerceIn(0.0, 100.0).toScore()
        val liquidityRisk = liquidityRisk(s.quoteVolume24h, evidence.spreadPercent)
        val whale = intelligence.whalePressure
        val news = intelligence.newsImpact
        val token = intelligence.tokenomicsRisk
        val holder = intelligence.holderConcentrationRisk
        val project = intelligence.projectRisk

        val pumpEvidence = weightedPositive(change, volumeAnomaly, whale, news ?: 0)
        val dumpEvidence = weightedNegative(change, volumeAnomaly, whale, news ?: 0, token, holder, project)
        val direction = when {
            pumpEvidence >= 60 && pumpEvidence > dumpEvidence + 10 -> RadarPumpDumpDirection.PUMP
            dumpEvidence >= 60 && dumpEvidence > pumpEvidence + 10 -> RadarPumpDumpDirection.DUMP
            else -> RadarPumpDumpDirection.WATCH
        }
        val score = maxOf(pumpEvidence, dumpEvidence).coerceIn(0, 100)
        val gaps = buildList {
            if (evidence.previousQuoteVolume24h == null) add("VOLUME_BASELINE")
            if (evidence.volatilityPercent == null) add("VOLATILITY_HISTORY")
            if (evidence.spreadPercent == null) add("SPREAD")
            if (evidence.openInterestChangePercent == null) add("OPEN_INTEREST_HISTORY")
            if (whale == null) add("WHALE_INTELLIGENCE")
            if (news == null) add("NEWS_IMPACT")
            if (token == null) add("TOKENOMICS_RISK")
            if (holder == null) add("HOLDER_CONCENTRATION")
            if (project == null) add("PROJECT_RISK")
        }
        val evidenceCount = 1 + listOfNotNull(evidence.previousQuoteVolume24h, evidence.volatilityPercent, evidence.spreadPercent, evidence.openInterestChangePercent, whale, news, token, holder, project).size
        val confidence = (35 + evidenceCount * 6 - gaps.size * 2).coerceIn(15, 95)
        return RadarPumpDumpSignal(
            symbol = s.symbol.uppercase(), direction = direction, score = score, confidence = confidence,
            momentumScore = momentum, volumeAnomalyScore = volumeAnomaly, volatilityRisk = volatilityRisk,
            liquidityRisk = liquidityRisk, whalePressure = whale, newsImpact = news, tokenomicsRisk = token,
            reasons = reasons(change, volumeAnomaly, volatilityRisk, liquidityRisk, whale, news, token, holder, project, direction),
            dataGaps = gaps
        )
    }

    private fun weightedPositive(change: Double, volume: Int, whale: Int?, news: Int): Int =
        round((change.coerceIn(0.0, 20.0) / 20.0 * 45.0) + volume * .25 + (whale?.coerceAtLeast(0) ?: 0) * .20 + news.coerceAtLeast(0) * .10).toInt().coerceIn(0, 100)

    private fun weightedNegative(change: Double, volume: Int, whale: Int?, news: Int, token: Int?, holder: Int?, project: Int?): Int =
        round(((-change).coerceIn(0.0, 20.0) / 20.0 * 45.0) + volume * .25 + (-(whale ?: 0)).coerceAtLeast(0) * .20 + (-news).coerceAtLeast(0) * .10 + (token ?: 0) * .15 + (holder ?: 0) * .10 + (project ?: 0) * .10).toInt().coerceIn(0, 100)

    private fun liquidityRisk(quoteVolume: Double, spread: Double?): Int {
        val volumeRisk = when { quoteVolume <= 0.0 -> 100; quoteVolume < 100_000.0 -> 85; quoteVolume < 1_000_000.0 -> 55; quoteVolume < 10_000_000.0 -> 25; else -> 10 }
        val spreadRisk = when { spread == null -> 0; spread >= 2.0 -> 80; spread >= 1.0 -> 50; spread >= .5 -> 25; else -> 5 }
        return maxOf(volumeRisk, spreadRisk)
    }

    private fun reasons(change: Double, volume: Int, volatility: Int, liquidity: Int, whale: Int?, news: Int?, token: Int?, holder: Int?, project: Int?, direction: RadarPumpDumpDirection) = buildList {
        if (change >= 3) add("Strong positive momentum") else if (change <= -3) add("Strong negative momentum")
        if (volume >= 50) add("Unusual volume increase")
        if (volatility >= 60) add("High volatility risk")
        if (liquidity >= 55) add("Liquidity risk is elevated")
        if (whale != null && abs(whale) >= 30) add("HuntFlo whale pressure is significant")
        if (news != null && abs(news) >= 30) add("News impact is significant")
        if (token != null && token >= 50) add("Tokenomics dilution/unlock risk is elevated")
        if (holder != null && holder >= 50) add("Holder concentration risk is elevated")
        if (project != null && project >= 50) add("Project risk is elevated")
        if (isEmpty()) add("No dominant anomaly; continue monitoring")
        if (direction == RadarPumpDumpDirection.WATCH) add("Signal is below directional confirmation threshold")
    }

    private fun Double.toScore() = round(coerceIn(0.0, 100.0)).toInt()
}