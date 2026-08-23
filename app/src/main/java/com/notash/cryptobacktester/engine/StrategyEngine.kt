package com.notash.cryptobacktester.engine

data class Candle(val time: Long, val open: Double, val high: Double, val low: Double, val close: Double, val volume: Double)
data class Position(val side: String, val entry: Double, val sizeUsdt: Double, val leverage: Double, val sl: Double?, val tp: Double?)
data class TradeResult(val pnl: Double, val roi: Double, val reason: String)

data class StrategyPackage(val id: String, val name: String, val version: String, val entryRule: String, val exitRule: String, val riskRule: String)

object StrategyEngine {
    fun validate(strategy: StrategyPackage): List<String> = buildList {
        if (strategy.id.isBlank()) add("شناسه استراتژی خالی است")
        if (strategy.name.isBlank()) add("نام استراتژی خالی است")
        if (strategy.entryRule.isBlank()) add("قانون ورود تعریف نشده است")
        if (strategy.exitRule.isBlank()) add("قانون خروج تعریف نشده است")
    }

    fun evaluate(position: Position, exitPrice: Double, feeRate: Double = 0.0005, funding: Double = 0.0): TradeResult {
        val direction = if (position.side.equals("LONG", true)) 1.0 else -1.0
        val gross = (exitPrice - position.entry) * direction / position.entry * position.sizeUsdt * position.leverage
        val fees = position.sizeUsdt * position.leverage * feeRate * 2
        val pnl = gross - fees - funding
        val roi = if (position.sizeUsdt == 0.0) 0.0 else pnl / position.sizeUsdt * 100
        val reason = if (pnl >= 0) "روند و نقطه خروج به نفع معامله بوده است" else "ورود/خروج یا شرایط روند برای معامله نامناسب بوده است"
        return TradeResult(pnl, roi, reason)
    }
}
