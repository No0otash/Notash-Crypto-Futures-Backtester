package com.notash.cryptobacktester.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notash.cryptobacktester.core.BacktestConfig
import com.notash.cryptobacktester.core.BacktestReport
import com.notash.cryptobacktester.core.MarketTicker
import com.notash.cryptobacktester.data.CoinExRepository
import com.notash.cryptobacktester.engine.CoinExBacktestRunner
import com.notash.cryptobacktester.imports.ImportedStrategyStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BacktestUiState(
    val market: String = "BTCUSDT",
    val timeframe: String = "1h",
    val strategy: String = "advanced_pullback_v1",
    val strategyName: String = "Advanced Pullback",
    val isRunning: Boolean = false,
    val isLoadingMarkets: Boolean = false,
    val status: String = "Ready",
    val error: String? = null,
    val report: BacktestReport? = null,
    val previousReport: BacktestReport? = null,
    val topMarkets: List<MarketTicker> = emptyList()
)

class BacktestViewModel : ViewModel() {
    private val runner = CoinExBacktestRunner()
    private val repository = CoinExRepository()
    private val _state = MutableStateFlow(BacktestUiState())
    val state: StateFlow<BacktestUiState> = _state.asStateFlow()

    init { ImportedStrategyStore.active?.let { _state.value = _state.value.copy(strategy = it.packageData.id, strategyName = it.packageData.name, market = it.packageData.symbol, timeframe = it.packageData.timeframe) }; loadTopMarkets() }

    fun setMarket(market: String) { _state.value = _state.value.copy(market = market) }
    fun setTimeframe(timeframe: String) { _state.value = _state.value.copy(timeframe = timeframe) }
    fun setStrategy(strategyId: String, name: String? = null) { _state.value = _state.value.copy(strategy = strategyId, strategyName = name ?: ImportedStrategyStore.get(strategyId)?.packageData?.name ?: strategyId) }

    fun loadTopMarkets() {
        if (_state.value.isLoadingMarkets) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingMarkets = true, error = null)
            try {
                val markets = repository.loadTopFuturesMarkets(10)
                _state.value = _state.value.copy(isLoadingMarkets = false, topMarkets = markets)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoadingMarkets = false, error = "Top 10 markets: ${e.message ?: "request failed"}")
            }
        }
    }

    fun runBacktest() {
        if (_state.value.isRunning) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isRunning = true, status = "Downloading CoinEx data…", error = null)
            try {
                val now = System.currentTimeMillis()
                val thirtyDays = 30L * 24L * 60L * 60L * 1000L
                val imported = ImportedStrategyStore.get(_state.value.strategy)
                val config = imported?.config ?: BacktestConfig(
                    initialBalance = 1000.0, riskPercent = 1.0, leverage = 3.0,
                    fastLwma = 20, slowLwma = 50, atrPeriod = 14, entryAtr = 0.5,
                    stopAtr = 1.5, takeProfitAtr = 3.0, takerFee = 0.0005,
                    slippageBps = 2.0, useFunding = true
                )
                _state.value = _state.value.copy(status = "Running ${_state.value.strategyName}…")
                val result = runner.run(_state.value.market, _state.value.timeframe, now - thirtyDays, now, _state.value.strategy, config)
                val old = _state.value.report
                _state.value = _state.value.copy(isRunning = false, status = "Backtest completed", previousReport = old, report = result)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isRunning = false, status = "Failed", error = e.message ?: "Unknown error")
            }
        }
    }
}
