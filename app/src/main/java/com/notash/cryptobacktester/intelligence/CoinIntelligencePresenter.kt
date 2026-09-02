package com.notash.cryptobacktester.intelligence

data class CoinIntelligenceUiModel(
    val symbol: String,
    val market: String,
    val verdict: String,
    val overallLabel: String,
    val riskLabel: String,
    val opportunityLabel: String,
    val confidenceLabel: String,
    val componentLabels: List<String>,
    val warningLabels: List<String>,
    val gapLabels: List<String>,
    val sourceLabel: String,
    val summary: String,
    val updatedAtMs: Long
)

object CoinIntelligencePresenter {
    fun present(report: CoinIntelligenceReport, updatedAtMs: Long): CoinIntelligenceUiModel {
        val verified = report.sourceCount.coerceAtLeast(0)
        return CoinIntelligenceUiModel(
            symbol = report.symbol,
            market = report.market,
            verdict = report.verdict,
            overallLabel = "${report.overallScore.toInt()} / 100",
            riskLabel = "${report.riskScore.toInt()} / 100",
            opportunityLabel = "${report.opportunityScore.toInt()} / 100",
            confidenceLabel = "${report.confidenceScore.toInt()}%",
            componentLabels = report.components.map { component ->
                "${component.name}: ${component.score.toInt()} / 100 — ${component.explanation}"
            },
            warningLabels = report.warnings,
            gapLabels = report.dataGaps.map { "$it: unavailable" },
            sourceLabel = "$verified verified ${if (verified == 1) "source" else "sources"}",
            summary = report.summary,
            updatedAtMs = updatedAtMs
        )
    }
}
