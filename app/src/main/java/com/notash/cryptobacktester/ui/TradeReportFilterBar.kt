package com.notash.cryptobacktester.ui

/**
 * Presentation state for the trade-report filter bar. Kept separate from
 * Compose so filtering remains deterministic and easy to test.
 */
data class TradeReportFilterState(
    val filter: TradeFilter = TradeFilter.ALL,
    val sort: TradeSort = TradeSort.TIME_ASC
)

fun nextTradeFilter(current: TradeFilter): TradeFilter = when (current) {
    TradeFilter.ALL -> TradeFilter.LONG
    TradeFilter.LONG -> TradeFilter.SHORT
    TradeFilter.SHORT -> TradeFilter.WIN
    TradeFilter.WIN -> TradeFilter.LOSS
    TradeFilter.LOSS -> TradeFilter.ALL
}

fun nextTradeSort(current: TradeSort): TradeSort = when (current) {
    TradeSort.TIME_ASC -> TradeSort.TIME_DESC
    TradeSort.TIME_DESC -> TradeSort.PNL_DESC
    TradeSort.PNL_DESC -> TradeSort.PNL_ASC
    TradeSort.PNL_ASC -> TradeSort.TIME_ASC
}
