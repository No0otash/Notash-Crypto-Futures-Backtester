package com.notash.cryptobacktester.strategy

/**
 * Converts a strategy's declared indicator configuration into chart-ready specs.
 * The chart must consume this registry rather than maintaining a second indicator list.
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
