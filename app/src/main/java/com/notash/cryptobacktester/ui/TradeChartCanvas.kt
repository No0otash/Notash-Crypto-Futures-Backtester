package com.notash.cryptobacktester.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.max

@Composable
fun TradeChartCanvas(
    data: TradeChartData,
    modifier: Modifier = Modifier
) {
    Canvas(modifier.fillMaxWidth().height(360.dp)) {
        if (data.candles.isEmpty()) return@Canvas
        val geometries = buildCandleGeometry(data, size.width, size.height)
        val candleWidth = max(2f, size.width / data.candles.size * 0.62f)

        geometries.forEach { c ->
            val bodyTop = minOf(c.openY, c.closeY)
            val bodyBottom = maxOf(c.openY, c.closeY)
            val half = candleWidth / 2f
            drawLine(
                color = if (c.bullish) androidx.compose.ui.graphics.Color(0xFF20D47A) else androidx.compose.ui.graphics.Color(0xFFFF4D67),
                start = Offset(c.x, c.highY), end = Offset(c.x, c.lowY), strokeWidth = 2f
            )
            drawRect(
                color = if (c.bullish) androidx.compose.ui.graphics.Color(0xFF20D47A) else androidx.compose.ui.graphics.Color(0xFFFF4D67),
                topLeft = Offset(c.x - half, bodyTop),
                size = androidx.compose.ui.geometry.Size(candleWidth, max(2f, bodyBottom - bodyTop))
            )
        }

        buildMarkerGeometry(data, size.width, size.height).forEach { marker ->
            val markerColor = when {
                marker.label.contains("ENTRY") -> androidx.compose.ui.graphics.Color(0xFF00D9FF)
                marker.label.contains("EXIT") -> androidx.compose.ui.graphics.Color.White
                marker.label.contains("STOP_LOSS") -> androidx.compose.ui.graphics.Color(0xFFFF4D67)
                else -> androidx.compose.ui.graphics.Color(0xFFFFC857)
            }
            val direction = if (marker.direction == MarkerDirection.UP) -1f else 1f
            val x = marker.x
            val y = marker.y
            val path = Path().apply {
                moveTo(x, y + direction * 12f)
                lineTo(x - 7f, y + direction * 2f)
                lineTo(x + 7f, y + direction * 2f)
                close()
            }
            drawPath(path, markerColor)
            drawCircle(markerColor, radius = 3.5f, center = Offset(x, y))
        }

        if (data.equityCurve.size > 1) {
            val minEquity = data.equityCurve.minOrNull() ?: 0.0
            val maxEquity = data.equityCurve.maxOrNull() ?: 1.0
            val equityRange = max(maxEquity - minEquity, 1e-12)
            val path = Path()
            data.equityCurve.forEachIndexed { i, value ->
                val x = i.toFloat() / (data.equityCurve.lastIndex.coerceAtLeast(1)) * size.width
                val y = size.height - 18f - (((value - minEquity) / equityRange).toFloat() * 70f)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, androidx.compose.ui.graphics.Color(0xFFB48CFF), style = Stroke(width = 2.5f))
        }
    }
}
