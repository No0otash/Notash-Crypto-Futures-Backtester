package com.notash.cryptobacktester.data

import kotlinx.serialization.Serializable

@Serializable
data class StrategyPackage(
    val id: String,
    val name: String,
    val version: String,
    val symbol: String = "BTCUSDT",
    val timeframe: String = "5m",
    val entryRules: List<String> = emptyList(),
    val exitRules: List<String> = emptyList(),
    val riskPercent: Double = 1.0
)

data class LiveCandle(val time: Long, val open: Double, val high: Double, val low: Double, val close: Double, val volume: Double)

data class LiveBacktestState(
    val running: Boolean = false,
    val balance: Double = 1000.0,
    val equity: Double = 1000.0,
    val trades: Int = 0,
    val winRate: Double = 0.0,
    val pnl: Double = 0.0
)

object StrategyValidator {
    fun validate(strategy: StrategyPackage): List<String> = buildList {
        if (strategy.id.isBlank()) add("Strategy ID is required")
        if (strategy.name.isBlank()) add("Strategy name is required")
        if (strategy.version.isBlank()) add("Strategy version is required")
        if (strategy.riskPercent <= 0.0 || strategy.riskPercent > 100.0) add("Risk percent must be between 0 and 100")
    }
}
