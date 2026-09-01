package com.notash.cryptobacktester.intelligence

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

data class ProviderHealth(val provider: String, val ok: Boolean, val statusCode: Int? = null, val error: String? = null, val observedAtEpochMs: Long = System.currentTimeMillis())

data class ProjectProviderResult(val profile: ProjectProfile, val product: ProductAnalysis, val development: DevelopmentActivity, val health: List<ProviderHealth>)
data class TokenomicsProviderResult(val supply: TokenSupply, val health: List<ProviderHealth>)
data class OnChainProviderResult(val snapshot: HolderSnapshot, val networkMetrics: Map<String, Double?>, val health: List<ProviderHealth>)

interface ProjectDataProvider { suspend fun load(projectId: String, githubRepo: String? = null): ProjectProviderResult }
interface TokenomicsDataProvider { suspend fun load(assetId: String): TokenomicsProviderResult }
interface OnChainDataProvider { suspend fun load(asset: String): OnChainProviderResult }

internal class HttpJsonClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).callTimeout(15, TimeUnit.SECONDS).build()
) {
    suspend fun getObject(url: String, headers: Map<String, String> = emptyMap()): Pair<Int, JSONObject> = withContext(Dispatchers.IO) {
        execute(url, headers).let { it.first to JSONObject(it.second) }
    }
    suspend fun getArray(url: String, headers: Map<String, String> = emptyMap()): Pair<Int, JSONArray> = withContext(Dispatchers.IO) {
        execute(url, headers).let { it.first to JSONArray(it.second) }
    }
    private suspend fun execute(url: String, headers: Map<String, String>): Pair<Int, String> = withContext(Dispatchers.IO) {
        val requestBuilder = Request.Builder().url(url).get()
        headers.forEach { (k, v) -> requestBuilder.header(k, v) }
        client.newCall(requestBuilder.build()).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw ProviderHttpException(response.code, body.take(240))
            response.code to body
        }
    }
}

private class ProviderHttpException(val status: Int, override val message: String) : IOException(message)

/** Public GitHub API: project identity and development activity, no user token required. */
class GitHubProjectProvider(private val http: HttpJsonClient = HttpJsonClient()) : ProjectDataProvider {
    override suspend fun load(projectId: String, githubRepo: String?): ProjectProviderResult {
        val health = mutableListOf<ProviderHealth>()
        var profile = ProjectProfile(symbol = projectId)
        var product = ProductAnalysis()
        var development = DevelopmentActivity()
        if (githubRepo.isNullOrBlank()) return ProjectProviderResult(profile, product, development, listOf(ProviderHealth("GitHub", false, error = "GITHUB_REPOSITORY_NOT_CONFIGURED")))
        try {
            val (_, repo) = http.getObject("https://api.github.com/repos/${githubRepo.trim()}", mapOf("Accept" to "application/vnd.github+json", "User-Agent" to "ALVEX"))
            val html = repo.optString("html_url").takeIf { it.isNotBlank() }
            profile = profile.copy(
                name = repo.optString("name", projectId), description = repo.optString("description", "Unknown"),
                github = html, website = repo.optString("homepage").takeIf { it.isNotBlank() },
                status = if (repo.optBoolean("archived", false)) "Archived" else "Active",
                source = SourceRef("GitHub repository", html, status = SourceStatus.CONFIRMED)
            )
            product = product.copy(
                realWorldUseCase = repo.optString("description", "Unknown"), liveProduct = !repo.optBoolean("archived", false),
                utility = if (repo.optInt("stargazers_count", 0) > 0) "Open-source project" else "Unknown",
                maturity = if (repo.optInt("forks_count", 0) > 10) "Community active" else "Unknown",
                score = ((repo.optInt("stargazers_count", 0).coerceAtMost(10000) / 10000.0) * 100.0).coerceIn(0.0, 100.0),
                confidence = 0.65, sources = listOf(SourceRef("GitHub repository", html, status = SourceStatus.CONFIRMED))
            )
            val (_, commits) = http.getArray("https://api.github.com/repos/${githubRepo.trim()}/commits?per_page=1", mapOf("Accept" to "application/vnd.github+json", "User-Agent" to "ALVEX"))
            val latestCommit = commits.optJSONObject(0)?.optString("sha")?.takeIf { it.isNotBlank() }
            val commitDate = commits.optJSONObject(0)?.optJSONObject("commit")?.optJSONObject("committer")?.optString("date")
            development = development.copy(
                latestCommit = latestCommit, commitTrend = if (latestCommit != null) "RECENT_ACTIVITY_AVAILABLE" else "Unknown",
                openIssues = repo.optInt("open_issues_count", 0), lastActivity = commitDate ?: repo.optString("updated_at").takeIf { it.isNotBlank() },
                score = if (repo.optBoolean("archived", false)) 15.0 else if (latestCommit != null) 70.0 else 35.0,
                risk = if (repo.optBoolean("archived", false)) "ARCHIVED" else "Unknown",
                sources = listOf(SourceRef("GitHub commits", "https://github.com/${githubRepo.trim()}/commits", status = SourceStatus.CONFIRMED))
            )
            health += ProviderHealth("GitHub", true, 200)
        } catch (e: ProviderHttpException) { health += ProviderHealth("GitHub", false, e.status, e.message)
        } catch (e: Exception) { health += ProviderHealth("GitHub", false, error = e.message ?: e::class.simpleName) }
        return ProjectProviderResult(profile, product, development, health)
    }
}

