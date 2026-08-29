package com.notash.cryptobacktester.analysis

import com.notash.cryptobacktester.core.BacktestReport

/** Bridges a completed core backtest report into the analysis state. */
class BacktestAnalysisCoordinator(
    private val state: BacktestAnalysisState = BacktestAnalysisState()
) {
    fun accept(report: BacktestReport): TradeAnalysis {
        state.updateFromBacktest(report.trades)
        return state.analyze()
    }

    fun currentTrades() = state.currentTrades()
}
