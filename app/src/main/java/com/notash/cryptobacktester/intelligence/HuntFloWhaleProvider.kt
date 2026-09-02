package com.notash.cryptobacktester.intelligence

/**
 * Source adapter for HuntFlo. The transport is injected so the domain layer stays
 * independent from Telegram/network details and can be tested deterministically.
 */
fun interface HuntFloMessageSource {
    suspend fun messages(from: Long, until: Long): List<String>
}

class HuntFloWhaleProvider(
    private val source: HuntFloMessageSource,
    private val parser: HuntFloMessageParser = HuntFloMessageParser()
) : OnChainWhaleProvider {
    override suspend fun getLargeTransfers(asset: String, since: Long, until: Long): List<WhaleTransfer> {
        return source.messages(since, until).mapNotNull(parser::parse)
            .filter { it.asset.equals(asset, ignoreCase = true) && it.timestamp in since..until }
    }
}
