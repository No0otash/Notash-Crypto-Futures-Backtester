package com.notash.cryptobacktester.intelligence

import com.notash.cryptobacktester.BuildConfig
import com.notash.cryptobacktester.core.Candle
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.time.LocalDate

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
    private val onChainProvider: DeepOnChainProvider = DeepOnChainProviderRouter(),
    private val moralisProvider: DeepOnChainProvider = MoralisEvmHolderProvider(BuildConfig.MORALIS_API_KEY.takeIf { it.isNotBlank() }),
    private val tokenomistProvider: TokenomistUnlockProvider = TokenomistUnlockProvider(BuildConfig.TOKENOMIST_API_KEY.takeIf { it.isNotBlank() })
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
        val provider = if (chain.lowercase() in setOf("ethereum", "eth", "polygon", "matic", "bsc", "bnb", "arbitrum", "optimism", "base", "avalanche")) moralisProvider else onChainProvider
        val onChainDeferred = async { provider.load(chain, contractOrMint) }
        val unlockDeferred = async { tokenomistProvider.load(contractOrMint) }
        val pump = marketEngine.analyzePriceAndVolume(candles)
        val deep = onChainDeferred.await()
        val (premiumUnlocks, unlockHealth) = unlockDeferred.await()
        val onChain = if (deep.snapshot.source != null) OnChainEngine().analyze(deep.snapshot) else null
        val resolvedUnlocks = unlocks ?: premiumUnlocks.takeIf { it.isNotEmpty() }?.let {
            UnlockEngine().analyze(it, emptyList(), null, LocalDate.now().toString())
        }
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
                unlocks = resolvedUnlocks
            )
        )
        val health = listOf(deep.health, unlockHealth)
        val gaps = (deep.gaps +
            if (!unlockHealth.ok) listOf("TOKENOMICS_UNLOCK_PROVIDER_UNAVAILABLE") else emptyList() +
            report.warnings.filter { it.contains("UNAVAILABLE") || it.contains("REQUIRES") }).distinct()
        ProductionIntelligenceResult(
            coin = report,
            pumpDump = pump,
            onChain = onChain,
            providerHealth = health,
            dataGaps = gaps
        )
    }
}
