package com.notash.cryptobacktester.intelligence

/**
 * Single orchestration point for ALVEX research/tokenomics/on-chain intelligence.
 * Existing analyzers remain the source of truth; this class only composes their outputs.
 * Missing provider data is preserved as an explicit gap and never replaced with a guess.
 */
data class AlvexIntelligenceInput(
    val profile: ProjectProfile,
    val product: ProductAnalysis,
    val development: DevelopmentActivity,
    val roadmap: List<RoadmapMilestone> = emptyList(),
    val roadmapOfficial: Boolean? = null,
    val team: TeamIntelligence = TeamIntelligence(),
    val investors: InvestorAssessment = InvestorAssessment(),
    val supply: TokenSupply = TokenSupply(),
    val allocations: List<Allocation> = emptyList(),
    val unlocks: List<UnlockEvent> = emptyList(),
    val burns: List<BurnEvent> = emptyList(),
    val emission: EmissionSchedule? = null,
    val holders: HolderSnapshot = HolderSnapshot(),
    val nowDate: String
)

data class AlvexIntelligenceReport(
    val research: ResearchSummary,
    val roadmap: RoadmapAnalysis,
    val teamInvestors: TeamInvestorAssessment,
    val tokenomics: TokenomicsAssessment,
    val unlocks: UnlockAnalysis,
    val onChain: OnChainAnalysis,
    val dataGaps: List<String>,
    val verifiedSources: Int,
    val confidence: Double,
    val overallScore: Double,
    val riskFlags: List<String>
)

class AlvexIntelligenceCoordinator(
    private val researchEngine: ProjectResearchEngine = ProjectResearchEngine(),
    private val roadmapEngine: RoadmapEngine = RoadmapEngine(),
    private val teamInvestorEngine: TeamInvestorEngine = TeamInvestorEngine(),
    private val tokenomicsEngine: TokenomicsEngine = TokenomicsEngine(),
    private val unlockEngine: UnlockEngine = UnlockEngine(),
    private val onChainEngine: OnChainEngine = OnChainEngine()
) {
    fun analyze(input: AlvexIntelligenceInput, timestamp: String): AlvexIntelligenceReport {
        val research = researchEngine.summarize(input.profile, input.product, input.development, timestamp)
        val roadmap = roadmapEngine.analyze(input.roadmap, input.roadmapOfficial)
        val teamInvestors = teamInvestorEngine.assessTeam(input.team, input.investors)
        val tokenomics = tokenomicsEngine.assess(input.supply, input.allocations)
        val unlocks = unlockEngine.analyze(input.unlocks, input.burns, input.emission, input.nowDate)
        val onChain = onChainEngine.analyze(input.holders)

        val gaps = linkedSetOf<String>()
        gaps += research.dataGaps
        if (!roadmap.available) gaps += "ROADMAP"
        if (teamInvestors.unknownData) gaps += "TEAM_OR_INVESTORS"
        if (tokenomics.confidence <= 0.0) gaps += "TOKENOMICS"
        if (input.unlocks.isEmpty() && input.burns.isEmpty() && input.emission == null) gaps += "UNLOCK_BURN_EMISSION"
        if (input.holders.source == null) gaps += "ONCHAIN"

        val sources = (research.sources + input.investors.sources + input.team.sources +
            listOfNotNull(input.supply.source, input.holders.source) +
            input.allocations.mapNotNull { it.source } + input.unlocks.mapNotNull { it.source } +
            input.burns.mapNotNull { it.source } + listOfNotNull(input.emission?.source)).distinct()

        val componentScores = listOf(
            research.overallScore.takeIf { research.confidence > 0 },
            roadmap.progressScore.takeIf { roadmap.available },
            teamInvestors.teamScore.takeIf { !teamInvestors.unknownData },
            tokenomics.score.takeIf { tokenomics.confidence > 0 },
            unlocks.overallScore.takeIf { input.unlocks.isNotEmpty() || input.emission != null || input.burns.isNotEmpty() },
            onChain.overallScore.takeIf { input.holders.source != null }
        ).filterNotNull()

        val overall = componentScores.takeIf { it.isNotEmpty() }?.average() ?: 0.0
        val baseConfidence = listOf(
            research.confidence,
            teamInvestors.confidence,
            tokenomics.confidence,
            onChain.confidence
        ).filter { it > 0.0 }
        val confidence = (if (baseConfidence.isEmpty()) 0.0 else baseConfidence.average() * (1.0 - gaps.size * 0.05))
            .coerceIn(0.0, 100.0)

        val risks = buildList {
            addAll(research.risks)
            addAll(roadmap.riskFlags)
            addAll(teamInvestors.riskFlags)
            addAll(tokenomics.findings)
            addAll(unlocks.risks)
            addAll(onChain.findings)
        }.distinct()

        return AlvexIntelligenceReport(
            research = research,
            roadmap = roadmap,
            teamInvestors = teamInvestors,
            tokenomics = tokenomics,
            unlocks = unlocks,
            onChain = onChain,
            dataGaps = gaps.toList(),
            verifiedSources = sources.count { it.status == SourceStatus.CONFIRMED },
            confidence = confidence,
            overallScore = overall,
            riskFlags = risks
        )
    }
}
