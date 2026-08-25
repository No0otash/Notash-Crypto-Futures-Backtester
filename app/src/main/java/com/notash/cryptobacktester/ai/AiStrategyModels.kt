package com.notash.cryptobacktester.ai

/** Structured strategy specification returned by the AI backend. */
data class AiStrategySpec(
    val id: String,
    val name: String,
    val version: String = "1.0.0",
    val market: String = "crypto-futures",
    val timeframe: String = "1h",
    val entryRules: List<RuleClause> = emptyList(),
    val exitRules: List<RuleClause> = emptyList(),
    val risk: RiskSpec = RiskSpec(),
    val explanation: String = "",
    val assumptions: List<String> = emptyList()
)

data class RuleClause(val indicator: String, val operator: String, val value: String, val timeframe: String? = null)

data class RiskSpec(
    val riskPercent: Double = 1.0,
    val stopLossAtr: Double = 1.5,
    val takeProfitAtr: Double = 3.0,
    val leverage: Int = 1
)

data class AiStrategyRequest(
    val prompt: String,
    val language: String = "fa",
    val market: String = "crypto-futures",
    val timeframe: String = "1h"
)

data class AiStrategyResponse(
    val strategy: AiStrategySpec,
    val generatedCode: String,
    val warnings: List<String> = emptyList()
)

/** The AI should emit this DSL instead of arbitrary executable Kotlin. */
object StrategyDsl {
    fun render(s: AiStrategySpec): String = buildString {
        appendLine("strategy ${s.id} v${s.version}")
        appendLine("name: ${s.name}")
        appendLine("market: ${s.market}")
        appendLine("timeframe: ${s.timeframe}")
        appendLine("entry:")
        s.entryRules.forEach { appendLine("  - ${it.indicator} ${it.operator} ${it.value}${it.timeframe?.let { tf -> " [$tf]" } ?: ""}") }
        appendLine("exit:")
        s.exitRules.forEach { appendLine("  - ${it.indicator} ${it.operator} ${it.value}${it.timeframe?.let { tf -> " [$tf]" } ?: ""}") }
        appendLine("risk: ${s.risk.riskPercent}% SL=${s.risk.stopLossAtr}ATR TP=${s.risk.takeProfitAtr}ATR leverage=${s.risk.leverage}x")
    }
}
