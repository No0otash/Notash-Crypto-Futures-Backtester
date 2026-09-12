package com.notash.cryptobacktester.ui

import com.notash.cryptobacktester.core.Candle
import com.notash.cryptobacktester.core.Side
import com.notash.cryptobacktester.core.TradeResult

data class TradingChartPoint(
    val entryIndex: Int,
    val exitIndex: Int,
    val side: Side,
    val entryPrice: Double,
    val exitPrice: Double,
    val stopLoss: Double,
    val takeProfit: Double,
    val exitReason: String,
    val slTouched: Boolean,
    val netPnl: Double,
    val timeframe: String
)

fun nearestCandleIndex(candles: List<Candle>, timestamp: Long): Int {
    require(candles.isNotEmpty()) { "Cannot map a trade to an empty candle list." }
    return candles.indices.minBy { index -> kotlin.math.abs(candles[index].timestamp - timestamp) }
}

fun buildTradingChartPoint(candles: List<Candle>, trade: TradeResult): TradingChartPoint {
    return TradingChartPoint(
        entryIndex = nearestCandleIndex(candles, trade.entryTime),
        exitIndex = nearestCandleIndex(candles, trade.exitTime),
        side = trade.side,
        entryPrice = trade.entryPrice,
        exitPrice = trade.exitPrice,
        stopLoss = trade.stopLoss,
        takeProfit = trade.takeProfit,
        exitReason = trade.exitReason,
        slTouched = trade.slTouched,
        netPnl = trade.netPnl,
        timeframe = trade.timeframe
    )
}
