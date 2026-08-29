package com.notash.cryptobacktester.ai

/**
 * The Android client talks to a trusted backend. The backend is the only component
 * allowed to hold the OpenAI API credential and to call the model.
 */
interface StrategyGenerationRepository {
    suspend fun generate(request: StrategyGenerationRequest): Result<StrategyGenerationResult>
    suspend fun save(userId: String, strategy: StrategyPackage): Result<Unit>
    suspend fun list(userId: String): Result<List<StrategyPackage>>
}
