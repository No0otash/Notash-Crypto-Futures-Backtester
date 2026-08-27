package com.notash.cryptobacktester.strategy

import org.junit.Assert.assertEquals
import org.junit.Test

class IndicatorCalculatorTest {
    @Test fun smaUsesOnlyClosedWindow() {
        val result = IndicatorCalculator.value(IndicatorType.SMA, listOf(1.0, 2.0, 3.0, 4.0), 3)
        assertEquals(null, result[1])
        assertEquals(2.0, result[2]!!, 0.000001)
        assertEquals(3.0, result[3]!!, 0.000001)
    }

    @Test fun emaStartsAtSeedAverage() {
        val result = IndicatorCalculator.value(IndicatorType.EMA, listOf(1.0, 2.0, 3.0, 4.0), 3)
        assertEquals(2.0, result[2]!!, 0.000001)
        assertEquals(3.0, result[3]!!, 0.000001)
    }

    @Test fun wmaWeightsRecentValuesMoreHeavily() {
        val result = IndicatorCalculator.value(IndicatorType.WMA, listOf(1.0, 2.0, 3.0), 3)
        assertEquals((1.0 + 4.0 + 9.0) / 6.0, result[2]!!, 0.000001)
    }
}
