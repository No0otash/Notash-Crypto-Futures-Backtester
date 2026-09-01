package com.notash.cryptobacktester.intelligence

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/** Coordinates real providers without allowing one failing provider to take down the intelligence pipeline. */
class RealIntelligenceOrchestrator(
    private val projectProvider: ProjectDataProvider,
    private val tokenomicsProvider: TokenomicsDataProvider,
    private val onChainProvider: OnChainDataProvider,
    private val defiLlamaProvider: DefiLlamaProjectProvider? = null
) {
    suspend fun load(
        projectId: String,
        githubRepo: String? = null,
        tokenAssetId: String = projectId,
        onChainAsset: String = projectId,
        defiLlamaSlug: String? = null
    ): RealIntelligenceBundle = coroutineScope {
        val project = async { runCatching { projectProvider.load(projectId, githubRepo) }.getOrElse { ProjectProviderResult(ProjectProfile(symbol = projectId), ProductAnalysis(), DevelopmentActivity(), listOf(ProviderHealth("Project", false, error = it.message))) } }
        val tokenomics = async { runCatching { tokenomicsProvider.load(tokenAssetId) }.getOrElse { TokenomicsProviderResult(TokenSupply(), listOf(ProviderHealth("Tokenomics", false, error = it.message))) } }
        val onChain = async { runCatching { onChainProvider.load(onChainAsset) }.getOrElse { OnChainProviderResult(HolderSnapshot(), emptyMap(), listOf(ProviderHealth("OnChain", false, error = it.message))) } }
        val defi = async {
            if (defiLlamaProvider != null && !defiLlamaSlug.isNullOrBlank()) {
                runCatching { defiLlamaProvider.load(defiLlamaSlug) }.getOrElse { ProductAnalysis() to ProviderHealth("DefiLlama", false, error = it.message) }
            } else ProductAnalysis() to ProviderHealth("DefiLlama", false, error = "DEFILLAMA_SLUG_NOT_CONFIGURED")
        }
        val results = awaitAll(project, tokenomics, onChain, defi)
        @Suppress("UNCHECKED_CAST")
        RealIntelligenceBundle(
            project = results[0] as ProjectProviderResult,
            tokenomics = results[1] as TokenomicsProviderResult,
            onChain = results[2] as OnChainProviderResult,
            defiLlamaProduct = (results[3] as Pair<ProductAnalysis, ProviderHealth>).first,
            health = listOf(
                *(results[0] as ProjectProviderResult).health.toTypedArray(),
                *(results[1] as TokenomicsProviderResult).health.toTypedArray(),
                *(results[2] as OnChainProviderResult).health.toTypedArray(),
                (results[3] as Pair<ProductAnalysis, ProviderHealth>).second
            )
        )
    }
}

data class RealIntelligenceBundle(
    val project: ProjectProviderResult,
    val tokenomics: TokenomicsProviderResult,
    val onChain: OnChainProviderResult,
    val defiLlamaProduct: ProductAnalysis,
    val health: List<ProviderHealth>
) {
    val verifiedProviderCount: Int get() = health.count { it.ok }
    val providerGaps: List<String> get() = health.filterNot { it.ok }.map { it.provider + ":" + (it.error ?: "UNAVAILABLE") }
}
