package com.notash.cryptobacktester.strategy

import com.notash.cryptobacktester.core.BacktestConfig
import com.notash.cryptobacktester.core.Candle
import com.notash.cryptobacktester.core.FundingRate
import com.notash.cryptobacktester.core.Signal

interface Strategy {

    val id: String

    val name: String

    val version: String

    val description: String

    fun generateSignal(
        index: Int,
        candles: List<Candle>,
        funding: List<FundingRate>,
        config: BacktestConfig
    ): Signal?
}
