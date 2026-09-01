package com.notash.cryptobacktester.robot

import com.notash.cryptobacktester.core.BacktestConfig
import com.notash.cryptobacktester.core.Candle
import com.notash.cryptobacktester.core.FundingRate
import com.notash.cryptobacktester.core.OrderType
import com.notash.cryptobacktester.core.Side
import com.notash.cryptobacktester.core.Signal
import com.notash.cryptobacktester.strategy.Strategy

data class AlvexRobotPackage(
    val schemaVersion: Int = 1,
    val id: String,
    val name: String,
    val version: String = "1.0.0",
    val description: String = "",
    val parameters: RobotParameters = RobotParameters(),
    val rules: RobotRules = RobotRules()
)

data class RobotParameters(
    val leverage: Double = 1.0,
    val riskPercent: Double = 1.0,
    val stopLossPercent: Double = 1.0,
    val takeProfitPercent: Double = 2.0
)

data class RobotRules(
    val longWhenCloseAboveOpen: Boolean = true,
    val shortWhenCloseBelowOpen: Boolean = true
)

class AlvexRobotStrategy(private val robot: AlvexRobotPackage) : Strategy {
    override val id: String = robot.id
    override val name: String = robot.name
    override val version: String = robot.version
    override val description: String = robot.description

    override fun generateSignal(
        index: Int,
        candles: List<Candle>,
        funding: List<FundingRate>,
        config: BacktestConfig
    ): Signal? {
        if (index <= 0 || index >= candles.size) return null
        val c = candles[index]
        val p = candles[index - 1]
        val long = robot.rules.longWhenCloseAboveOpen && c.close > c.open && c.close > p.close
        val short = robot.rules.shortWhenCloseBelowOpen && c.close < c.open && c.close < p.close
        return when {
            long -> {
                val sl = c.close * (1.0 - robot.parameters.stopLossPercent / 100.0)
                val tp = c.close * (1.0 + robot.parameters.takeProfitPercent / 100.0)
                Signal(Side.LONG, OrderType.MARKET, c.close, sl, tp, "ALVEX robot long rule")
            }
            short -> {
                val sl = c.close * (1.0 + robot.parameters.stopLossPercent / 100.0)
                val tp = c.close * (1.0 - robot.parameters.takeProfitPercent / 100.0)
                Signal(Side.SHORT, OrderType.MARKET, c.close, sl, tp, "ALVEX robot short rule")
            }
            else -> null
        }
    }
}
