package com.notash.cryptobacktester.ai

/**
 * Production implementation calls the secure AI gateway.
 * API credentials must remain on the server, never in the APK.
 */
interface AiStrategyService {
    suspend fun generateStrategy(request: AiStrategyRequest): Result<AiStrategyResponse>
    suspend fun improveStrategy(strategy: AiStrategySpec, backtestReportJson: String): Result<AiStrategyResponse>
}

class AiStrategyPromptBuilder {
    fun build(request: AiStrategyRequest): String = """
        Convert the user's trading idea into a deterministic crypto-futures strategy.
        Return ONLY structured strategy data matching AiStrategySpec plus generatedCode.
        Never invent unavailable market data. Explicitly state assumptions.
        User language: ${request.language}
        Market: ${request.market}
        Timeframe: ${request.timeframe}
        User request: ${request.prompt}
    """.trimIndent()
}
