package com.notash.cryptobacktester.engine

import com.notash.cryptobacktester.core.BacktestConfig
import com.notash.cryptobacktester.core.BacktestReport
import com.notash.cryptobacktester.core.Candle
import com.notash.cryptobacktester.core.FundingRate
import com.notash.cryptobacktester.strategy.Strategy

class BacktestRunner(
    private val engine: BacktestEngine = BacktestEngine()
) {

    suspend fun run(
        strategy: Strategy,
        candles: List<Candle>,
        funding: List<FundingRate>,
        config: BacktestConfig
    ): BacktestReport {

        require(candles.isNotEmpty()) {
            "No candle data available."
        }

        return engine.run(
            candles = candles,
            funding = funding,
            strategy = strategy,
            config = config
        )
    }
}
