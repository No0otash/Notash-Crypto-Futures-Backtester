package com.notash.cryptobacktester.intelligence

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/** Real public market-data adapters. No exchange API key is required for these endpoints. */
interface RadarMarketProvider {
    val exchange: RadarExchange
    suspend fun snapshot(symbol: String): Result<RadarMarketSnapshot>
}

abstract class JsonRadarProvider(
    final override val exchange: RadarExchange,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS).readTimeout(8, TimeUnit.SECONDS).build()
) : RadarMarketProvider {
    protected abstract fun requestUrl(symbol: String): String
    protected abstract fun parse(symbol: String, body: String, now: Long): RadarMarketSnapshot

    override suspend fun snapshot(symbol: String): Result<RadarMarketSnapshot> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder().url(requestUrl(symbol)).header("User-Agent", "ALVEX/1.0").build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) error("${exchange.displayName} HTTP ${response.code}")
                parse(symbol, response.body?.string() ?: error("Empty response"), System.currentTimeMillis())
            }
        }
    }
}

class BinanceRadarProvider : JsonRadarProvider(RadarExchange.BINANCE) {
    override fun requestUrl(symbol: String) = "https://fapi.binance.com/fapi/v1/ticker/24hr?symbol=${symbol.uppercase()}"
    override fun parse(symbol: String, body: String, now: Long): RadarMarketSnapshot {
        val o = JSONObject(body)
        return RadarMarketSnapshot(exchange.displayName, symbol.uppercase(),
            o.getDouble("lastPrice"), o.getDouble("openPrice"), o.getDouble("highPrice"),
            o.getDouble("lowPrice"), o.getDouble("volume"), o.getDouble("quoteVolume"),
            null, null, null, now)
    }
}

class BybitRadarProvider : JsonRadarProvider(RadarExchange.BYBIT) {
    override fun requestUrl(symbol: String) = "https://api.bybit.com/v5/market/tickers?category=linear&symbol=${symbol.uppercase()}"
    override fun parse(symbol: String, body: String, now: Long): RadarMarketSnapshot {
        val item = JSONObject(body).getJSONObject("result").getJSONArray("list").getJSONObject(0)
        return RadarMarketSnapshot(exchange.displayName, symbol.uppercase(),
            item.getDouble("lastPrice"), item.getDouble("prevPrice24h"),
            item.getDouble("highPrice24h"), item.getDouble("lowPrice24h"),
            item.getDouble("volume24h"), item.getDouble("turnover24h"),
            null, null, item.optDouble("openInterest", Double.NaN).takeUnless { it.isNaN() }, now)
    }
}

class OkxRadarProvider : JsonRadarProvider(RadarExchange.OKX) {
    override fun requestUrl(symbol: String) = "https://www.okx.com/api/v5/market/ticker?instId=${symbol.uppercase()}-SWAP"
    override fun parse(symbol: String, body: String, now: Long): RadarMarketSnapshot {
        val item = JSONObject(body).getJSONArray("data").getJSONObject(0)
        return RadarMarketSnapshot(exchange.displayName, symbol.uppercase(),
            item.getDouble("last"), item.getDouble("open24h"), item.getDouble("high24h"),
            item.getDouble("low24h"), item.getDouble("vol24h"), item.optDouble("volCcy24h", 0.0),
            null, null, null, now)
    }
}

class CoinExRadarProvider : JsonRadarProvider(RadarExchange.COINEX) {
    override fun requestUrl(symbol: String) = "https://api.coinex.com/v2/futures/ticker?market=${symbol.uppercase()}"
    override fun parse(symbol: String, body: String, now: Long): RadarMarketSnapshot {
        val root = JSONObject(body)
        val raw = root.get("data")
        val data = if (raw is org.json.JSONArray) raw.optJSONObject(0) ?: error("CoinEx returned no ticker") else raw as JSONObject
        return RadarMarketSnapshot(exchange.displayName, symbol.uppercase(),
            data.getDouble("last"), data.getDouble("open"), data.getDouble("high"), data.getDouble("low"),
            data.getDouble("volume"), data.optDouble("value", 0.0),
            data.optDouble("volume_buy", Double.NaN).takeUnless { it.isNaN() },
            data.optDouble("volume_sell", Double.NaN).takeUnless { it.isNaN() },
            data.optDouble("open_interest_size", Double.NaN).takeUnless { it.isNaN() }, now)
    }
}

class MultiExchangeRadarRepository(
    private val providers: List<RadarMarketProvider> = listOf(
        CoinExRadarProvider(), BinanceRadarProvider(), BybitRadarProvider(), OkxRadarProvider()
    )
) {
    suspend fun snapshots(symbols: List<String>): Pair<List<RadarMarketSnapshot>, List<RadarProviderHealth>> {
        val snapshots = mutableListOf<RadarMarketSnapshot>()
        val health = mutableListOf<RadarProviderHealth>()
        for (provider in providers) for (symbol in symbols) {
            val result = provider.snapshot(symbol)
            result.onSuccess { snapshots += it }
            health += RadarProviderHealth(provider.exchange, result.isSuccess, System.currentTimeMillis(), result.exceptionOrNull()?.message)
        }
        return snapshots to health
    }
}
