package com.notash.cryptobacktester.strategy

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StrategyManagerCoreTest {
    private fun strategy(id: String, version: String) = ManagedStrategy(
        id = id,
        name = "Strategy $id",
        version = version,
        type = StrategyType.ROBOT,
        entryRules = "close > open",
        exitRules = "SL/TP",
        riskRules = "1%",
        timeframe = "15m",
        tradeAmount = 100.0,
        leverage = 3.0,
        createdAt = 100L,
        source = StrategySource.MANUAL
    )

    @Test fun saveAsNewVersion_keepsPreviousVersion() {
        val core = StrategyManagerCore(listOf(strategy("s1", "1.0.0")), "s1@1.0.0")
        val result = core.saveAsNewVersion(strategy("s1", "1.1.0"))
        assertEquals(listOf("1.0.0", "1.1.0"), result.history.filter { it.id == "s1" }.map { it.version })
        assertEquals("s1@1.1.0", result.activeIdVersion)
    }

    @Test fun activate_changesActiveWithoutDeletingOtherStrategies() {
        val core = StrategyManagerCore(listOf(strategy("s1", "1.0.0"), strategy("s2", "1.0.0")), "s1@1.0.0")
        val result = core.activate("s2", "1.0.0")
        assertEquals(2, result.history.size)
        assertEquals("s2@1.0.0", result.activeIdVersion)
    }

    @Test fun delete_requiresConfirmation() {
        val core = StrategyManagerCore(listOf(strategy("s1", "1.0.0")), "s1@1.0.0")
        assertFalse(core.canDelete("s1", "1.0.0", confirmed = false))
        val unchanged = core.delete("s1", "1.0.0", confirmed = false)
        assertEquals(1, unchanged.history.size)
        assertTrue(core.canDelete("s1", "1.0.0", confirmed = true))
    }
}
