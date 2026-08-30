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
            return MarketTicker(
                market = market,
                last = item.optString("last", item.optString("close", "0")).toDouble(),
                changeRate = item.optString("change_rate", "0").toDouble(),
                volume = item.optString("volume", "0").toDouble(),
                markPrice = item.optString("mark_price", "0").toDouble()
            )
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
