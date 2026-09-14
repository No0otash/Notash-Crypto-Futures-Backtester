package com.notash.cryptobacktester.intelligence

import com.notash.cryptobacktester.core.Candle
import com.notash.cryptobacktester.data.CoinExRepository
import com.notash.cryptobacktester.data.FuturesMarketDescriptor
import com.notash.cryptobacktester.data.FuturesTickerSnapshot

interface MarketIntelligenceDataSource {
    suspend fun loadMarkets(): List<FuturesMarketDescriptor>
    suspend fun loadTickers(): List<FuturesTickerSnapshot>
    suspend fun loadCandles(market: String, period: String, limit: Int): List<Candle>
}

class CoinExMarketIntelligenceDataSource(
    private val repository: CoinExRepository = CoinExRepository()
) : MarketIntelligenceDataSource {
    override suspend fun loadMarkets() = repository.loadFuturesMarkets()
    override suspend fun loadTickers() = repository.loadFuturesTickers()
    override suspend fun loadCandles(market: String, period: String, limit: Int) = repository.loadKlines(market, period, limit)
}

data class ScannerConfig(
    val maxMarkets: Int = 5000,
    val candlePeriod: String = "5min",
    val candleLimit: Int = 60,
    val deepScanLimit: Int = 50
)

data class MarketScanReport(
    val signals: List<RadarPumpDumpSignal>,
    val scannedMarkets: Int,
    val unavailableMarkets: Int,
    val scannedAtMs: Long,
    val errors: List<String>
)

class MarketIntelligenceScanner(
    private val source: MarketIntelligenceDataSource,
    private val engine: PumpDumpRadarEngine = PumpDumpRadarEngine()
) {
    constructor(repository: CoinExRepository) : this(CoinExMarketIntelligenceDataSource(repository))
    constructor() : this(CoinExMarketIntelligenceDataSource())

    suspend fun scanPumpDump(config: ScannerConfig = ScannerConfig()): MarketScanReport {
        val scannedAt = System.currentTimeMillis()
        val markets = source.loadMarkets().asSequence().filter { it.isTrading }.take(config.maxMarkets).toList()
        if (markets.isEmpty()) return MarketScanReport(emptyList(), 0, 0, scannedAt, listOf("NO_TRADING_MARKETS"))

        val tickerByMarket = try {
            source.loadTickers().associateBy { it.market.uppercase() }
        } catch (error: Exception) {
            return MarketScanReport(emptyList(), 0, markets.size, scannedAt, listOf(error.message ?: "TICKER_LOAD_FAILED"))
        }

        val errors = mutableListOf<String>()
        val candidates = markets.mapNotNull { market -> tickerByMarket[market.market.uppercase()]?.let { market to it } }
        val unavailable = markets.size - candidates.size
        val deepMarkets = candidates.sortedByDescending { (_, ticker) -> ticker.quoteVolume24h }
            .take(config.deepScanLimit).map { it.first.market.uppercase() }.toSet()

        val signals = candidates.map { (market, ticker) ->
            val snapshot = ticker.toRadarSnapshot()
            val evidence = if (market.market.uppercase() in deepMarkets) {
                try {
                    evidenceFromCandles(snapshot, source.loadCandles(market.market, config.candlePeriod, config.candleLimit))
                } catch (error: Exception) {
                    errors += "${market.market}:${error.message ?: "CANDLE_LOAD_FAILED"}"
                    PumpDumpMarketEvidence(snapshot)
                }
            } else PumpDumpMarketEvidence(snapshot)
            engine.analyze(evidence)
        }.sortedByDescending { it.score }

        return MarketScanReport(signals, candidates.size, unavailable, scannedAt, errors.take(20))
    }

    private fun evidenceFromCandles(snapshot: RadarMarketSnapshot, candles: List<Candle>): PumpDumpMarketEvidence {
        if (candles.size < 2) return PumpDumpMarketEvidence(snapshot)
        val ordered = candles.sortedBy { it.timestamp }
        val previous = ordered.dropLast(1).takeLast(20)
        val latest = ordered.last()
        val baselineValue = previous.map { it.value.takeIf { value -> value > 0.0 } ?: it.volume * it.close }
            .average().takeIf { it > 0.0 }
        val returns = previous.zipWithNext().mapNotNull { (a, b) ->
            if (a.close > 0.0 && b.close > 0.0) (b.close / a.close - 1.0) * 100.0 else null
        }
        val volatility = if (returns.size >= 2) {
            val mean = returns.average()
            kotlin.math.sqrt(returns.map { (it - mean) * (it - mean) }.average())
        } else null
        val latestChange = if (latest.open > 0.0) (latest.close / latest.open - 1.0) * 100.0 else null
        return PumpDumpMarketEvidence(snapshot, baselineValue, latestChange, volatility)
    }

    private fun FuturesTickerSnapshot.toRadarSnapshot() = RadarMarketSnapshot(
        exchange = "CoinEx", symbol = market, lastPrice = lastPrice, open24h = open24h,
        high24h = high24h, low24h = low24h, volume24h = volume24h, quoteVolume24h = quoteVolume24h,
        buyVolume24h = buyVolume24h, sellVolume24h = sellVolume24h, openInterest = openInterest,
        timestampMs = timestampMs
    )
}
