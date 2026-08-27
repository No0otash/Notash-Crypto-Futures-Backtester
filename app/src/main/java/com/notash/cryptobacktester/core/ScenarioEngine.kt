package com.notash.cryptobacktester.core

object ScenarioEngine {
    enum class Outlook { BULL, BASE, BEAR }

    data class Scenario(
        val outlook: Outlook,
        val probabilityBand: String,
        val triggers: List<String>
    )

    fun build(candidate: GrowthCandidate): List<Scenario> {
        val score = candidate.score.total
        val risk = candidate.risks.size
        return listOf(
            Scenario(Outlook.BULL, band(score + if (risk == 0) 10 else 0), listOf("Demand and sector strength persist", "Supply pressure remains controlled")),
            Scenario(Outlook.BASE, band(score), listOf("Mixed fundamentals and market conditions persist")),
            Scenario(Outlook.BEAR, band(100 - score + risk * 8), listOf("Support fails", "Demand weakens", "Unlock/supply pressure increases"))
        )
    }

    private fun band(value: Int): String = when {
        value >= 75 -> "High"
        value >= 50 -> "Medium"
        else -> "Low"
    }
}
