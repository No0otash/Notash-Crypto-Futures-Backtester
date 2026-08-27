package com.notash.cryptobacktester.strategy

/**
 * Single source of truth for indicators declared by a strategy.
 * UI/chart layers consume these specs instead of maintaining a second list.
 */
object StrategyIndicatorRegistry {
    fun fromPeriods(
        fastPeriod: Int,
        slowPeriod: Int,
        fastType: IndicatorType = IndicatorType.LWMA,
        slowType: IndicatorType = IndicatorType.LWMA
    ): List<IndicatorSpec> = buildList {
        if (fastPeriod > 0) add(IndicatorSpec("fast-$fastType-$fastPeriod", fastType, fastPeriod))
        if (slowPeriod > 0 && slowPeriod != fastPeriod) add(IndicatorSpec("slow-$slowType-$slowPeriod", slowType, slowPeriod))
    }

    fun calculate(spec: IndicatorSpec, closes: List<Double>): List<Double?> =
        IndicatorCalculator.value(spec.type, closes, spec.period)
}
