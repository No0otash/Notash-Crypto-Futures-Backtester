package com.notash.cryptobacktester.ai

/** Machine-readable strategy contract returned by the trusted AI backend. */
data class StrategyPackage(
    val id: String,
    val name: String,
    val version: Int,
    val symbol: String,
    val timeframe: String,
    val direction: String,
    val indicators: List<IndicatorSpec>,
    val entryRules: List<String>,
    val exitRules: List<String>,
    val risk: RiskSpec,
    val filters: List<String> = emptyList(),
    val explanationFa: String = "",
    val explanationEn: String = ""
)

data class IndicatorSpec(val name: String, val period: Int? = null, val parameters: Map<String, Double> = emptyMap())
data class RiskSpec(val riskPercent: Double, val leverage: Double, val stopLossAtr: Double?, val takeProfitAtr: Double?)

data class StrategyGenerationRequest(
    val prompt: String,
    val symbol: String = "BTCUSDT",
    val timeframe: String = "1h",
    val language: String = "fa"
)

data class StrategyGenerationResult(
    val strategy: StrategyPackage,
    val warnings: List<String> = emptyList(),
    val model: String,
    val requestId: String
)

object StrategyPackageValidator {
    fun validate(s: StrategyPackage): List<String> = buildList {
        if (s.id.isBlank()) add("Strategy id is required")
        if (s.name.isBlank()) add("Strategy name is required")
        if (s.symbol.isBlank()) add("Symbol is required")
        if (s.timeframe.isBlank()) add("Timeframe is required")
        if (s.entryRules.isEmpty()) add("At least one entry rule is required")
        if (s.exitRules.isEmpty()) add("At least one exit rule is required")
        if (s.risk.riskPercent <= 0.0 || s.risk.riskPercent > 10.0) add("Risk must be between 0 and 10 percent")
        if (s.risk.leverage <= 0.0 || s.risk.leverage > 100.0) add("Leverage must be between 1 and 100")
        if (s.indicators.any { it.period != null && it.period <= 0 }) add("Indicator periods must be positive")
    }
}
