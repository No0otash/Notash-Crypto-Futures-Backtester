package com.notash.cryptobacktester.core

object SignalQualityGate {
    data class Decision(val eligible: Boolean, val reasons: List<String>)

    fun evaluate(candidate: GrowthCandidate): Decision {
        val reasons = buildList {
            if (candidate.score.dataConfidence < 50) add("Data Confidence below 50")
            if (candidate.risks.any { it.startsWith("High Risk") }) add("High-risk classification")
            if (candidate.earlySignals.isEmpty()) add("No verified early-growth signals")
            if (candidate.score.total < 60) add("Growth score below threshold")
        }
        return Decision(reasons.isEmpty(), reasons)
    }
}
