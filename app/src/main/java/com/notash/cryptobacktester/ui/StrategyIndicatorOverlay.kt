package com.notash.cryptobacktester.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.notash.cryptobacktester.core.Candle
import kotlin.math.max

/** Strategy overlays are visual only; the selected strategy remains the source of truth. */
data class StrategyIndicator(
    val id: String,
    val label: String,
    val period: Int,
    val color: Color = Color.Unspecified
)

@Composable
fun StrategyIndicatorPanel(
    indicators: List<StrategyIndicator>,
    fa: Boolean,
    onVisibilityChanged: (String, Boolean) -> Unit = { _, _ -> }
) {
    val enabled = remember { mutableStateMapOf<String, Boolean>().apply { indicators.forEach { put(it.id, true) } } }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(if (fa) "اندیکاتورهای استراتژی" else "STRATEGY INDICATORS")
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            indicators.forEach { indicator ->
                val visible = enabled[indicator.id] == true
                FilterChip(
                    selected = visible,
                    onClick = {
                        val next = !visible
                        enabled[indicator.id] = next
                        onVisibilityChanged(indicator.id, next)
                    },
                    label = { Text("${indicator.label} ${indicator.period}") }
                )
            }
        }
    }
}

@Composable
fun StrategyMovingAveragesOverlay(
    candles: List<Candle>,
    indicators: List<StrategyIndicator>,
    visibility: Map<String, Boolean>,
    modifier: Modifier = Modifier
) {
    if (candles.size < 2) return
    Canvas(modifier.fillMaxWidth().height(320.dp)) {
        val low = candles.minOf { it.low }
        val high = candles.maxOf { it.high }
        val range = max(high - low, 1e-9)
        fun y(value: Double) = size.height - ((value - low) / range).toFloat() * size.height
        fun average(index: Int, period: Int): Double? {
            if (index + 1 < period) return null
            var sum = 0.0
            for (i in index - period + 1..index) sum += candles[i].close
            return sum / period
        }
        indicators.filter { visibility[it.id] != false }.forEachIndexed { indicatorIndex, indicator ->
            val points = candles.indices.mapNotNull { i -> average(i, indicator.period)?.let { i to it } }
            if (points.size < 2) return@forEachIndexed
            val fallback = listOf(Color(0xFF22D3EE), Color(0xFFFFB84D), Color(0xFF8B5CF6), Color(0xFF20E6A5))[indicatorIndex % 4]
            for (i in 1 until points.size) {
                val (aIndex, aValue) = points[i - 1]
                val (bIndex, bValue) = points[i]
                val ax = aIndex.toFloat() / (candles.size - 1) * size.width
                val bx = bIndex.toFloat() / (candles.size - 1) * size.width
                drawLine(fallback, androidx.compose.ui.geometry.Offset(ax, y(aValue)), androidx.compose.ui.geometry.Offset(bx, y(bValue)), strokeWidth = 2.5f)
            }
        }
    }
}
