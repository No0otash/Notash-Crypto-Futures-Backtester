package com.notash.cryptobacktester.strategy

class StrategyRegistry {

    private val strategies =
        LinkedHashMap<String, Strategy>()

    fun register(strategy: Strategy) {
        strategies[strategy.id] = strategy
    }

    fun unregister(id: String) {
        strategies.remove(id)
    }

    fun get(id: String): Strategy? {
        return strategies[id]
    }

    fun all(): List<Strategy> {
        return strategies.values.toList()
    }

    fun ids(): List<String> {
        return strategies.keys.toList()
    }

    fun contains(id: String): Boolean {
        return strategies.containsKey(id)
    }

    fun clear() {
        strategies.clear()
    }

    fun count(): Int {
        return strategies.size
    }
}
