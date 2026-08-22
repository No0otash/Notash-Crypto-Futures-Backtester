package com.notash.cryptobacktester.export

import com.notash.cryptobacktester.core.BacktestReport
import java.util.Locale

object BacktestExporter {
    fun toCsv(report: BacktestReport): String = buildString {
        appendLine("side,entry_price,exit_price,quantity,gross_pnl,fees,funding,net_pnl,entry_time,exit_time")
        report.trades.forEach { t ->
            appendLine(listOf(t.side, t.entryPrice, t.exitPrice, t.quantity, t.grossPnl, t.fees, t.funding, t.netPnl, t.entryTime, t.exitTime).joinToString(","))
        }
    }

    fun toJson(report: BacktestReport): String = buildString {
        fun d(v: Double) = String.format(Locale.US, "%.8f", v)
        append("{\"initialBalance\":${d(report.initialBalance)},\"finalBalance\":${d(report.finalBalance)},")
        append("\"netPnl\":${d(report.netPnl)},\"roiPercent\":${d(report.roiPercent)},")
        append("\"maxDrawdownPercent\":${d(report.maxDrawdownPercent)},\"winRatePercent\":${d(report.winRatePercent)},")
        append("\"profitFactor\":${d(report.profitFactor)},\"totalFees\":${d(report.totalFees)},\"totalFunding\":${d(report.totalFunding)},")
        append("\"trades\":[")
        report.trades.forEachIndexed { i, t ->
            if (i > 0) append(',')
            append("{\"side\":\"${t.side}\",\"entryPrice\":${d(t.entryPrice)},\"exitPrice\":${d(t.exitPrice)},")
            append("\"quantity\":${d(t.quantity)},\"grossPnl\":${d(t.grossPnl)},\"fees\":${d(t.fees)},")
            append("\"funding\":${d(t.funding)},\"netPnl\":${d(t.netPnl)},\"entryTime\":${t.entryTime},\"exitTime\":${t.exitTime}}")
        }
        append("]}")
    }
}
