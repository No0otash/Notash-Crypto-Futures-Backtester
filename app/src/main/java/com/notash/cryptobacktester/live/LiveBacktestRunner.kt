package com.notash.cryptobacktester.live

import com.notash.cryptobacktester.core.BacktestConfig
import com.notash.cryptobacktester.core.BacktestReport
import com.notash.cryptobacktester.strategy.Strategy
import com.notash.cryptobacktester.engine.BacktestRunner

/**
 * Live-backtest means continuously re-running the same simulation engine against
 * the latest read-only market window. No exchange order endpoint is ever called.
 */
class LiveBacktestRunner(
    private val marketData: LiveMarketData,
    private val backtestRunner: BacktestRunner = BacktestRunner()
) {
    suspend fun snapshot(strategy: Strategy, symbol: String, interval: String, config: BacktestConfig, limit: Int = 200): BacktestReport {
        val candles = marketData.fetchCandles(symbol, interval, limit)
        require(candles.size >= 10) { "Live market returned insufficient candles." }
        return backtestRunner.run(strategy, candles, emptyList(), config)
    }
}
