package com.notash.cryptobacktester.live

import com.notash.cryptobacktester.core.BacktestConfig
import com.notash.cryptobacktester.core.Candle
import com.notash.cryptobacktester.robot.AlvexRobotImporter
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

class LiveBacktestSessionTest {
    @Test
    fun simulatesCapturedLiveWindowWithoutExchangeOrders() = runBlocking {
        val robot = AlvexRobotImporter.fromJson("""{"id":"live","name":"Live Test"}""")
        val candles = (0 until 12).map { i ->
            val close = 100.0 + i
            Candle(i.toLong(), close - 0.5, close + 1.0, close - 1.0, close, 1000.0)
        }
        val report = LiveBacktestSession().simulate(robot.let(::com.notash.cryptobacktester.robot.AlvexRobotStrategy), candles, config = BacktestConfig())
        assertTrue(report.equityCurve.isNotEmpty())
    }
}
