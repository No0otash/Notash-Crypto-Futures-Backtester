package com.notash.cryptobacktester.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AiHubModelsTest {
    @Test
    fun chatCanBeIndependentOfAppContext() {
        val request = AiHubRequest(question = "What is funding rate?")
        assertFalse(request.includeMarketContext)
        assertFalse(request.includeStrategyContext)
        assertFalse(request.includeTradeContext)
        assertFalse(request.includeTokenContext)
    }

    @Test
    fun allResearchModesAreAvailable() {
        assertTrue(AiHubMode.values().contains(AiHubMode.LEARN))
        assertTrue(AiHubMode.values().contains(AiHubMode.COIN_RESEARCH))
        assertTrue(AiHubMode.values().contains(AiHubMode.MARKET_RESEARCH))
    }
}
