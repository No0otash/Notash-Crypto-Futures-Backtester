package com.notash.cryptobacktester.core

import org.junit.Test
import org.junit.Assert.assertEquals

class ScenarioEngineTest {
    @Test
    fun generatesThreeDistinctScenarios() {
        val candidate = GrowthCandidate(
            symbol = "TEST",
            score = GrowthScore(90, 80, 70, 90, 80, 80, 70, 80, 90, 80, 90, 85)
        )
        val scenarios = ScenarioEngine.build(candidate)
        assertEquals(3, scenarios.size)
        assertEquals(ScenarioEngine.Outlook.BULL, scenarios[0].outlook)
        assertEquals(ScenarioEngine.Outlook.BASE, scenarios[1].outlook)
        assertEquals(ScenarioEngine.Outlook.BEAR, scenarios[2].outlook)
    }
}
