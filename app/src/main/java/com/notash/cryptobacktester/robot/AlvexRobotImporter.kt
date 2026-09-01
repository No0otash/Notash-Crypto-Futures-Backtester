package com.notash.cryptobacktester.robot

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object AlvexRobotImporter {
    private val parser = Json { ignoreUnknownKeys = true; isLenient = false }

    fun fromJson(json: String): AlvexRobotPackage {
        val root = parser.parseToJsonElement(json).jsonObject
        val schemaVersion = root["schemaVersion"]?.jsonPrimitive?.intOrNull ?: 1
        require(schemaVersion == 1) { "Unsupported robot schemaVersion: $schemaVersion" }

        val id = root.requiredString("id")
        val name = root.requiredString("name")
        val parameters = root["parameters"]?.jsonObject ?: JsonObject(emptyMap())
        val rules = root["rules"]?.jsonObject ?: JsonObject(emptyMap())

        val robotParameters = RobotParameters(
            leverage = parameters.number("leverage", 1.0),
            riskPercent = parameters.number("riskPercent", 1.0),
            stopLossPercent = parameters.number("stopLossPercent", 1.0),
            takeProfitPercent = parameters.number("takeProfitPercent", 2.0)
        )
        require(robotParameters.leverage > 0.0) { "leverage must be > 0" }
        require(robotParameters.riskPercent > 0.0) { "riskPercent must be > 0" }
        require(robotParameters.stopLossPercent > 0.0) { "stopLossPercent must be > 0" }
        require(robotParameters.takeProfitPercent > 0.0) { "takeProfitPercent must be > 0" }

        return AlvexRobotPackage(
            id = id,
            name = name,
            version = root.string("version", "1.0.0"),
            description = root.string("description", ""),
            parameters = robotParameters,
            rules = RobotRules(
                longWhenCloseAboveOpen = rules.boolean("longWhenCloseAboveOpen", true),
                shortWhenCloseBelowOpen = rules.boolean("shortWhenCloseBelowOpen", true)
            )
        )
    }

    private fun JsonObject.requiredString(key: String): String = string(key, "").also {
        require(it.isNotBlank()) { "$key is required" }
    }

    private fun JsonObject.string(key: String, default: String): String =
        this[key]?.jsonPrimitive?.contentOrNull ?: default

    private fun JsonObject.number(key: String, default: Double): Double =
        this[key]?.jsonPrimitive?.doubleOrNull ?: default

    private fun JsonObject.boolean(key: String, default: Boolean): Boolean =
        this[key]?.jsonPrimitive?.booleanOrNull ?: default

    private val kotlinx.serialization.json.JsonPrimitive.contentOrNull: String?
        get() = if (isString) content else content
}
