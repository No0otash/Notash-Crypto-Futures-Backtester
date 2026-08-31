package com.notash.cryptobacktester.intelligence

class ProductResearchAnalyzer {
    fun analyze(
        realWorldUseCase: String = "Unknown",
        targetUsers: String = "Unknown",
        utility: String = "Unknown",
        tokenNecessary: Boolean? = null,
        tokenUseCases: List<String> = emptyList(),
        maturity: String = "Unknown",
        mainnet: Boolean? = null,
        liveProduct: Boolean? = null,
        strengths: List<String> = emptyList(),
        weaknesses: List<String> = emptyList(),
        risks: List<String> = emptyList(),
        sources: List<SourceRef> = emptyList()
    ): ProductAnalysis {
        var score = 0.0
        var evidence = 0
        if (realWorldUseCase != "Unknown") { score += 20; evidence++ }
        if (targetUsers != "Unknown") { score += 10; evidence++ }
        if (utility != "Unknown") { score += 20; evidence++ }
        if (tokenNecessary == true) score += 10
        if (tokenUseCases.isNotEmpty()) { score += 10; evidence++ }
        if (liveProduct == true) score += 15 else if (liveProduct == false) score += 5
        if (mainnet == true) score += 10
        if (maturity != "Unknown") { score += 5; evidence++ }
        val confidence = ((evidence.toDouble() / 6.0) * 100.0).coerceIn(0.0,100.0)
        val finalScore = (score - weaknesses.size * 3.0 - risks.size * 4.0).coerceIn(0.0,100.0)
        return ProductAnalysis(realWorldUseCase, targetUsers, utility, tokenNecessary, tokenUseCases, maturity, mainnet, liveProduct, strengths, weaknesses, risks, finalScore, confidence, sources)
    }
}

class TeamInvestorEngine {
    fun assessTeam(team: TeamIntelligence, investors: InvestorAssessment): TeamInvestorAssessment {
        val anonymous = team.anonymous
        val unknown = team.members.isEmpty() || investors.investors.any { it.status == SourceStatus.UNKNOWN }
        val reputation = investors.reputationScore.coerceIn(0.0,100.0)
        val riskFlags = buildList {
            if (anonymous) add("ANONYMOUS_TEAM")
            if (unknown) add("UNKNOWN_TEAM_OR_INVESTOR_DATA")
            if (investors.investors.isEmpty()) add("NO_VERIFIED_INVESTORS")
            if (team.transparencyScore < 50) add("LOW_TEAM_TRANSPARENCY")
        }
        val confidence = ((team.confidence + investors.confidence) / 2.0).coerceIn(0.0,100.0)
        return TeamInvestorAssessment(
            teamScore = ((team.transparencyScore * .6) + ((100.0 - team.teamRisk) * .4)).coerceIn(0.0,100.0),
            transparencyScore = team.transparencyScore.coerceIn(0.0,100.0),
            investorQuality = investors.qualityScore.coerceIn(0.0,100.0),
            fundingStrength = investors.fundingStrength.coerceIn(0.0,100.0),
            reputation = reputation,
            riskFlags = riskFlags,
            anonymousTeam = anonymous,
            unknownData = unknown,
            confidence = confidence
        )
    }
}
