package com.notash.cryptobacktester.ui

import kotlin.math.max
import kotlin.math.min

data class ChartPoint(val x: Float, val y: Float)
data class CandleGeometry(
    val x: Float,
    val openY: Float,
    val closeY: Float,
    val highY: Float,
    val lowY: Float,
    val bullish: Boolean
)

data class MarkerGeometry(
    val x: Float,
    val y: Float,
    val direction: MarkerDirection,
    val label: String
)

enum class MarkerDirection { UP, DOWN }

fun priceToY(price: Double, minPrice: Double, maxPrice: Double, height: Float, padding: Float = 12f): Float {
    val range = max(maxPrice - minPrice, 1e-12)
    val normalized = ((price - minPrice) / range).coerceIn(0.0, 1.0)
    return height - padding - (normalized.toFloat() * (height - padding * 2f))
}

fun buildCandleGeometry(data: TradeChartData, width: Float, height: Float): List<CandleGeometry> {
    if (data.candles.isEmpty()) return emptyList()
    val minPrice = data.candles.minOf { it.low }
    val maxPrice = data.candles.maxOf { it.high }
    val step = width / data.candles.size.coerceAtLeast(1)
    return data.candles.mapIndexed { index, candle ->
        CandleGeometry(
            x = step * index + step / 2f,
            openY = priceToY(candle.open, minPrice, maxPrice, height),
            closeY = priceToY(candle.close, minPrice, maxPrice, height),
            highY = priceToY(candle.high, minPrice, maxPrice, height),
            lowY = priceToY(candle.low, minPrice, maxPrice, height),
            bullish = candle.close >= candle.open
        )
    }
}

fun buildMarkerGeometry(data: TradeChartData, width: Float, height: Float): List<MarkerGeometry> {
    if (data.candles.isEmpty() || data.markers.isEmpty()) return emptyList()
    val minPrice = data.candles.minOf { it.low }
    val maxPrice = data.candles.maxOf { it.high }
    val first = data.candles.first().timestamp
    val last = data.candles.last().timestamp.coerceAtLeast(first + 1)
    val span = (last - first).toDouble()
    return data.markers.map { marker ->
        val x = (((marker.timestamp - first).toDouble() / span).coerceIn(0.0, 1.0) * width).toFloat()
        MarkerGeometry(
            x = x,
            y = priceToY(marker.price, minPrice, maxPrice, height),
            direction = when (marker.type) {
                MarkerType.ENTRY -> if (marker.side.name == "LONG") MarkerDirection.UP else MarkerDirection.DOWN
                MarkerType.EXIT -> if (marker.side.name == "LONG") MarkerDirection.DOWN else MarkerDirection.UP
                MarkerType.STOP_LOSS -> MarkerDirection.DOWN
                MarkerType.TAKE_PROFIT -> MarkerDirection.UP
            },
            label = "${marker.side.name} ${marker.type.name} #${marker.tradeIndex + 1}"
        )
    }
}
