package com.notash.cryptobacktester.engine

import com.notash.cryptobacktester.core.BacktestConfig
import com.notash.cryptobacktester.core.BacktestReport
import com.notash.cryptobacktester.data.HistoricalDataManager
import com.notash.cryptobacktester.data.CoinExRepository
import com.notash.cryptobacktester.strategy.StrategyFactory

class CoinExBacktestRunner(
    private val dataManager: HistoricalDataManager = HistoricalDataManager(),
    private val repository: CoinExRepository = CoinExRepository(),
    private val runner: BacktestRunner = BacktestRunner()
) {

    suspend fun run(
        market: String,
        period: String,
        startTime: Long,
        endTime: Long,
        strategyId: String,
        config: BacktestConfig
    ): BacktestReport {

        val strategy = StrategyFactory.create(strategyId)
            ?: throw IllegalArgumentException("Unknown strategy: $strategyId")

        val candles = dataManager.downloadKlines(
            market = market,
            period = period,
            startTime = startTime,
            endTime = endTime
        )

        if (candles.isEmpty()) {
            throw IllegalStateException("CoinEx returned no candle data.")
        }

        // Funding is fetched separately because the CoinEx funding endpoint
        // currently exposes the latest funding information. Keep it optional
        // so a temporary funding API failure does not prevent candle backtests.
        val funding = if (config.useFunding) {
            repository.loadFundingRate(market)?.let { listOf(it) } ?: emptyList()
        } else {
            emptyList()
        }

        return runner.run(
            strategy = strategy,
            candles = candles,
            funding = funding,
            config = config
        )
    }
}