/** CoinGecko REST: supply/FDV/token metadata. Optional Demo API key is supplied at runtime, never committed. */
class CoinGeckoTokenomicsProvider(private val apiKey: String? = null, private val http: HttpJsonClient = HttpJsonClient()) : TokenomicsDataProvider {
    override suspend fun load(assetId: String): TokenomicsProviderResult {
        val headers = buildMap { if (!apiKey.isNullOrBlank()) put("x-cg-demo-api-key", apiKey) }
        return try {
            val url = "https://api.coingecko.com/api/v3/coins/${assetId.trim()}?localization=false&tickers=false&market_data=true&community_data=false&developer_data=false"
            val (_, json) = http.getObject(url, headers)
            val market = json.optJSONObject("market_data")
            val total = market?.optDoubleOrNull("total_supply")
            val max = market?.optDoubleOrNull("max_supply")
            val supply = TokenSupply(
                circulating = market?.optDoubleOrNull("circulating_supply"), total = total, max = max,
                fullyDiluted = market?.optJSONObject("fully_diluted_valuation")?.optDoubleOrNull("usd"),
                inflationary = if (total != null && max != null) max > total else null,
                source = SourceRef("CoinGecko", "https://www.coingecko.com/en/coins/${assetId.trim()}", status = SourceStatus.CONFIRMED)
            )
            TokenomicsProviderResult(supply, listOf(ProviderHealth("CoinGecko", true, 200)))
        } catch (e: ProviderHttpException) { TokenomicsProviderResult(TokenSupply(), listOf(ProviderHealth("CoinGecko", false, e.status, e.message)))
        } catch (e: Exception) { TokenomicsProviderResult(TokenSupply(), listOf(ProviderHealth("CoinGecko", false, error = e.message ?: e::class.simpleName))) }
    }
}

/** DefiLlama public protocol endpoint: TVL/category and protocol metadata. */
class DefiLlamaProjectProvider(private val http: HttpJsonClient = HttpJsonClient()) {
    suspend fun load(slug: String): Pair<ProductAnalysis, ProviderHealth> = try {
        val (_, json) = http.getObject("https://api.llama.fi/protocol/${slug.trim()}")
        val tvl = json.optJSONArray("tvl")?.lastObjectDouble("totalLiquidityUSD")
        val category = json.optString("category").takeIf { it.isNotBlank() }
        val score = when { tvl == null -> 40.0; tvl >= 1_000_000_000 -> 90.0; tvl >= 100_000_000 -> 80.0; tvl >= 10_000_000 -> 65.0; else -> 45.0 }
        ProductAnalysis(
            realWorldUseCase = category ?: "Unknown", utility = category ?: "Unknown", maturity = if (tvl != null) "TVL_DATA_AVAILABLE" else "Unknown",
            liveProduct = tvl != null, score = score, confidence = if (tvl != null) 0.7 else 0.35,
            sources = listOf(SourceRef("DefiLlama", "https://defillama.com/protocol/${slug.trim()}", status = SourceStatus.CONFIRMED))
        ) to ProviderHealth("DefiLlama", true, 200)
    } catch (e: ProviderHttpException) { ProductAnalysis() to ProviderHealth("DefiLlama", false, e.status, e.message)
    } catch (e: Exception) { ProductAnalysis() to ProviderHealth("DefiLlama", false, error = e.message ?: e::class.simpleName) }
}

/** Coin Metrics Community API: no key required; exposes network metrics for supported assets. */
class CoinMetricsCommunityProvider(private val http: HttpJsonClient = HttpJsonClient()) : OnChainDataProvider {
    override suspend fun load(asset: String): OnChainProviderResult = try {
        val url = "https://community-api.coinmetrics.io/v4/timeseries/asset-metrics".toHttpUrl().newBuilder()
            .addQueryParameter("assets", asset.lowercase()).addQueryParameter("metrics", "AdrAct30dCnt,TxCnt,CapMrktCurUSD")
            .addQueryParameter("frequency", "1d").addQueryParameter("paging_from", "end").addQueryParameter("page_size", "1").build().toString()
        val (_, json) = http.getObject(url)
        val row = json.optJSONArray("data")?.optJSONObject(0)
        val metrics = mapOf("active_addresses_30d" to row?.optDoubleOrNull("AdrAct30dCnt"), "transaction_count" to row?.optDoubleOrNull("TxCnt"), "market_cap_usd" to row?.optDoubleOrNull("CapMrktCurUSD"))
        val snapshot = HolderSnapshot(source = SourceRef("Coin Metrics Community", "https://coinmetrics.io/", status = SourceStatus.CONFIRMED))
        OnChainProviderResult(snapshot, metrics, listOf(ProviderHealth("CoinMetricsCommunity", true, 200)))
    } catch (e: ProviderHttpException) { OnChainProviderResult(HolderSnapshot(), emptyMap(), listOf(ProviderHealth("CoinMetricsCommunity", false, e.status, e.message)))
    } catch (e: Exception) { OnChainProviderResult(HolderSnapshot(), emptyMap(), listOf(ProviderHealth("CoinMetricsCommunity", false, error = e.message ?: e::class.simpleName))) }
}

private fun JSONObject.optDoubleOrNull(key: String): Double? = if (!has(key) || isNull(key)) null else optDouble(key).takeUnless { it.isNaN() }
private fun JSONArray.lastObjectDouble(key: String): Double? = if (length() == 0) null else optJSONObject(length() - 1)?.optDoubleOrNull(key)
