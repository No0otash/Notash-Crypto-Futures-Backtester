package com.notash.cryptobacktester.ui

import com.notash.cryptobacktester.core.Candle
import com.notash.cryptobacktester.core.ExitReason
import com.notash.cryptobacktester.core.Side
import com.notash.cryptobacktester.core.TradeResult

data class TradeMarker(
    val timestamp: Long,
    val price: Double,
    val type: MarkerType,
    val side: Side,
    val tradeIndex: Int
)

enum class MarkerType { ENTRY, EXIT, STOP_LOSS, TAKE_PROFIT }

data class TradeChartData(
    val candles: List<Candle>,
    val markers: List<TradeMarker>,
    val equityCurve: List<Double>
)

fun buildTradeChartData(
    candles: List<Candle>,
    trades: List<TradeResult>,
    equityCurve: List<Double>
): TradeChartData {
    val markers = trades.flatMapIndexed { index, trade ->
        buildList {
            add(TradeMarker(trade.entryTime, trade.entryPrice, MarkerType.ENTRY, trade.side, index))
            add(TradeMarker(trade.exitTime, trade.exitPrice, MarkerType.EXIT, trade.side, index))
            when (trade.exitReason) {
                ExitReason.STOP_LOSS -> add(TradeMarker(trade.exitTime, trade.exitPrice, MarkerType.STOP_LOSS, trade.side, index))
                ExitReason.TAKE_PROFIT -> add(TradeMarker(trade.exitTime, trade.exitPrice, MarkerType.TAKE_PROFIT, trade.side, index))
                else -> Unit
            }
        }
    }
    return TradeChartData(candles = candles, markers = markers, equityCurve = equityCurve)
}
