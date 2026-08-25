package com.notash.cryptobacktester.market

import com.notash.cryptobacktester.core.MarketTicker

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
        val momentum = ((ticker.change24h + 10.0) / 30.0).coerceIn(0.0, 1.0) * 65.0
        val liquidity = if (maxValue24h > 0.0) (ticker.value24h / maxValue24h).coerceIn(0.0, 1.0) * 20.0 else 0.0
        val activity = (kotlin.math.abs(ticker.change24h) / 20.0).coerceIn(0.0, 1.0) * 15.0
        return (momentum + liquidity + activity).toInt().coerceIn(0, 100)
    }

    fun isPump(ticker: MarketTicker): Boolean = ticker.change24h >= 5.0
    fun isDump(ticker: MarketTicker): Boolean = ticker.change24h <= -5.0

    fun riskBand(score: Int): String = when {
        score >= 75 -> "HIGH"
        score >= 60 -> "MEDIUM"
        else -> "LOW"
    }

    fun rankGrowthCandidates(tickers: List<MarketTicker>, limit: Int = 5): List<GrowthCandidate> {
        if (tickers.isEmpty()) return emptyList()
        val maxValue = tickers.maxOfOrNull { it.value24h } ?: 1.0
        return tickers.asSequence()
            .map { ticker ->
                val score = score(ticker, maxValue)
                GrowthCandidate(
                    market = ticker.market,
                    score = score,
                    change24h = ticker.change24h,
                    value24h = ticker.value24h,
                    risk = riskBand(score),
                    reasons = buildList {
                        if (ticker.change24h > 0) add("Momentum مثبت در ۲۴ ساعت")
                        if (ticker.value24h >= maxValue * 0.5) add("ارزش معاملات بالا")
                        if (isPump(ticker)) add("حرکت غیرعادی صعودی")
                        if (isDump(ticker)) add("فشار فروش شدید")
                    }
                )
            }
            .filter { it.change24h > 0.0 && it.score >= 60 }
            .sortedWith(compareByDescending<GrowthCandidate> { it.score }.thenByDescending { it.change24h })
            .take(limit)
            .toList()
    }

    fun pumpList(tickers: List<MarketTicker>, limit: Int = 8): List<MarketTicker> =
        tickers.filter(::isPump).sortedByDescending { it.change24h }.take(limit)

    fun dumpList(tickers: List<MarketTicker>, limit: Int = 8): List<MarketTicker> =
        tickers.filter(::isDump).sortedBy { it.change24h }.take(limit)
}
