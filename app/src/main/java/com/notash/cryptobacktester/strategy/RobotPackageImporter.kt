package com.notash.cryptobacktester.strategy

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class RobotPackageImporter {
    private val parser = Json { ignoreUnknownKeys = true; isLenient = false }

    fun importJson(json: String): Result<RobotPackage> = runCatching {
        val root = parser.parseToJsonElement(json).jsonObject
        val rulesJson = root["rules"]?.jsonArray ?: JsonArray(emptyList())
        val rules = rulesJson.mapIndexed { i, element ->
            val r = element.jsonObject
            RobotRule(
                field = r.requiredString("field", "rule ${i + 1}"),
                operator = r.requiredString("operator", "rule ${i + 1}"),
                value = r["value"]?.jsonPrimitive?.doubleOrNull
                    ?: throw IllegalArgumentException("Rule ${i + 1}: value is required and must be numeric"),
                action = r.requiredString("action", "rule ${i + 1}")
            )
        }
        val parametersJson = root["parameters"]?.jsonObject ?: JsonObject(emptyMap())
        val parameters = parametersJson.mapValues { (key, value) ->
            value.jsonPrimitive.doubleOrNull
                ?: throw IllegalArgumentException("Parameter $key must be numeric")
        }
        RobotPackage(
            schemaVersion = root["schemaVersion"]?.jsonPrimitive?.intOrNull ?: 1,
            id = root.requiredString("id", "robot"),
            name = root.requiredString("name", "robot"),
            version = root.string("version", "1.0.0"),
            description = root.string("description", ""),
            parameters = parameters,
            rules = rules,
            sourceLanguage = root.stringOrNull("sourceLanguage"),
            sourceCode = root.stringOrNull("sourceCode")
        ).also { robot ->
            val validation = RobotPackageValidator.validate(robot)
            require(validation.valid) { validation.errors.joinToString("; ") }
        }
    }

    private fun JsonObject.requiredString(key: String, context: String): String =
        stringOrNull(key)?.trim()?.takeIf { it.isNotEmpty() }
            ?: throw IllegalArgumentException("$context: $key is required")

    private fun JsonObject.string(key: String, default: String): String = stringOrNull(key) ?: default

    private fun JsonObject.stringOrNull(key: String): String? =
        this[key]?.jsonPrimitive?.contentOrNull

    private val kotlinx.serialization.json.JsonPrimitive.contentOrNull: String?
        get() = content
}
