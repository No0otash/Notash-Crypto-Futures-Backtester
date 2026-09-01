package com.notash.cryptobacktester.live

import com.notash.cryptobacktester.core.Candle

interface LiveMarketData {
    suspend fun fetchCandles(symbol: String, interval: String, limit: Int = 200): List<Candle>
}

/** Public Binance market-data adapter. It only reads market data; it never submits orders. */
class BinancePublicMarketData : LiveMarketData {
    override suspend fun fetchCandles(symbol: String, interval: String, limit: Int): List<Candle> =
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val url = java.net.URL("https://api.binance.com/api/v3/klines?symbol=${symbol.uppercase()}&interval=$interval&limit=$limit")
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.connectTimeout = 10_000
            connection.readTimeout = 15_000
            connection.requestMethod = "GET"
            try {
                require(connection.responseCode in 200..299) { "Binance market data HTTP ${connection.responseCode}" }
                val text = connection.inputStream.bufferedReader().use { it.readText() }
                val rows = org.json.JSONArray(text)
                buildList {
                    for (i in 0 until rows.length()) {
                        val r = rows.getJSONArray(i)
                        add(Candle(r.getLong(0), r.getDouble(1), r.getDouble(2), r.getDouble(3), r.getDouble(4), r.getDouble(5), r.getDouble(7)))
                    }
                }
            } finally { connection.disconnect() }
        }
}
