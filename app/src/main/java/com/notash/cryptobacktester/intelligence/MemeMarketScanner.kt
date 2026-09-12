package com.notash.cryptobacktester.intelligence

class MemeMarketScanner(
    private val source: MarketIntelligenceDataSource,
    private val scanner: MemeShitcoinScanner = MemeShitcoinScanner()
) {
    suspend fun scan(config: ScannerConfig = ScannerConfig()): List<MemeScanResult> {
        val markets = source.loadMarkets().asSequence().filter { it.isTrading }.take(config.maxMarkets).toList()
        val tickers = source.loadTickers().associateBy { it.market.uppercase() }
        val candidates = markets.mapNotNull { market -> tickers[market.market.uppercase()]?.let { market to it } }
            .sortedByDescending { (_, ticker) -> kotlin.math.abs(ticker.lastPrice / ticker.open24h.coerceAtLeast(0.0000000001) - 1.0) }
            .take(config.deepScanLimit)
        return candidates.mapNotNull { (market, _) ->
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
