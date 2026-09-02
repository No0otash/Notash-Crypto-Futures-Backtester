package com.notash.cryptobacktester.data

import kotlinx.serialization.json.Json

/** Validates and imports the supported ALVEX StrategyPackage JSON format. */
class StrategyPackageImporter(
    private val json: Json = Json { ignoreUnknownKeys = false; isLenient = false }
) {
    fun importJson(content: String): Result<StrategyPackage> = runCatching {
        require(content.isNotBlank()) { "Strategy package is empty" }
        val packageData = json.decodeFromString<StrategyPackage>(content)
        val errors = StrategyValidator.validate(packageData)
        require(errors.isEmpty()) { errors.joinToString("; ") }
        packageData
    }
}
