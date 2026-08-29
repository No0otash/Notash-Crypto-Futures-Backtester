package com.notash.cryptobacktester.engine

import com.notash.cryptobacktester.core.BacktestConfig
import com.notash.cryptobacktester.core.Candle
import com.notash.cryptobacktester.core.FundingRate
import com.notash.cryptobacktester.core.OrderType
import com.notash.cryptobacktester.core.Signal
import com.notash.cryptobacktester.core.Side
import com.notash.cryptobacktester.strategy.Strategy
import org.junit.Assert.assertEquals
import org.junit.Test

class BacktestEngineTest {
    @Test
    fun limit_entry_uses_maker_fee() {
        val candles = (0L..11L).map { index ->
            Candle(index, 100.0, 101.0, 99.0, 100.0, 1.0)
        }.toMutableList()
        candles[10] = candles[10].copy(high = 103.0, low = 99.0, close = 102.0)
        candles[11] = candles[11].copy(high = 106.0, low = 100.0, close = 105.0)

        val strategy = object : Strategy {
            override val id = "test"
            override val name = "Test"
            override val version = "1"
            override val description = ""
            override fun generateSignal(index: Int, candles: List<Candle>, funding: List<FundingRate>, config: BacktestConfig): Signal? =
                if (index == 9) Signal(Side.LONG, OrderType.LIMIT, 100.0, 90.0, 130.0) else null
        }

        val config = BacktestConfig(
            initialBalance = 1000.0,
            riskPercent = 1.0,
            leverage = 10.0,
            makerFee = 0.0002,
            takerFee = 0.001,
            slippageBps = 0.0,
            useFunding = false
        )

        val report = BacktestEngine().run(candles, emptyList(), strategy, config)
        val trade = report.trades.single()
        val expectedEntryFee = trade.quantity * trade.entryPrice * config.makerFee
        val expectedExitFee = trade.quantity * trade.exitPrice * config.takerFee

        assertEquals(expectedEntryFee + expectedExitFee, report.totalFees, 1e-9)
    }
}
