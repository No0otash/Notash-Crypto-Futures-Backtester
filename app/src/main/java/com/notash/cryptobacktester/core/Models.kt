package com.notash.cryptobacktester.core

data class Candle(
    val timestamp: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double,
    val value: Double = 0.0
)

data class FundingRate(
    val timestamp: Long,
    val rate: Double,
    val markPrice: Double = 0.0
)

data class MarketTicker(
    val market: String,
    val last: Double,
    val changeRate: Double,
    val volume: Double = 0.0,
    val markPrice: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)

enum class Side { LONG, SHORT }
enum class OrderType { MARKET, LIMIT }

data class Signal(
    val side: Side,
    val orderType: OrderType,
    val entryPrice: Double,
    val stopLoss: Double,
    val takeProfit: Double,
    val reason: String = ""
)

data class Position(
    val side: Side,
    val entryPrice: Double,
    val quantity: Double,
    val stopLoss: Double,
    val takeProfit: Double,
    val entryTime: Long,
    val entryFee: Double = 0.0
)

data class TradeResult(
    val side: Side,
    val entryPrice: Double,
    val exitPrice: Double,
    val quantity: Double,
    val grossPnl: Double,
    val fees: Double,
    val funding: Double,
    val netPnl: Double,
    val entryTime: Long,
    val exitTime: Long,
    val stopLoss: Double = 0.0,
    val takeProfit: Double = 0.0,
    val exitReason: String = "Unknown"
)

data class BacktestConfig(
    val initialBalance: Double = 1000.0,
    val riskPercent: Double = 1.0,
    val leverage: Double = 10.0,
    val makerFee: Double = 0.0002,
    val takerFee: Double = 0.0005,
    val slippageBps: Double = 2.0,
    val fastLwma: Int = 20,
    val slowLwma: Int = 50,
    val atrPeriod: Int = 14,
    val entryAtr: Double = 0.5,
    val stopAtr: Double = 1.5,
    val takeProfitAtr: Double = 3.0,
    val useFunding: Boolean = true
)

data class BacktestReport(
    val initialBalance: Double,
    val finalBalance: Double,
    val netPnl: Double,
    val roiPercent: Double,
    val maxDrawdownPercent: Double,
    val winRatePercent: Double,
    val profitFactor: Double,
    val totalFees: Double,
    val totalFunding: Double,
    val trades: List<TradeResult>,
    val equityCurve: List<Double>
)
