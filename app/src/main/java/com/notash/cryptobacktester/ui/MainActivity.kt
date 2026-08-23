package com.notash.cryptobacktester.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.notash.cryptobacktester.ai.AiReportFormatter
import com.notash.cryptobacktester.ai.BacktestComparator
import com.notash.cryptobacktester.ai.TradeAiAnalyzer
import com.notash.cryptobacktester.core.BacktestReport
import com.notash.cryptobacktester.export.BacktestExporter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { MaterialTheme { TradingDashboard() } } }
    private fun shareText(subject: String, body: String) { startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_SUBJECT, subject); putExtra(Intent.EXTRA_TEXT, body) }, "Share")) }
    private fun shareReport(report: BacktestReport, json: Boolean) { shareText("Notash Backtest ${if (json) "JSON" else "CSV"}", if (json) BacktestExporter.toJson(report) else BacktestExporter.toCsv(report)) }

    @Composable private fun TradingDashboard(vm: BacktestViewModel = viewModel()) {
        val state by vm.state.collectAsState(); val report = state.report
        var showAi by remember(report) { mutableStateOf(false) }
        val ai = remember(report, showAi) { if (showAi) report?.let(TradeAiAnalyzer::analyze) else null }
        Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState()).padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Column { Text("NOTASH", style = MaterialTheme.typography.headlineSmall); Text("CRYPTO FUTURES TERMINAL", style = MaterialTheme.typography.labelMedium) }; Text(if (state.isRunning) "● RUNNING" else "● READY") }
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { MarketSelector(state.market, vm::setMarket); TimeframeSelector(state.timeframe, vm::setTimeframe) }
                Text("Advanced Pullback v1 • LWMA 20/50 • ATR 14", style = MaterialTheme.typography.bodySmall)
                Button(vm::runBacktest, enabled = !state.isRunning, modifier = Modifier.fillMaxWidth()) { if (state.isRunning) CircularProgressIndicator(Modifier.size(20.dp)) else Text("RUN BACKTEST") }
                Text(state.status, style = MaterialTheme.typography.bodySmall)
            } }
            report?.let { r ->
                MetricGrid(r); EquityChart(r.equityCurve)
                Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("TRADE ANALYSIS", style = MaterialTheme.typography.titleMedium); Text("${r.trades.count { it.netPnl > 0 }} winners • ${r.trades.count { it.netPnl <= 0 }} losers • ${r.trades.size} total"); r.trades.takeLast(8).reversed().forEachIndexed { i,t -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("#${r.trades.size-i} ${t.side}"); Text(String.format("%.2f", t.netPnl)) } } } }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedButton({ shareReport(r,true) }, Modifier.weight(1f)) { Text("EXPORT JSON") }; OutlinedButton({ shareReport(r,false) }, Modifier.weight(1f)) { Text("EXPORT CSV") } }
                Button({ showAi = !showAi }, Modifier.fillMaxWidth()) { Text(if (showAi) "HIDE AI DIAGNOSIS" else "ANALYZE STRATEGY WITH AI") }
                ai?.let { analysis -> AiAnalysisCard(analysis); OutlinedButton({ shareText("Notash AI Strategy Diagnosis", AiReportFormatter.toText(analysis)) }, Modifier.fillMaxWidth()) { Text("SHARE AI DIAGNOSIS") } }
                state.previousReport?.let { old -> ComparisonCard(r, old) }
            }
            state.error?.let { Text("ERROR: $it") }
        }
    }

    @Composable private fun ComparisonCard(current: BacktestReport, previous: BacktestReport) {
        val c = remember(current, previous) { BacktestComparator.compare(current, previous) }
        Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("BACKTEST COMPARISON", style = MaterialTheme.typography.titleMedium); Text("Current vs previous run • Better: ${c.better}")
            Text("PNL Δ: ${"%.2f".format(c.pnlDelta)}   ROI Δ: ${"%.2f".format(c.roiDelta)}%")
            Text("Win Rate Δ: ${"%.2f".format(c.winRateDelta)}%   Drawdown Δ: ${"%.2f".format(c.drawdownDelta)}%")
            Text("Profit Factor Δ: ${"%.2f".format(c.profitFactorDelta)}")
        } }
    }

    @Composable private fun AiAnalysisCard(a: TradeAiAnalyzer.Analysis) { Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("AI STRATEGY DIAGNOSIS", style = MaterialTheme.typography.titleMedium); Text(a.strategy.summary); Text("STRENGTHS", style = MaterialTheme.typography.labelLarge); a.strategy.strengths.forEach { Text("• $it") }; Text("WEAKNESSES", style = MaterialTheme.typography.labelLarge); a.strategy.weaknesses.forEach { Text("• $it") }; Text("RECOMMENDATIONS", style = MaterialTheme.typography.labelLarge); a.strategy.recommendations.forEach { Text("• $it") }; Text("TRADE DIAGNOSIS", style = MaterialTheme.typography.labelLarge); a.trades.takeLast(6).reversed().forEach { d -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(10.dp)) { Text("#${d.index} ${d.outcome} • ${d.severity}"); Text(d.primaryCause); Text(d.recommendation) } } } } } }
    @Composable private fun MetricGrid(r: BacktestReport) { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { MetricCard("NET PNL",r.netPnl,modifier=Modifier.weight(1f)); MetricCard("ROI",r.roiPercent,"%",Modifier.weight(1f)) }; Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){MetricCard("WIN RATE",r.winRatePercent,"%",Modifier.weight(1f));MetricCard("DRAWDOWN",r.maxDrawdownPercent,"%",Modifier.weight(1f))};Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){MetricCard("PROFIT FACTOR",r.profitFactor,modifier=Modifier.weight(1f));MetricCard("FUNDING",r.totalFunding,modifier=Modifier.weight(1f))} } }
    @Composable private fun MetricCard(title:String,value:Double,suffix:String="",modifier:Modifier){Card(modifier){Column(Modifier.padding(12.dp)){Text(title,style=MaterialTheme.typography.labelSmall);Text(String.format("%.2f%s",value,suffix),style=MaterialTheme.typography.titleLarge)}}}
    @Composable private fun EquityChart(values:List<Double>){val lineColor=MaterialTheme.colorScheme.primary;Card(Modifier.fillMaxWidth()){Column(Modifier.padding(12.dp)){Text("EQUITY CURVE",style=MaterialTheme.typography.titleMedium);Spacer(Modifier.height(8.dp));if(values.size>1){Canvas(Modifier.fillMaxWidth().height(150.dp)){val min=values.minOrNull()?:0.0;val max=values.maxOrNull()?:1.0;val range=(max-min).takeIf{it>0}?:1.0;val path=Path();values.forEachIndexed{i,v->val x=size.width*i/values.lastIndex.toFloat();val y=size.height-((v-min)/range*size.height).toFloat();if(i==0)path.moveTo(x,y)else path.lineTo(x,y)};drawPath(path,lineColor,style=Stroke(width=4f))}}else Text("Run a backtest to generate the equity curve")}}}
    @Composable private fun MarketSelector(selected:String,onSelected:(String)->Unit){var expanded by remember{mutableStateOf(false)};Box{OutlinedButton({expanded=true}){Text(selected)};DropdownMenu(expanded,{expanded=false}){listOf("BTCUSDT","ETHUSDT","SOLUSDT").forEach{m->DropdownMenuItem(text={Text(m)},onClick={onSelected(m);expanded=false})}}}}
    @Composable private fun TimeframeSelector(selected:String,onSelected:(String)->Unit){var expanded by remember{mutableStateOf(false)};Box{OutlinedButton({expanded=true}){Text(selected)};DropdownMenu(expanded,{expanded=false}){listOf("1min","5min","15min","30min","1h","4h","1d").forEach{t->DropdownMenuItem(text={Text(t)},onClick={onSelected(t);expanded=false})}}}}
}
