package com.notash.cryptobacktester.strategy

class StrategyRegistry {

    private val strategies =
        LinkedHashMap<String, Strategy>()

    fun register(strategy: Strategy) {
        strategies[strategy.id] = strategy
    }

    fun remove(id: String) {
        strategies.remove(id)
    }

    fun get(id: String): Strategy? {
        return strategies[id]
    }

    fun all(): List<Strategy> {
        return strategies.values.toList()
    }

    fun clear() {
        strategies.clear()
    }
}
