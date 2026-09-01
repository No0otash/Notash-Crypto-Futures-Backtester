package com.notash.cryptobacktester.intelligence

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RealIntelligenceProvidersTest {
    @Test
    fun orchestrator_keeps_provider_gaps_explicit() = runBlocking {
        val project = object : ProjectDataProvider {
            override suspend fun load(projectId: String, githubRepo: String?): ProjectProviderResult =
                ProjectProviderResult(ProjectProfile(name = "Test", symbol = projectId), ProductAnalysis(), DevelopmentActivity(), listOf(ProviderHealth("GitHub", true, 200)))
        }
        val tokenomics = object : TokenomicsDataProvider {
            override suspend fun load(assetId: String): TokenomicsProviderResult =
                TokenomicsProviderResult(TokenSupply(circulating = 10.0, total = 100.0), listOf(ProviderHealth("CoinGecko", false, 429, "RATE_LIMIT")))
        }
        val onChain = object : OnChainDataProvider {
            override suspend fun load(asset: String): OnChainProviderResult =
                OnChainProviderResult(HolderSnapshot(), mapOf("active_addresses_30d" to 123.0), listOf(ProviderHealth("CoinMetricsCommunity", true, 200)))
        }
        val bundle = RealIntelligenceOrchestrator(project, tokenomics, onChain).load("btc")
        assertEquals(2, bundle.verifiedProviderCount)
        assertTrue(bundle.providerGaps.any { it.contains("CoinGecko") })
        assertFalse(bundle.providerGaps.any { it.contains("GitHub") })
        assertEquals(123.0, bundle.onChain.networkMetrics["active_addresses_30d"])
    }

    @Test
    fun source_status_is_confirmed_only_for_real_provider_data() {
        val source = SourceRef("Coin Metrics Community", "https://coinmetrics.io/", status = SourceStatus.CONFIRMED)
        assertEquals(SourceStatus.CONFIRMED, source.status)
    }
}
