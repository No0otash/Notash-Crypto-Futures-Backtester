package com.notash.cryptobacktester.engine

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LiveBacktestState {
    private val _running = MutableStateFlow(false)
    private val _equity = MutableStateFlow(1000.0)
    private val _pnl = MutableStateFlow(0.0)
    private val _trades = MutableStateFlow(0)
    private val _winRate = MutableStateFlow(0.0)

    val running: StateFlow<Boolean> = _running.asStateFlow()
    val equity: StateFlow<Double> = _equity.asStateFlow()
    val pnl: StateFlow<Double> = _pnl.asStateFlow()
    val trades: StateFlow<Int> = _trades.asStateFlow()
    val winRate: StateFlow<Double> = _winRate.asStateFlow()

    fun start() { _running.value = true }
    fun stop() { _running.value = false }
    fun reset(balance: Double = 1000.0) {
        _running.value = false; _equity.value = balance; _pnl.value = 0.0; _trades.value = 0; _winRate.value = 0.0
    }
    fun applyTrade(result: TradeResult) {
        _pnl.value += result.pnl
        _equity.value += result.pnl
        _trades.value += 1
        val wins = if (result.pnl > 0) 1 else 0
        _winRate.value = ((_winRate.value / 100.0 * (_trades.value - 1) + wins) / _trades.value) * 100.0
    }
}
