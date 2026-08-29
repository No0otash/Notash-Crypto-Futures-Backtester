package com.notash.cryptobacktester.strategy

import com.notash.cryptobacktester.imports.ImportedStrategyStore

object StrategyFactory {
    fun createDefaultRegistry(): StrategyRegistry {
        val registry = StrategyRegistry()
        registry.register(AdvancedPullbackStrategy())
        return registry
    }

    fun create(strategyId: String): Strategy? = when (strategyId) {
        "advanced_pullback_v1" -> AdvancedPullbackStrategy()
        else -> ImportedStrategyStore.strategy(strategyId)
    }
}
