package com.notash.cryptobacktester.strategy

import org.json.JSONArray
import org.json.JSONObject

class RobotPackageImporter {
    fun importJson(json: String): Result<RobotPackage> = runCatching {
        val root = JSONObject(json)
        val rulesJson = root.optJSONArray("rules") ?: JSONArray()
        val rules = buildList {
            for (i in 0 until rulesJson.length()) {
                val r = rulesJson.getJSONObject(i)
                add(RobotRule(r.getString("field"), r.getString("operator"), r.getDouble("value"), r.getString("action")))
            }
        }
        val parameters = mutableMapOf<String, Double>()
        val p = root.optJSONObject("parameters")
        if (p != null) p.keys().forEach { key -> parameters[key] = p.getDouble(key) }
        RobotPackage(
            schemaVersion = root.optInt("schemaVersion", 1),
            id = root.getString("id"),
            name = root.getString("name"),
            version = root.optString("version", "1.0.0"),
            description = root.optString("description", ""),
            parameters = parameters,
            rules = rules,
            sourceLanguage = root.optString("sourceLanguage", null),
            sourceCode = root.optString("sourceCode", null)
        ).also { robot ->
            val validation = RobotPackageValidator.validate(robot)
            require(validation.valid) { validation.errors.joinToString("; ") }
        }
    }
}
