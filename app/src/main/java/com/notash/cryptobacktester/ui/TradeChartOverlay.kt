package com.notash.cryptobacktester.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.notash.cryptobacktester.core.Candle
import com.notash.cryptobacktester.core.Side
import com.notash.cryptobacktester.core.TradeResult
import kotlin.math.max

/** Lightweight overlay layer for Entry/Exit/SL/TP annotations. */
@Composable
fun TradeLevelsOverlay(
    candles: List<Candle>,
    trades: List<TradeResult>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier.fillMaxWidth().height(320.dp)) {
        if (candles.size < 2 || trades.isEmpty()) return@Canvas
        val low = candles.minOf { it.low }
        val high = candles.maxOf { it.high }
        val range = max(high - low, 1e-12)
        val first = candles.first().timestamp
        val last = candles.last().timestamp.coerceAtLeast(first + 1)
        val span = (last - first).toDouble()

        fun x(time: Long): Float = (((time - first).toDouble() / span).coerceIn(0.0, 1.0) * size.width).toFloat()
        fun y(price: Double): Float = size.height - (((price - low) / range).coerceIn(0.0, 1.0).toFloat() * size.height)

        trades.forEachIndexed { index, trade ->
            val entryX = x(trade.entryTime)
            val exitX = x(trade.exitTime)
            val entryY = y(trade.entryPrice)
            val exitY = y(trade.exitPrice)
            val sideColor = if (trade.side == Side.LONG) Color(0xFF20E6A5) else Color(0xFFFF5577)

            drawCircle(sideColor, 7f, Offset(entryX, entryY))
            drawCircle(Color(0xFF121A2D), 3f, Offset(entryX, entryY))
            drawCircle(Color.White, 6f, Offset(exitX, exitY), style = Stroke(2f))
            drawLine(sideColor.copy(alpha = .55f), Offset(entryX, entryY), Offset(exitX, exitY), 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(7f, 5f)))

            // Trade number tick makes overlapping entries distinguishable.
            val tickX = entryX + 10f
            drawLine(sideColor, Offset(tickX, entryY - 7f), Offset(tickX, entryY + 7f), 2f)
            if (index == trades.lastIndex) {
                // Keep the latest trade visually emphasized.
                drawCircle(sideColor.copy(alpha = .35f), 12f, Offset(entryX, entryY))
            }
        }
    }
}
