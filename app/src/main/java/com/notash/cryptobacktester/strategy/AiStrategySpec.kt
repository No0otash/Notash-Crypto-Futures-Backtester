package com.notash.cryptobacktester.strategy

data class AiStrategySpec(
    val id: String,
    val name: String,
    val description: String,
    val entryRules: String,
    val exitRules: String,
    val riskRules: String,
    val timeframe: String,
    val leverage: Int,
    val stopLossAtr: Double?,
    val takeProfitAtr: Double?
)

object AiStrategySpecValidator {
    fun validate(spec: AiStrategySpec): List<String> = buildList {
        if (spec.id.isBlank()) add("Strategy ID is required")
        if (spec.name.isBlank()) add("Strategy name is required")
        if (spec.entryRules.isBlank()) add("Entry rules are required")
        if (spec.exitRules.isBlank()) add("Exit rules are required")
        if (spec.leverage !in 1..100) add("Leverage must be between 1x and 100x")
        if (spec.stopLossAtr != null && spec.stopLossAtr <= 0) add("Stop loss ATR must be positive")
        if (spec.takeProfitAtr != null && spec.takeProfitAtr <= 0) add("Take profit ATR must be positive")
    }
}
