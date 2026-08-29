package com.notash.cryptobacktester.engine

import com.notash.cryptobacktester.core.BacktestConfig
import com.notash.cryptobacktester.core.BacktestReport
import com.notash.cryptobacktester.core.Candle
import com.notash.cryptobacktester.core.ExitReason
import com.notash.cryptobacktester.core.FundingRate
import com.notash.cryptobacktester.core.OrderType
import com.notash.cryptobacktester.core.Position
import com.notash.cryptobacktester.core.Side
import com.notash.cryptobacktester.core.TradeResult
import kotlin.math.abs
import kotlin.math.max

class BacktestEngine {
    fun run(candles: List<Candle>, funding: List<FundingRate>, strategy: com.notash.cryptobacktester.strategy.Strategy, config: BacktestConfig): BacktestReport {
        require(candles.size >= 10) { "Not enough candle data." }
        var balance = config.initialBalance
        var position: Position? = null
        var pendingSignal: com.notash.cryptobacktester.core.Signal? = null
        val trades = mutableListOf<TradeResult>()
        val equityCurve = mutableListOf<Double>()
        var totalFees = 0.0
        var totalFunding = 0.0
        var peakEquity = balance
        var maxDrawdown = 0.0

        for (i in candles.indices) {
            val candle = candles[i]

            if (position == null && pendingSignal != null) {
                val signal = pendingSignal!!
                val filled = when (signal.side) {
                    Side.LONG -> candle.low <= signal.entryPrice
                    Side.SHORT -> candle.high >= signal.entryPrice
                }
                if (filled) {
                    val quantity = calculatePositionSize(balance, signal.entryPrice, signal.stopLoss, config)
                    if (quantity > 0.0) {
                        position = Position(signal.side, applyEntrySlippage(signal.entryPrice, signal.side, config), quantity, signal.stopLoss, signal.takeProfit, candle.timestamp)
                        val entryFee = position.quantity * position.entryPrice * config.makerFee
                        balance -= entryFee
                        totalFees += entryFee
                    }
                    pendingSignal = null
                }
            }

            val currentPosition = position
            if (currentPosition != null) {
                val exit = checkExit(currentPosition, candle)
                if (exit != null) {
                    val (exitPriceRaw, exitReason) = exit
                    val exitPrice = applyExitSlippage(exitPriceRaw, currentPosition.side, config)
                    val grossPnl = calculatePnl(currentPosition, exitPrice)
                    val exitFee = abs(exitPrice * currentPosition.quantity) * config.takerFee
                    val fundingCost = calculateFunding(currentPosition, funding, candle.timestamp, config)
                    val netPnl = grossPnl - exitFee - fundingCost
                    balance += netPnl
                    totalFees += exitFee
                    totalFunding += fundingCost
                    trades += TradeResult(
                        currentPosition.side, currentPosition.entryPrice, exitPrice,
                        currentPosition.quantity, grossPnl, exitFee, fundingCost, netPnl,
                        currentPosition.entryTime, candle.timestamp, exitReason,
                        exitReason == ExitReason.STOP_LOSS,
                        exitReason == ExitReason.TAKE_PROFIT
                    )
                    position = null
                }
            }

            if (position == null && pendingSignal == null) {
                val signal = strategy.generateSignal(i, candles, funding, config)
                if (signal != null && signal.orderType == OrderType.LIMIT) pendingSignal = signal
            }

            val unrealized = position?.let { calculatePnl(it, candle.close) } ?: 0.0
            val equity = balance + unrealized
            equityCurve += equity
            peakEquity = max(peakEquity, equity)
            if (peakEquity > 0.0) maxDrawdown = max(maxDrawdown, ((peakEquity - equity) / peakEquity) * 100.0)
        }

        position?.let { finalPosition ->
            val last = candles.last()
            val exitPrice = applyExitSlippage(last.close, finalPosition.side, config)
            val grossPnl = calculatePnl(finalPosition, exitPrice)
            val exitFee = abs(exitPrice * finalPosition.quantity) * config.takerFee
            val netPnl = grossPnl - exitFee
            balance += netPnl
            totalFees += exitFee
            trades += TradeResult(
                finalPosition.side, finalPosition.entryPrice, exitPrice,
                finalPosition.quantity, grossPnl, exitFee, 0.0, netPnl,
                finalPosition.entryTime, last.timestamp, ExitReason.END_OF_DATA,
                false, false
            )
        }

        val netPnl = balance - config.initialBalance
        val roi = if (config.initialBalance != 0.0) netPnl / config.initialBalance * 100.0 else 0.0
        val wins = trades.count { it.netPnl > 0.0 }
        val winRate = if (trades.isNotEmpty()) wins.toDouble() / trades.size * 100.0 else 0.0
        val grossProfit = trades.filter { it.netPnl > 0 }.sumOf { it.netPnl }
        val grossLoss = trades.filter { it.netPnl < 0 }.sumOf { abs(it.netPnl) }
        val profitFactor = when {
            grossLoss > 0.0 -> grossProfit / grossLoss
            grossProfit > 0.0 -> Double.POSITIVE_INFINITY
            else -> 0.0
        }
        return BacktestReport(config.initialBalance, balance, netPnl, roi, maxDrawdown, winRate, profitFactor, totalFees, totalFunding, trades, equityCurve, candles)
    }

