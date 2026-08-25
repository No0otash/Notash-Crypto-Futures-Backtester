package com.notash.cryptobacktester.ai

/** User-facing request for turning natural-language strategy rules into a backtestable strategy. */
data class StrategyBuildRequest(
    val prompt: String,
    val market: String,
    val timeframe: String,
    val initialBalance: Double,
    val leverage: Int
)

data class GeneratedStrategy(
    val name: String,
    val description: String,
    val sourceCode: String,
    val normalizedRules: List<String>,
    val warnings: List<String> = emptyList()
)

data class StrategyValidationResult(
    val valid: Boolean,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList()
)

interface AiStrategyBuilder {
    suspend fun generate(request: StrategyBuildRequest): Result<GeneratedStrategy>
    suspend fun validate(strategy: GeneratedStrategy): StrategyValidationResult
}
