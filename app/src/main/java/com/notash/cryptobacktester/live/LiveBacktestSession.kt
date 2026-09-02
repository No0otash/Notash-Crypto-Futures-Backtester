package com.notash.cryptobacktester.live

import com.notash.cryptobacktester.core.BacktestConfig
import com.notash.cryptobacktester.core.BacktestReport
import com.notash.cryptobacktester.core.Candle
import com.notash.cryptobacktester.core.FundingRate
import com.notash.cryptobacktester.engine.BacktestRunner
import com.notash.cryptobacktester.strategy.Strategy

/** Simulates a strategy against a captured live-market candle window; never submits exchange orders. */
class LiveBacktestSession(private val runner: BacktestRunner = BacktestRunner()) {
    suspend fun simulate(
        strategy: Strategy,
        candles: List<Candle>,
        funding: List<FundingRate> = emptyList(),
        config: BacktestConfig
    ): BacktestReport {
        require(candles.size >= 10) { "At least 10 live candles are required." }
        return runner.run(strategy, candles, funding, config)
    }
}
