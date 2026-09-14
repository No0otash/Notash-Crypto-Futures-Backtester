package com.notash.cryptobacktester.strategy

import com.notash.cryptobacktester.robot.AlvexRobotPackage
import com.notash.cryptobacktester.robot.AlvexRobotStrategy

object StrategyFactory {
    private val importedRobots = LinkedHashMap<String, AlvexRobotPackage>()

    fun createDefaultRegistry(): StrategyRegistry {
        val registry = StrategyRegistry()
        registry.register(AdvancedPullbackStrategy())
        importedRobots.values.forEach { registry.register(AlvexRobotStrategy(it)) }
        return registry
    }

    fun registerImportedRobot(robot: AlvexRobotPackage) {
        importedRobots[robot.id] = robot
    }

    fun removeImportedRobot(id: String) {
        importedRobots.remove(id)
    }

    fun create(strategyId: String): Strategy? = when (strategyId) {
        "advanced_pullback_v1" -> AdvancedPullbackStrategy()
        else -> importedRobots[strategyId]?.let(::AlvexRobotStrategy)
    }
}
