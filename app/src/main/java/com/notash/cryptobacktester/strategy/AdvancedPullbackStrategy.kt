package com.notash.cryptobacktester.strategy

import com.notash.cryptobacktester.core.BacktestConfig
import com.notash.cryptobacktester.core.Candle
import com.notash.cryptobacktester.core.FundingRate
import com.notash.cryptobacktester.core.OrderType
import com.notash.cryptobacktester.core.Side
import com.notash.cryptobacktester.core.Signal
import kotlin.math.abs

class AdvancedPullbackStrategy : Strategy {

    override val id = "advanced_pullback_v1"

    override val name = "Advanced Pullback"

    override val version = "1.0"

    override val description =
        "LWMA trend + ATR pullback strategy for crypto futures."

    override fun generateSignal(
        index: Int,
        candles: List<Candle>,
        funding: List<FundingRate>,
        config: BacktestConfig
    ): Signal? {

        val minimumBars =
            maxOf(
                config.slowLwma,
                config.atrPeriod
            ) + 2

        if (index < minimumBars) {
            return null
        }

        val fast =
            calculateLwma(
                candles,
                index,
                config.fastLwma
            )

        val slow =
            calculateLwma(
                candles,
                index,
                config.slowLwma
            )

        val atr =
            calculateAtr(
                candles,
                index,
                config.atrPeriod
            )

        if (atr <= 0.0) {
            return null
        }

        val candle = candles[index]

        /*
         * BULLISH TREND
         *
         * Fast LWMA > Slow LWMA
         *
         * Entry is placed below the current
         * fast LWMA using ATR.
         */

        if (fast > slow) {

            val entry =
                fast -
                    atr * config.entryAtr

            val stop =
                entry -
                    atr * config.stopAtr

            val takeProfit =
                entry +
                    atr * config.takeProfitAtr

            /*
             * We only create the signal.
             *
             * The BacktestEngine will decide
             * whether the limit order was filled.
             */

            if (entry < candle.close) {

                return Signal(
                    side = Side.LONG,
                    orderType = OrderType.LIMIT,
                    entryPrice = entry,
                    stopLoss = stop,
                    takeProfit = takeProfit,
                    reason =
                        "Bullish LWMA trend + ATR pullback"
                )
            }
        }

        /*
         * BEARISH TREND
         */

        if (fast < slow) {

            val entry =
                fast +
                    atr * config.entryAtr

            val stop =
                entry +
                    atr * config.stopAtr

            val takeProfit =
                entry -
                    atr * config.takeProfitAtr

            if (entry > candle.close) {

                return Signal(
                    side = Side.SHORT,
                    orderType = OrderType.LIMIT,
                    entryPrice = entry,
                    stopLoss = stop,
                    takeProfit = takeProfit,
                    reason =
                        "Bearish LWMA trend + ATR pullback"
                )
            }
        }

        return null
    }

    private fun calculateLwma(
        candles: List<Candle>,
        index: Int,
        period: Int
    ): Double {

        if (period <= 0) {
            return 0.0
        }

        var weightedSum = 0.0
        var weightSum = 0.0

        for (i in 0 until period) {

            val weight =
                (period - i).toDouble()

            val price =
                candles[index - i].close

            weightedSum +=
                price * weight

            weightSum += weight
        }

        return if (weightSum == 0.0) {
            0.0
        } else {
            weightedSum / weightSum
        }
    }

    private fun calculateAtr(
        candles: List<Candle>,
        index: Int,
        period: Int
    ): Double {

        if (period <= 0) {
            return 0.0
        }

        var totalTrueRange = 0.0

        for (i in 0 until period) {

            val current =
                candles[index - i]

            val previous =
                candles[
                    (index - i - 1)
                        .coerceAtLeast(0)
                ]

            val range1 =
                current.high -
                    current.low

            val range2 =
                abs(
                    current.high -
                        previous.close
                )

            val range3 =
                abs(
                    current.low -
                        previous.close
                )

            val trueRange =
                maxOf(
                    range1,
                    range2,
                    range3
                )

            totalTrueRange +=
                trueRange
        }

        return totalTrueRange / period
    }
}
