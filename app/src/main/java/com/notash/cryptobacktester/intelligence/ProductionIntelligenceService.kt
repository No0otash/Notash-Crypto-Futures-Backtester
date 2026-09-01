package com.notash.cryptobacktester.intelligence

import com.notash.cryptobacktester.core.Candle
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/**
 * Single application-facing enrichment service for Coin Intelligence and Pump/Dump.
 * Existing UI can keep its current models; this service supplies verified enrichment
 * and explicit gaps without deleting or replacing existing signals.
 */
data class ProductionIntelligenceResult(
    val coin: CoinIntelligenceReport,
    val pumpDump: PumpDumpSignal?,
    val onChain: OnChainAnalysis?,
    val providerHealth: List<ProviderHealth>,
    val dataGaps: List<String>
)

class ProductionIntelligenceService(
    private val coinEngine: CoinIntelligenceEngine = CoinIntelligenceEngine(),
    private val marketEngine: MarketIntelligenceEngine = MarketIntelligenceEngine(),
    private val onChainProvider: DeepOnChainProvider = DeepOnChainProviderRouter()
) {
    suspend fun analyze(
        snapshot: MemeCoinSnapshot,
        candles: List<Candle>,
        chain: String,
        contractOrMint: String,
        whaleActivity: WhaleActivity? = null,
        projectQualityScore: Double? = null,
        tokenomicsScore: Double? = null,
        research: ResearchSummary? = null,
        roadmap: RoadmapAnalysis? = null,
        unlocks: UnlockAnalysis? = null
    ): ProductionIntelligenceResult = coroutineScope {
        val onChainDeferred = async { onChainProvider.load(chain, contractOrMint) }
        val pump = marketEngine.analyzePriceAndVolume(candles)
        val deep = onChainDeferred.await()
        val onChain = if (deep.snapshot.source != null) OnChainEngine().analyze(deep.snapshot) else null
        val report = coinEngine.analyze(
            CoinIntelligenceInput(
                snapshot = snapshot,
                candles = candles,
                whaleActivity = whaleActivity,
                projectQualityScore = projectQualityScore,
                tokenomicsScore = tokenomicsScore,
                research = research,
                roadmap = roadmap,
                onChain = onChain,
                unlocks = unlocks
            )
        )
        ProductionIntelligenceResult(
            coin = report,
            pumpDump = pump,
            onChain = onChain,
            providerHealth = listOf(deep.health),
            dataGaps = deep.gaps + report.warnings.filter { it.contains("UNAVAILABLE") || it.contains("REQUIRES") }.distinct()
        )
    }
}
