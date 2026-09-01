package com.notash.cryptobacktester.strategy

import com.notash.cryptobacktester.core.BacktestConfig

/** Versioned, AI-friendly interchange format. Source code is optional; execution uses the validated rule set. */
data class RobotPackage(
    val schemaVersion: Int,
    val id: String,
    val name: String,
    val version: String,
    val description: String,
    val parameters: Map<String, Double>,
    val rules: List<RobotRule>,
    val sourceLanguage: String? = null,
    val sourceCode: String? = null
)

data class RobotRule(
    val field: String,
    val operator: String,
    val value: Double,
    val action: String
)

data class RobotValidationResult(
    val valid: Boolean,
    val errors: List<String>,
    val warnings: List<String> = emptyList()
)

object RobotPackageValidator {
    private val allowedFields = setOf("close", "open", "high", "low", "volume")
    private val allowedOperators = setOf(">", ">=", "<", "<=", "==")
    private val allowedActions = setOf("LONG", "SHORT", "FLAT")

    fun validate(robot: RobotPackage): RobotValidationResult {
        val errors = mutableListOf<String>()
        if (robot.schemaVersion != 1) errors += "Unsupported robot schema version: ${robot.schemaVersion}"
        if (robot.id.isBlank()) errors += "Robot id is required"
        if (robot.name.isBlank()) errors += "Robot name is required"
        if (robot.rules.isEmpty()) errors += "At least one rule is required"
        robot.rules.forEachIndexed { i, r ->
            if (r.field !in allowedFields) errors += "Rule ${i + 1}: unsupported field ${r.field}"
            if (r.operator !in allowedOperators) errors += "Rule ${i + 1}: unsupported operator ${r.operator}"
            if (r.action !in allowedActions) errors += "Rule ${i + 1}: unsupported action ${r.action}"
            if (!r.value.isFinite()) errors += "Rule ${i + 1}: value must be finite"
        }
        robot.parameters.forEach { (k, v) -> if (!v.isFinite()) errors += "Parameter $k must be finite" }
        return RobotValidationResult(errors.isEmpty(), errors)
    }
}

class RobotPackageStrategy(private val robot: RobotPackage) : Strategy {
    override val id = robot.id
    override val name = robot.name
    override val version = robot.version
    override val description = robot.description

    override fun generateSignal(index: Int, candles: List<com.notash.cryptobacktester.core.Candle>, funding: List<com.notash.cryptobacktester.core.FundingRate>, config: BacktestConfig): com.notash.cryptobacktester.core.Signal? {
        if (index !in candles.indices) return null
        val candle = candles[index]
        val matched = robot.rules.filter { rule ->
            val field = when (rule.field) { "open" -> candle.open; "high" -> candle.high; "low" -> candle.low; "volume" -> candle.volume; else -> candle.close }
            when (rule.operator) { ">" -> field > rule.value; ">=" -> field >= rule.value; "<" -> field < rule.value; "<=" -> field <= rule.value; "==" -> field == rule.value; else -> false }
        }
        val action = matched.firstOrNull()?.action ?: return null
        if (action == "FLAT") return null
        val slPct = robot.parameters["stopLossPercent"] ?: 1.5
        val tpPct = robot.parameters["takeProfitPercent"] ?: 3.0
        val sl = if (action == "LONG") candle.close * (1.0 - slPct / 100.0) else candle.close * (1.0 + slPct / 100.0)
        val tp = if (action == "LONG") candle.close * (1.0 + tpPct / 100.0) else candle.close * (1.0 - tpPct / 100.0)
        return com.notash.cryptobacktester.core.Signal(
            side = if (action == "LONG") com.notash.cryptobacktester.core.Side.LONG else com.notash.cryptobacktester.core.Side.SHORT,
            orderType = com.notash.cryptobacktester.core.OrderType.MARKET,
            entryPrice = candle.close,
            stopLoss = sl,
            takeProfit = tp,
            reason = "Robot rule: ${matched.first().field} ${matched.first().operator} ${matched.first().value}"
        )
    }
}
