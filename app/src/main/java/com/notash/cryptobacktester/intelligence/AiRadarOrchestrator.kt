package com.notash.cryptobacktester.intelligence

/**
 * Production orchestration boundary for AI Radar.
 * Public exchange market data is fetched independently; one failed venue never
 * suppresses healthy venues. HuntFlo/other intelligence is injected by the
 * caller so the Radar never invents whale/news/tokenomics data.
 */
class AiRadarOrchestrator(
    private val repository: MultiExchangeRadarRepository = MultiExchangeRadarRepository(),
    private val engine: AiRadarEngine = AiRadarEngine()
) {
    data class Result(
        val signals: List<RadarSignal>,
        val health: List<RadarProviderHealth>,
        val generatedAtMs: Long
    )

    suspend fun scan(
        symbols: List<String>,
        whalePressure: Map<String, Int> = emptyMap(),
        newsImpact: Map<String, Int> = emptyMap(),
        tokenomicsRisk: Map<String, Int> = emptyMap()
    ): Result {
        val cleanSymbols = symbols.map { it.trim().uppercase() }.filter { it.isNotEmpty() }.distinct()
        require(cleanSymbols.isNotEmpty()) { "At least one market symbol is required" }
        val (snapshots, health) = repository.snapshots(cleanSymbols)
        val signals = engine.score(snapshots, whalePressure, newsImpact, tokenomicsRisk)
        return Result(signals, health, System.currentTimeMillis())
    }
}
