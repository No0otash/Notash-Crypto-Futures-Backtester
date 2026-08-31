package com.notash.cryptobacktester.intelligence

/**
 * Production contracts shared by ALVEX intelligence surfaces.
 * No contract is allowed to fabricate data: unavailable fields stay null.
 */
enum class IntelligenceDirection { PUMP, DUMP, NEUTRAL }
enum class WhaleFlow { EXCHANGE_INFLOW, EXCHANGE_OUTFLOW, WALLET_TO_WALLET, UNKNOWN }

data class IntelligenceEvidence(
    val source: String,
    val signal: String,
    val observedAtEpochMs: Long,
    val confidence: Double? = null
)

data class IntelligenceSnapshot(
    val symbol: String,
    val direction: IntelligenceDirection,
    val score: Int,
    val confidence: Double?,
    val observedAtEpochMs: Long,
    val evidence: List<IntelligenceEvidence>,
    val dataGaps: List<String> = emptyList()
)

data class WhaleEvent(
    val asset: String?,
    val amount: Double?,
    val source: String,
    val flow: WhaleFlow,
    val exchange: String?,
    val observedAtEpochMs: Long,
    val rawMessage: String,
    val evidence: List<IntelligenceEvidence> = emptyList()
)

/** A provider boundary keeps Telegram, market APIs and future providers replaceable. */
interface WhaleIntelligenceProvider {
    suspend fun latestEvents(limit: Int = 50): List<WhaleEvent>
}

/** HuntFlo transport feeds this parser; price-only posts are deliberately ignored. */
object HuntFloMessageClassifier {
    private val asset = Regex("\\b([A-Z0-9]{2,12})(?:USDT|USD|USDC)?\\b")
    private val amount = Regex("(?i)([0-9][0-9,]*(?:\\.[0-9]+)?)\\s*(BTC|ETH|USDT|USDC|USD)")
    private val exchange = Regex("(?i)\\b(Binance|Coinbase|OKX|Bybit|KuCoin|Kraken)\\b")

    fun parse(message: String, observedAtEpochMs: Long): WhaleEvent? {
        val text = message.trim()
        if (text.isEmpty()) return null
        val lower = text.lowercase()
        val whaleSignal = listOf("whale", "transfer", "moved", "deposit", "withdraw", "inflow", "outflow", "wallet")
            .any(lower::contains)
        if (!whaleSignal || isPriceOnly(lower)) return null

        val flow = when {
            listOf("inflow", "deposit", "to binance", "to coinbase", "to okx", "to bybit").any(lower::contains) -> WhaleFlow.EXCHANGE_INFLOW
            listOf("outflow", "withdraw", "from binance", "from coinbase", "from okx", "from bybit").any(lower::contains) -> WhaleFlow.EXCHANGE_OUTFLOW
            listOf("wallet to wallet", "wallet-to-wallet", "transferred between wallets").any(lower::contains) -> WhaleFlow.WALLET_TO_WALLET
            else -> WhaleFlow.UNKNOWN
        }
        val amountMatch = amount.find(text)
        val parsedAmount = amountMatch?.groupValues?.getOrNull(1)?.replace(",", "")?.toDoubleOrNull()
        val assetValue = amountMatch?.groupValues?.getOrNull(2)
            ?: asset.find(text)?.groupValues?.getOrNull(1)
        val exchangeValue = exchange.find(text)?.groupValues?.getOrNull(1)
        return WhaleEvent(
            asset = assetValue,
            amount = parsedAmount,
            source = "HuntFlo",
            flow = flow,
            exchange = exchangeValue,
            observedAtEpochMs = observedAtEpochMs,
            rawMessage = text,
            evidence = listOf(IntelligenceEvidence("HuntFlo", "whale-message", observedAtEpochMs))
        )
    }

    private fun isPriceOnly(lower: String): Boolean {
        val priceTerms = listOf("price", "trading at", "24h", "market price", "usd price")
        val actionTerms = listOf("whale", "transfer", "wallet", "deposit", "withdraw", "inflow", "outflow")
        return priceTerms.any(lower::contains) && actionTerms.none(lower::contains)
    }
}

object AlvexSignalScorer {
    fun score(changePercent: Double, volumeRatio: Double?, volatilityRatio: Double?): IntelligenceSnapshot {
        val evidence = mutableListOf<IntelligenceEvidence>()
        var raw = 0.0
        if (changePercent > 0) { raw += minOf(changePercent * 4.0, 40.0); evidence += IntelligenceEvidence("market", "positive-price-momentum", 0L) }
        if (changePercent < 0) { raw += minOf(-changePercent * 4.0, 40.0); evidence += IntelligenceEvidence("market", "negative-price-momentum", 0L) }
        volumeRatio?.let { if (it > 1.5) { raw += minOf((it - 1.0) * 20.0, 30.0); evidence += IntelligenceEvidence("market", "volume-anomaly", 0L) } }
        volatilityRatio?.let { if (it > 1.25) { raw += minOf((it - 1.0) * 20.0, 20.0); evidence += IntelligenceEvidence("market", "volatility-anomaly", 0L) } }
        val direction = when {
            changePercent >= 0.5 -> IntelligenceDirection.PUMP
            changePercent <= -0.5 -> IntelligenceDirection.DUMP
            else -> IntelligenceDirection.NEUTRAL
        }
        val score = raw.coerceIn(0.0, 100.0).toInt()
        val confidence = if (evidence.isEmpty()) null else (0.35 + evidence.size * 0.15).coerceAtMost(0.95)
        return IntelligenceSnapshot("", direction, score, confidence, 0L, evidence)
    }
}
