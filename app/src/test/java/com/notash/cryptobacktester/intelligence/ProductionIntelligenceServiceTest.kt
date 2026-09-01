package com.notash.cryptobacktester.intelligence

import com.notash.cryptobacktester.core.Candle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ProductionIntelligenceServiceTest {
    private class FakeOnChainProvider : DeepOnChainProvider {
        override suspend fun load(chain: String, contractOrMint: String) = DeepOnChainResult(
            HolderSnapshot(
                top10Percent = 72.0,
                top20Percent = 81.0,
                largestHolderPercent = 24.0,
                holderGrowthTrend = "GROWING",
                source = SourceRef("test-onchain", status = SourceStatus.CONFIRMED)
            ),
            ProviderHealth("test-onchain", true, 200),
            emptyList()
        )
    }

    @Test
    fun deepOnChainDataReachesCoinIntelligence() = runTest {
        val candles = (1L..25L).map { i ->
            Candle(i, 100.0 + i, 101.0 + i, 99.0 + i, 100.5 + i, 1000.0 + i)
        }
        val service = ProductionIntelligenceService(onChainProvider = FakeOnChainProvider())
        val result = service.analyze(
            snapshot = MemeCoinSnapshot("TEST", "TEST/USDT", 1_000_000.0, 10_000_000.0, ageDays = 100, contractVerified = true),
            candles = candles,
            chain = "solana",
            contractOrMint = "mint"
        )
        assertNotNull(result.onChain)
        assertEquals(72.0, result.onChain!!.concentrationRisk, 0.001)
        assertNotNull(result.coin.components.firstOrNull { it.name == "ONCHAIN" })
        assertEquals(200, result.providerHealth.first().statusCode)
    }

    @Test
    fun missingProviderIsReportedWithoutFabricatingValues() = runTest {
        val missing = object : DeepOnChainProvider {
            override suspend fun load(chain: String, contractOrMint: String) = DeepOnChainResult(
                HolderSnapshot(), ProviderHealth("missing", false, error = "UNAVAILABLE"), listOf("ONCHAIN_UNAVAILABLE")
            )
        }
        val service = ProductionIntelligenceService(onChainProvider = missing)
        val candles = (1L..3L).map { i -> Candle(i, 100.0, 101.0, 99.0, 100.0, 1000.0) }
        val result = service.analyze(MemeCoinSnapshot("TEST", "TEST/USDT", 1.0, 1.0), candles, "ethereum", "0x0")
        assertEquals(null, result.onChain)
        assert(result.dataGaps.contains("ONCHAIN_UNAVAILABLE"))
    }
}
