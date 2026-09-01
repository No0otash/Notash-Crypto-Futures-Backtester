package com.notash.cryptobacktester.robot

import org.json.JSONObject

object AlvexRobotImporter {
    fun fromJson(json: String): AlvexRobotPackage {
        val root = JSONObject(json)
        require(root.optInt("schemaVersion", 1) == 1) { "Unsupported robot schemaVersion" }
        val id = root.optString("id").trim()
        val name = root.optString("name").trim()
        require(id.isNotEmpty()) { "Robot id is required" }
        require(name.isNotEmpty()) { "Robot name is required" }
        val p = root.optJSONObject("parameters") ?: JSONObject()
        val r = root.optJSONObject("rules") ?: JSONObject()
        val parameters = RobotParameters(
            leverage = p.optDouble("leverage", 1.0),
            riskPercent = p.optDouble("riskPercent", 1.0),
            stopLossPercent = p.optDouble("stopLossPercent", 1.0),
            takeProfitPercent = p.optDouble("takeProfitPercent", 2.0)
        )
        require(parameters.leverage > 0) { "leverage must be > 0" }
        require(parameters.riskPercent > 0) { "riskPercent must be > 0" }
        require(parameters.stopLossPercent > 0) { "stopLossPercent must be > 0" }
        require(parameters.takeProfitPercent > 0) { "takeProfitPercent must be > 0" }
        return AlvexRobotPackage(
            id = id,
            name = name,
            version = root.optString("version", "1.0.0"),
            description = root.optString("description", ""),
            parameters = parameters,
            rules = RobotRules(
                longWhenCloseAboveOpen = r.optBoolean("longWhenCloseAboveOpen", true),
                shortWhenCloseBelowOpen = r.optBoolean("shortWhenCloseBelowOpen", true)
            )
        )
    }
}
