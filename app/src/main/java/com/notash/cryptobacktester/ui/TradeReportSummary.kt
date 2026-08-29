package com.notash.cryptobacktester.ui

import com.notash.cryptobacktester.core.Side
import com.notash.cryptobacktester.core.TradeResult

/** Pure report aggregation used by UI and AI analysis. */
data class TradeReportSummary(
    val totalTrades: Int,
    val wins: Int,
    val losses: Int,
    val longTrades: Int,
    val shortTrades: Int,
    val grossProfit: Double,
    val grossLoss: Double,
    val netPnl: Double,
    val totalFees: Double,
    val totalFunding: Double,
    val winRatePercent: Double
)

fun summarizeTrades(trades: List<TradeResult>): TradeReportSummary {
    val wins = trades.count { it.netPnl > 0.0 }
    val losses = trades.count { it.netPnl < 0.0 }
    val grossProfit = trades.filter { it.netPnl > 0.0 }.sumOf { it.netPnl }
    val grossLoss = trades.filter { it.netPnl < 0.0 }.sumOf { -it.netPnl }
    return TradeReportSummary(
        totalTrades = trades.size,
        wins = wins,
        losses = losses,
        longTrades = trades.count { it.side == Side.LONG },
        shortTrades = trades.count { it.side == Side.SHORT },
        grossProfit = grossProfit,
        grossLoss = grossLoss,
        netPnl = trades.sumOf { it.netPnl },
        totalFees = trades.sumOf { it.fees },
        totalFunding = trades.sumOf { it.funding },
        winRatePercent = if (trades.isEmpty()) 0.0 else wins * 100.0 / trades.size
    )
}
