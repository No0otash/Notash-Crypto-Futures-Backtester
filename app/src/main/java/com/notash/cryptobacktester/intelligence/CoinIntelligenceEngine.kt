package com.notash.cryptobacktester.intelligence

import com.notash.cryptobacktester.core.Candle
import kotlin.math.abs

data class CoinIntelligenceInput(
    val snapshot: MemeCoinSnapshot,
    val candles: List<Candle>,
    val whaleActivity: WhaleActivity? = null,
    val projectQualityScore: Double? = null,
    val tokenomicsScore: Double? = null
)

data class IntelligenceComponent(
    val name: String,
    val score: Double,
    val weight: Double,
    val explanation: String
)

data class CoinIntelligenceReport(
    val symbol: String,
    val market: String,
    val overallScore: Double,
    val riskScore: Double,
    val opportunityScore: Double,
    val confidenceScore: Double,
    val components: List<IntelligenceComponent>,
    val warnings: List<String>,
    val dataComplete: Boolean,
    val summary: String
)

class CoinIntelligenceEngine(private val memeScanner: MemeShitcoinScanner = MemeShitcoinScanner()) {
    fun analyze(input: CoinIntelligenceInput): CoinIntelligenceReport {
        val meme = memeScanner.scan(input.snapshot, input.candles)
        val marketScore = marketScore(input.candles)
        val components = mutableListOf(
            IntelligenceComponent("MARKET", marketScore, 0.30, "Price movement, volatility and supplied candles"),
            IntelligenceComponent("MEME_RISK", 100.0 - meme.riskScore, 0.25, "Liquidity, age, concentration, tax and market-behaviour risk"),
            IntelligenceComponent("MOMENTUM", meme.opportunityScore, 0.15, "Recent price/volume opportunity without predicting direction")
        )
        input.whaleActivity?.let {
            components += IntelligenceComponent("WHALE", 100.0 - it.score.coerceIn(0.0, 100.0), 0.15, "Whale activity risk from supplied provider data")
        }
        input.projectQualityScore?.coerceIn(0.0, 100.0)?.let { components += IntelligenceComponent("PROJECT", it, 0.075, "Supplied project-quality score") }
        input.tokenomicsScore?.coerceIn(0.0, 100.0)?.let { components += IntelligenceComponent("TOKENOMICS", it, 0.075, "Supplied tokenomics score") }
        val weightSum = components.sumOf { it.weight }
        val overall = if (weightSum > 0.0) components.sumOf { it.score * it.weight } / weightSum else 0.0
        val risk = (meme.riskScore * 0.65 + (input.whaleActivity?.score ?: 0.0) * 0.35).coerceIn(0.0, 100.0)
        val confidence = confidence(input, meme).coerceIn(0.0, 100.0)
        val warnings = buildList {
            addAll(meme.flags)
            if (input.whaleActivity?.dataAvailable == false) add("WHALE_DATA_UNAVAILABLE")
            if (!meme.dataComplete) add("INCOMPLETE_TOKEN_DATA")
        }.distinct()
        val summary = when {
            confidence < 50.0 -> "Low-confidence intelligence: more verified data is required"
            risk >= 70.0 -> "High-risk coin profile; intelligence is not a trading recommendation"
            overall >= 70.0 -> "Favourable supplied-data profile; confirm risk before trading"
            overall >= 45.0 -> "Mixed supplied-data profile; monitor before acting"
            else -> "Weak supplied-data profile; elevated caution is appropriate"
        }
        return CoinIntelligenceReport(input.snapshot.symbol, input.snapshot.market, overall, risk,
            meme.opportunityScore, confidence, components, warnings,
            meme.dataComplete && (input.whaleActivity == null || input.whaleActivity.dataAvailable), summary)
    }

    private fun marketScore(candles: List<Candle>): Double {
        if (candles.size < 2) return 0.0
        val first = candles.first().close
        val last = candles.last().close
        if (first <= 0.0 || last <= 0.0) return 0.0
        val change = ((last / first) - 1.0) * 100.0
        val ranges = candles.takeLast(20).mapNotNull { c -> if (c.close > 0.0) abs(c.high - c.low) / c.close * 100.0 else null }
        val volatility = ranges.average().takeIf { it.isFinite() } ?: 0.0
        return (50.0 + change.coerceIn(-50.0, 50.0) - volatility.coerceIn(0.0, 30.0)).coerceIn(0.0, 100.0)
    }

    private fun confidence(input: CoinIntelligenceInput, meme: MemeScanResult): Double {
        var score = 0.0
        if (input.candles.size >= 20) score += 35.0
        if (meme.liquidityUsd > 0.0) score += 20.0
        if (meme.marketCapUsd > 0.0) score += 20.0
        if (input.snapshot.contractVerified != null) score += 10.0
        if (input.snapshot.holderConcentrationPercent != null) score += 10.0
        if (input.whaleActivity?.dataAvailable == true) score += 5.0
        return score
    }
}
