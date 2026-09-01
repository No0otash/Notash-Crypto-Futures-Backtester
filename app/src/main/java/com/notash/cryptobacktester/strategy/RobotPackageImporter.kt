package com.notash.cryptobacktester.strategy

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class RobotPackageImporter {
    private val json = Json { ignoreUnknownKeys = true }

    fun importJson(input: String): Result<RobotPackage> = runCatching {
        val root = json.parseToJsonElement(input).jsonObject
        val rules = root.arrayObjects("rules").map { r ->
            RobotRule(
                field = r.string("field"),
                operator = r.string("operator"),
                value = r.double("value"),
                action = r.string("action")
            )
        }
        val parameters = root.object("parameters")?.mapValues { (_, value) -> value.asDouble() } ?: emptyMap()

        RobotPackage(
            schemaVersion = root.int("schemaVersion", 1),
            id = root.string("id"),
            name = root.string("name"),
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

    private fun JsonObject.string(key: String, default: String? = null): String =
        get(key)?.asString() ?: default ?: error("Missing required field: $key")

    private fun JsonObject.stringOrNull(key: String): String? = get(key)?.asString()

    private fun JsonObject.int(key: String, default: Int): Int = get(key)?.asDouble()?.toInt() ?: default
    private fun JsonObject.double(key: String): Double = get(key)?.asDouble() ?: error("Missing required field: $key")
    private fun JsonObject.object(key: String): JsonObject? = get(key)?.jsonObject

    private fun JsonObject.arrayObjects(key: String): List<JsonObject> =
        get(key)?.jsonArray?.map { it.jsonObject } ?: emptyList()

    private fun JsonElement.asString(): String =
        (this as? JsonPrimitive)?.contentOrNull ?: error("Expected string value")

    private fun JsonElement.asDouble(): Double =
        (this as? JsonPrimitive)?.doubleOrNull ?: error("Expected numeric value")
}
