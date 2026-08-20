package com.notash.cryptobacktester.engine

import com.notash.cryptobacktester.core.BacktestConfig
import com.notash.cryptobacktester.core.BacktestReport
import com.notash.cryptobacktester.core.Candle
import com.notash.cryptobacktester.core.FundingRate
import com.notash.cryptobacktester.core.OrderType
import com.notash.cryptobacktester.core.Position
import com.notash.cryptobacktester.core.Side
import com.notash.cryptobacktester.core.Strategy
import com.notash.cryptobacktester.core.TradeResult
import kotlin.math.abs
import kotlin.math.max

class BacktestEngine {

    fun run(
        candles: List<Candle>,
        funding: List<FundingRate>,
        strategy: com.notash.cryptobacktester.strategy.Strategy,
        config: BacktestConfig
    ): BacktestReport {

        require(candles.size >= 10) {
            "Not enough candle data."
        }

        var balance = config.initialBalance

        var position: Position? = null

        var pendingSignal:
                com.notash.cryptobacktester.core.Signal? = null

        val trades =
            mutableListOf<TradeResult>()

        val equityCurve =
            mutableListOf<Double>()

        var totalFees = 0.0
        var totalFunding = 0.0

        var peakEquity = balance
        var maxDrawdown = 0.0

        for (i in candles.indices) {

            val candle = candles[i]

            /*
             * -----------------------------------------------------
             * 1. CHECK PENDING LIMIT ORDER
             * -----------------------------------------------------
             */

            if (position == null && pendingSignal != null) {

                val signal = pendingSignal!!

                val filled =
                    when (signal.side) {

                        Side.LONG ->
                            candle.low <=
                                    signal.entryPrice

                        Side.SHORT ->
                            candle.high >=
                                    signal.entryPrice
                    }

                if (filled) {

                    val quantity =
                        calculatePositionSize(
                            balance,
                            signal.entryPrice,
                            signal.stopLoss,
                            config
                        )

                    if (quantity > 0.0) {

                        position =
                            Position(
                                side = signal.side,
                                entryPrice =
                                    applyEntrySlippage(
                                        signal.entryPrice,
                                        signal.side,
                                        config
                                    ),
                                quantity = quantity,
                                stopLoss =
                                    signal.stopLoss,
                                takeProfit =
                                    signal.takeProfit,
                                entryTime =
                                    candle.timestamp
                            )

                        val entryFee =
                            position.quantity *
                                    position.entryPrice *
                                    config.takerFee

                        balance -= entryFee

                        totalFees += entryFee
                    }

                    pendingSignal = null
                }
            }

            /*
             * -----------------------------------------------------
             * 2. CHECK OPEN POSITION
             * -----------------------------------------------------
             */

            val currentPosition = position

            if (currentPosition != null) {

                val exit =
                    checkExit(
                        currentPosition,
                        candle
                    )

                if (exit != null) {

                    val exitPrice =
                        applyExitSlippage(
                            exit,
                            currentPosition.side,
                            config
                        )

                    val grossPnl =
                        calculatePnl(
                            currentPosition,
                            exitPrice
                        )

                    val exitFee =
                        abs(
                            exitPrice *
                                    currentPosition.quantity
                        ) *
                                config.takerFee

                    val fundingCost =
                        calculateFunding(
                            currentPosition,
                            funding,
                            candle.timestamp,
                            config
                        )

                    val netPnl =
                        grossPnl -
                                exitFee -
                                fundingCost

                    balance += netPnl

                    totalFees += exitFee
                    totalFunding += fundingCost

                    trades.add(
                        TradeResult(
                            side =
                                currentPosition.side,
                            entryPrice =
                                currentPosition.entryPrice,
                            exitPrice =
                                exitPrice,
                            quantity =
                                currentPosition.quantity,
                            grossPnl =
                                grossPnl,
                            fees =
                                exitFee,
                            funding =
                                fundingCost,
                            netPnl =
                                netPnl,
                            entryTime =
                                currentPosition.entryTime,
                            exitTime =
                                candle.timestamp
                        )
                    )

                    position = null
                }
            }

            /*
             * -----------------------------------------------------
             * 3. GENERATE NEW SIGNAL
             * -----------------------------------------------------
             */

            if (
                position == null &&
                pendingSignal == null
            ) {

                val signal =
                    strategy.generateSignal(
                        index = i,
                        candles = candles,
                        funding = funding,
                        config = config
                    )

                if (
                    signal != null &&
                    signal.orderType ==
                    OrderType.LIMIT
                ) {

                    pendingSignal = signal
                }
            }

            /*
             * -----------------------------------------------------
             * 4. EQUITY
             * -----------------------------------------------------
             */

            val unrealized =
                position?.let {
                    calculatePnl(
                        it,
                        candle.close
                    )
                } ?: 0.0

            val equity =
                balance + unrealized

            equityCurve.add(equity)

            peakEquity =
                max(
                    peakEquity,
                    equity
                )

            if (peakEquity > 0.0) {

                val drawdown =
                    (
                        (peakEquity - equity)
                                / peakEquity
                        ) * 100.0

                maxDrawdown =
                    max(
                        maxDrawdown,
                        drawdown
                    )
            }
        }

        /*
         * ---------------------------------------------------------
         * FORCE CLOSE REMAINING POSITION
         * ---------------------------------------------------------
         */

        if (position != null) {

            val lastCandle =
                candles.last()

            val finalPosition =
                position!!

            val exitPrice =
                applyExitSlippage(
                    lastCandle.close,
                    finalPosition.side,
                    config
                )

            val grossPnl =
                calculatePnl(
                    finalPosition,
                    exitPrice
                )

            val exitFee =
                abs(
                    exitPrice *
                            finalPosition.quantity
                ) *
                        config.takerFee

            val netPnl =
                grossPnl -
                        exitFee

            balance += netPnl

            totalFees += exitFee

            trades.add(
                TradeResult(
                    side =
                        finalPosition.side,
                    entryPrice =
                        finalPosition.entryPrice,
                    exitPrice =
                        exitPrice,
                    quantity =
                        finalPosition.quantity,
                    grossPnl =
                        grossPnl,
                    fees =
                        exitFee,
                    funding = 0.0,
                    netPnl =
                        netPnl,
                    entryTime =
                        finalPosition.entryTime,
                    exitTime =
                        lastCandle.timestamp
                )
            )
        }

        /*
         * ---------------------------------------------------------
         * REPORT
         * ---------------------------------------------------------
         */

        val netPnl =
            balance -
                    config.initialBalance

        val roi =
            if (config.initialBalance != 0.0) {

                netPnl /
                        config.initialBalance *
                        100.0

            } else {
                0.0
            }

        val wins =
            trades.count {
                it.netPnl > 0
            }

        val winRate =
            if (trades.isNotEmpty()) {

                wins.toDouble() /
                        trades.size *
                        100.0

            } else {
                0.0
            }

        val grossProfit =
            trades
                .filter {
                    it.netPnl > 0
                }
                .sumOf {
                    it.netPnl
                }

        val grossLoss =
            trades
                .filter {
                    it.netPnl < 0
                }
                .sumOf {
                    abs(it.netPnl)
                }

        val profitFactor =
            if (grossLoss > 0.0) {

                grossProfit /
                        grossLoss

            } else if (grossProfit > 0.0) {

                Double.POSITIVE_INFINITY

            } else {
                0.0
            }

        return BacktestReport(
            initialBalance =
                config.initialBalance,
            finalBalance =
                balance,
            netPnl =
                netPnl,
            roiPercent =
                roi,
            maxDrawdownPercent =
                maxDrawdown,
            winRatePercent =
                winRate,
            profitFactor =
                profitFactor,
            totalFees =
                totalFees,
            totalFunding =
                totalFunding,
            trades =
                trades,
            equityCurve =
                equityCurve
        )
    }

