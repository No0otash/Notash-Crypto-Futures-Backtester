package com.notash.cryptobacktester.engine

import com.notash.cryptobacktester.core.BacktestConfig
import com.notash.cryptobacktester.core.BacktestReport
import com.notash.cryptobacktester.data.CoinExRepository
import com.notash.cryptobacktester.strategy.Strategy

/** Runs the same BacktestEngine against freshly fetched CoinEx Futures candles. */
class LiveBacktestRunner(
    private val repository: CoinExRepository = CoinExRepository(),
    private val engine: BacktestEngine = BacktestEngine()
) {
    suspend fun run(
        market: String,
        period: String,
        strategy: Strategy,
        config: BacktestConfig,
        limit: Int = 1000
    ): BacktestReport {
        require(market.isNotBlank()) { "Market is required" }
        require(period.isNotBlank()) { "Period is required" }
        require(limit >= 10) { "At least 10 candles are required" }
        val candles = repository.loadKlines(market.uppercase(), period, limit)
        require(candles.size >= 10) { "CoinEx returned insufficient candle data" }
        return engine.run(candles, emptyList(), strategy, config)
    }
}
