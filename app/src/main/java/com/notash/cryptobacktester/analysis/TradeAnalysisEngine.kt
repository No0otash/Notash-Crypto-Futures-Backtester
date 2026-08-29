package com.notash.cryptobacktester.analysis

import com.notash.cryptobacktester.core.TradeResult
import com.notash.cryptobacktester.ui.TradeReportSummary
import com.notash.cryptobacktester.ui.summarizeTrades

data class TradeAnalysis(
    val score: Int,
    val strengths: List<String>,
    val weaknesses: List<String>,
    val recommendations: List<String>
)

fun analyzeTrades(trades: List<TradeResult>): TradeAnalysis {
    val s: TradeReportSummary = summarizeTrades(trades)
    if (trades.isEmpty()) return TradeAnalysis(0, emptyList(), listOf("No completed trades"), listOf("Run a backtest with completed trades first"))

    val strengths = buildList {
        if (s.winRatePercent >= 60.0) add("Strong win rate")
        if (s.netPnl > 0.0) add("Positive net PnL")
        if (s.totalFees <= s.grossProfit * 0.10) add("Fees are controlled relative to gross profit")
    }
    val weaknesses = buildList {
        if (s.winRatePercent < 50.0) add("Win rate is below 50%")
        if (s.netPnl <= 0.0) add("Net PnL is not positive")
        if (s.totalFees > s.grossProfit * 0.10 && s.grossProfit > 0.0) add("Fees materially reduce gross profit")
        if (s.losses > s.wins) add("Loss count is higher than win count")
    }
    val recommendations = buildList {
        if (s.totalFees > s.grossProfit * 0.10 && s.grossProfit > 0.0) add("Review trading frequency and fee impact")
        if (s.winRatePercent < 50.0) add("Review entry and confirmation rules")
        if (s.losses > 0) add("Review losing trades and stop placement")
        if (isEmpty()) add("Strategy metrics look healthy; validate on additional market periods")
    }
    val score = (50 + (s.winRatePercent - 50.0) * 0.8 + if (s.netPnl > 0) 15 else -15).toInt().coerceIn(0, 100)
    return TradeAnalysis(score, strengths, weaknesses, recommendations)
}
