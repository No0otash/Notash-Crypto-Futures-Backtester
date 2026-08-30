package com.notash.cryptobacktester.intelligence

/** Provider-neutral on-chain whale layer. Real blockchain/indexer data must be supplied by a provider. */
interface OnChainWhaleProvider {
    suspend fun getLargeTransfers(asset: String, since: Long, until: Long): List<WhaleTransfer>
}

object UnavailableOnChainWhaleProvider : OnChainWhaleProvider {
    override suspend fun getLargeTransfers(asset: String, since: Long, until: Long): List<WhaleTransfer> = emptyList()
}

data class WhaleTransfer(
    val timestamp: Long,
    val asset: String,
    val amount: Double,
    val usdValue: Double,
    val direction: WhaleDirection,
    val source: String,
    val destination: String,
    val exchangeRelated: Boolean = false
)

enum class WhaleDirection { INFLOW, OUTFLOW, WALLET_TO_WALLET, UNKNOWN }
en
enum class SmartMoneyBias { ACCUMULATION, DISTRIBUTION, NEUTRAL, UNKNOWN }
en
enum class WhaleSeverity { LOW, MEDIUM, HIGH, EXTREME }

data class WhaleActivity(
    val asset: String,
    val fromTimestamp: Long,
    val toTimestamp: Long,
    val transfers: List<WhaleTransfer>,
    val totalUsdValue: Double,
    val inflowUsd: Double,
    val outflowUsd: Double,
    val score: Double,
    val severity: WhaleSeverity,
    val bias: SmartMoneyBias,
    val alert: Boolean,
    val dataAvailable: Boolean,
    val message: String
)

/** Aggregates real provider records and intentionally reports unavailable when no records exist. */
class WhaleSmartMoneyAnalyzer(
    private val minimumUsdValue: Double = 1_000_000.0,
    private val highUsdValue: Double = 10_000_000.0,
    private val extremeUsdValue: Double = 50_000_000.0
) {
    fun analyze(asset: String, transfers: List<WhaleTransfer>, from: Long, until: Long): WhaleActivity {
        val valid = transfers.filter {
            it.asset.equals(asset, ignoreCase = true) && it.timestamp in from..until && it.usdValue >= minimumUsdValue
        }
        if (valid.isEmpty()) {
            return WhaleActivity(
                asset = asset, fromTimestamp = from, toTimestamp = until, transfers = emptyList(),
                totalUsdValue = 0.0, inflowUsd = 0.0, outflowUsd = 0.0, score = 0.0,
                severity = WhaleSeverity.LOW, bias = SmartMoneyBias.UNKNOWN,
                alert = false, dataAvailable = false,
                message = "On-chain whale data is unavailable for this asset/time window"
            )
        }
        val inflow = valid.filter { it.direction == WhaleDirection.INFLOW }.sumOf { it.usdValue }
        val outflow = valid.filter { it.direction == WhaleDirection.OUTFLOW }.sumOf { it.usdValue }
        val total = valid.sumOf { it.usdValue }
        val score = (total / highUsdValue * 100.0).coerceIn(0.0, 100.0)
        val severity = when {
            total >= extremeUsdValue -> WhaleSeverity.EXTREME
            total >= highUsdValue -> WhaleSeverity.HIGH
            total >= minimumUsdValue * 3.0 -> WhaleSeverity.MEDIUM
            else -> WhaleSeverity.LOW
        }
        val bias = when {
            inflow > outflow * 1.25 -> SmartMoneyBias.ACCUMULATION
            outflow > inflow * 1.25 -> SmartMoneyBias.DISTRIBUTION
            else -> SmartMoneyBias.NEUTRAL
        }
        return WhaleActivity(
            asset = asset, fromTimestamp = from, toTimestamp = until, transfers = valid,
            totalUsdValue = total, inflowUsd = inflow, outflowUsd = outflow, score = score,
            severity = severity, bias = bias, alert = severity >= WhaleSeverity.HIGH,
            dataAvailable = true,
            message = "Real provider data aggregated; bias is directional, not a prediction"
        )
    }
}
