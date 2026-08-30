package com.notash.cryptobacktester.intelligence

import com.notash.cryptobacktester.core.Candle
import kotlin.math.abs
import kotlin.math.ln

/**
 * Provider-neutral Meme/Shitcoin risk scanner. It never invents market/on-chain data;
 * callers provide the snapshot and candles they actually have.
 */
data class MemeCoinSnapshot(
    val symbol: String,
    val market: String,
    val liquidityUsd: Double,
    val marketCapUsd: Double,
    val ageDays: Int = 0,
    val holderConcentrationPercent: Double? = null,
    val contractVerified: Boolean? = null,
    val buyTaxPercent: Double? = null,
    val sellTaxPercent: Double? = null
)

data class MemeScanResult(
    val symbol: String,
    val market: String,
    val isMemeLike: Boolean,
    val riskScore: Double,
    val opportunityScore: Double,
    val flags: List<String>,
    val priceChangePercent: Double,
    val volumeRatio: Double,
    val volatilityPercent: Double,
    val liquidityUsd: Double,
    val marketCapUsd: Double,
    val dataComplete: Boolean,
    val message: String
)

class MemeShitcoinScanner(
    private val baselinePeriods: Int = 20
) {
    fun scan(snapshot: MemeCoinSnapshot, candles: List<Candle>): MemeScanResult {
        require(snapshot.liquidityUsd >= 0.0) { "liquidityUsd must not be negative" }
        require(snapshot.marketCapUsd >= 0.0) { "marketCapUsd must not be negative" }

        val recent = candles.takeLast(baselinePeriods.coerceAtLeast(2))
        val firstClose = candles.firstOrNull()?.close ?: 0.0
        val lastClose = candles.lastOrNull()?.close ?: 0.0
        val priceChange = if (firstClose > 0.0) (lastClose / firstClose - 1.0) * 100.0 else 0.0
        val avgVolume = recent.dropLast(1).map { it.volume }.average().takeIf { it > 0.0 } ?: 0.0
        val lastVolume = recent.lastOrNull()?.volume ?: 0.0
        val volumeRatio = if (avgVolume > 0.0) lastVolume / avgVolume else 0.0
        val volatility = recent.map { candle ->
            if (candle.close > 0.0) abs(candle.high - candle.low) / candle.close * 100.0 else 0.0
        }.average().takeIf { it.isFinite() } ?: 0.0

        var risk = 0.0
        val flags = mutableListOf<String>()
        if (snapshot.liquidityUsd < 100_000.0) { risk += 25.0; flags += "LOW_LIQUIDITY" }
        if (snapshot.marketCapUsd in 0.0..5_000_000.0 && snapshot.marketCapUsd > 0.0) { risk += 20.0; flags += "MICRO_CAP" }
        if (snapshot.ageDays in 1..30) { risk += 15.0; flags += "NEW_TOKEN" }
        if (snapshot.holderConcentrationPercent != null && snapshot.holderConcentrationPercent >= 60.0) { risk += 20.0; flags += "HIGH_HOLDER_CONCENTRATION" }
        if (snapshot.contractVerified == false) { risk += 10.0; flags += "UNVERIFIED_CONTRACT" }
        if ((snapshot.buyTaxPercent ?: 0.0) >= 5.0 || (snapshot.sellTaxPercent ?: 0.0) >= 5.0) { risk += 15.0; flags += "HIGH_TAX" }
        if (volumeRatio >= 5.0) { risk += 10.0; flags += "VOLUME_SPIKE" }
        if (abs(priceChange) >= 30.0) { risk += 10.0; flags += "EXTREME_PRICE_MOVE" }
        if (volatility >= 8.0) { risk += 10.0; flags += "HIGH_VOLATILITY" }
        risk = risk.coerceIn(0.0, 100.0)

        val memeLike = risk >= 35.0 || flags.any { it in setOf("MICRO_CAP", "NEW_TOKEN", "HIGH_HOLDER_CONCENTRATION") }
        val opportunity = ((volumeRatio.coerceIn(0.0, 5.0) / 5.0) * 35.0 +
            (abs(priceChange).coerceIn(0.0, 30.0) / 30.0) * 25.0 +
            (1.0 - (risk / 100.0)) * 40.0).coerceIn(0.0, 100.0)
        val complete = candles.isNotEmpty() && snapshot.liquidityUsd > 0.0 && snapshot.marketCapUsd > 0.0
        val message = if (complete) "Scan based only on supplied market/token metadata" else "Incomplete data: score is provisional and no missing data was fabricated"

        return MemeScanResult(snapshot.symbol, snapshot.market, memeLike, risk, opportunity, flags.distinct(),
            priceChange, volumeRatio, volatility, snapshot.liquidityUsd, snapshot.marketCapUsd, complete, message)
    }
}
