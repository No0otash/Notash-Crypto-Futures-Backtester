package com.notash.cryptobacktester.core

import org.junit.Test
import org.junit.Assert.assertTrue

class TokenRiskAnalyzerTest {
    @Test
    fun missingDataNeverCreatesPositiveConfidence() {
        val result = TokenRiskAnalyzer.analyze(TokenIntelligence(symbol = "UNKNOWN", dataConfidence = 20))
        assertTrue(result.dataConfidence < 50)
        assertTrue(result.highRisk)
        assertTrue(result.risks.isNotEmpty())
    }
}
