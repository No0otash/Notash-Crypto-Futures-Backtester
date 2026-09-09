package com.notash.cryptobacktester.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TradeResultTest {
    @Test
    fun tradeResult_exposes_complete_trade_report_fields() {
        val trade = TradeResult(
            side = Side.LONG,
            entryPrice = 100.0,
            exitPrice = 110.0,
            quantity = 2.0,
            grossPnl = 20.0,
            fees = 1.0,
            funding = 0.5,
            netPnl = 18.5,
            entryTime = 1L,
            exitTime = 2L,
            stopLoss = 95.0,
            takeProfit = 110.0,
            exitReason = "TP",
            timeframe = "15min",
            leverage = 10.0,
            slTouched = false
        )

        assertEquals("15min", trade.timeframe)
        assertEquals(10.0, trade.leverage, 0.0)
        assertFalse(trade.slTouched)
        assertEquals(18.5 / (100.0 * 2.0) * 100.0, trade.pnlPercent, 0.0001)
        assertTrue(trade.isWin)
    }
}
