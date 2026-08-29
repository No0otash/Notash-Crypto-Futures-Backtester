package com.notash.cryptobacktester.ui

import com.notash.cryptobacktester.analysis.TradeAnalysis
import com.notash.cryptobacktester.analysis.analyzeTrades
import com.notash.cryptobacktester.core.TradeResult

/**
 * Keeps the Analysis action independent from Compose and from network services.
 * The screen can request analysis only when a completed backtest report exists.
 */
sealed interface TradeAnalysisState {
    data object Idle : TradeAnalysisState
    data object Ready : TradeAnalysisState
    data class Result(val analysis: TradeAnalysis) : TradeAnalysisState
}

fun analysisStateFor(trades: List<TradeResult>): TradeAnalysisState =
    if (trades.isEmpty()) TradeAnalysisState.Idle else TradeAnalysisState.Ready

fun runTradeAnalysis(trades: List<TradeResult>): TradeAnalysisState =
    if (trades.isEmpty()) TradeAnalysisState.Idle
    else TradeAnalysisState.Result(analyzeTrades(trades))
