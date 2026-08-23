package com.notash.cryptobacktester.ai

import com.notash.cryptobacktester.core.BacktestReport

object BacktestComparator {
    data class Result(val pnlDelta: Double, val roiDelta: Double, val winRateDelta: Double, val drawdownDelta: Double, val profitFactorDelta: Double, val better: String)

    fun compare(current: BacktestReport, previous: BacktestReport): Result {
        val pnl = current.netPnl - previous.netPnl
        val roi = current.roiPercent - previous.roiPercent
        val win = current.winRatePercent - previous.winRatePercent
        val dd = current.maxDrawdownPercent - previous.maxDrawdownPercent
        val pf = current.profitFactor - previous.profitFactor
        val scoreCurrent = current.netPnl + current.profitFactor * 10.0 - current.maxDrawdownPercent
        val scorePrevious = previous.netPnl + previous.profitFactor * 10.0 - previous.maxDrawdownPercent
        return Result(pnl, roi, win, dd, pf, if (scoreCurrent >= scorePrevious) "CURRENT" else "PREVIOUS")
    }
}
