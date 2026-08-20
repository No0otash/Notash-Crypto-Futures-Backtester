package com.notash.cryptobacktester.data

import com.notash.cryptobacktester.core.Candle
import com.notash.cryptobacktester.core.FundingRate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CoinExRepository(
    private val api: CoinExApi = CoinExApi()
) {

    suspend fun loadKlines(
        market: String,
        period: String,
        limit: Int = 1000,
        startTime: Long? = null,
        endTime: Long? = null
    ): List<Candle> {

        return withContext(Dispatchers.IO) {

            api.getKlines(
                market = market,
                period = period,
                limit = limit,
                startTime = startTime,
                endTime = endTime
            )
        }
    }

    suspend fun loadFundingRate(
        market: String
    ): FundingRate? {

        return withContext(Dispatchers.IO) {

            api.getFundingRate(
                market = market
            )
        }
    }
}
