package com.notash.cryptobacktester.strategy

enum class StrategyType { ROBOT, MANUAL }
enum class StrategySource { MANUAL, JSON, ZIP }

data class ManagedStrategy(
    val id: String,
    val name: String,
    val version: String,
    val type: StrategyType,
    val entryRules: String,
    val exitRules: String,
    val riskRules: String,
    val timeframe: String,
    val tradeAmount: Double,
    val leverage: Double,
    val createdAt: Long,
    val source: StrategySource
) { val key: String get() = "$id@$version" }

data class StrategyManagerState(val history: List<ManagedStrategy>, val activeIdVersion: String?)

class StrategyManagerCore(initial: List<ManagedStrategy> = emptyList(), active: String? = null) {
    private var state = StrategyManagerState(initial.distinctBy { it.key }, active)
    fun state(): StrategyManagerState = state
    fun saveAsNewVersion(strategy: ManagedStrategy): StrategyManagerState {
        require(strategy.id.isNotBlank()) { "Strategy ID is required" }
        require(strategy.name.isNotBlank()) { "Strategy name is required" }
        require(strategy.version.isNotBlank()) { "Strategy version is required" }
        require(strategy.entryRules.isNotBlank()) { "Entry rules are required" }
        require(strategy.exitRules.isNotBlank()) { "Exit rules are required" }
        require(strategy.leverage > 0.0) { "Leverage must be greater than zero" }
        require(strategy.tradeAmount > 0.0) { "Trade amount must be greater than zero" }
        state = state.copy(history = state.history.filterNot { it.key == strategy.key } + strategy, activeIdVersion = strategy.key)
        return state
    }
    fun activate(id: String, version: String): StrategyManagerState {
        val key = "$id@$version"
        require(state.history.any { it.key == key }) { "Strategy version not found: $key" }
        state = state.copy(activeIdVersion = key)
        return state
    }
    fun canDelete(id: String, version: String, confirmed: Boolean): Boolean = confirmed && state.history.any { it.key == "$id@$version" }
    fun delete(id: String, version: String, confirmed: Boolean): StrategyManagerState {
        if (!canDelete(id, version, confirmed)) return state
        val key = "$id@$version"
        val remaining = state.history.filterNot { it.key == key }
        val nextActive = if (state.activeIdVersion == key) remaining.lastOrNull()?.key else state.activeIdVersion
        state = state.copy(history = remaining, activeIdVersion = nextActive)
        return state
    }
}
