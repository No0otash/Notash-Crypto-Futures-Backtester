package com.notash.cryptobacktester.ui

import com.notash.cryptobacktester.core.Candle
import com.notash.cryptobacktester.core.Side
import com.notash.cryptobacktester.core.TradeResult

/** Immutable chart annotation data derived only from the executed backtest. */
data class TradeChartDetail(
    val index: Int,
    val side: Side,
    val entryTime: Long,
    val exitTime: Long,
    val entryPrice: Double,
    val exitPrice: Double,
    val stopLossPrice: Double?,
    val takeProfitPrice: Double?,
    val outcome: ExitOutcome,
    val netPnl: Double
)

fun buildTradeChartDetails(
    candles: List<Candle>,
    trades: List<TradeResult>
): List<TradeChartDetail> {
    val outcomes = resolveTradeOutcomes(candles, trades)
    return trades.mapIndexed { index, trade ->
        val entryCandle = candles.lastOrNull { it.timestamp <= trade.entryTime }
        val exitCandle = candles.firstOrNull { it.timestamp >= trade.exitTime }
        val stop = when (trade.side) {
            Side.LONG -> entryCandle?.let { it.low.coerceAtMost(trade.entryPrice) }
            Side.SHORT -> entryCandle?.let { it.high.coerceAtLeast(trade.entryPrice) }
        }
        val take = when (trade.side) {
            Side.LONG -> exitCandle?.let { maxOf(it.high, trade.exitPrice) }
            Side.SHORT -> exitCandle?.let { minOf(it.low, trade.exitPrice) }
        }
        val outcome = outcomes.getOrNull(index)?.outcome ?: ExitOutcome.OTHER
        TradeChartDetail(index, trade.side, trade.entryTime, trade.exitTime, trade.entryPrice, trade.exitPrice, stop, take, outcome, trade.netPnl)
    }
}
