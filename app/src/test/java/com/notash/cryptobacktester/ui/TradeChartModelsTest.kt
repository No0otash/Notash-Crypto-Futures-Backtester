package com.notash.cryptobacktester.ui

import com.notash.cryptobacktester.core.Candle
import com.notash.cryptobacktester.core.ExitReason
import com.notash.cryptobacktester.core.Side
import com.notash.cryptobacktester.core.TradeResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TradeChartModelsTest {
    private fun trade(reason: ExitReason) = TradeResult(
        side = Side.LONG,
        entryPrice = 100.0,
        exitPrice = 110.0,
        quantity = 1.0,
        grossPnl = 10.0,
        fees = 0.1,
        funding = 0.0,
        netPnl = 9.9,
        entryTime = 1L,
        exitTime = 2L,
        exitReason = reason,
        stopLossTouched = reason == ExitReason.STOP_LOSS,
        takeProfitTouched = reason == ExitReason.TAKE_PROFIT
    )

    @Test
    fun stop_loss_trade_creates_stop_loss_marker() {
        val data = buildTradeChartData(
            listOf(Candle(1L, 100.0, 101.0, 99.0, 100.0, 1.0)),
            listOf(trade(ExitReason.STOP_LOSS)),
            listOf(1000.0)
        )

        assertEquals(3, data.markers.size)
        assertTrue(data.markers.any { it.type == MarkerType.STOP_LOSS })
    }

    @Test
    fun take_profit_trade_creates_take_profit_marker() {
        val data = buildTradeChartData(
            listOf(Candle(1L, 100.0, 101.0, 99.0, 100.0, 1.0)),
            listOf(trade(ExitReason.TAKE_PROFIT)),
            listOf(1000.0)
        )

        assertEquals(3, data.markers.size)
        assertTrue(data.markers.any { it.type == MarkerType.TAKE_PROFIT })
    }

    @Test
    fun signal_exit_has_only_entry_and_exit_markers() {
        val data = buildTradeChartData(
            emptyList(),
            listOf(trade(ExitReason.SIGNAL)),
            emptyList()
        )

        assertEquals(listOf(MarkerType.ENTRY, MarkerType.EXIT), data.markers.map { it.type })
    }
}
