package com.notash.cryptobacktester.robot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AlvexRobotImporterTest {
    @Test
    fun importsRobotAndPreservesEditableParameters() {
        val robot = AlvexRobotImporter.fromJson("""
            {"schemaVersion":1,"id":"ai-1","name":"AI Momentum","version":"1.2.0",
             "parameters":{"leverage":7,"riskPercent":2,"stopLossPercent":1.5,"takeProfitPercent":3},
             "rules":{"longWhenCloseAboveOpen":true,"shortWhenCloseBelowOpen":false}}
        """.trimIndent())
        assertEquals("AI Momentum", robot.name)
        assertEquals(7.0, robot.parameters.leverage)
        assertEquals(2.0, robot.parameters.riskPercent)
        assertEquals(false, robot.rules.shortWhenCloseBelowOpen)
    }

    @Test
    fun rejectsInvalidParameters() {
        assertFailsWith<IllegalArgumentException> {
            AlvexRobotImporter.fromJson("""{"id":"bad","name":"Bad","parameters":{"leverage":0}}""")
        }
    }
}
