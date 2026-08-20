package com.notash.cryptobacktester.strategy

object StrategyFactory {

    fun createDefaultRegistry(): StrategyRegistry {

        val registry =
            StrategyRegistry()

        registry.register(
            AdvancedPullbackStrategy()
        )

        return registry
    }

    fun create(
        strategyId: String
    ): Strategy? {

        return when (strategyId) {

            "advanced_pullback_v1" ->
                AdvancedPullbackStrategy()

            else ->
                null
        }
    }
}
