package com.notash.cryptobacktester.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.notash.cryptobacktester.core.BacktestReport
import com.notash.cryptobacktester.core.Side

private val Bg = Color(0xFF070A12)
private val Panel = Color(0xFF0E1422)
private val Panel2 = Color(0xFF141C2E)
private val Accent = Color(0xFF8B5CF6)
private val Cyan = Color(0xFF22D3EE)
private val Green = Color(0xFF22C55E)
private val Red = Color(0xFFEF4444)
private val Muted = Color(0xFF8B95A7)

@Composable
fun ProfessionalTerminal() {
    val vm: BacktestViewModel = viewModel()
    val state by vm.state.collectAsState()
    var page by remember { mutableIntStateOf(0) }
    var market by remember { mutableStateOf(state.market) }
    var timeframe by remember { mutableStateOf(state.timeframe) }
    val report = state.report

    MaterialTheme(colorScheme = androidx.compose.material3.darkColorScheme(primary = Accent, secondary = Cyan, background = Bg, surface = Panel)) {
        Scaffold(containerColor = Bg, bottomBar = { BottomBar(page) { page = it } }) { padding ->
            Surface(Modifier.fillMaxSize().padding(padding), color = Bg) {
                AnimatedContent(targetState = page, label = "page") { target ->
                    when (target) {
                        0 -> Dashboard(state, report, market)
                        1 -> BacktestScreen(state, vm, market, timeframe, { market = it }, { timeframe = it })
                        2 -> TradesScreen(report)
                        else -> SettingsScreen()
                    }
                }
            }
        }
    }
}

@Composable
private fun Dashboard(state: BacktestUiState, report: BacktestReport?, market: String) {
    val pnlProgress by animateFloatAsState(if (report == null) 0f else (kotlin.math.abs(report.roiPercent).coerceAtMost(100.0) / 100.0).toFloat(), label = "pnl")
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("NOTASH", color = Cyan, fontSize = 12.sp, fontWeight = FontWeight.Black)
                    Text("Futures Command Center", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Bold)
                    Text("$market  •  CoinEx Futures", color = Muted, fontSize = 13.sp)
                }
                StatusPill(state)
            }
        }
        item { MetricGrid(report) }
        item {
            PanelCard {
                Text("EQUITY CURVE", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                if (report?.equityCurve?.isNotEmpty() == true) EquityChart(report.equityCurve) else EmptyChart()
                Spacer(Modifier.height(8.dp))
                Text(state.status, color = Muted, fontSize = 12.sp)
            }
        }
        item {
            PanelCard {
                Text("ENGINE", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("HTF LWMA 20/50  •  LTF LWMA 20  •  ATR 14", color = Color.White)
                Text("Limit 0.5 ATR  •  SL 1.5 ATR  •  TP 3 ATR", color = Muted, fontSize = 12.sp)
                Text("Risk-based sizing  •  leverage cap  •  funding aware", color = Muted, fontSize = 12.sp)
                Spacer(Modifier.height(10.dp))
                LinearPulse(pnlProgress)
            }
        }
    }
}

@Composable
private fun BacktestScreen(state: BacktestUiState, vm: BacktestViewModel, market: String, timeframe: String, setMarket: (String) -> Unit, setTimeframe: (String) -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("BACKTEST LAB", color = Cyan, fontSize = 12.sp, fontWeight = FontWeight.Black)
            Text("Run a real historical test", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
            Text("CoinEx data is downloaded by the engine. No artificial input-size cap.", color = Muted, fontSize = 12.sp)
        }
        item {
            PanelCard {
                Text("MARKET", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(market, setMarket, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Symbol") })
                Spacer(Modifier.height(8.dp))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("5m", "15m", "30m", "1h", "4h", "1d").forEach { tf ->
                        FilterChip(selected = timeframe == tf, onClick = { setTimeframe(tf); vm.setTimeframe(tf) }, label = { Text(tf) })
                    }
                }
            }
        }
        item {
            PanelCard {
                Text("EXECUTION", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                SettingLine("Initial balance", "1,000 USDT")
                SettingLine("Risk per trade", "1.0%")
                SettingLine("Leverage", "3x")
                SettingLine("Entry fee", "Maker")
                SettingLine("Exit fee", "Taker")
                SettingLine("Funding", "Enabled")
            }
        }
        item {
            Button(
                onClick = { vm.setMarket(market); vm.runBacktest() },
                enabled = !state.isRunning,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent)
            ) {
                if (state.isRunning) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp, color = Color.White)
                else Text("RUN BACKTEST", fontWeight = FontWeight.Bold)
            }
        }
        state.error?.let { error ->
            item { PanelCard { Text("ENGINE ERROR", color = Red, fontWeight = FontWeight.Bold); Text(error, color = Color.White, fontSize = 13.sp) } }
        }
        state.report?.let { r ->
            item { ResultSummary(r) }
        }
    }
}

