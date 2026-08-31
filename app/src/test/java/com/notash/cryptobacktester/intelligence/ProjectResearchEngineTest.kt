package com.notash.cryptobacktester.intelligence

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProjectResearchEngineTest {
    @Test
    fun missingResearchDataIsExplicit() {
        val summary = ProjectResearchEngine().summarize(ProjectProfile(), ProductAnalysis(), DevelopmentActivity(), "2026-08-30T00:00:00Z")
        assertTrue("WEBSITE" in summary.dataGaps)
        assertTrue("GITHUB" in summary.dataGaps)
        assertEquals(0.0, summary.overallScore)
    }

    @Test
    fun allocationTotalIsValidatedAndLargeUnlockFlagged() {
        val assessment = TokenomicsEngine().assess(
            TokenSupply(circulating = 10.0, total = 100.0, max = 100.0),
            listOf(Allocation("team", 20.0, unlock = "2027", confidence = 90.0), Allocation("community", 70.0, confidence = 90.0))
        )
        assertEquals(90.0, assessment.allocationTotal)
        assertTrue(assessment.lowCirculation)
        assertTrue(assessment.largeFutureUnlock)
        assertTrue("ALLOCATION_TOTAL_NOT_100_PERCENT" in assessment.findings)
    }
}
