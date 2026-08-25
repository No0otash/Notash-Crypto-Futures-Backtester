package com.notash.cryptobacktester.market

import com.notash.cryptobacktester.core.MarketTicker
import kotlin.math.abs
import kotlin.math.ln

/**
 * On-device market radar. It scans every futures ticker returned by CoinEx,
 * including low-cap/high-volatility markets. Scores describe signal strength,
 * not guaranteed future performance.
 */
data class GrowthCandidate(
    val market: String,
    val score: Int,
    val change24h: Double,
    val value24h: Double,
    val risk: String,
    val reasons: List<String>
)

object MarketRadar {
    fun score(ticker: MarketTicker, maxValue24h: Double = 1.0): Int {
        if (ticker.change24h <= 0.0) return 0
        val momentum = (ticker.change24h / 12.0).coerceIn(0.0, 1.0) * 45.0
        val liquidity = if (maxValue24h > 0.0) relativeLog(ticker.value24h, maxValue24h) * 25.0 else 0.0
        val activity = (abs(ticker.change24h) / 20.0).coerceIn(0.0, 1.0) * 15.0
        val volatilityPenalty = when {
            ticker.change24h > 25.0 -> 18.0
            ticker.change24h > 15.0 -> 8.0
            else -> 0.0
        }
        val controlledMomentumBonus = if (ticker.change24h in 1.0..8.0) 15.0 else 0.0
        return (momentum + liquidity + activity + controlledMomentumBonus - volatilityPenalty)
            .toInt().coerceIn(0, 100)
    }

    fun isPump(ticker: MarketTicker): Boolean = ticker.change24h >= 5.0
    fun isDump(ticker: MarketTicker): Boolean = ticker.change24h <= -5.0

    fun riskBand(score: Int, ticker: MarketTicker? = null): String = when {
        ticker != null && abs(ticker.change24h) > 25.0 -> "بسیار بالا"
        ticker != null && ticker.value24h > 0.0 && ticker.change24h > 15.0 -> "بالا"
        score >= 75 -> "متوسط"
        score >= 60 -> "متوسط"
        else -> "بالا"
    }

    fun rankGrowthCandidates(tickers: List<MarketTicker>, limit: Int = 5): List<GrowthCandidate> {
        if (tickers.isEmpty()) return emptyList()
        val maxValue = tickers.maxOfOrNull { it.value24h } ?: 1.0
        return tickers.asSequence()
            .filter { it.change24h > 0.5 }
            .map { ticker ->
                val score = score(ticker, maxValue)
                val reasons = buildList {
                    if (ticker.change24h >= 3.0) add("Momentum مثبت و قوی")
                    if (ticker.change24h in 0.5..3.0) add("رشد اولیه و کنترل‌شده")
                    if (ticker.value24h >= maxValue * 0.50) add("ارزش معاملات بالا")
                    else if (ticker.value24h >= maxValue * 0.10) add("فعالیت معاملاتی قابل توجه")
                    if (isPump(ticker)) add("حرکت غیرعادی صعودی")
                    if (ticker.change24h > 15.0) add("نوسان شدید؛ نیازمند احتیاط")
                }
                GrowthCandidate(
                    market = ticker.market,
                    score = score,
                    change24h = ticker.change24h,
                    value24h = ticker.value24h,
                    risk = riskBand(score, ticker),
                    reasons = reasons.ifEmpty { listOf("سیگنال ترکیبی بازار") }
                )
            }
            .filter { it.score >= 60 }
            .sortedWith(compareByDescending<GrowthCandidate> { it.score }.thenByDescending { it.change24h })
            .take(limit)
            .toList()
    }

    fun pumpList(tickers: List<MarketTicker>, limit: Int = 8): List<MarketTicker> =
        tickers.filter(::isPump).sortedWith(compareByDescending<MarketTicker> { it.change24h }.thenByDescending { it.value24h }).take(limit)

    fun dumpList(tickers: List<MarketTicker>, limit: Int = 8): List<MarketTicker> =
        tickers.filter(::isDump).sortedWith(compareBy<MarketTicker> { it.change24h }.thenByDescending { it.value24h }).take(limit)

    private fun relativeLog(value: Double, maxValue: Double): Double {
        if (value <= 0.0 || maxValue <= 0.0) return 0.0
        return (ln(value + 1.0) / ln(maxValue + 1.0)).coerceIn(0.0, 1.0)
    }
}
