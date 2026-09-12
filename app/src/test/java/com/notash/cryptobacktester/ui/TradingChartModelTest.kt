package com.notash.cryptobacktester.ui

import com.notash.cryptobacktester.core.Candle
import com.notash.cryptobacktester.core.Side
import com.notash.cryptobacktester.core.TradeResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TradingChartModelTest {
    private val candles = listOf(
        Candle(1_000L, 100.0, 103.0, 99.0, 102.0, 10.0),
        Candle(2_000L, 102.0, 106.0, 101.0, 105.0, 12.0),
        Candle(3_000L, 105.0, 109.0, 104.0, 108.0, 14.0)
    )

    @Test
    fun maps_entry_exit_and_risk_levels_to_real_candles() {
        val trade = TradeResult(
            side = Side.LONG, entryPrice = 102.0, exitPrice = 108.0, quantity = 1.0,
            grossPnl = 6.0, fees = 0.1, funding = 0.0, netPnl = 5.9,
            entryTime = 2_000L, exitTime = 3_000L, stopLoss = 99.0, takeProfit = 108.0,
            exitReason = "TP", timeframe = "15min", leverage = 10.0, slTouched = false
        )
        val point = buildTradingChartPoint(candles, trade)
        assertEquals(1, point.entryIndex)
        assertEquals(2, point.exitIndex)
        assertEquals(trade.entryPrice, point.entryPrice, 0.0)
        assertEquals(trade.exitPrice, point.exitPrice, 0.0)
        assertEquals(trade.stopLoss, point.stopLoss, 0.0)
        assertEquals(trade.takeProfit, point.takeProfit, 0.0)
        assertEquals(Side.LONG, point.side)
        assertFalse(point.slTouched)
    }

    @Test
    fun distinguishes_short_trade_and_sl_exit() {
        val trade = TradeResult(
            side = Side.SHORT, entryPrice = 105.0, exitPrice = 109.0, quantity = 1.0,
            grossPnl = -4.0, fees = 0.1, funding = 0.0, netPnl = -4.1,
            entryTime = 2_000L, exitTime = 3_000L, stopLoss = 109.0, takeProfit = 99.0,
            exitReason = "SL", timeframe = "1hour", leverage = 5.0, slTouched = true
        )
        val point = buildTradingChartPoint(candles, trade)
        assertEquals(Side.SHORT, point.side)
        assertEquals(1, point.entryIndex)
        assertEquals(2, point.exitIndex)
        assertTrue(point.slTouched)
        assertEquals("SL", point.exitReason)
    }

    @Test
    fun maps_nearest_candle_when_trade_timestamp_is_between_bars() {
        val trade = TradeResult(
            side = Side.LONG, entryPrice = 103.0, exitPrice = 106.0, quantity = 1.0,
            grossPnl = 3.0, fees = 0.0, funding = 0.0, netPnl = 3.0,
            entryTime = 2_600L, exitTime = 2_900L, stopLoss = 100.0, takeProfit = 108.0
        )
        val point = buildTradingChartPoint(candles, trade)
        assertEquals(2, point.entryIndex)
        assertEquals(2, point.exitIndex)
    }
}
