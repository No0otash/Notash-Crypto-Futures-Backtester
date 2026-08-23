package com.notash.cryptobacktester.ai

import com.notash.cryptobacktester.core.BacktestReport
import java.util.Locale

object TradeAnalysisExporter {
    fun csv(report: BacktestReport, analysis: TradeAiAnalyzer.Analysis): String {
        val sb = StringBuilder()
        sb.appendLine("trade_id,side,entry_price,exit_price,quantity,gross_pnl,fees,funding,net_pnl,entry_time,exit_time,outcome,severity,primary_cause,evidence,recommendation")
        analysis.trades.forEach { d ->
            val t = report.trades.getOrNull(d.index - 1) ?: return@forEach
            val values = listOf(d.index, t.side, f(t.entryPrice), f(t.exitPrice), f(t.quantity), f(t.grossPnl), f(t.fees), f(t.funding), f(t.netPnl), t.entryTime, t.exitTime, d.outcome, d.severity, d.primaryCause, d.evidence.joinToString(" | "), d.recommendation)
            sb.appendLine(values.joinToString(",") { csv(it.toString()) })
        }
        return sb.toString()
    }

    fun json(report: BacktestReport, analysis: TradeAiAnalyzer.Analysis): String {
        fun q(s: String) = "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ") + "\""
        val trades = analysis.trades.mapIndexedNotNull { i, d -> report.trades.getOrNull(i)?.let { t ->
            "{\"id\":${d.index},\"side\":${q(t.side.name)},\"entry\":${f(t.entryPrice)},\"exit\":${f(t.exitPrice)},\"quantity\":${f(t.quantity)},\"grossPnl\":${f(t.grossPnl)},\"fees\":${f(t.fees)},\"funding\":${f(t.funding)},\"netPnl\":${f(t.netPnl)},\"outcome\":${q(d.outcome)},\"severity\":${q(d.severity)},\"cause\":${q(d.primaryCause)},\"evidence\":[${d.evidence.joinToString(",") { q(it) }}],\"recommendation\":${q(d.recommendation)}}"
        }}
        return "{\"summary\":${q(analysis.strategy.summary)},\"strengths\":[${analysis.strategy.strengths.joinToString(",") { q(it) }}],\"weaknesses\":[${analysis.strategy.weaknesses.joinToString(",") { q(it) }}],\"recommendations\":[${analysis.strategy.recommendations.joinToString(",") { q(it) }}],\"trades\":[${trades.joinToString(",")}]}"
    }

    private fun f(v: Double) = String.format(Locale.US, "%.8f", v)
    private fun csv(s: String) = if (s.any { it == ',' || it == '"' || it == '\n' }) "\"${s.replace("\"", "\"\"")}\"" else s
}
