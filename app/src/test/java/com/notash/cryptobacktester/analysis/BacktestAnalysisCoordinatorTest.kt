package com.notash.cryptobacktester.analysis

import com.notash.cryptobacktester.core.BacktestReport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BacktestAnalysisCoordinatorTest {
    @Test
    fun `coordinator keeps trades from completed report`() {
        val report = BacktestReport(
            initialBalance = 1000.0,
            finalBalance = 1010.0,
            netPnl = 10.0,
            roiPercent = 1.0,
            maxDrawdownPercent = 0.0,
            winRatePercent = 100.0,
            profitFactor = Double.POSITIVE_INFINITY,
            totalFees = 0.0,
            totalFunding = 0.0,
            trades = emptyList(),
            equityCurve = listOf(1000.0, 1010.0)
        )
        val coordinator = BacktestAnalysisCoordinator()
        val analysis = coordinator.accept(report)
        assertEquals(0, coordinator.currentTrades().size)
        assertEquals(0, analysis.score)
        assertTrue(analysis.weaknesses.isNotEmpty())
    }
}
