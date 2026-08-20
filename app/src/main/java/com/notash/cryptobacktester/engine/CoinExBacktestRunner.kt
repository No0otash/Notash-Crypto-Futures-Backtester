package com.notash.cryptobacktester.engine

import com.notash.cryptobacktester.core.BacktestConfig
import com.notash.cryptobacktester.core.BacktestReport
import com.notash.cryptobacktester.data.HistoricalDataManager
import com.notash.cryptobacktester.strategy.StrategyFactory

class CoinExBacktestRunner(
    private val dataManager:
        HistoricalDataManager =
        HistoricalDataManager(),

    private val runner:
        BacktestRunner =
        BacktestRunner()
) {

    suspend fun run(
        market: String,
        period: String,
        startTime: Long,
        endTime: Long,
        strategyId: String,
        config: BacktestConfig
    ): BacktestReport {

        val strategy =
            StrategyFactory.create(
                strategyId
            )
                ?: throw IllegalArgumentException(
                    "Unknown strategy: $strategyId"
                )

        val candles =
            dataManager.downloadKlines(
                market = market,
                period = period,
                startTime = startTime,
                endTime = endTime
            )

        if (candles.isEmpty()) {
            throw IllegalStateException(
                "CoinEx returned no candle data."
            )
        }

        return runner.run(
            strategy = strategy,
            candles = candles,
            funding = emptyList(),
            config = config
        )
    }
}
