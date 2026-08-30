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
    val riskPercent: Double = 1.0,
    val leverage: Double = 3.0,
    val makerFee: Double = 0.0002,
    val takerFee: Double = 0.0005,
    val useFunding: Boolean = true,
    val isRunning: Boolean = false,
    val status: String = "Ready",
    val error: String? = null,
    val report: BacktestReport? = null,
    val previousReport: BacktestReport? = null
)

class BacktestViewModel : ViewModel() {
    private val runner = CoinExBacktestRunner()
    private val _state = MutableStateFlow(BacktestUiState())
    val state: StateFlow<BacktestUiState> = _state.asStateFlow()

    fun setMarket(market: String) { _state.value = _state.value.copy(market = market.trim().uppercase()) }
    fun setTimeframe(timeframe: String) { _state.value = _state.value.copy(timeframe = timeframe) }
    fun setStrategy(strategy: String) { _state.value = _state.value.copy(strategy = strategy) }
    fun setRiskPercent(value: Double) { _state.value = _state.value.copy(riskPercent = value.coerceIn(0.01, 100.0)) }
    fun setLeverage(value: Double) { _state.value = _state.value.copy(leverage = value.coerceIn(1.0, 100.0)) }
    fun setMakerFee(value: Double) { _state.value = _state.value.copy(makerFee = value.coerceIn(0.0, 0.1)) }
    fun setTakerFee(value: Double) { _state.value = _state.value.copy(takerFee = value.coerceIn(0.0, 0.1)) }
    fun setUseFunding(value: Boolean) { _state.value = _state.value.copy(useFunding = value) }

    fun runBacktest() {
        val current = _state.value
        if (current.isRunning) return
        viewModelScope.launch {
            _state.value = current.copy(isRunning = true, status = "Downloading CoinEx data...", error = null)
            try {
                val now = System.currentTimeMillis()
                val thirtyDays = 30L * 24L * 60L * 60L * 1000L
                val config = BacktestConfig(
                    initialBalance = 1000.0,
                    riskPercent = current.riskPercent,
                    leverage = current.leverage,
                    fastLwma = 20,
                    slowLwma = 50,
                    atrPeriod = 14,
                    entryAtr = 0.5,
                    stopAtr = 1.5,
                    takeProfitAtr = 3.0,
                    makerFee = current.makerFee,
                    takerFee = current.takerFee,
                    slippageBps = 2.0,
                    useFunding = current.useFunding
                )
                _state.value = _state.value.copy(status = "Running backtest...")
                val result = runner.run(current.market, current.timeframe, now - thirtyDays, now, current.strategy, config)
                val old = _state.value.report
                _state.value = _state.value.copy(isRunning = false, status = "Backtest completed", previousReport = old, report = result)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isRunning = false, status = "Failed", error = e.message ?: "Unknown error")
            }
        }
    }
}
