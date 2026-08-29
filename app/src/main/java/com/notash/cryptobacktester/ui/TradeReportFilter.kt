package com.notash.cryptobacktester.ui

import com.notash.cryptobacktester.core.Side
import com.notash.cryptobacktester.core.TradeResult

enum class TradeFilter { ALL, LONG, SHORT, WIN, LOSS }
enum class TradeSort { TIME_ASC, TIME_DESC, PNL_ASC, PNL_DESC }

fun filterAndSortTrades(
    trades: List<TradeResult>,
    filter: TradeFilter = TradeFilter.ALL,
    sort: TradeSort = TradeSort.TIME_ASC
): List<TradeResult> {
    val filtered = when (filter) {
        TradeFilter.ALL -> trades
        TradeFilter.LONG -> trades.filter { it.side == Side.LONG }
        TradeFilter.SHORT -> trades.filter { it.side == Side.SHORT }
        TradeFilter.WIN -> trades.filter { it.netPnl > 0.0 }
        TradeFilter.LOSS -> trades.filter { it.netPnl < 0.0 }
    }
    return when (sort) {
        TradeSort.TIME_ASC -> filtered.sortedBy { it.entryTime }
        TradeSort.TIME_DESC -> filtered.sortedByDescending { it.entryTime }
        TradeSort.PNL_ASC -> filtered.sortedBy { it.netPnl }
        TradeSort.PNL_DESC -> filtered.sortedByDescending { it.netPnl }
    }
}
