package com.notash.cryptobacktester.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notash.cryptobacktester.core.BacktestReport

@Composable
fun TradeReportPanel(report: BacktestReport?, fa: Boolean) {
    var filter by remember { mutableStateOf(TradeFilter.ALL) }
    var sort by remember { mutableStateOf(TradeSort.TIME_DESC) }
    val trades = filterAndSortTrades(report?.trades.orEmpty(), filter, sort)
    val summary = summarizeTrades(trades)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Text(if (fa) "گزارش معاملات" else "TRADE REPORT", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TradeFilter.entries.forEach { item ->
                FilterChip(
                    selected = filter == item,
                    onClick = { filter = item },
                    label = { Text(filterLabel(item, fa), fontSize = 10.sp) }
                )
            }
        }
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TradeSort.entries.forEach { item ->
                FilterChip(
                    selected = sort == item,
                    onClick = { sort = item },
                    label = { Text(sortLabel(item, fa), fontSize = 9.sp) }
                )
            }
        }
        Text(
            if (fa) "${summary.totalTrades} معامله • برد ${summary.wins} • باخت ${summary.losses} • نرخ برد ${"%.1f".format(summary.winRatePercent)}٪ • PnL ${"%.2f".format(summary.netPnl)}"
            else "${summary.totalTrades} trades • ${summary.wins} wins • ${summary.losses} losses • Win rate ${"%.1f".format(summary.winRatePercent)}% • PnL ${"%.2f".format(summary.netPnl)}",
            color = Color(0xFF8490A7), fontSize = 10.sp, modifier = Modifier.padding(bottom = 4.dp)
        )
        trades.forEachIndexed { index, trade ->
            val sideColor = if (trade.side.name == "LONG") Color(0xFF20E6A5) else Color(0xFFFF5577)
            Text(
                "#${index + 1}  ${trade.side.name}  ${"%.4f".format(trade.entryPrice)} → ${"%.4f".format(trade.exitPrice)}   PnL ${"%.2f".format(trade.netPnl)}",
                color = sideColor, fontSize = 10.sp, fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun filterLabel(value: TradeFilter, fa: Boolean): String = when (value) {
    TradeFilter.ALL -> if (fa) "همه" else "ALL"
    TradeFilter.LONG -> "LONG"
    TradeFilter.SHORT -> "SHORT"
    TradeFilter.WIN -> if (fa) "سودده" else "WIN"
    TradeFilter.LOSS -> if (fa) "زیان‌ده" else "LOSS"
}

private fun sortLabel(value: TradeSort, fa: Boolean): String = when (value) {
    TradeSort.TIME_ASC -> if (fa) "زمان ↑" else "TIME ↑"
    TradeSort.TIME_DESC -> if (fa) "زمان ↓" else "TIME ↓"
    TradeSort.PNL_ASC -> if (fa) "PnL ↑" else "PNL ↑"
    TradeSort.PNL_DESC -> if (fa) "PnL ↓" else "PNL ↓"
}
