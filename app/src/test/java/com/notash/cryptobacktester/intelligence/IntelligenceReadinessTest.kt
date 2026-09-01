package com.notash.cryptobacktester.intelligence

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IntelligenceReadinessTest {
    @Test
    fun missingProvidersKeepReadinessFalse() {
        val report = ProjectResearchEngine().summarize(ProjectProfile(), ProductAnalysis(), DevelopmentActivity(), "2026-09-01T00:00:00Z")
        val readiness = IntelligenceReadinessEvaluator.evaluate(report, RoadmapEngine().analyze(emptyList()), TokenomicsEngine().assess(TokenSupply(), emptyList()), OnChainEngine().analyze(HolderSnapshot()))
        assertFalse(readiness.ready)
        assertTrue(readiness.gaps.isNotEmpty())
    }
}
