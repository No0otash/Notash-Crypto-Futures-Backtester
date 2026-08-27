package com.notash.cryptobacktester.strategy

/** A strategy-declared indicator. The chart consumes these specs; it never invents indicators. */
data class IndicatorSpec(
    val id: String,
    val type: IndicatorType,
    val period: Int,
    val enabledByDefault: Boolean = true
)

enum class IndicatorType { SMA, EMA, WMA, LWMA }

object IndicatorCalculator {
    fun value(type: IndicatorType, closes: List<Double>, period: Int): List<Double?> {
        require(period > 0) { "period must be positive" }
        if (closes.isEmpty()) return emptyList()
        return when (type) {
            IndicatorType.SMA -> sma(closes, period)
            IndicatorType.EMA -> ema(closes, period)
            IndicatorType.WMA, IndicatorType.LWMA -> wma(closes, period)
        }
    }

    private fun sma(values: List<Double>, period: Int): List<Double?> =
        values.indices.map { i -> if (i + 1 < period) null else values.subList(i - period + 1, i + 1).average() }

    private fun ema(values: List<Double>, period: Int): List<Double?> {
        if (values.size < period) return values.map { null }
        val out = MutableList<Double?>(values.size) { null }
        var previous = values.take(period).average()
        out[period - 1] = previous
        val alpha = 2.0 / (period + 1.0)
        for (i in period until values.size) {
            previous = (values[i] - previous) * alpha + previous
            out[i] = previous
        }
        return out
    }

    private fun wma(values: List<Double>, period: Int): List<Double?> {
        val denominator = period * (period + 1) / 2.0
        return values.indices.map { i ->
            if (i + 1 < period) null
            else values.subList(i - period + 1, i + 1).mapIndexed { index, v -> v * (index + 1) }.sum() / denominator
        }
    }
}
