package com.notash.cryptobacktester.ui

import com.notash.cryptobacktester.core.BacktestReport
import com.notash.cryptobacktester.core.Side
import com.notash.cryptobacktester.core.TradeResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TradeByTradeReportTest {
    @Test
    fun trade_report_contract_contains_every_required_trade_detail() {
        val trade = TradeResult(
            side = Side.LONG,
            entryPrice = 100.0,
            exitPrice = 110.0,
            quantity = 2.0,
            grossPnl = 20.0,
            fees = 1.0,
            funding = 0.25,
            netPnl = 18.75,
            entryTime = 1_000L,
            exitTime = 2_000L,
            stopLoss = 95.0,
            takeProfit = 110.0,
            exitReason = "TP",
            timeframe = "15m",
            leverage = 10.0,
            slTouched = false
        )
        val report = BacktestReport(
            initialBalance = 1_000.0,
            finalBalance = 1_018.75,
            netPnl = 18.75,
            roiPercent = 1.875,
            maxDrawdownPercent = 0.5,
            winRatePercent = 100.0,
            profitFactor = 20.0,
            totalFees = 1.0,
            totalFunding = 0.25,
            trades = listOf(trade),
            equityCurve = listOf(1_000.0, 1_018.75),
            timeframe = "15m",
            leverage = 10.0
        )

        assertEquals(1, report.trades.size)
        assertEquals(Side.LONG, trade.side)
        assertEquals(100.0, trade.entryPrice, 0.0)
        assertEquals(110.0, trade.exitPrice, 0.0)
        assertEquals("15m", trade.timeframe)
        assertEquals(95.0, trade.stopLoss, 0.0)
        assertEquals(110.0, trade.takeProfit, 0.0)
        assertEquals("TP", trade.exitReason)
        assertTrue(!trade.slTouched)
        assertEquals(18.75, trade.netPnl, 0.0)
        assertEquals(18.75 / (100.0 * 2.0) * 100.0, trade.pnlPercent, 0.000001)
    }
}
