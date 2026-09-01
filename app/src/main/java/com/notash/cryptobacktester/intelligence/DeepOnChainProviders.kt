package com.notash.cryptobacktester.intelligence

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class DeepOnChainResult(
    val snapshot: HolderSnapshot,
    val health: ProviderHealth,
    val gaps: List<String>
)

interface DeepOnChainProvider {
    suspend fun load(chain: String, contractOrMint: String): DeepOnChainResult
}

/** Real Solana holder-account distribution via the public mainnet RPC; no API key. */
class SolanaHolderProvider(
    private val rpcUrl: String = "https://api.mainnet-beta.solana.com",
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS).readTimeout(12, TimeUnit.SECONDS).callTimeout(15, TimeUnit.SECONDS).build()
) : DeepOnChainProvider {
    override suspend fun load(chain: String, contractOrMint: String): DeepOnChainResult = withContext(Dispatchers.IO) {
        if (!chain.equals("solana", true)) return@withContext DeepOnChainResult(
            HolderSnapshot(), ProviderHealth("SolanaRPC", false, error = "UNSUPPORTED_CHAIN"), listOf("SOLANA_PROVIDER_REQUIRES_SOLANA_CHAIN")
        )
        try {
            val result = rpc("getTokenLargestAccounts", JSONArray().put(contractOrMint))
                .optJSONObject("result")?.optJSONArray("value") ?: JSONArray()
            val amounts = (0 until result.length()).mapNotNull { i ->
                result.optJSONObject(i)?.optJSONObject("uiAmount")?.optDouble("uiAmount")
            }.filter { it.isFinite() && it > 0.0 }
            val total = amounts.sum()
            val shares = if (total > 0.0) amounts.map { it / total * 100.0 } else emptyList()
            val snapshot = HolderSnapshot(
                top10Percent = shares.take(10).sum().takeIf { it > 0.0 },
                top20Percent = shares.take(20).sum().takeIf { it > 0.0 },
                largestHolderPercent = shares.firstOrNull(),
                distribution = if (shares.isNotEmpty()) "TOP_TOKEN_ACCOUNTS" else "Unknown",
                source = SourceRef("Solana public RPC", rpcUrl, status = SourceStatus.CONFIRMED)
            )
            DeepOnChainResult(snapshot, ProviderHealth("SolanaRPC", true, 200), if (shares.isEmpty()) listOf("HOLDER_DISTRIBUTION_UNAVAILABLE") else emptyList())
        } catch (e: Exception) {
            DeepOnChainResult(HolderSnapshot(), ProviderHealth("SolanaRPC", false, error = e.message ?: e::class.simpleName), listOf("SOLANA_RPC_ERROR"))
        }
    }

    private fun rpc(method: String, params: JSONArray): JSONObject {
        val payload = JSONObject().put("jsonrpc", "2.0").put("id", 1).put("method", method).put("params", params)
        val body = okhttp3.RequestBody.create(okhttp3.MediaType.parse("application/json"), payload.toString())
        val request = Request.Builder().url(rpcUrl).header("Content-Type", "application/json").post(body).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("HTTP ${response.code}")
            val json = JSONObject(response.body?.string().orEmpty())
            json.optJSONObject("error")?.let { error("RPC ${it.optInt("code")}: ${it.optString("message")}") }
            return json
        }
    }
}

/** EVM holder concentration requires a chain-specific indexer; it remains an explicit gap until configured. */
class EvmHolderProvider : DeepOnChainProvider {
    override suspend fun load(chain: String, contractOrMint: String): DeepOnChainResult = DeepOnChainResult(
        HolderSnapshot(), ProviderHealth("EVMHolderIndexer", false, error = "PROVIDER_CREDENTIAL_REQUIRED"), listOf("EVM_HOLDER_CONCENTRATION_REQUIRES_INDEXER")
    )
}

class DeepOnChainProviderRouter(
    private val solana: DeepOnChainProvider = SolanaHolderProvider(),
    private val evm: DeepOnChainProvider = EvmHolderProvider()
) : DeepOnChainProvider {
    override suspend fun load(chain: String, contractOrMint: String): DeepOnChainResult =
        if (chain.equals("solana", true)) solana.load(chain, contractOrMint) else evm.load(chain, contractOrMint)
}
