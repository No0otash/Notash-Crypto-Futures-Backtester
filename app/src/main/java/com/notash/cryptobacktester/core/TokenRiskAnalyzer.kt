package com.notash.cryptobacktester.core

object TokenRiskAnalyzer {
    data class Result(
        val dataConfidence: Int,
        val highRisk: Boolean,
        val risks: List<String>
    )

    fun analyze(i: TokenIntelligence): Result {
        var confidence = 100
        val risks = buildList {
            if (i.project.isNullOrBlank()) { confidence -= 15; add("Project identity data unavailable") }
            if (i.product.isNullOrBlank()) { confidence -= 15; add("No verified product information") }
            if (i.utility.isNullOrBlank()) { confidence -= 10; add("Token utility is unverified") }
            if (i.founders.isEmpty()) { confidence -= 10; add("Founder/team information unavailable") }
            if (i.investors.isEmpty()) { confidence -= 5; add("Investor information unavailable") }
            if (i.tokenSupply.maxSupply == null || i.tokenSupply.circulatingSupply == null) { confidence -= 15; add("Supply data incomplete") }
            if (i.unlocks.isEmpty()) { confidence -= 10; add("Unlock schedule unavailable") }
            if (i.holderCount == null || i.topHolderPercent == null) { confidence -= 10; add("Holder concentration data unavailable") }
            if (i.whaleCount == null) { confidence -= 5; add("Whale data unavailable") }
            if (i.dataConfidence < 60) add("Source confidence supplied by upstream data is low")
        }
        confidence = confidence.coerceIn(0, 100)
        return Result(confidence, confidence < 50 || i.highRisk, risks)
    }
}
