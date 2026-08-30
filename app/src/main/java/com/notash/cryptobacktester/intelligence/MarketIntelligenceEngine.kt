package com.notash.cryptobacktester.intelligence

import com.notash.cryptobacktester.core.Candle

/** Single orchestration layer for sections 11 and 12. Market data is supplied by the existing data layer. */
class MarketIntelligenceEngine(
    private val pumpDumpDetector: PumpDumpDetector = PumpDumpDetector(),
    private val whaleAnalyzer: WhaleSmartMoneyAnalyzer = WhaleSmartMoneyAnalyzer(),
    private val whaleProvider: OnChainWhaleProvider = UnavailableOnChainWhaleProvider
) {
    fun analyzePriceAndVolume(candles: List<Candle>): PumpDumpSignal? = pumpDumpDetector.analyze(candles)

    suspend fun analyzeWhales(asset: String, from: Long, until: Long): WhaleActivity {
        val transfers = whaleProvider.getLargeTransfers(asset, from, until)
        return whaleAnalyzer.analyze(asset, transfers, from, until)
    }

    fun reset() = pumpDumpDetector.reset()
}
