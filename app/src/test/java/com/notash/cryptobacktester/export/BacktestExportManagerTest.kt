package com.notash.cryptobacktester.export

import com.notash.cryptobacktester.core.BacktestReport
import com.notash.cryptobacktester.core.Side
import com.notash.cryptobacktester.core.TradeResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BacktestExportManagerTest {
    private val trade = TradeResult(
        side = Side.SHORT,
        entryPrice = 200.0,
        exitPrice = 190.0,
        quantity = 1.5,
        grossPnl = 15.0,
        fees = 1.25,
        funding = 0.25,
        netPnl = 13.5,
        entryTime = 1000L,
        exitTime = 2000L,
        stopLoss = 210.0,
        takeProfit = 180.0,
        exitReason = "SL",
        timeframe = "1h",
        leverage = 5.0,
        slTouched = true
    )

    private val report = BacktestReport(
        initialBalance = 1000.0,
        finalBalance = 1013.5,
        netPnl = 13.5,
        roiPercent = 1.35,
        maxDrawdownPercent = 0.5,
        winRatePercent = 100.0,
        profitFactor = 12.0,
        totalFees = 1.25,
        totalFunding = 0.25,
        trades = listOf(trade),
        equityCurve = listOf(1000.0, 1013.5),
        timeframe = "1h",
        leverage = 5.0
    )

    @Test
    fun csv_contains_all_trade_report_fields_and_values() {
        val lines = BacktestExportManager.csv(report).trim().lines()
        val header = lines.first().split(',')
        val values = lines[1].split(',')

        val expectedHeaders = listOf(
            "tradeNumber", "side", "entryPrice", "exitPrice", "timeframe", "entryTime", "exitTime",
            "positionSize", "leverage", "stopLoss", "takeProfit", "exitReason", "slTouched",
            "grossPnl", "netPnl", "pnlPercent", "fees", "funding", "status"
        )
        assertEquals(expectedHeaders, header)
        assertEquals("1", values[0])
        assertEquals("SHORT", values[1])
        assertEquals("1h", values[4])
        assertEquals("5.0", values[8])
        assertEquals("true", values[12])
        assertEquals("WIN", values[18])
        assertEquals(trade.pnlPercent.toString(), values[15])
    }

    @Test
    fun json_contains_all_trade_report_fields_and_values() {
        val json = BacktestExportManager.json(report)

        assertTrue(json.contains("\"tradeNumber\": 1"))
        assertTrue(json.contains("\"side\": \"SHORT\""))
        assertTrue(json.contains("\"entryPrice\": 200.0"))
        assertTrue(json.contains("\"exitPrice\": 190.0"))
        assertTrue(json.contains("\"timeframe\": \"1h\""))
        assertTrue(json.contains("\"entryTime\": 1000"))
        assertTrue(json.contains("\"exitTime\": 2000"))
        assertTrue(json.contains("\"positionSize\": 1.5"))
        assertTrue(json.contains("\"leverage\": 5.0"))
        assertTrue(json.contains("\"stopLoss\": 210.0"))
        assertTrue(json.contains("\"takeProfit\": 180.0"))
        assertTrue(json.contains("\"exitReason\": \"SL\""))
        assertTrue(json.contains("\"slTouched\": true"))
        assertTrue(json.contains("\"grossPnl\": 15.0"))
        assertTrue(json.contains("\"netPnl\": 13.5"))
        assertTrue(json.contains("\"pnlPercent\": ${trade.pnlPercent}"))
        assertTrue(json.contains("\"fees\": 1.25"))
        assertTrue(json.contains("\"funding\": 0.25"))
        assertTrue(json.contains("\"status\": \"WIN\""))
    }
}
