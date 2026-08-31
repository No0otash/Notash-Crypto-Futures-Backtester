package com.notash.cryptobacktester.intelligence

/**
 * Parses HuntFlo whale/news messages into the provider-neutral whale model.
 * Price-only messages are intentionally ignored; market prices belong to the market-data provider.
 */
class HuntFloMessageParser {
    private val assetRegex = Regex("\\b([A-Z]{2,10})(?:USDT|USD)?\\b")
    private val usdRegex = Regex("\\$\\s*([0-9]+(?:[.,][0-9]+)?)\\s*([KMB])?", RegexOption.IGNORE_CASE)
    private val amountRegex = Regex("(?:transfer|moved|sent|withdrew|deposited)\\s+([0-9]+(?:[.,][0-9]+)?)\\s+([A-Z]{2,10})\\b", RegexOption.IGNORE_CASE)

    fun parse(message: String): WhaleTransfer? {
        val normalized = message.trim()
        if (normalized.isEmpty() || !containsWhaleSignal(normalized)) return null

        val amountMatch = amountRegex.find(normalized)
        val asset = amountMatch?.groupValues?.getOrNull(2)?.uppercase()
            ?: assetRegex.find(normalized)?.groupValues?.getOrNull(1)?.uppercase()
            ?: return null

        val amount = amountMatch?.groupValues?.getOrNull(1)?.replace(",", "")?.toDoubleOrNull() ?: 0.0
        val usdValue = usdRegex.find(normalized)?.let { parseUsd(it.groupValues[1], it.groupValues.getOrNull(2)) } ?: 0.0
        if (amount <= 0.0 && usdValue <= 0.0) return null

        val lower = normalized.lowercase()
        val direction = when {
            (lower.contains("to binance") || lower.contains("to coinbase") || lower.contains("to okx") || lower.contains("to bybit") || lower.contains("exchange inflow")) -> WhaleDirection.INFLOW
            (lower.contains("from binance") || lower.contains("from coinbase") || lower.contains("from okx") || lower.contains("from bybit") || lower.contains("exchange outflow")) -> WhaleDirection.OUTFLOW
            lower.contains("from") && lower.contains("to") -> WhaleDirection.WALLET_TO_WALLET
            else -> WhaleDirection.UNKNOWN
        }

        val exchangeRelated = lower.contains("binance") || lower.contains("coinbase") ||
            lower.contains("okx") || lower.contains("bybit") || lower.contains("kraken")

        return WhaleTransfer(
            timestamp = System.currentTimeMillis(),
            asset = asset,
            amount = amount,
            usdValue = usdValue,
            direction = direction,
            source = "HuntFlo",
            destination = "HuntFlo",
            exchangeRelated = exchangeRelated
        )
    }

    private fun containsWhaleSignal(text: String): Boolean {
        val lower = text.lowercase()
        return listOf("whale", "large transfer", "whale transfer", "moved", "sent", "deposited", "withdrew", "exchange inflow", "exchange outflow")
            .any(lower::contains)
    }

    private fun parseUsd(number: String, suffix: String?): Double {
        val base = number.replace(",", "").toDoubleOrNull() ?: return 0.0
        return when (suffix?.uppercase()) {
            "K" -> base * 1_000.0
            "M" -> base * 1_000_000.0
            "B" -> base * 1_000_000_000.0
            else -> base
        }
    }
}
