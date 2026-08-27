package com.notash.cryptobacktester.core

object GrowthScoringEngine {
    fun score(
        intelligence: TokenIntelligence,
        market: MarketSnapshot,
        narrativeScore: Int,
        onChainScore: Int,
        earlySignals: List<String>
    ): GrowthCandidate {
        val supply = intelligence.tokenSupply
        val unlockRisk = unlockRisk(intelligence, market.timestamp)
        val burn = burnScore(intelligence)
        val confidence = intelligence.dataConfidence.coerceIn(0, 100)
        val product = if (intelligence.product.isNullOrBlank()) 0 else if (intelligence.utility.isNullOrBlank()) 45 else 90
        val team = if (intelligence.founders.isEmpty()) 0 else 80
        val investors = if (intelligence.investors.isEmpty()) 0 else 75
        val roadmap = when {
            intelligence.completedMilestones.isNotEmpty() && intelligence.delayedMilestones.isEmpty() -> 90
            intelligence.completedMilestones.isNotEmpty() -> 65
            else -> 0
        }
        val tokenomics = tokenomicsScore(supply)
        val score = GrowthScore(
            productUtility = product,
            team = team,
            investors = investors,
            roadmap = roadmap,
            tokenomics = tokenomics,
            unlockRisk = unlockRisk,
            burnMechanism = burn,
            onChain = onChainScore.coerceIn(0, 100),
            marketTrend = ((market.btcTrendScore + market.sectorScore + market.momentumScore) / 3).coerceIn(0, 100),
            liquidity = liquidityScore(market),
            narrative = narrativeScore.coerceIn(0, 100),
            dataConfidence = confidence
        )
        val risks = buildList {
            if (confidence < 60) add("Data Confidence is low; missing data is not treated as positive evidence")
            if (intelligence.highRisk) add("High Risk: project or token data is incomplete/unverified")
            if (unlockRisk < 50) add("Material near-term unlock/vesting pressure")
            if ((supply.maxSupply ?: 0.0) > 0 && (supply.circulatingSupply ?: 0.0) / (supply.maxSupply ?: 1.0) < .35) add("Low circulating share may create future supply pressure")
            if ((intelligence.topHolderPercent ?: 0.0) > 30) add("Top holders have high supply concentration")
            if ((market.liquidityUsd ?: 0.0) < 1_000_000) add("Low liquidity increases execution and manipulation risk")
        }
        val reasons = buildList {
            if (earlySignals.isNotEmpty()) addAll(earlySignals.take(5))
            if (market.momentumScore >= 70) add("Momentum is strengthening")
            if (market.sectorScore >= 70) add("Sector strength is positive")
            if ((intelligence.holderCount ?: 0) > 0) add("Holder data is available")
            if (burn >= 70) add("Burn mechanism has measurable support")
        }
        return GrowthCandidate(
            symbol = intelligence.symbol,
            score = score,
            reasons = reasons,
            risks = risks,
            bullScenario = "Bull: sustained demand, improving fundamentals and healthy market structure",
            baseScenario = "Base: mixed fundamentals with moderate market support",
            bearScenario = "Bear: demand weakens, supply unlocks accelerate selling, or market support fails",
            earlySignals = earlySignals
        )
    }

    private fun tokenomicsScore(s: TokenSupply): Int {
        val max = s.maxSupply ?: return 0
        val circ = s.circulatingSupply ?: return 0
        if (max <= 0 || circ < 0) return 0
        return ((circ / max).coerceIn(0.0, 1.0) * 100).toInt()
    }

    private fun liquidityScore(m: MarketSnapshot): Int {
        val l = m.liquidityUsd ?: return 0
        return when {
            l >= 100_000_000 -> 95
            l >= 20_000_000 -> 85
            l >= 5_000_000 -> 75
            l >= 1_000_000 -> 60
            else -> 25
        }
    }

    private fun burnScore(i: TokenIntelligence): Int {
        val burn = i.burnPercentAnnual ?: return 0
        val emission = i.emissionPercentAnnual ?: 0.0
        val net = emission - burn
        return when {
            burn <= 0.0 -> 0
            net <= 0.0 -> 95
            net <= 2.0 -> 80
            net <= 5.0 -> 65
            else -> 40
        }
    }

    private fun unlockRisk(i: TokenIntelligence, nowMillis: Long): Int {
        // Missing unlock data is unknown, never positive evidence.
        if (i.unlocks.isEmpty()) return 0
        val horizon = nowMillis + 90L * 24L * 60L * 60L * 1000L
        val upcoming = i.unlocks
            .filter { it.timestamp in (nowMillis + 1)..horizon }
            .sumOf { it.percentOfSupply.coerceAtLeast(0.0) }
        return when {
            upcoming <= 2 -> 95
            upcoming <= 5 -> 80
            upcoming <= 10 -> 60
            upcoming <= 20 -> 40
            else -> 20
        }
    }
}
