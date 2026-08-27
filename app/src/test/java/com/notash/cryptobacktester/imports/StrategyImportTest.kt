package com.notash.cryptobacktester.imports

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StrategyImportTest {
    @Test
    fun `json import preserves strategy and backtest parameters`() {
        val imported = StrategyImportParser.parse(
            """
            {
              "id":"momentum-01",
              "name":"Momentum Bot",
              "version":"1.2",
              "symbol":"BTCUSDT",
              "timeframe":"15m",
              "entryRules":["LWMA20 > LWMA50"],
              "exitRules":["ATR stop"],
              "riskPercent":2.0,
              "leverage":5,
              "stopAtr":2.0,
              "takeProfitAtr":4.0,
              "useFunding":true
            }
            """.trimIndent(),
            "momentum.json"
        )

        assertEquals("momentum-01", imported.packageData.id)
        assertEquals("Momentum Bot", imported.packageData.name)
        assertEquals("BTCUSDT", imported.packageData.symbol)
        assertEquals("15m", imported.packageData.timeframe)
        assertEquals(2.0, imported.packageData.riskPercent, 0.0001)
        assertEquals(5.0, imported.config.leverage, 0.0001)
        assertEquals(2.0, imported.config.stopAtr, 0.0001)
        assertEquals(4.0, imported.config.takeProfitAtr, 0.0001)
        assertTrue(imported.config.useFunding)
    }

    @Test
    fun `key value import registers active strategy`() {
        val imported = StrategyImportParser.parse(
            """
            id=scalper-01
            name=Scalper Bot
            version=1.0
            symbol=ETHUSDT
            timeframe=5m
            risk=1.5
            leverage=3
            """.trimIndent(),
            "scalper.txt"
        )

        ImportedStrategyStore.register(imported)

        assertNotNull(ImportedStrategyStore.get("scalper-01"))
        assertEquals("scalper-01", ImportedStrategyStore.activeId())
        assertEquals("Scalper Bot", ImportedStrategyStore.get("scalper-01")!!.packageData.name)
    }
}
