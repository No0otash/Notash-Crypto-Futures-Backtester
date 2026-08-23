package com.notash.cryptobacktester.data

import com.notash.cryptobacktester.core.Candle
import com.notash.cryptobacktester.core.FundingRate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CoinExRepository(private val api: CoinExApi = CoinExApi()) {
    suspend fun loadKlines(market: String, period: String, limit: Int = 1000, startTime: Long? = null, endTime: Long? = null): List<Candle> =
        withContext(Dispatchers.IO) { api.getKlines(market, period, limit, startTime, endTime) }

    suspend fun loadFundingRates(market: String, limit: Int = 1000): List<FundingRate> =
        withContext(Dispatchers.IO) { api.getFundingRates(market, limit) }
}
