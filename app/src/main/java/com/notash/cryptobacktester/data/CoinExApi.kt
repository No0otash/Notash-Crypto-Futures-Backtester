package com.notash.cryptobacktester.data

import com.notash.cryptobacktester.core.Candle
import com.notash.cryptobacktester.core.FundingRate
import com.notash.cryptobacktester.core.MarketTicker
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

data class FuturesMarketDescriptor(val market: String, val baseAsset: String, val quoteAsset: String, val isTrading: Boolean)

data class FuturesTickerSnapshot(
    val market: String,
    val lastPrice: Double,
    val open24h: Double,
    val high24h: Double,
    val low24h: Double,
    val volume24h: Double,
    val quoteVolume24h: Double,
    val buyVolume24h: Double?,
    val sellVolume24h: Double?,
    val openInterest: Double?,
    val timestampMs: Long
)

class CoinExApi(private val client: OkHttpClient = OkHttpClient()) {
    companion object {
        private const val BASE_URL = "https://api.coinex.com/v2"

        internal fun parseFuturesMarkets(body: String): List<FuturesMarketDescriptor> {
            val data = JSONObject(body).optJSONArray("data") ?: return emptyList()
            return buildList {
                for (i in 0 until data.length()) {
                    val item = data.getJSONObject(i)
                    val status = item.optString("status")
                    val available = if (item.has("is_market_available")) item.optBoolean("is_market_available") else status == "online"
                    add(FuturesMarketDescriptor(item.optString("market"), item.optString("base_ccy"), item.optString("quote_ccy"), available))
                }
            }.filter { it.market.isNotBlank() }
        }
    }

    fun getFuturesMarkets(): List<FuturesMarketDescriptor> {
        val request = Request.Builder().url("$BASE_URL/futures/market").get().build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw RuntimeException("CoinEx HTTP ${response.code}")
            val body = response.body?.string() ?: throw RuntimeException("Empty CoinEx response")
            val json = JSONObject(body)
            if (json.optInt("code") != 0) throw RuntimeException(json.optString("message", "CoinEx API error"))
            return parseFuturesMarkets(body)
        }
    }

    fun getFuturesTickers(): List<FuturesTickerSnapshot> {
        val request = Request.Builder().url("$BASE_URL/futures/ticker").get().build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw RuntimeException("CoinEx HTTP ${response.code}")
            val json = JSONObject(response.body?.string() ?: throw RuntimeException("Empty CoinEx response"))
            if (json.optInt("code") != 0) throw RuntimeException(json.optString("message", "CoinEx API error"))
            val data = json.optJSONArray("data") ?: return emptyList()
            val now = System.currentTimeMillis()
            return buildList {
                for (i in 0 until data.length()) {
                    val item = data.getJSONObject(i)
                    val market = item.optString("market")
                    if (market.isBlank()) continue
                    add(FuturesTickerSnapshot(market, item.optString("last", "0").toDoubleOrNull() ?: 0.0, item.optString("open", "0").toDoubleOrNull() ?: 0.0, item.optString("high", "0").toDoubleOrNull() ?: 0.0, item.optString("low", "0").toDoubleOrNull() ?: 0.0, item.optString("volume", "0").toDoubleOrNull() ?: 0.0, item.optString("value", "0").toDoubleOrNull() ?: 0.0, item.optString("volume_buy", "").toDoubleOrNull(), item.optString("volume_sell", "").toDoubleOrNull(), item.optString("open_interest_volume", "").toDoubleOrNull(), now))
                }
            }
        }
    }

    fun getKlines(market: String, period: String, limit: Int = 1000, startTime: Long? = null, endTime: Long? = null): List<Candle> {
        val url = buildString {
            append("$BASE_URL/futures/kline?market=$market&period=$period&limit=$limit")
            startTime?.let { append("&start_time=$it") }; endTime?.let { append("&end_time=$it") }
        }
        val request = Request.Builder().url(url).get().build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw RuntimeException("CoinEx HTTP ${response.code}")
            val json = JSONObject(response.body?.string() ?: throw RuntimeException("Empty CoinEx response"))
            if (json.optInt("code") != 0) throw RuntimeException(json.optString("message", "CoinEx API error"))
            val data = json.optJSONArray("data") ?: return emptyList()
            return buildList {
                for (i in 0 until data.length()) {
                    val item = data.getJSONObject(i)
                    add(Candle(item.getLong("created_at"), item.getString("open").toDouble(), item.getString("high").toDouble(), item.getString("low").toDouble(), item.getString("close").toDouble(), item.getString("volume").toDouble(), item.optString("value", "0").toDouble()))
                }
            }.sortedBy { it.timestamp }
        }
    }

    fun getTicker(market: String): MarketTicker? {
        val request = Request.Builder().url("$BASE_URL/futures/ticker?market=$market").get().build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw RuntimeException("CoinEx HTTP ${response.code}")
            val json = JSONObject(response.body?.string() ?: return null)
            if (json.optInt("code") != 0) throw RuntimeException(json.optString("message", "CoinEx API error"))
            val data = json.optJSONArray("data") ?: return null
            if (data.length() == 0) return null
            val item = data.getJSONObject(0)
            return MarketTicker(market, item.optString("last", item.optString("close", "0")).toDouble(), item.optString("change_rate", "0").toDouble(), item.optString("volume", "0").toDouble(), item.optString("mark_price", "0").toDouble())
        }
    }

    fun getFundingRate(market: String): FundingRate? {
        val request = Request.Builder().url("$BASE_URL/futures/funding-rate?market=$market").get().build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw RuntimeException("CoinEx HTTP ${response.code}")
            val body = response.body?.string() ?: return null
            val json = JSONObject(body)
            if (json.optInt("code") != 0) return null
            val data = json.optJSONArray("data") ?: return null
            if (data.length() == 0) return null
            val item = data.getJSONObject(0)
            return FundingRate(item.optLong("next_funding_time"), item.optString("latest_funding_rate", "0").toDouble(), item.optString("mark_price", "0").toDouble())
        }
    }
}
