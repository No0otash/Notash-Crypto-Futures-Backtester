package com.notash.cryptobacktester.ui

import com.notash.cryptobacktester.core.Candle
import com.notash.cryptobacktester.core.Side
import com.notash.cryptobacktester.core.TradeResult

/**
 * Pure mapping layer for chart annotations. No assumptions are made about
 * execution: SL/TP labels are only emitted when the candle path can prove
 * that level was touched between entry and exit.
 */
enum class ExitOutcome { STOP_LOSS, TAKE_PROFIT, OTHER }

data class TradeOutcome(
    val tradeIndex: Int,
    val side: Side,
    val outcome: ExitOutcome,
    val exitTime: Long,
    val exitPrice: Double,
    val netPnl: Double
)

fun resolveTradeOutcomes(
    candles: List<Candle>,
    trades: List<TradeResult>
): List<TradeOutcome> = trades.mapIndexed { index, trade ->
    val path = candles.filter { it.timestamp in trade.entryTime..trade.exitTime }
    val touchedTp = when (trade.side) {
        Side.LONG -> path.any { it.high >= trade.exitPrice && trade.exitPrice >= trade.entryPrice }
        Side.SHORT -> path.any { it.low <= trade.exitPrice && trade.exitPrice <= trade.entryPrice }
    }
    val outcome = if (touchedTp) {
        if (trade.netPnl >= 0.0) ExitOutcome.TAKE_PROFIT else ExitOutcome.STOP_LOSS
    } else ExitOutcome.OTHER
    TradeOutcome(index, trade.side, outcome, trade.exitTime, trade.exitPrice, trade.netPnl)
}
