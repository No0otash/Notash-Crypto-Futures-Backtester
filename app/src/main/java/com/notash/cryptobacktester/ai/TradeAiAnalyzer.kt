package com.notash.cryptobacktester.ai

import com.notash.cryptobacktester.core.BacktestReport
import com.notash.cryptobacktester.core.Side
import com.notash.cryptobacktester.core.TradeResult
import kotlin.math.abs

/** Deterministic, offline-first trade diagnosis. AI providers can consume the same structured report later. */
object TradeAiAnalyzer {
    data class TradeDiagnosis(
        val index: Int,
        val outcome: String,
        val severity: String,
        val primaryCause: String,
        val evidence: List<String>,
        val recommendation: String
    )

    data class StrategyDiagnosis(
        val summary: String,
        val strengths: List<String>,
        val weaknesses: List<String>,
        val recommendations: List<String>
    )

    data class Analysis(
        val trades: List<TradeDiagnosis>,
        val strategy: StrategyDiagnosis
    )

    fun analyze(report: BacktestReport): Analysis {
        val diagnoses = report.trades.mapIndexed { index, trade -> diagnose(index + 1, trade) }
        val wins = report.trades.count { it.netPnl > 0 }
        val losses = report.trades.count { it.netPnl < 0 }
        val fundingDrag = report.trades.sumOf { abs(it.funding) }
        val feeDrag = report.totalFees
        val strengths = mutableListOf<String>()
        val weaknesses = mutableListOf<String>()
        val recommendations = mutableListOf<String>()

        if (report.winRatePercent >= 50.0) strengths += "Win rate is at or above 50%."
        if (report.profitFactor >= 1.5) strengths += "Profit factor indicates strong aggregate payoff."
        if (report.netPnl > 0) strengths += "The backtest finished profitable after fees and funding."
        if (strengths.isEmpty()) strengths += "No strong aggregate advantage was detected."

        if (report.maxDrawdownPercent >= 20.0) weaknesses += "Maximum drawdown is high relative to the tested result."
        if (feeDrag > abs(report.netPnl) * 0.10) weaknesses += "Trading fees materially affect net performance."
        if (fundingDrag > abs(report.netPnl) * 0.10) weaknesses += "Funding materially affects net performance."
        if (losses > wins) weaknesses += "Losing trades outnumber winning trades."
        if (weaknesses.isEmpty()) weaknesses += "No dominant aggregate weakness was detected."

        if (report.maxDrawdownPercent >= 20.0) recommendations += "Reduce position risk or add a drawdown filter."
        if (feeDrag > abs(report.netPnl) * 0.10) recommendations += "Test lower turnover, maker execution, or wider signal filters."
        if (fundingDrag > abs(report.netPnl) * 0.10) recommendations += "Test funding-aware entries and avoid expensive funding windows."
        recommendations += "Compare this result against an out-of-sample period before optimizing parameters."

        val summary = "${report.trades.size} trades: $wins wins, $losses losses; net PNL ${"%.2f".format(report.netPnl)}, ROI ${"%.2f".format(report.roiPercent)}%."
        return Analysis(diagnoses, StrategyDiagnosis(summary, strengths, weaknesses, recommendations))
    }

    private fun diagnose(index: Int, t: TradeResult): TradeDiagnosis {
        val evidence = mutableListOf<String>()
        val cause: String
        val recommendation: String
        val outcome: String
        val severity: String

        if (t.netPnl > 0) {
            outcome = "WIN"
            severity = "POSITIVE"
            cause = if (t.grossPnl > abs(t.fees) + abs(t.funding)) "Gross price movement overcame trading costs." else "Costs were a major part of the result."
            evidence += "Gross PNL: ${"%.4f".format(t.grossPnl)}"
            evidence += "Fees: ${"%.4f".format(t.fees)}"
            evidence += "Funding: ${"%.4f".format(t.funding)}"
            recommendation = "Preserve the entry/exit pattern and test whether the edge survives out-of-sample."
        } else {
            outcome = "LOSS"
            severity = "NEGATIVE"
            cause = when {
                t.grossPnl < 0 && abs(t.fees) + abs(t.funding) > abs(t.grossPnl) * 0.25 -> "Price movement was adverse and execution costs amplified the loss."
                t.grossPnl < 0 -> "The market move after entry was adverse to the position."
                else -> "The trade had little gross edge and costs turned it negative."
            }
            evidence += "Gross PNL: ${"%.4f".format(t.grossPnl)}"
            evidence += "Fees: ${"%.4f".format(t.fees)}"
            evidence += "Funding: ${"%.4f".format(t.funding)}"
            recommendation = "Review the entry filter, stop distance, and market regime around this trade."
        }
        evidence += "Side: ${if (t.side == Side.LONG) "LONG" else "SHORT"}"
        return TradeDiagnosis(index, outcome, severity, cause, evidence, recommendation)
    }
}
