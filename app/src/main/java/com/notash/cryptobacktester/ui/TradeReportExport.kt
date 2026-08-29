package com.notash.cryptobacktester.ui

import com.notash.cryptobacktester.core.TradeResult
import java.util.Locale

fun tradesToCsv(trades: List<TradeResult>): String {
    val header = "trade,side,entryTime,exitTime,entryPrice,exitPrice,netPnl,fees,funding\n"
    return buildString {
        append(header)
        trades.forEachIndexed { i, t ->
            append(i + 1).append(',')
            append(t.side.name).append(',')
            append(t.entryTime).append(',')
            append(t.exitTime).append(',')
            append(format(t.entryPrice)).append(',')
            append(format(t.exitPrice)).append(',')
            append(format(t.netPnl)).append(',')
            append(format(t.fees)).append(',')
            append(format(t.funding)).append('\n')
        }
    }
}

fun tradesToJson(trades: List<TradeResult>): String = buildString {
    append("[\n")
    trades.forEachIndexed { i, t ->
        append("  {")
        append("\"trade\":${i + 1},")
        append("\"side\":\"${t.side.name}\",")
        append("\"entryTime\":${t.entryTime},")
        append("\"exitTime\":${t.exitTime},")
        append("\"entryPrice\":${format(t.entryPrice)},")
        append("\"exitPrice\":${format(t.exitPrice)},")
        append("\"netPnl\":${format(t.netPnl)},")
        append("\"fees\":${format(t.fees)},")
        append("\"funding\":${format(t.funding)}")
        append("}")
        if (i != trades.lastIndex) append(',')
        append('\n')
    }
    append("]")
}

private fun format(value: Double): String = String.format(Locale.US, "%.8f", value)
