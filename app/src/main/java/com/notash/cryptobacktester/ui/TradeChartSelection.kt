package com.notash.cryptobacktester.ui

import com.notash.cryptobacktester.core.Candle
import com.notash.cryptobacktester.core.TradeResult
import kotlin.math.abs

fun nearestTradeIndex(
    candles: List<Candle>,
    trades: List<TradeResult>,
    candleIndex: Int
): Int {
    if (candles.isEmpty() || trades.isEmpty() || candleIndex !in candles.indices) return -1
    val timestamp = candles[candleIndex].timestamp
    return trades.indices.minByOrNull { i ->
        minOf(abs(trades[i].entryTime - timestamp), abs(trades[i].exitTime - timestamp))
    } ?: -1
}
