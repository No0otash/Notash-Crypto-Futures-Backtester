package com.notash.cryptobacktester.core

import java.time.Instant

enum class DataMode { LIVE, HISTORICAL }

data class MarketRequest(
    val symbol: String = "BTCUSDT",
    val timeframe: String = "5m",
    val mode: DataMode = DataMode.LIVE,
    val from: Instant? = null,
    val to: Instant? = null
)

data class PositionSettings(
    val amountUsdt: Double = 100.0,
    val leverage: Double = 3.0,
    val side: Side = Side.LONG,
    val entryPrice: Double? = null,
    val stopLoss: Double? = null,
    val takeProfit: Double? = null
)

data class StrategyDescriptor(
    val id: String,
    val name: String,
    val version: String,
    val sourceType: String = "package",
    val enabled: Boolean = true
)

data class LiveBacktestState(
    val running: Boolean = false,
    val strategyId: String? = null,
    val candlesProcessed: Long = 0,
    val trades: Int = 0,
    val netPnl: Double = 0.0,
    val startedAt: Instant? = null
)
