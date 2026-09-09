package com.notash.cryptobacktester.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notash.cryptobacktester.core.BacktestReport
import com.notash.cryptobacktester.core.TradeResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val ReportBg = Color(0xFF070A10)
private val ReportPanel = Color(0xFF0F1722)
private val ReportPanel2 = Color(0xFF151F2C)
private val ReportGreen = Color(0xFF2BD69A)
private val ReportRed = Color(0xFFFF6074)
private val ReportGold = Color(0xFFFFC857)
private val ReportMuted = Color(0xFF8995A8)
private val ReportDivider = Color(0xFF263343)

@Composable
fun TradeByTradeReport(fa: Boolean, report: BacktestReport?, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.background(ReportBg).padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Spacer(Modifier.height(6.dp))
            Text(if (fa) "گزارش کامل معاملات" else "Complete Trade-by-Trade Report", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
            Text(
                if (report == null) if (fa) "پس از اجرای بک‌تست، جزئیات هر معامله اینجا نمایش داده می‌شود." else "Run a backtest to populate the complete trade-level report."
                else if (fa) "${report.trades.size} معامله • ${report.timeframe} • ${formatLeverage(report.leverage)}x" else "${report.trades.size} trades • ${report.timeframe} • ${formatLeverage(report.leverage)}x",
                color = ReportMuted, fontSize = 11.sp
            )
        }
        if (report != null) itemsIndexed(report.trades) { index, trade -> TradeCard(fa, index + 1, trade) }
    }
}

@Composable
private fun TradeCard(fa: Boolean, number: Int, trade: TradeResult) {
    val positive = trade.netPnl >= 0.0
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = ReportPanel), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.background(if (trade.side.name == "LONG") ReportGreen.copy(.14f) else ReportRed.copy(.14f), RoundedCornerShape(10.dp)).padding(horizontal = 9.dp, vertical = 5.dp)) {
                    Text(trade.side.name, color = if (trade.side.name == "LONG") ReportGreen else ReportRed, fontWeight = FontWeight.Black, fontSize = 11.sp)
                }
                Spacer(Modifier.weight(1f)); Text("#%03d".format(number), color = ReportMuted, fontWeight = FontWeight.Bold, fontSize = 11.sp); Spacer(Modifier.weight(1f))
                Text(if (trade.isWin) "WIN" else "LOSS", color = if (trade.isWin) ReportGreen else ReportRed, fontWeight = FontWeight.Black, fontSize = 11.sp)
            }
            HorizontalDivider(color = ReportDivider)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCell("Entry", price(trade.entryPrice), Modifier.weight(1f)); MetricCell("Exit", price(trade.exitPrice), Modifier.weight(1f)); MetricCell("TF", trade.timeframe, Modifier.weight(1f))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCell("SL", price(trade.stopLoss), Modifier.weight(1f)); MetricCell("TP", price(trade.takeProfit), Modifier.weight(1f)); MetricCell(if (fa) "اهرم" else "Leverage", formatLeverage(trade.leverage) + "x", Modifier.weight(1f))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCell(if (fa) "اندازه" else "Position", compact(trade.quantity), Modifier.weight(1f)); MetricCell(if (fa) "SL لمس شد؟" else "SL touched?", if (trade.slTouched) "YES" else "NO", Modifier.weight(1f), if (trade.slTouched) ReportRed else ReportGreen); MetricCell(if (fa) "خروج" else "Exit reason", trade.exitReason, Modifier.weight(1f), ReportGold)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCell(if (fa) "ورود" else "Entry time", time(trade.entryTime), Modifier.weight(1f)); MetricCell(if (fa) "خروج" else "Exit time", time(trade.exitTime), Modifier.weight(1f))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCell("Fees", "%.4f".format(trade.fees), Modifier.weight(1f)); MetricCell("Funding", "%.4f".format(trade.funding), Modifier.weight(1f))
                }
            }
            HorizontalDivider(color = ReportDivider)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column { Text(if (fa) "PnL خالص" else "Net PnL", color = ReportMuted, fontSize = 9.sp); Text("%+.4f".format(trade.netPnl), color = if (positive) ReportGreen else ReportRed, fontWeight = FontWeight.Black, fontSize = 18.sp) }
                Column(horizontalAlignment = Alignment.End) { Text("PnL %", color = ReportMuted, fontSize = 9.sp); Text("%+.2f%%".format(trade.pnlPercent), color = if (positive) ReportGreen else ReportRed, fontWeight = FontWeight.Black, fontSize = 15.sp) }
            }
        }
    }
}

@Composable private fun MetricCell(label: String, value: String, modifier: Modifier, valueColor: Color = Color.White) {
    Column(modifier.background(ReportPanel2, RoundedCornerShape(12.dp)).padding(9.dp)) { Text(label, color = ReportMuted, fontSize = 8.sp); Spacer(Modifier.height(3.dp)); Text(value, color = valueColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
}
private fun time(value: Long): String = if (value <= 0L) "—" else SimpleDateFormat("MM/dd HH:mm", Locale.US).format(Date(value))
private fun price(value: Double): String = if (value == 0.0) "—" else "%.6f".format(value)
private fun compact(value: Double): String = if (value >= 1_000_000) "%.2fM".format(value / 1_000_000.0) else if (value >= 1_000) "%.2fK".format(value / 1_000.0) else "%.4f".format(value)
private fun formatLeverage(value: Double): String = if (value <= 0.0) "—" else "%.1f".format(value)
