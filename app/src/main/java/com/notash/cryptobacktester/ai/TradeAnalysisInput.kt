package com.notash.cryptobacktester.ai

import com.notash.cryptobacktester.core.TradeResult
import com.notash.cryptobacktester.ui.TradeReportSummary

/** Stable, privacy-safe text payload builder for a future AI trade analyst. */
fun buildTradeAnalysisInput(
    summary: TradeReportSummary,
    trades: List<TradeResult>
): String = buildString {
    appendLine("TRADE ANALYSIS DATA")
    appendLine("totalTrades=${summary.totalTrades}")
    appendLine("wins=${summary.wins}")
    appendLine("losses=${summary.losses}")
    appendLine("longTrades=${summary.longTrades}")
    appendLine("shortTrades=${summary.shortTrades}")
    appendLine("grossProfit=${summary.grossProfit}")
    appendLine("grossLoss=${summary.grossLoss}")
    appendLine("netPnl=${summary.netPnl}")
    appendLine("totalFees=${summary.totalFees}")
    appendLine("totalFunding=${summary.totalFunding}")
    appendLine("winRatePercent=${summary.winRatePercent}")
    appendLine("trades=")
    trades.forEachIndexed { index, trade ->
        append(index + 1).append('|')
            .append(trade.side.name).append('|')
            .append(trade.entryTime).append('|')
            .append(trade.exitTime).append('|')
            .append(trade.entryPrice).append('|')
            .append(trade.exitPrice).append('|')
            .append(trade.netPnl).append('|')
            .append(trade.fees).append('|')
            .append(trade.funding)
            .appendLine()
    }
}
