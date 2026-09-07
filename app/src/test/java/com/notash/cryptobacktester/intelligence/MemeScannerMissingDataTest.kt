package com.notash.cryptobacktester.intelligence

import com.notash.cryptobacktester.core.Candle
import kotlin.test.Test
import kotlin.test.assertFalse

class MemeScannerMissingDataTest {
    @Test
    fun zero_liquidity_means_unknown_and_is_not_misreported_as_low_liquidity() {
        val candles = (1L..25L).map { i -> Candle(i, 1.0, 1.02, 0.98, 1.0, 1000.0) }
        val result = MemeShitcoinScanner().scan(
            MemeCoinSnapshot(
                symbol = "UNKNOWN",
                market = "UNKNOWNUSDT",
                liquidityUsd = 0.0,
                marketCapUsd = 0.0
            ),
            candles
        )
        assertFalse(result.flags.contains("LOW_LIQUIDITY"))
    }
}
