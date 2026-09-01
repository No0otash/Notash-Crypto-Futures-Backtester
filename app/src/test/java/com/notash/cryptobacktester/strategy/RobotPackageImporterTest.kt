package com.notash.cryptobacktester.strategy

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RobotPackageImporterTest {
    @Test fun importsAndValidatesAiRobotPackage() {
        val json = """
          {"schemaVersion":1,"id":"ai-rsi-1","name":"AI RSI","version":"1.0.0","description":"test",
           "parameters":{"stopLossPercent":1.5,"takeProfitPercent":3.0},
           "rules":[{"field":"close","operator":">","value":100.0,"action":"LONG"}]}
        """.trimIndent()
        val robot = RobotPackageImporter().importJson(json).getOrThrow()
        assertEquals("AI RSI", robot.name)
        assertTrue(RobotPackageValidator.validate(robot).valid)
        assertEquals(SideName.LONG, robot.rules.first().action)
    }

    @Test fun rejectsUnknownRuleField() {
        val json = """{"schemaVersion":1,"id":"x","name":"x","rules":[{"field":"rsi","operator":">","value":50,"action":"LONG"}]}"""
        assertTrue(RobotPackageImporter().importJson(json).isFailure)
    }

    private object SideName { const val LONG = "LONG" }
}
