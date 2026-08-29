package com.notash.cryptobacktester.analysis

import com.notash.cryptobacktester.core.TradeResult

/** Keeps the latest completed backtest trades as the single source for analysis. */
class BacktestAnalysisState {
    private var trades: List<TradeResult> = emptyList()

    fun updateFromBacktest(results: List<TradeResult>) {
        trades = results.toList()
    }

    fun currentTrades(): List<TradeResult> = trades

    fun analyze(): TradeAnalysis = analyzeTrades(trades)
}
