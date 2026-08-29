package com.notash.cryptobacktester.ui

import com.notash.cryptobacktester.core.Side
import com.notash.cryptobacktester.core.TradeResult

enum class TradeMarkerType { LONG_ENTRY, SHORT_ENTRY, EXIT, STOP_LOSS, TAKE_PROFIT }

data class TradeChartMarker(
    val time: Long,
    val price: Double,
    val type: TradeMarkerType,
    val tradeIndex: Int
)

fun buildTradeChartMarkers(trades: List<TradeResult>): List<TradeChartMarker> = buildList {
    trades.forEachIndexed { index, trade ->
        add(TradeChartMarker(trade.entryTime, trade.entryPrice, if (trade.side == Side.LONG) TradeMarkerType.LONG_ENTRY else TradeMarkerType.SHORT_ENTRY, index))
        add(TradeChartMarker(trade.exitTime, trade.exitPrice, TradeMarkerType.EXIT, index))
    }
}
