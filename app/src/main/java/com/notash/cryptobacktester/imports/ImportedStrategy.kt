package com.notash.cryptobacktester.imports

import com.notash.cryptobacktester.core.BacktestConfig
import com.notash.cryptobacktester.data.StrategyPackage
import com.notash.cryptobacktester.strategy.AdvancedPullbackStrategy
import com.notash.cryptobacktester.strategy.Strategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.Locale

/** Imported strategies are configuration packages only; arbitrary source code is never executed. */
data class ImportedStrategy(val packageData: StrategyPackage, val config: BacktestConfig, val warnings: List<String> = emptyList())

object StrategyImportParser {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun parse(text: String, filename: String): ImportedStrategy {
        val trimmed = text.trim()
        require(trimmed.isNotEmpty()) { "فایل خالی است / File is empty" }
        return when {
            filename.lowercase(Locale.US).endsWith(".json") || trimmed.startsWith("{") -> parseJson(trimmed)
            filename.lowercase(Locale.US).endsWith(".csv") -> parseCsv(trimmed)
            else -> parseKeyValue(trimmed)
        }
    }

    private fun parseJson(text: String): ImportedStrategy {
        val packageData = json.decodeFromString<StrategyPackage>(text)
        require(validate(packageData).isEmpty()) { validate(packageData).joinToString("\n") }
        return ImportedStrategy(packageData, configFrom(json.parseToJsonElement(text).jsonObject, packageData.riskPercent))
    }

    private fun parseCsv(text: String): ImportedStrategy {
        val rows = text.lines().filter { it.isNotBlank() }.map { it.split(',').map(String::trim) }
        require(rows.size >= 2 && rows[0].size == rows[1].size) { "CSV باید یک ردیف عنوان و یک ردیف مقدار داشته باشد." }
        return fromMap(rows[0].zip(rows[1]).toMap())
    }

    private fun parseKeyValue(text: String): ImportedStrategy {
        val map = text.lines().mapNotNull { line ->
            val p = line.indexOf('=').takeIf { it > 0 } ?: line.indexOf(':').takeIf { it > 0 }
            p?.let { line.substring(0, it).trim() to line.substring(it + 1).trim() }
        }.toMap()
        require(map.isNotEmpty()) { "فرمت فایل قابل تشخیص نیست. از JSON، CSV یا key=value استفاده کنید." }
        return fromMap(map)
    }

    private fun fromMap(map: Map<String, String>): ImportedStrategy {
        fun value(vararg keys: String) = keys.firstNotNullOfOrNull { key -> map.entries.firstOrNull { it.key.equals(key, true) }?.value }
        val p = StrategyPackage(
            id = value("id", "strategyId") ?: "imported_${System.currentTimeMillis()}",
            name = value("name", "strategyName") ?: "Imported Strategy",
            version = value("version") ?: "1.0",
            symbol = value("symbol", "market") ?: "BTCUSDT",
            timeframe = value("timeframe", "period") ?: "5m",
            entryRules = value("entryRules")?.split('|', ';').orEmpty(),
            exitRules = value("exitRules")?.split('|', ';').orEmpty(),
            riskPercent = value("riskPercent", "risk")?.toDoubleOrNull() ?: 1.0
        )
        require(validate(p).isEmpty()) { validate(p).joinToString("\n") }
        return ImportedStrategy(p, configFromMap(map, p.riskPercent))
    }

    private fun configFrom(root: Map<String, JsonElement>, risk: Double): BacktestConfig {
        fun d(key: String, fallback: Double) = root[key]?.jsonPrimitive?.doubleOrNull ?: fallback
        fun i(key: String, fallback: Int) = root[key]?.jsonPrimitive?.intOrNull ?: fallback
        return BacktestConfig(riskPercent = d("riskPercent", risk), leverage = d("leverage", 10.0), makerFee = d("makerFee", 0.0002), takerFee = d("takerFee", 0.0005), slippageBps = d("slippageBps", 2.0), fastLwma = i("fastLwma", 20), slowLwma = i("slowLwma", 50), atrPeriod = i("atrPeriod", 14), entryAtr = d("entryAtr", 0.5), stopAtr = d("stopAtr", 1.5), takeProfitAtr = d("takeProfitAtr", 3.0), useFunding = root["useFunding"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: true)
    }

    private fun configFromMap(map: Map<String, String>, risk: Double): BacktestConfig {
        fun d(vararg keys: String, fallback: Double) = keys.firstNotNullOfOrNull { k -> map.entries.firstOrNull { it.key.equals(k, true) }?.value?.toDoubleOrNull() } ?: fallback
        fun i(vararg keys: String, fallback: Int) = keys.firstNotNullOfOrNull { k -> map.entries.firstOrNull { it.key.equals(k, true) }?.value?.toIntOrNull() } ?: fallback
        return BacktestConfig(riskPercent = d("riskPercent", "risk", fallback = risk), leverage = d("leverage", fallback = 10.0), makerFee = d("makerFee", fallback = 0.0002), takerFee = d("takerFee", fallback = 0.0005), slippageBps = d("slippageBps", fallback = 2.0), fastLwma = i("fastLwma", fallback = 20), slowLwma = i("slowLwma", fallback = 50), atrPeriod = i("atrPeriod", fallback = 14), entryAtr = d("entryAtr", fallback = 0.5), stopAtr = d("stopAtr", fallback = 1.5), takeProfitAtr = d("takeProfitAtr", fallback = 3.0))
    }

    private fun validate(p: StrategyPackage): List<String> = buildList {
        if (p.id.isBlank()) add("شناسه استراتژی خالی است")
        if (p.name.isBlank()) add("نام استراتژی خالی است")
        if (p.riskPercent <= 0.0 || p.riskPercent > 100.0) add("Risk باید بین 0 و 100 باشد")
    }
}

object ImportedStrategyStore {
    private val imported = linkedMapOf<String, ImportedStrategy>()
    var active: ImportedStrategy? = null
        private set
    fun register(value: ImportedStrategy) { imported[value.packageData.id] = value; active = value }
    fun get(id: String): ImportedStrategy? = imported[id]
    fun activeId(): String? = active?.packageData?.id
    fun strategy(id: String): Strategy? = imported[id]?.let { ImportedConfiguredStrategy(it) }
}

private class ImportedConfiguredStrategy(private val imported: ImportedStrategy) : Strategy {
    private val delegate = AdvancedPullbackStrategy()
    override val id = imported.packageData.id
    override val name = imported.packageData.name
    override val version = imported.packageData.version
    override val description = "Imported safe configuration: ${imported.packageData.name}"
    override fun generateSignal(index: Int, candles: List<com.notash.cryptobacktester.core.Candle>, funding: List<com.notash.cryptobacktester.core.FundingRate>, config: BacktestConfig) = delegate.generateSignal(index, candles, funding, imported.config)
}