    private fun calculatePositionSize(balance: Double, entry: Double, stop: Double, config: BacktestConfig): Double {
        val riskAmount = balance * config.riskPercent / 100.0
        val stopDistance = abs(entry - stop)
        if (riskAmount <= 0.0 || stopDistance <= 0.0 || entry <= 0.0) return 0.0
        val quantityByRisk = riskAmount / stopDistance
        val quantityByMargin = balance * config.leverage / entry
        return minOf(quantityByRisk, quantityByMargin)
    }

    private fun calculatePnl(position: Position, exitPrice: Double): Double =
        when (position.side) {
            Side.LONG -> (exitPrice - position.entryPrice) * position.quantity
            Side.SHORT -> (position.entryPrice - exitPrice) * position.quantity
        }

    private fun checkExit(position: Position, candle: Candle): Pair<Double, ExitReason>? = when (position.side) {
        Side.LONG -> when {
            candle.low <= position.stopLoss -> position.stopLoss to ExitReason.STOP_LOSS
            candle.high >= position.takeProfit -> position.takeProfit to ExitReason.TAKE_PROFIT
            else -> null
        }
        Side.SHORT -> when {
            candle.high >= position.stopLoss -> position.stopLoss to ExitReason.STOP_LOSS
            candle.low <= position.takeProfit -> position.takeProfit to ExitReason.TAKE_PROFIT
            else -> null
        }
    }

    private fun applyEntrySlippage(price: Double, side: Side, config: BacktestConfig): Double {
        val factor = config.slippageBps / 10000.0
        return when (side) {
            Side.LONG -> price * (1.0 + factor)
            Side.SHORT -> price * (1.0 - factor)
        }
    }

    private fun applyExitSlippage(price: Double, side: Side, config: BacktestConfig): Double {
        val factor = config.slippageBps / 10000.0
        return when (side) {
            Side.LONG -> price * (1.0 - factor)
            Side.SHORT -> price * (1.0 + factor)
        }
    }

    private fun calculateFunding(position: Position, funding: List<FundingRate>, timestamp: Long, config: BacktestConfig): Double {
        if (!config.useFunding) return 0.0
        val applicable = funding.filter { it.timestamp <= timestamp && it.timestamp > position.entryTime && it.rate != 0.0 }
        if (applicable.isEmpty()) return 0.0
        return applicable.sumOf {
            val payment = position.entryPrice * position.quantity * it.rate
            when (position.side) {
                Side.LONG -> payment
                Side.SHORT -> -payment
            }
        }
    }
}
