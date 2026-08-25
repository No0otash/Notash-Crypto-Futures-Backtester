package com.notash.cryptobacktester.ai

data class StrategyPackage(
    val name: String,
    val description: String,
    val market: String,
    val timeframe: String,
    val entryRules: List<String>,
    val exitRules: List<String>,
    val stopLoss: String,
    val takeProfit: String,
    val riskPercent: Double,
    val leverage: Int,
    val indicators: List<String>,
    val warnings: List<String> = emptyList()
)

interface AiStrategyService {
    suspend fun generateStrategy(prompt: String, language: String = "fa"): Result<StrategyPackage>
    suspend fun improveStrategy(strategy: StrategyPackage, feedback: String): Result<StrategyPackage>
}
