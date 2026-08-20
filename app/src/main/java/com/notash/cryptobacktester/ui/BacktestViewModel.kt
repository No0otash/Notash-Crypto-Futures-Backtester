package com.notash.cryptobacktester.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notash.cryptobacktester.core.BacktestConfig
import com.notash.cryptobacktester.core.BacktestReport
import com.notash.cryptobacktester.engine.CoinExBacktestRunner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BacktestUiState(
    val market: String = "BTCUSDT",
    val timeframe: String = "1h",
    val strategy: String = "advanced_pullback_v1",
    val isRunning: Boolean = false,
    val status: String = "Ready",
    val error: String? = null,
    val report: BacktestReport? = null
)

class BacktestViewModel : ViewModel() {

    private val runner = CoinExBacktestRunner()

    private val _state =
        MutableStateFlow(BacktestUiState())

    val state: StateFlow<BacktestUiState> =
        _state.asStateFlow()

    fun setMarket(market: String) {
        _state.value =
            _state.value.copy(
                market = market
            )
    }

    fun setTimeframe(timeframe: String) {
        _state.value =
            _state.value.copy(
                timeframe = timeframe
            )
    }

    fun runBacktest() {

        if (_state.value.isRunning) {
            return
        }

        viewModelScope.launch {

            _state.value =
                _state.value.copy(
                    isRunning = true,
                    status = "Downloading CoinEx data...",
                    error = null
                )

            try {

                val now =
                    System.currentTimeMillis()

                val thirtyDays =
                    30L *
                        24L *
                        60L *
                        60L *
                        1000L

                val config =
                    BacktestConfig(
                        initialBalance = 1000.0,
                        riskPercent = 1.0,
                        leverage = 3.0,
                        fastLwma = 20,
                        slowLwma = 50,
                        atrPeriod = 14,
                        entryAtr = 0.5,
                        stopAtr = 1.5,
                        takeProfitAtr = 3.0,
                        takerFee = 0.0005,
                        slippageBps = 2.0,
                        useFunding = true
                    )

                _state.value =
                    _state.value.copy(
                        status = "Running backtest..."
                    )

                val result =
                    runner.run(
                        market = _state.value.market,
                        period = _state.value.timeframe,
                        startTime = now - thirtyDays,
                        endTime = now,
                        strategyId = _state.value.strategy,
                        config = config
                    )

                _state.value =
                    _state.value.copy(
                        isRunning = false,
                        status = "Backtest completed",
                        report = result
                    )

            } catch (e: Exception) {

                _state.value =
                    _state.value.copy(
                        isRunning = false,
                        status = "Failed",
                        error =
                            e.message
                                ?: "Unknown error"
                    )
            }
        }
    }
}
