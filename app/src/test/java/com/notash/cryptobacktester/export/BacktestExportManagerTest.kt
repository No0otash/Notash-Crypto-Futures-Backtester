package com.notash.cryptobacktester.export

import com.notash.cryptobacktester.core.BacktestReport
import com.notash.cryptobacktester.core.Side
import com.notash.cryptobacktester.core.TradeResult
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
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
        val root = Json.parseToJsonElement(BacktestExportManager.json(report)).jsonObject
        val exported = root.getValue("trades").jsonArray.first().jsonObject

        assertEquals("1", exported.getValue("tradeNumber").jsonPrimitive.content)
        assertEquals("SHORT", exported.getValue("side").jsonPrimitive.content)
        assertEquals("1h", exported.getValue("timeframe").jsonPrimitive.content)
        assertEquals("1.5", exported.getValue("positionSize").jsonPrimitive.content)
        assertEquals("5.0", exported.getValue("leverage").jsonPrimitive.content)
        assertEquals("210.0", exported.getValue("stopLoss").jsonPrimitive.content)
        assertEquals("180.0", exported.getValue("takeProfit").jsonPrimitive.content)
        assertEquals("SL", exported.getValue("exitReason").jsonPrimitive.content)
        assertTrue(exported.getValue("slTouched").jsonPrimitive.content.toBoolean())
        assertEquals(trade.pnlPercent, exported.getValue("pnlPercent").jsonPrimitive.content.toDouble(), 0.000001)
        assertEquals("WIN", exported.getValue("status").jsonPrimitive.content)
    }
}