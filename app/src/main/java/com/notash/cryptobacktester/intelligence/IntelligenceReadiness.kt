package com.notash.cryptobacktester.intelligence

data class IntelligenceReadiness(val verifiedSources: Int, val gaps: List<String>, val ready: Boolean)

object IntelligenceReadinessEvaluator {
    fun evaluate(research: ResearchSummary, roadmap: RoadmapAnalysis, tokenomics: TokenomicsAssessment, onChain: OnChainAnalysis): IntelligenceReadiness {
        val gaps = mutableListOf<String>()
        gaps += research.dataGaps
        if (!roadmap.available) gaps += "ROADMAP"
        if (tokenomics.confidence <= 0.0) gaps += "TOKENOMICS"
        if (onChain.confidence <= 0.0) gaps += "ONCHAIN"
        val sources = (research.sources + listOfNotNull(tokenomics.supply.source, onChain.snapshot.source)).distinct()
        return IntelligenceReadiness(sources.count { it.status == SourceStatus.CONFIRMED }, gaps.distinct(), gaps.isEmpty())
    }
}
