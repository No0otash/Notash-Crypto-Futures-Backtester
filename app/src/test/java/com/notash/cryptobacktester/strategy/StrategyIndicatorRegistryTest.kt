package com.notash.cryptobacktester.strategy

import org.junit.Assert.assertEquals
import org.junit.Test

class StrategyIndicatorRegistryTest {
    @Test fun registryUsesConfiguredPeriodsAndTypes() {
        val specs = StrategyIndicatorRegistry.fromPeriods(20, 50, IndicatorType.EMA, IndicatorType.LWMA)
        assertEquals(2, specs.size)
        assertEquals(IndicatorType.EMA, specs[0].type)
        assertEquals(20, specs[0].period)
        assertEquals(IndicatorType.LWMA, specs[1].type)
        assertEquals(50, specs[1].period)
    }

    @Test fun duplicatePeriodsDoNotCreateDuplicateLines() {
        assertEquals(1, StrategyIndicatorRegistry.fromPeriods(20, 20).size)
    }
}
