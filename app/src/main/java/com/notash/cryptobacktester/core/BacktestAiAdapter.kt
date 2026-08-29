package com.notash.cryptobacktester.core

import com.notash.cryptobacktester.TradeResult

/** Converts the app's native backtest trade model into evidence for AITradeAnalyst. */
object BacktestAiAdapter {
    fun analyzeTrade(
        symbol: String,
        trade: TradeResult,
        timeframe: String? = null,
        strategyName: String? = null,
        marketTrendScore: Int? = null,
        momentumScore: Int? = null,
        fundingPercent: Double? = null,
        dataConfidence: Int = 100
    ): AITradeAnalyst.Report {
        val pnlBase = trade.entry * trade.qty
        val pnlPercent = if (pnlBase > 0.0) trade.pnl / pnlBase * 100.0 else 0.0
        return AITradeAnalyst.analyze(
            AITradeAnalyst.TradeInput(
                symbol = symbol,
                side = trade.side,
                entryPrice = trade.entry,
                exitPrice = trade.exit,
                pnlPercent = pnlPercent,
                exitReason = trade.reason,
                timeframe = timeframe,
                stopLossTouched = trade.reason.equals("SL", ignoreCase = true),
                takeProfitTouched = trade.reason.equals("TP", ignoreCase = true),
                strategyName = strategyName,
                marketTrendScore = marketTrendScore,
                momentumScore = momentumScore,
                fundingPercent = fundingPercent,
                dataConfidence = dataConfidence
            )
        )
    }
}
