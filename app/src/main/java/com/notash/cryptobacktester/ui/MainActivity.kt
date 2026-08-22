package com.notash.cryptobacktester.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.notash.cryptobacktester.core.BacktestReport
import com.notash.cryptobacktester.export.BacktestExporter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { TradingDashboard() } }
    }

    private fun shareReport(report: BacktestReport, json: Boolean) {
        val body = if (json) BacktestExporter.toJson(report) else BacktestExporter.toCsv(report)
        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Notash Backtest ${if (json) "JSON" else "CSV"}")
            putExtra(Intent.EXTRA_TEXT, body)
        }, "Share backtest ${if (json) "JSON" else "CSV"}"))
    }

    @Composable
    private fun TradingDashboard(vm: BacktestViewModel = viewModel()) {
        val state by vm.state.collectAsState()
        val report = state.report
        Column(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState()).padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("NOTASH", style = MaterialTheme.typography.headlineSmall)
                    Text("CRYPTO FUTURES TERMINAL", style = MaterialTheme.typography.labelMedium)
                }
                Text(if (state.isRunning) "● RUNNING" else "● READY", style = MaterialTheme.typography.labelLarge)
            }

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        MarketSelector(state.market, vm::setMarket)
                        TimeframeSelector(state.timeframe, vm::setTimeframe)
                    }
                    Text("Advanced Pullback v1 • LWMA 20/50 • ATR 14", style = MaterialTheme.typography.bodySmall)
                    Button(onClick = vm::runBacktest, enabled = !state.isRunning, modifier = Modifier.fillMaxWidth()) {
                        if (state.isRunning) CircularProgressIndicator(Modifier.size(20.dp)) else Text("RUN BACKTEST")
                    }
                    Text(state.status, style = MaterialTheme.typography.bodySmall)
                }
            }

            report?.let { r ->
                MetricGrid(r)
                EquityChart(r.equityCurve)
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("TRADE ANALYSIS", style = MaterialTheme.typography.titleMedium)
                        Text("${r.trades.count { it.netPnl > 0 }} winners  •  ${r.trades.count { it.netPnl <= 0 }} losers  •  ${r.trades.size} total")
                        r.trades.takeLast(8).reversed().forEachIndexed { index, trade ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("#${r.trades.size - index} ${trade.side}")
                                Text(String.format("%.2f", trade.netPnl))
                            }
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { shareReport(r, true) }, modifier = Modifier.weight(1f)) { Text("EXPORT JSON") }
                    OutlinedButton(onClick = { shareReport(r, false) }, modifier = Modifier.weight(1f)) { Text("EXPORT CSV") }
                }
                Button(onClick = { shareReport(r, true) }, modifier = Modifier.fillMaxWidth()) {
                    Text("ANALYZE WITH AI  →  SHARE JSON")
                }
            }
            state.error?.let { Text("ERROR: $it") }
        }
    }

    @Composable
    private fun MetricGrid(r: BacktestReport) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricCard("NET PNL", r.netPnl, Modifier.weight(1f))
                MetricCard("ROI", r.roiPercent, "%", Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricCard("WIN RATE", r.winRatePercent, "%", Modifier.weight(1f))
                MetricCard("DRAWDOWN", r.maxDrawdownPercent, "%", Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricCard("PROFIT FACTOR", r.profitFactor, "", Modifier.weight(1f))
                MetricCard("FUNDING", r.totalFunding, "", Modifier.weight(1f))
            }
        }
    }

    @Composable
    private fun MetricCard(title: String, value: Double, suffix: String = "", modifier: Modifier) {
        Card(modifier) { Column(Modifier.padding(12.dp)) { Text(title, style = MaterialTheme.typography.labelSmall); Text(String.format("%.2f%s", value, suffix), style = MaterialTheme.typography.titleLarge) } }
    }

    @Composable
    private fun EquityChart(values: List<Double>) {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                Text("EQUITY CURVE", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                if (values.size > 1) {
                    Canvas(Modifier.fillMaxWidth().height(150.dp)) {
                        val min = values.minOrNull() ?: 0.0
                        val max = values.maxOrNull() ?: 1.0
                        val range = (max - min).takeIf { it > 0 } ?: 1.0
                        val path = Path()
                        values.forEachIndexed { i, value ->
                            val x = size.width * i / (values.lastIndex.toFloat())
                            val y = size.height - ((value - min) / range * size.height).toFloat()
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        drawPath(path, MaterialTheme.colorScheme.primary, style = Stroke(width = 4f))
                    }
                } else Text("Run a backtest to generate the equity curve")
            }
        }
    }

    @Composable
    private fun MarketSelector(selected: String, onSelected: (String) -> Unit) {
        var expanded by remember { mutableStateOf(false) }
        Box {
            OutlinedButton(onClick = { expanded = true }) { Text(selected) }
            DropdownMenu(expanded, { expanded = false }) {
                listOf("BTCUSDT", "ETHUSDT", "SOLUSDT").forEach { market ->
                    DropdownMenuItem(text = { Text(market) }, onClick = { onSelected(market); expanded = false })
                }
            }
        }
    }

    @Composable
    private fun TimeframeSelector(selected: String, onSelected: (String) -> Unit) {
        var expanded by remember { mutableStateOf(false) }
        Box {
            OutlinedButton(onClick = { expanded = true }) { Text(selected) }
            DropdownMenu(expanded, { expanded = false }) {
                listOf("1min", "5min", "15min", "30min", "1h", "4h", "1d").forEach { tf ->
                    DropdownMenuItem(text = { Text(tf) }, onClick = { onSelected(tf); expanded = false })
                }
            }
        }
    }
}
