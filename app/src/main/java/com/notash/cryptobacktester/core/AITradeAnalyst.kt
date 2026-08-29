package com.notash.cryptobacktester.core

/** Evidence-based trade review engine. It explains outcomes without inventing missing evidence. */
object AITradeAnalyst {
    data class TradeInput(
        val symbol: String,
        val side: String,
        val entryPrice: Double,
        val exitPrice: Double,
        val pnlPercent: Double,
        val exitReason: String? = null,
        val timeframe: String? = null,
        val stopLossTouched: Boolean? = null,
        val takeProfitTouched: Boolean? = null,
        val strategyName: String? = null,
        val marketTrendScore: Int? = null,
        val momentumScore: Int? = null,
        val fundingPercent: Double? = null,
        val dataConfidence: Int = 0
    )

    data class Report(
        val symbol: String,
        val outcome: String,
        val reasons: List<String>,
        val risks: List<String>,
        val optimizationSuggestions: List<String>,
        val confidence: Int
    )

    fun analyze(input: TradeInput): Report {
        val confidence = input.dataConfidence.coerceIn(0, 100)
        val outcome = when {
            input.pnlPercent > 0.0 -> "PROFITABLE"
            input.pnlPercent < 0.0 -> "LOSS"
            else -> "BREAKEVEN"
        }
        val reasons = buildList {
            add("Outcome: $outcome (${"%.2f".format(input.pnlPercent)}%)")
            input.exitReason?.takeIf { it.isNotBlank() }?.let { add("Exit reason: $it") }
            input.timeframe?.takeIf { it.isNotBlank() }?.let { add("Timeframe: $it") }
            input.strategyName?.takeIf { it.isNotBlank() }?.let { add("Strategy: $it") }
            input.marketTrendScore?.let { add("Market trend evidence: $it/100") }
            input.momentumScore?.let { add("Momentum evidence: $it/100") }
            input.stopLossTouched?.let { add("Stop-loss touched: $it") }
            input.takeProfitTouched?.let { add("Take-profit touched: $it") }
        }
        val risks = buildList {
            if (confidence < 60) add("Low data confidence; conclusions are limited")
            input.fundingPercent?.let { if (kotlin.math.abs(it) > 0.05) add("Funding suggests possible positioning crowding") }
            if (input.marketTrendScore != null && input.marketTrendScore < 40) add("Market trend evidence is weak")
            if (input.momentumScore != null && input.momentumScore < 40) add("Momentum evidence is weak")
        }
        val suggestions = buildList {
            if (input.marketTrendScore != null && input.marketTrendScore < 50) add("Test a trend filter before taking counter-trend entries")
            if (input.momentumScore != null && input.momentumScore < 50) add("Compare entries with momentum confirmation")
            if (input.stopLossTouched == true) add("Review stop distance and volatility before optimizing the strategy")
            if (input.exitReason.isNullOrBlank()) add("Record an explicit exit reason for better future analysis")
            if (confidence < 60) add("Collect more market and trade context before changing strategy parameters")
        }.distinct()
        return Report(input.symbol, outcome, reasons, risks, suggestions, confidence)
    }
}
