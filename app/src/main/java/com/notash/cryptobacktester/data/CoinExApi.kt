package com.notash.cryptobacktester.data

import com.notash.cryptobacktester.core.Candle
import com.notash.cryptobacktester.core.FundingRate
import com.notash.cryptobacktester.core.MarketTicker
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class CoinExApi(private val client: OkHttpClient = OkHttpClient()) {
    companion object { private const val BASE_URL = "https://api.coinex.com/v2" }

    fun getKlines(market: String, period: String, limit: Int = 1000, startTime: Long? = null, endTime: Long? = null): List<Candle> {
        val coinExPeriod = normalizePeriod(period)
        val url = buildString {
            append("$BASE_URL/futures/kline?market=$market&period=$coinExPeriod&limit=$limit")
            startTime?.let { append("&start_time=$it") }
            endTime?.let { append("&end_time=$it") }
        }
        val request = Request.Builder().url(url).get().build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw RuntimeException("CoinEx HTTP ${response.code}")
            val body = response.body?.string() ?: throw RuntimeException("Empty CoinEx response")
            val json = JSONObject(body)
            if (json.optInt("code") != 0) throw RuntimeException(json.optString("message", "CoinEx API error"))
            val data = json.optJSONArray("data") ?: return emptyList()
            return buildList(data.length()) {
                for (i in 0 until data.length()) {
                    val item = data.getJSONObject(i)
                    add(Candle(
                        timestamp = item.getLong("created_at"),
                        open = item.getString("open").toDouble(),
                        close = item.getString("close").toDouble(),
                        high = item.getString("high").toDouble(),
                        low = item.getString("low").toDouble(),
                        volume = item.getString("volume").toDouble(),
                        value = item.optString("value", "0").toDouble()
                    ))
                }
            }.sortedBy { it.timestamp }
        }
    }

    fun getFuturesTickers(): List<MarketTicker> {
        val request = Request.Builder().url("$BASE_URL/futures/ticker").get().build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw RuntimeException("CoinEx HTTP ${response.code}")
            val body = response.body?.string() ?: throw RuntimeException("Empty CoinEx ticker response")
            val json = JSONObject(body)
            if (json.optInt("code") != 0) throw RuntimeException(json.optString("message", "CoinEx ticker error"))
            val data = json.optJSONArray("data") ?: return emptyList()
            return buildList(data.length()) {
                for (i in 0 until data.length()) {
                    val item = data.getJSONObject(i)
                    val market = item.optString("market")
                    if (market.isBlank() || !market.endsWith("USDT")) continue
                    val last = item.optString("last", item.optString("close", "0")).toDoubleOrNull() ?: 0.0
                    val open = item.optString("open", "0").toDoubleOrNull() ?: 0.0
                    val change = if (open != 0.0) (last / open - 1.0) * 100.0 else 0.0
                    add(MarketTicker(
                        market = market,
                        last = last,
                        change24h = change,
                        volume24h = item.optString("volume", "0").toDoubleOrNull() ?: 0.0,
                        value24h = item.optString("value", "0").toDoubleOrNull() ?: 0.0
                    ))
                }
            }.sortedByDescending { it.value24h }
        }
    }

    fun getFundingRates(market: String, limit: Int = 1000): List<FundingRate> {
        val url = "$BASE_URL/futures/funding-rate-history?market=$market&limit=$limit"
        val request = Request.Builder().url(url).get().build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw RuntimeException("CoinEx HTTP ${response.code}")
            val body = response.body?.string() ?: return emptyList()
            val json = JSONObject(body)
            if (json.optInt("code") != 0) return emptyList()
            val data = json.optJSONArray("data") ?: return emptyList()
            return buildList(data.length()) {
                for (i in 0 until data.length()) {
                    val item = data.getJSONObject(i)
                    add(FundingRate(
                        timestamp = item.optLong("funding_time", item.optLong("next_funding_time")),
                        rate = item.optString("actual_funding_rate", item.optString("latest_funding_rate", "0")).toDoubleOrNull() ?: 0.0,
                        markPrice = item.optString("mark_price", "0").toDoubleOrNull() ?: 0.0
                    ))
                }
            }.sortedBy { it.timestamp }
        }
    }

    private fun normalizePeriod(period: String): String = when (period.lowercase()) {
        "1m", "1min" -> "1min"
        "3m", "3min" -> "3min"
        "5m", "5min" -> "5min"
        "15m", "15min" -> "15min"
        "30m", "30min" -> "30min"
        "1h", "1hour" -> "1hour"
        "2h", "2hour" -> "2hour"
        "4h", "4hour" -> "4hour"
        "6h", "6hour" -> "6hour"
        "12h", "12hour" -> "12hour"
        "1d", "1day" -> "1day"
        "3d", "3day" -> "3day"
        "1w", "1week" -> "1week"
        else -> throw IllegalArgumentException("Unsupported CoinEx timeframe: $period")
    }
}