    private fun calculatePositionSize(
        balance: Double,
        entry: Double,
        stop: Double,
        config: BacktestConfig
    ): Double {

        val riskAmount =
            balance *
                    config.riskPercent /
                    100.0

        val stopDistance =
            abs(entry - stop)

        if (
            riskAmount <= 0.0 ||
            stopDistance <= 0.0 ||
            entry <= 0.0
        ) {
            return 0.0
        }

        val quantityByRisk =
            riskAmount /
                    stopDistance

        val maxNotional =
            balance *
                    config.leverage

        val quantityByMargin =
            maxNotional /
                    entry

        return minOf(
            quantityByRisk,
            quantityByMargin
        )
    }

    private fun calculatePnl(
        position: Position,
        exitPrice: Double
    ): Double {

        val priceDifference =
            when (position.side) {

                Side.LONG ->
                    exitPrice -
                            position.entryPrice

                Side.SHORT ->
                    position.entryPrice -
                            exitPrice
            }

        return priceDifference *
                position.quantity
    }

    private fun checkExit(
        position: Position,
        candle: Candle
    ): Double? {

        return when (position.side) {

            Side.LONG -> {

                if (
                    candle.low <=
                    position.stopLoss
                ) {

                    position.stopLoss

                } else if (
                    candle.high >=
                    position.takeProfit
                ) {

                    position.takeProfit

                } else {
                    null
                }
            }

            Side.SHORT -> {

                if (
                    candle.high >=
                    position.stopLoss
                ) {

                    position.stopLoss

                } else if (
                    candle.low <=
                    position.takeProfit
                ) {

                    position.takeProfit

                } else {
                    null
                }
            }
        }
    }

    private fun applyEntrySlippage(
        price: Double,
        side: Side,
        config: BacktestConfig
    ): Double {

        val factor =
            config.slippageBps /
                    10000.0

        return when (side) {

            Side.LONG ->
                price *
                        (1.0 + factor)

            Side.SHORT ->
                price *
                        (1.0 - factor)
        }
    }

    private fun applyExitSlippage(
        price: Double,
        side: Side,
        config: BacktestConfig
    ): Double {

        val factor =
            config.slippageBps /
                    10000.0

        return when (side) {

            Side.LONG ->
                price *
                        (1.0 - factor)

            Side.SHORT ->
                price *
                        (1.0 + factor)
        }
    }

    private fun calculateFunding(
        position: Position,
        funding: List<FundingRate>,
        timestamp: Long,
        config: BacktestConfig
    ): Double {

        if (!config.useFunding) {
            return 0.0
        }

        val applicable =
            funding.filter {
                it.timestamp <= timestamp &&
                        it.rate != 0.0
            }

        if (applicable.isEmpty()) {
            return 0.0
        }

        var total = 0.0

        for (rate in applicable) {

            val notional =
                position.entryPrice *
                        position.quantity

            val payment =
                notional *
                        rate.rate

            /*
             * Positive funding:
             * Long pays Short.
             *
             * Negative funding:
             * Short pays Long.
             */

            total += when (position.side) {

                Side.LONG ->
                    payment

                Side.SHORT ->
                    -payment
            }
        }

        return total
    }
}
