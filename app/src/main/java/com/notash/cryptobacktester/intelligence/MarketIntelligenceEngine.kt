package com.notash.cryptobacktester.intelligence

import com.notash.cryptobacktester.core.Candle

/** Orchestration boundary for market, pump/dump and whale intelligence. */
class MarketIntelligenceEngine(
    private val pumpDumpDetector: PumpDumpDetector = PumpDumpDetector(),
    private val whaleAnalyzer: WhaleSmartMoneyAnalyzer = WhaleSmartMoneyAnalyzer(),
    private val whaleProvider: OnChainWhaleProvider = UnavailableOnChainWhaleProvider,
    private val huntFloProvider: WhaleIntelligenceProvider = HuntFloTelegramProvider()
) {
    fun analyzePriceAndVolume(candles: List<Candle>): PumpDumpSignal? = pumpDumpDetector.analyze(candles)

    suspend fun analyzeWhales(asset: String, from: Long, until: Long): WhaleActivity {
        val transfers = whaleProvider.getLargeTransfers(asset, from, until)
        return whaleAnalyzer.analyze(asset, transfers, from, until)
    }

    suspend fun latestHuntFloWhaleEvents(limit: Int = 50): List<WhaleEvent> = huntFloProvider.latestEvents(limit)

    fun reset() = pumpDumpDetector.reset()
}
