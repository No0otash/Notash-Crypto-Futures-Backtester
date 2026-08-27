package com.notash.cryptobacktester.core

enum class AiHubMode { CHAT, LEARN, TRADE_ANALYST, STRATEGY_ANALYST, COIN_RESEARCH, MARKET_RESEARCH }

data class AiHubMessage(val role: String, val content: String, val timestamp: Long)

data class AiHubSession(
    val id: String,
    val title: String,
    val mode: AiHubMode = AiHubMode.CHAT,
    val messages: List<AiHubMessage> = emptyList()
)

data class AiHubRequest(
    val question: String,
    val mode: AiHubMode = AiHubMode.CHAT,
    val includeMarketContext: Boolean = false,
    val includeStrategyContext: Boolean = false,
    val includeTradeContext: Boolean = false,
    val includeTokenContext: Boolean = false
)

data class AiHubResponse(
    val answer: String,
    val confidence: Int,
    val evidence: List<String> = emptyList(),
    val limitations: List<String> = emptyList()
)
