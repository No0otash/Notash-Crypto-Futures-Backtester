package com.notash.cryptobacktester.ai

object AiReportFormatter {
    fun toText(analysis: TradeAiAnalyzer.Analysis): String = buildString {
        appendLine("NOTASH AI STRATEGY DIAGNOSIS")
        appendLine()
        appendLine(analysis.strategy.summary)
        appendLine()
        appendLine("STRENGTHS")
        analysis.strategy.strengths.forEach { appendLine("- $it") }
        appendLine()
        appendLine("WEAKNESSES")
        analysis.strategy.weaknesses.forEach { appendLine("- $it") }
        appendLine()
        appendLine("RECOMMENDATIONS")
        analysis.strategy.recommendations.forEach { appendLine("- $it") }
        appendLine()
        appendLine("TRADE DIAGNOSIS")
        analysis.trades.forEach { trade ->
            appendLine("#${trade.index} ${trade.outcome} / ${trade.severity}")
            appendLine("Cause: ${trade.primaryCause}")
            trade.evidence.forEach { appendLine("  $it") }
            appendLine("Recommendation: ${trade.recommendation}")
            appendLine()
        }
    }
}
