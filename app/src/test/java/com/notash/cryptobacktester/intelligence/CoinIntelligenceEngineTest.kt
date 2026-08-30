package com.notash.cryptobacktester.intelligence

import com.notash.cryptobacktester.core.Candle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoinIntelligenceEngineTest {
    @Test
    fun memeScanner_flags_high_risk_low_liquidity_token() {
        val candles = (1L..25L).map { i ->
            Candle(i, 1.0, 1.05, 0.95, 1.0 + i * 0.001, if (i < 20) 100.0 else 700.0)
        }
        val result = MemeShitcoinScanner().scan(
            MemeCoinSnapshot(
                symbol = "MEME",
                market = "MEMEUSDT",
                liquidityUsd = 12_000.0,
                marketCapUsd = 80_000.0,
                ageDays = 2,
                holderConcentrationPercent = 78.0,
                contractVerified = false,
                buyTaxPercent = 8.0,
                sellTaxPercent = 12.0
            ),
            candles
        )
        assertTrue(result.isMemeLike)
        assertTrue(result.riskScore >= 70.0)
        assertTrue(result.flags.isNotEmpty())
    }

    @Test
    fun intelligence_engine_combines_market_risk_and_signals() {
        val candles = (1L..25L).map { i ->
            Candle(i, 100.0, 101.0, 99.0, 100.0 + i * 0.5, 100.0)
        }
        val report = CoinIntelligenceEngine().analyze(
            CoinIntelligenceInput(
                snapshot = MemeCoinSnapshot("BTC", "BTCUSDT", 50_000_000.0, 1_000_000_000.0, 3000),
                candles = candles,
                whaleActivity = null
            )
        )
        assertEquals("BTCUSDT", report.market)
        assertTrue(report.overallScore in 0.0..100.0)
        assertTrue(report.components.isNotEmpty())
    }
}
