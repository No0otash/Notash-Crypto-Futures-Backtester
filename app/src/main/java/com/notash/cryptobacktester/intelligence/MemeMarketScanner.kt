package com.notash.cryptobacktester.intelligence

class MemeMarketScanner(
    private val source: MarketIntelligenceDataSource,
    private val scanner: MemeShitcoinScanner = MemeShitcoinScanner()
) {
    suspend fun scan(config: ScannerConfig = ScannerConfig()): List<MemeScanResult> {
        val markets = source.loadMarkets().asSequence().filter { it.isTrading }.take(config.maxMarkets).toList()
        val tickers = source.loadTickers().associateBy { it.market.uppercase() }
        return markets.mapNotNull { market ->
            tickers[market.market.uppercase()] ?: return@mapNotNull null
            val candles = try {
                source.loadCandles(market.market, config.candlePeriod, config.candleLimit)
            } catch (_: Exception) {
                emptyList()
            }
            val snapshot = MemeCoinSnapshot(
                symbol = market.baseAsset.ifBlank { market.market.removeSuffix("USDT") },
                market = market.market,
                liquidityUsd = 0.0,
                marketCapUsd = 0.0
            )
            scanner.scan(snapshot, candles)
        }.filter { it.isMemeLike || it.opportunityScore >= 45.0 }
            .sortedByDescending { it.opportunityScore }
    }
}
