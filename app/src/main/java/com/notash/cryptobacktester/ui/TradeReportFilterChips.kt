package com.notash.cryptobacktester.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TradeReportFilterChips(
    state: TradeReportFilterState,
    fa: Boolean,
    onFilter: (TradeFilter) -> Unit,
    onSort: (TradeSort) -> Unit
) {
    val filters = listOf(
        TradeFilter.ALL to if (fa) "همه" else "ALL",
        TradeFilter.LONG to "LONG",
        TradeFilter.SHORT to "SHORT",
        TradeFilter.WIN to if (fa) "سودده" else "WIN",
        TradeFilter.LOSS to if (fa) "زیان‌ده" else "LOSS"
    )
    Row(Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 4.dp)) {
        filters.forEach { (filter, label) ->
            FilterChip(
                selected = state.filter == filter,
                onClick = { onFilter(filter) },
                label = { Text(label) },
                modifier = Modifier.padding(end = 5.dp)
            )
        }
        FilterChip(
            selected = state.sort == TradeSort.PNL_DESC,
            onClick = { onSort(if (state.sort == TradeSort.PNL_DESC) TradeSort.PNL_ASC else TradeSort.PNL_DESC) },
            label = { Text(if (fa) "مرتب‌سازی PnL" else "SORT PnL") }
        )
    }
}
