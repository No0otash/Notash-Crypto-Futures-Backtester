package com.notash.cryptobacktester.intelligence

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AlvexIntelligenceCoordinatorTest {
    @Test
    fun incompleteProvidersAreReportedAsGaps() {
        val report = AlvexIntelligenceCoordinator().analyze(
            AlvexIntelligenceInput(
                profile = ProjectProfile(),
                product = ProductAnalysis(),
                development = DevelopmentActivity(),
                nowDate = "2026-09-01"
            ),
            "2026-09-01T00:00:00Z"
        )
        assertTrue("WEBSITE" in report.dataGaps)
        assertTrue("ROADMAP" in report.dataGaps)
        assertTrue("ONCHAIN" in report.dataGaps)
        assertEquals(0, report.verifiedSources)
        assertEquals(0.0, report.overallScore)
    }

    @Test
    fun confirmedInputsProduceAReportWithoutFillingMissingData() {
        val source = SourceRef("official", "https://example.com", "2026-09-01T00:00:00Z", SourceStatus.CONFIRMED)
        val report = AlvexIntelligenceCoordinator().analyze(
            AlvexIntelligenceInput(
                profile = ProjectProfile(name = "Example", symbol = "EX", website = "https://example.com", source = source),
                product = ProductAnalysis(utility = "Payments", score = 80.0, confidence = 90.0, sources = listOf(source)),
                development = DevelopmentActivity(latestCommit = "abc", score = 75.0, sources = listOf(source)),
                roadmap = listOf(RoadmapMilestone("v1", status = MilestoneStatus.COMPLETED, source = source)),
                roadmapOfficial = true,
                team = TeamIntelligence(members = listOf(TeamMember("Founder", source = source)), anonymous = false, transparencyScore = 80.0, teamRisk = 20.0, confidence = 80.0, sources = listOf(source)),
                investors = InvestorAssessment(qualityScore = 75.0, fundingStrength = 70.0, reputationScore = 80.0, confidence = 80.0, sources = listOf(source)),
                supply = TokenSupply(circulating = 60.0, total = 100.0, max = 100.0, source = source),
                allocations = listOf(Allocation("community", 80.0, confidence = 90.0, source = source)),
                holders = HolderSnapshot(top10Percent = 25.0, holderCount = 1000, holderGrowthTrend = "UP", source = source),
                nowDate = "2026-09-01"
            ),
            "2026-09-01T00:00:00Z"
        )
        assertTrue(report.verifiedSources >= 4)
        assertTrue(report.confidence > 0.0)
        assertTrue(report.overallScore > 0.0)
        assertTrue("UNLOCK_BURN_EMISSION" in report.dataGaps)
    }
}
