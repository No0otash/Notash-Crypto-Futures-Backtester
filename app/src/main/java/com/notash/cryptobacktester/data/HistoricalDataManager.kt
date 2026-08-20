package com.notash.cryptobacktester.data

import com.notash.cryptobacktester.core.Candle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class HistoricalDataManager(
    private val repository: CoinExRepository = CoinExRepository()
) {

    /**
     * Downloads historical candles in chunks.
     *
     * CoinEx limits the number of candles per request,
     * so large periods are downloaded in multiple requests.
     */
    suspend fun downloadKlines(
        market: String,
        period: String,
        startTime: Long,
        endTime: Long,
        chunkSize: Int = 1000
    ): List<Candle> {

        require(startTime < endTime) {
            "startTime must be smaller than endTime"
        }

        require(chunkSize in 1..1000) {
            "chunkSize must be between 1 and 1000"
        }

        return withContext(Dispatchers.IO) {

            val allCandles =
                mutableListOf<Candle>()

            var currentStart =
                startTime

            while (currentStart < endTime) {

                val candles =
                    repository.loadKlines(
                        market = market,
                        period = period,
                        limit = chunkSize,
                        startTime = currentStart,
                        endTime = endTime
                    )

                if (candles.isEmpty()) {
                    break
                }

                allCandles.addAll(candles)

                val lastTimestamp =
                    candles.maxOf {
                        it.timestamp
                    }

                /*
                 * Move one millisecond forward
                 * to prevent requesting the same
                 * candle repeatedly.
                 */
                val nextStart =
                    lastTimestamp + 1

                if (nextStart <= currentStart) {
                    break
                }

                currentStart =
                    nextStart

                /*
                 * Small delay to avoid hammering
                 * the exchange API.
                 */
                delay(150)
            }

            allCandles
                .distinctBy {
                    it.timestamp
                }
                .sortedBy {
                    it.timestamp
                }
                .filter {
                    it.timestamp in
                            startTime..endTime
                }
        }
    }

    suspend fun downloadLatest(
        market: String,
        period: String,
        limit: Int = 1000
    ): List<Candle> {

        require(limit in 1..1000) {
            "limit must be between 1 and 1000"
        }

        return withContext(Dispatchers.IO) {

            repository.loadKlines(
                market = market,
                period = period,
                limit = limit
            )
        }
            .distinctBy {
                it.timestamp
            }
            .sortedBy {
                it.timestamp
            }
    }
}
