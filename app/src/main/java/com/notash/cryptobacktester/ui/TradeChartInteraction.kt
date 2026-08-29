package com.notash.cryptobacktester.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.notash.cryptobacktester.core.Candle

/** Returns the trade index nearest to a tap in chart pixel coordinates. */
fun nearestTradeIndex(
    candles: List<Candle>,
    details: List<TradeChartDetail>,
    tap: Offset,
    width: Float,
    height: Float
): Int? {
    if (candles.isEmpty() || details.isEmpty() || width <= 0f || height <= 0f) return null
    val low = candles.minOf { it.low }
    val high = candles.maxOf { it.high }
    val range = (high - low).takeIf { it > 0.0 } ?: 1.0
    val first = candles.first().timestamp
    val last = candles.last().timestamp.coerceAtLeast(first + 1)
    val span = (last - first).toDouble()
    return details.minByOrNull { detail ->
        val x = ((detail.entryTime - first).toDouble() / span).coerceIn(0.0, 1.0) * width
        val y = height - ((detail.entryPrice - low) / range).coerceIn(0.0, 1.0) * height
        val dx = x - tap.x
        val dy = y - tap.y
        dx * dx + dy * dy
    }?.index
}

@Composable
fun rememberTradeChartSelection(
    candles: List<Candle>,
    details: List<TradeChartDetail>,
    modifier: Modifier = Modifier,
    onSelected: (TradeChartDetail?) -> Unit
) {
    var selected by remember(candles.size, details.size) { mutableStateOf<TradeChartDetail?>(null) }
    androidx.compose.foundation.layout.Box(
        modifier
            .fillMaxWidth()
            .height(1.dp)
            .pointerInput(candles, details) {
                detectTapGestures { tap ->
                    val index = nearestTradeIndex(candles, details, tap, size.width.toFloat(), size.height.toFloat())
                    selected = details.firstOrNull { it.index == index }
                    onSelected(selected)
                }
            }
    )
}
