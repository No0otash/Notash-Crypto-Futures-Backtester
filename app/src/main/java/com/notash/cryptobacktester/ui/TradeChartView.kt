package com.notash.cryptobacktester.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.max

@Composable
fun InteractiveTradeChart(
    data: TradeChartData,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var translationX by remember { mutableFloatStateOf(0f) }

    Canvas(
        modifier
            .fillMaxWidth()
            .height(380.dp)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.75f, 6f)
                    translationX += pan.x
                }
            }
    ) {
        if (data.candles.isEmpty()) return@Canvas
        val baseWidth = size.width * scale
        val candleWidth = max(2f, baseWidth / data.candles.size * 0.62f)
        val candleGeometries = buildCandleGeometry(data, baseWidth, size.height)
        val markers = buildMarkerGeometry(data, baseWidth, size.height)
        val offset = translationX.coerceIn(size.width - baseWidth, 0f)

        candleGeometries.forEach { candle ->
            val x = candle.x + offset
            if (x < -candleWidth || x > size.width + candleWidth) return@forEach
            val color = if (candle.bullish) Color(0xFF20D47A) else Color(0xFFFF4D67)
            drawLine(color, Offset(x, candle.highY), Offset(x, candle.lowY), 2f)
            drawRect(color, Offset(x - candleWidth / 2f, minOf(candle.openY, candle.closeY)), androidx.compose.ui.geometry.Size(candleWidth, max(2f, kotlin.math.abs(candle.closeY - candle.openY))))
        }

        markers.forEach { marker ->
            val x = marker.x + offset
            if (x < -20f || x > size.width + 20f) return@forEach
            val color = when {
                marker.label.contains("ENTRY") -> Color(0xFF00D9FF)
                marker.label.contains("EXIT") -> Color.White
                marker.label.contains("STOP_LOSS") -> Color(0xFFFF4D67)
                else -> Color(0xFFFFC857)
            }
            val direction = if (marker.direction == MarkerDirection.UP) -1f else 1f
            val path = Path().apply {
                moveTo(x, marker.y + direction * 14f)
                lineTo(x - 8f, marker.y + direction * 3f)
                lineTo(x + 8f, marker.y + direction * 3f)
                close()
            }
            drawPath(path, color)
            drawCircle(color, 3.5f, Offset(x, marker.y))
        }
    }
}
