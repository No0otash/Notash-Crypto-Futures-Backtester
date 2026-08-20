package com.notash.cryptobacktester.data

import com.notash.cryptobacktester.core.Candle
import com.notash.cryptobacktester.core.FundingRate
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class CoinExApi(
    private val client: OkHttpClient = OkHttpClient()
) {

    companion object {
        private const val BASE_URL =
            "https://api.coinex.com/v2"
    }

    fun getKlines(
        market: String,
        period: String,
        limit: Int = 1000,
        startTime: Long? = null,
        endTime: Long? = null
    ): List<Candle> {

        val url = buildString {

            append(
                "$BASE_URL/futures/kline" +
                    "?market=$market" +
                    "&period=$period" +
                    "&limit=$limit"
            )

            startTime?.let {
                append("&start_time=$it")
            }

            endTime?.let {
                append("&end_time=$it")
            }
        }

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request)
            .execute()
            .use { response ->

                if (!response.isSuccessful) {
                    throw RuntimeException(
                        "CoinEx HTTP ${response.code}"
                    )
                }

                val body =
                    response.body?.string()
                        ?: throw RuntimeException(
                            "Empty CoinEx response"
                        )

                val json = JSONObject(body)

                if (json.optInt("code") != 0) {
                    throw RuntimeException(
                        json.optString(
                            "message",
                            "CoinEx API error"
                        )
                    )
                }

                val data =
                    json.optJSONArray("data")
                        ?: return emptyList()

                val result =
                    mutableListOf<Candle>()

                for (i in 0 until data.length()) {

                    val item =
                        data.getJSONObject(i)

                    result.add(
                        Candle(
                            timestamp =
                                item.getLong("created_at"),

                            open =
                                item.getString(
                                    "open"
                                ).toDouble(),

                            close =
                                item.getString(
                                    "close"
                                ).toDouble(),

                            high =
                                item.getString(
                                    "high"
                                ).toDouble(),

                            low =
                                item.getString(
                                    "low"
                                ).toDouble(),

                            volume =
                                item.getString(
                                    "volume"
                                ).toDouble(),

                            value =
                                item.optString(
                                    "value",
                                    "0"
                                ).toDouble()
                        )
                    )
                }

                return result.sortedBy {
                    it.timestamp
                }
            }
    }

    fun getFundingRate(
        market: String
    ): FundingRate? {

        val url =
            "$BASE_URL/futures/funding-rate" +
                "?market=$market"

        val request =
            Request.Builder()
                .url(url)
                .get()
                .build()

        client.newCall(request)
            .execute()
            .use { response ->

                if (!response.isSuccessful) {
                    throw RuntimeException(
                        "CoinEx HTTP ${response.code}"
                    )
                }

                val body =
                    response.body?.string()
                        ?: return null

                val json =
                    JSONObject(body)

                if (json.optInt("code") != 0) {
                    return null
                }

                val data =
                    json.optJSONArray("data")
                        ?: return null

                if (data.length() == 0) {
                    return null
                }

                val item =
                    data.getJSONObject(0)

                return FundingRate(
                    timestamp =
                        item.optLong(
                            "next_funding_time"
                        ),

                    rate =
                        item.optString(
                            "latest_funding_rate",
                            "0"
                        ).toDouble(),

                    markPrice =
                        item.optString(
                            "mark_price",
                            "0"
                        ).toDouble()
                )
            }
    }
}