@Composable
private fun TradesScreen(report: BacktestReport?) {
    val trades = report?.trades.orEmpty()
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Text("TRADE TAPE", color = Cyan, fontSize = 12.sp, fontWeight = FontWeight.Black)
            Text("Executed positions", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
        }
        if (trades.isEmpty()) item { PanelCard { Text("No trades yet. Run a backtest first.", color = Muted) } }
        items(trades.reversed()) { trade ->
            PanelCard {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(if (trade.side == Side.LONG) "LONG" else "SHORT", color = if (trade.side == Side.LONG) Green else Red, fontWeight = FontWeight.Bold)
                    Text(formatNumber(trade.netPnl) + " USDT", color = if (trade.netPnl >= 0) Green else Red, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(5.dp))
                Text("${formatNumber(trade.entryPrice)} → ${formatNumber(trade.exitPrice)}   qty ${formatNumber(trade.quantity)}", color = Color.White, fontSize = 12.sp)
                Text("Fees ${formatNumber(trade.fees)}   Funding ${formatNumber(trade.funding)}", color = Muted, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun SettingsScreen() {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("SYSTEM", color = Cyan, fontSize = 12.sp, fontWeight = FontWeight.Black); Text("Backtester settings", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold) }
        item { PanelCard { SettingLine("Data processing", "Streaming / chunked"); SettingLine("Artificial file limit", "None"); SettingLine("Min Android", "26+"); SettingLine("Target Android", "35"); SettingLine("Engine", "Crypto-native") } }
        item { PanelCard { Text("Designed for large historical datasets", color = Color.White, fontWeight = FontWeight.Bold); Text("The application does not impose a 50MB/100MB/500MB input cap. Device storage and available memory remain the only practical limits.", color = Muted, fontSize = 12.sp) } }
    }
}

@Composable
private fun MetricGrid(report: BacktestReport?) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MetricCard("BALANCE", report?.let { formatNumber(it.finalBalance) } ?: "—", Modifier.weight(1f))
        MetricCard("PNL", report?.let { formatNumber(it.netPnl) } ?: "—", Modifier.weight(1f), report?.let { if (it.netPnl >= 0) Green else Red })
    }
    Spacer(Modifier.height(8.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MetricCard("ROI", report?.let { "%.2f%%".format(it.roiPercent) } ?: "—", Modifier.weight(1f), report?.let { if (it.roiPercent >= 0) Green else Red })
        MetricCard("WIN RATE", report?.let { "%.1f%%".format(it.winRatePercent) } ?: "—", Modifier.weight(1f))
    }
}

@Composable
private fun MetricCard(title: String, value: String, modifier: Modifier, valueColor: Color = Color.White) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(14.dp)) { Text(title, color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(4.dp)); Text(value, color = valueColor, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun ResultSummary(r: BacktestReport) {
    PanelCard {
        Text("RESULT", color = Cyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        SettingLine("Trades", r.trades.size.toString())
        SettingLine("Win rate", "%.1f%%".format(r.winRatePercent))
        SettingLine("Max drawdown", "%.2f%%".format(r.maxDrawdownPercent))
        SettingLine("Profit factor", if (r.profitFactor.isInfinite()) "∞" else "%.2f".format(r.profitFactor))
        SettingLine("Fees", formatNumber(r.totalFees) + " USDT")
        SettingLine("Funding", formatNumber(r.totalFunding) + " USDT")
    }
}

@Composable
private fun StatusPill(state: BacktestUiState) {
    val color = if (state.isRunning) Cyan else if (state.error != null) Red else Green
    Surface(color = color.copy(alpha = .13f), shape = RoundedCornerShape(50)) { Text(if (state.isRunning) "RUNNING" else "READY", color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)) }
}

@Composable
private fun BottomBar(page: Int, select: (Int) -> Unit) {
    Surface(color = Panel) {
        Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf("HOME", "BACKTEST", "TRADES", "SETTINGS").forEachIndexed { index, label ->
                TextButton(onClick = { select(index) }) { Text(label, color = if (page == index) Cyan else Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
private fun PanelCard(content: @Composable ColumnScope.() -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp)) { Column(Modifier.padding(15.dp), content = content) }
}

@Composable
private fun SettingLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(label, color = Muted, fontSize = 12.sp); Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
}

@Composable
private fun EquityChart(values: List<Double>) {
    Canvas(Modifier.fillMaxWidth().height(190.dp).clip(RoundedCornerShape(12.dp)).background(Panel2).padding(8.dp)) {
        if (values.size < 2) return@Canvas
        val min = values.minOrNull() ?: return@Canvas
        val max = values.maxOrNull() ?: return@Canvas
        val range = (max - min).takeIf { it > 0 } ?: 1.0
        val path = Path()
        values.forEachIndexed { index, value ->
            val x = index.toFloat() / (values.lastIndex).coerceAtLeast(1) * size.width
            val y = size.height - ((value - min) / range).toFloat() * size.height
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color = Cyan, style = Stroke(width = 4f, cap = StrokeCap.Round))
    }
}

@Composable
private fun EmptyChart() { Box(Modifier.fillMaxWidth().height(190.dp).clip(RoundedCornerShape(12.dp)).background(Panel2), contentAlignment = Alignment.Center) { Text("Run a backtest to populate the chart", color = Muted, fontSize = 12.sp) } }

@Composable
private fun LinearPulse(progress: Float) { Box(Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(4.dp)).background(Panel2)) { Box(Modifier.fillMaxWidth(progress.coerceIn(.02f, 1f)).height(5.dp).background(Cyan)) } }

private fun formatNumber(value: Double): String = if (value.isFinite()) "%,.4f".format(value) else "∞"
