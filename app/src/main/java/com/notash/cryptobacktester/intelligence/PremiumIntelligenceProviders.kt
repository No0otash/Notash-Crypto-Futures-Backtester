package com.notash.cryptobacktester.intelligence

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class MoralisEvmHolderProvider(
    private val apiKey: String?,
    private val client: OkHttpClient = OkHttpClient.Builder().connectTimeout(8, TimeUnit.SECONDS).readTimeout(12, TimeUnit.SECONDS).build()
) : DeepOnChainProvider {
    override suspend fun load(chain: String, contractOrMint: String): DeepOnChainResult = withContext(Dispatchers.IO) {
        if (apiKey.isNullOrBlank()) return@withContext DeepOnChainResult(HolderSnapshot(), ProviderHealth("Moralis", false, error = "MORALIS_API_KEY_NOT_CONFIGURED"), listOf("EVM_HOLDER_CONCENTRATION_REQUIRES_MORALIS_KEY"))
        try {
            val chainCode = when (chain.lowercase()) {
                "ethereum", "eth" -> "eth"; "polygon", "matic" -> "polygon"; "bsc", "bnb" -> "bsc"; "arbitrum" -> "arbitrum"; "optimism" -> "optimism"; "base" -> "base"; "avalanche" -> "avalanche"
                else -> return@withContext DeepOnChainResult(HolderSnapshot(), ProviderHealth("Moralis", false, error = "UNSUPPORTED_EVM_CHAIN"), listOf("UNSUPPORTED_EVM_CHAIN"))
            }
            val url = "https://deep-index.moralis.io/api/v2.2/erc20/$contractOrMint/owners?chain=$chainCode&order=DESC&limit=100"
            val request = Request.Builder().url(url).header("accept", "application/json").header("X-API-Key", apiKey).get().build()
            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) error("HTTP ${response.code}: ${body.take(180)}")
                val rows = JSONObject(body).optJSONArray("result") ?: JSONArray()
                val percentages = (0 until rows.length()).mapNotNull { i -> rows.optJSONObject(i)?.optDouble("percentage_relative_to_total_supply")?.takeIf { it.isFinite() && it >= 0.0 } }
                val labeled = (0 until rows.length()).mapNotNull { i -> rows.optJSONObject(i)?.optString("owner_address_label")?.takeIf { it.isNotBlank() } }
                val snapshot = HolderSnapshot(
                    top10Percent = percentages.take(10).sum().takeIf { it > 0.0 }, top20Percent = percentages.take(20).sum().takeIf { it > 0.0 },
                    largestHolderPercent = percentages.firstOrNull(), exchangeWallets = labeled.filter { it.contains("exchange", true) || it.contains("binance", true) || it.contains("coinbase", true) || it.contains("bybit", true) },
                    distribution = if (percentages.isNotEmpty()) "MORALIS_TOP_HOLDERS" else "Unknown", source = SourceRef("Moralis Token Holder API", url, status = SourceStatus.CONFIRMED)
                )
                DeepOnChainResult(snapshot, ProviderHealth("Moralis", true, 200), if (percentages.isEmpty()) listOf("HOLDER_PERCENTAGES_UNAVAILABLE") else emptyList())
            }
        } catch (e: Exception) {
            DeepOnChainResult(HolderSnapshot(), ProviderHealth("Moralis", false, error = e.message ?: e::class.simpleName), listOf("MORALIS_REQUEST_FAILED"))
        }
    }
}

class TokenomistUnlockProvider(
    private val apiKey: String?,
    private val client: OkHttpClient = OkHttpClient.Builder().connectTimeout(8, TimeUnit.SECONDS).readTimeout(12, TimeUnit.SECONDS).build()
) {
    suspend fun load(tokenId: String): Pair<List<UnlockEvent>, ProviderHealth> = withContext(Dispatchers.IO) {
        if (apiKey.isNullOrBlank()) return@withContext emptyList<UnlockEvent>() to ProviderHealth("Tokenomist", false, error = "TOKENOMIST_API_KEY_NOT_CONFIGURED")
        try {
            val url = "https://api.tokenomist.ai/v4/unlock/events?tokenId=$tokenId"
            val request = Request.Builder().url(url).header("x-api-key", apiKey).get().build()
            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) error("HTTP ${response.code}: ${body.take(180)}")
                val rows = JSONObject(body).optJSONArray("data") ?: JSONArray()
                val events = (0 until rows.length()).mapNotNull { i ->
                    val row = rows.optJSONObject(i) ?: return@mapNotNull null
                    val date = row.optString("unlockDate").takeIf { it.isNotBlank() } ?: return@mapNotNull null
                    val cliff = row.optJSONObject("cliffUnlocks")
                    val linear = row.optJSONObject("linearUnlocks")
                    val amount = cliff?.optDouble("cliffAmount")?.takeIf { it.isFinite() } ?: linear?.optDouble("linearAmount")?.takeIf { it.isFinite() }
                    UnlockEvent(date, amount, category = cliff?.optJSONArray("allocationBreakdown")?.optJSONObject(0)?.optString("standardAllocationName") ?: "Unknown", cliff = cliff?.optString("cliffAmount"), source = SourceRef("Tokenomist", url, status = SourceStatus.CONFIRMED))
                }
                events to ProviderHealth("Tokenomist", true, 200)
            }
        } catch (e: Exception) {
            emptyList<UnlockEvent>() to ProviderHealth("Tokenomist", false, error = e.message ?: e::class.simpleName)
        }
    }
}
