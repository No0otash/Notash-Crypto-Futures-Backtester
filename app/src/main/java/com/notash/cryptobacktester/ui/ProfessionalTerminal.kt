package com.notash.cryptobacktester.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.notash.cryptobacktester.core.MarketTicker
import com.notash.cryptobacktester.data.CoinExRepository
import com.notash.cryptobacktester.engine.LiveBacktestState
import com.notash.cryptobacktester.export.BacktestExportManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Local UI model deliberately exposes the rule fields consumed by StrategyPage.
data class StrategyPackage(
    val id: String,
    val name: String,
    val version: String,
    val entryRules: String,
    val exitRules: String,
    val riskRules: String
)

@Composable
fun ProfessionalTerminal() {
    var page by remember { mutableStateOf(TerminalPage.MARKET) }
    var persian by remember { mutableStateOf(true) }
    val liveState = remember { LiveBacktestState() }
    val backtestVm = remember { BacktestViewModel() }
    val backtestState by backtestVm.state.collectAsState()
    var strategy by remember { mutableStateOf(StrategyPackage("ema-v1", "EMA Trend", "1.0", "EMA20 > EMA50", "EMA20 < EMA50", "1% risk")) }
    var timeframe by remember { mutableStateOf("1h") }
    var riskPercent by remember { mutableStateOf(1.0) }
    var leverage by remember { mutableStateOf(3.0) }
    var fundingEnabled by remember { mutableStateOf(true) }
    var makerFee by remember { mutableStateOf(0.02) }
    var takerFee by remember { mutableStateOf(0.05) }
    Scaffold(bottomBar = { TerminalNavigation(page, { page = it }, persian) }) { padding ->
        Surface(Modifier.fillMaxSize().padding(padding)) {
            when (page) {
                TerminalPage.MARKET -> MarketPage(persian, timeframe) { timeframe = it }
                TerminalPage.FUTURES -> FuturesPage(persian, leverage) { leverage = it }
                TerminalPage.BACKTEST -> BacktestPage(persian, strategy, timeframe, riskPercent, leverage, fundingEnabled, backtestVm, backtestState.report)
                TerminalPage.LIVE_BACKTEST -> LiveBacktestPage(persian, liveState, strategy)
                TerminalPage.STRATEGY -> StrategyPage(persian, strategy) { strategy = it }
                TerminalPage.AI -> AiPage(persian)
                TerminalPage.TRADES -> TradesPage(persian, backtestState.report)
                TerminalPage.SETTINGS -> SettingsPage(persian, { persian = it }, timeframe, { timeframe = it }, riskPercent, { riskPercent = it }, leverage, { leverage = it }, fundingEnabled, { fundingEnabled = it }, makerFee, { makerFee = it }, takerFee, { takerFee = it })
            }
        }
    }
}

@Composable private fun Page(title: String, content: @Composable ColumnScope.() -> Unit) = Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
    Text("NOTASH", style = MaterialTheme.typography.titleLarge); Text(title, style = MaterialTheme.typography.headlineSmall); content()
}

private val supportedTimeframes = listOf("1m", "3m", "5m", "15m", "30m", "1h", "2h", "4h", "6h", "12h", "1d", "3d", "1w")

@Composable private fun MarketPage(fa: Boolean, timeframe: String, onTimeframe: (String) -> Unit) {
    var market by remember { mutableStateOf("BTCUSDT") }
    var ticker by remember { mutableStateOf<MarketTicker?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val repository = remember { CoinExRepository() }
    val scope = rememberCoroutineScope()
    LaunchedEffect(market) {
        while (true) {
            try { ticker = repository.loadLatestTicker(market); error = null } catch (e: Exception) { error = e.message ?: "Network error" }
            delay(5000)
        }
    }
    Page(if (fa) "روند بازار" else "Market Trend") {
        OutlinedTextField(market, { market = it.trim().uppercase() }, label = { Text(if (fa) "نماد CoinEx" else "CoinEx market") }, modifier = Modifier.fillMaxWidth())
        Text(if (fa) "تایم‌فریم فعال: $timeframe" else "Active timeframe: $timeframe")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) { supportedTimeframes.take(7).forEach { tf -> FilterChip(timeframe == tf, { onTimeframe(tf) }, label = { Text(tf) }) } }
        val t = ticker
        if (t != null) {
            Text("${t.market}   ${"%.8f".format(t.last)}   ${"%.2f".format(t.changeRate * 100)}%   ${if (t.changeRate >= 0) "🟢" else "🔴"}")
            Text("Volume: ${"%.2f".format(t.volume)}   Mark: ${"%.8f".format(t.markPrice)}")
        } else Text(error ?: if (fa) "در حال دریافت قیمت واقعی CoinEx..." else "Loading live CoinEx price...")
        Button({ scope.launch { try { ticker = repository.loadLatestTicker(market); error = null } catch (e: Exception) { error = e.message } } }) { Text(if (fa) "بروزرسانی" else "Refresh") }
        Text(if (fa) "کندل‌ها از OHLC واقعی CoinEx در مسیر Backtest/Market Data تأمین می‌شوند." else "OHLC candles are sourced from the real CoinEx market-data layer.")
    }
}

@Composable private fun FuturesPage(fa: Boolean, leverage: Double, onLeverage: (Double) -> Unit) = Page(if (fa) "فیوچرز" else "Futures") {
    Text("${if (fa) "اهرم" else "Leverage"}: ${"%.1f".format(leverage)}x")
    Slider(leverage.toFloat(), { onLeverage(it.toDouble()) }, valueRange = 1f..100f, steps = 98)
    Row { Button({}) { Text("LONG") }; Spacer(Modifier.width(8.dp)); OutlinedButton({}) { Text("SHORT") } }
}

@Composable private fun BacktestPage(fa: Boolean, strategy: StrategyPackage, timeframe: String, risk: Double, leverage: Double, funding: Boolean, vm: BacktestViewModel, report: com.notash.cryptobacktester.core.BacktestReport?) {
    val context = LocalContext.current
    Page(if (fa) "بک‌تست" else "Backtest") {
        Text("Strategy: ${strategy.name} v${strategy.version}")
        Text("TF $timeframe • Risk ${"%.2f".format(risk)}% • Leverage ${"%.1f".format(leverage)}x • Funding ${if (funding) "ON" else "OFF"}")
        Button({ vm.setTimeframe(timeframe); vm.setRiskPercent(risk); vm.setLeverage(leverage); vm.setUseFunding(funding); vm.runBacktest() }) { Text(if (fa) "اجرای بک‌تست" else "Run Backtest") }
        report?.let { r ->
            Text("Balance: %.2f • PnL: %.2f • ROI: %.2f%%".format(r.finalBalance, r.netPnl, r.roiPercent))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button({ if (BacktestExportManager.save(context, "backtest_${BacktestExportManager.timestamp()}.csv", BacktestExportManager.csv(r), "text/csv")) Toast.makeText(context, "CSV saved", Toast.LENGTH_SHORT).show() }) { Text("CSV") }
                Button({ if (BacktestExportManager.save(context, "backtest_${BacktestExportManager.timestamp()}.json", BacktestExportManager.json(r), "application/json")) Toast.makeText(context, "JSON saved", Toast.LENGTH_SHORT).show() }) { Text("JSON") }
                OutlinedButton({ if (BacktestExportManager.save(context, "ai_report_${BacktestExportManager.timestamp()}.txt", BacktestExportManager.aiReport(r), "text/plain")) Toast.makeText(context, "AI report saved", Toast.LENGTH_SHORT).show() }) { Text("AI Report") }
            }
        }
    }
}

@Composable private fun LiveBacktestPage(fa: Boolean, state: LiveBacktestState, strategy: StrategyPackage) = Page(if (fa) "بک‌تست زنده" else "Live Backtest") {
    val running by state.running.collectAsState(); val equity by state.equity.collectAsState(); val pnl by state.pnl.collectAsState(); val trades by state.trades.collectAsState(); val win by state.winRate.collectAsState()
    Text("Strategy: ${strategy.name} v${strategy.version}"); Text(if (running) "🔴 RUNNING" else "⏸ STOPPED")
    Row { Button({ state.start() }) { Text(if (fa) "شروع" else "Start") }; Spacer(Modifier.width(6.dp)); OutlinedButton({ state.stop() }) { Text(if (fa) "توقف" else "Stop") }; Spacer(Modifier.width(6.dp)); OutlinedButton({ state.reset() }) { Text(if (fa) "ریست" else "Reset") } }
    Text("Equity: %.2f USDT".format(equity)); Text("PNL: %.2f USDT".format(pnl)); Text("Trades: $trades   Win Rate: %.1f%%".format(win))
}

@Composable private fun StrategyPage(fa: Boolean, current: StrategyPackage, onChange: (StrategyPackage) -> Unit) = Page(if (fa) "مدیریت استراتژی" else "Strategy Manager") {
    var name: String by remember(current.id) { mutableStateOf(current.name) }
    var version: String by remember(current.id) { mutableStateOf(current.version) }
    var entry: String by remember(current.id) { mutableStateOf(current.entryRules) }
    var exit: String by remember(current.id) { mutableStateOf(current.exitRules) }
    var risk: String by remember(current.id) { mutableStateOf(current.riskRules) }
    OutlinedTextField(name, { name = it }, label = { Text(if (fa) "نام Strategy" else "Strategy name") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(version, { version = it }, label = { Text("Version") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(entry, { entry = it }, label = { Text(if (fa) "قوانین ورود" else "Entry rules") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(exit, { exit = it }, label = { Text(if (fa) "قوانین خروج" else "Exit rules") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(risk, { risk = it }, label = { Text(if (fa) "قوانین ریسک" else "Risk rules") }, modifier = Modifier.fillMaxWidth())
    Button({ onChange(StrategyPackage(current.id, name.trim().ifBlank { current.name }, version.trim().ifBlank { current.version }, entry.trim(), exit.trim(), risk.trim())) }) { Text(if (fa) "ذخیره و فعال‌سازی" else "Save & Activate") }; OutlinedButton({}) { Text("Import Bot / Strategy (JSON/ZIP)") }
}

@Composable private fun AiPage(fa: Boolean) = Page(if (fa) "تحلیل هوشمند" else "AI Analyst") { Text(if (fa) "تحلیل فارسی علت سود/زیان و پیشنهاد بهینه‌سازی Strategy." else "Trade diagnosis and strategy optimization.") }

@Composable private fun TradesPage(fa: Boolean, report: com.notash.cryptobacktester.core.BacktestReport?) = Page(if (fa) "معاملات" else "Trades") {
    if (report == null) Text(if (fa) "هنوز گزارشی اجرا نشده است." else "No backtest report yet.") else report.trades.forEachIndexed { i, t -> Text("#${i + 1} ${t.side}  ${t.entryPrice} → ${t.exitPrice}  PnL ${"%.4f".format(t.netPnl)}") }
}

@Composable private fun SettingsPage(persian: Boolean, onLang: (Boolean) -> Unit, timeframe: String, onTimeframe: (String) -> Unit, riskPercent: Double, onRisk: (Double) -> Unit, leverage: Double, onLeverage: (Double) -> Unit, fundingEnabled: Boolean, onFunding: (Boolean) -> Unit, makerFee: Double, onMakerFee: (Double) -> Unit, takerFee: Double, onTakerFee: (Double) -> Unit) = Page(if (persian) "تنظیمات" else "Settings") {
    Row { Text(if (persian) "زبان فارسی" else "Persian language"); Spacer(Modifier.weight(1f)); Switch(persian, onLang) }
    Text(if (persian) "Timeframe بک‌تست" else "Backtest timeframe")
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) { supportedTimeframes.forEach { tf -> FilterChip(timeframe == tf, { onTimeframe(tf) }, label = { Text(tf) }) } }
    Text("Risk: ${"%.2f".format(riskPercent)}%"); Slider(riskPercent.toFloat(), { onRisk(it.toDouble()) }, valueRange = 0.1f..10f, steps = 99)
    Text("Leverage: ${"%.1f".format(leverage)}x"); Slider(leverage.toFloat(), { onLeverage(it.toDouble()) }, valueRange = 1f..100f, steps = 98)
    Row { Text("Funding"); Spacer(Modifier.weight(1f)); Switch(fundingEnabled, onFunding) }
    OutlinedTextField(makerFee.toString(), { it.toDoubleOrNull()?.let { v -> onMakerFee(v.coerceIn(0.0, 10.0)) } }, label = { Text("Maker fee %") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(takerFee.toString(), { it.toDoubleOrNull()?.let { v -> onTakerFee(v.coerceIn(0.0, 10.0)) } }, label = { Text("Taker fee %") }, modifier = Modifier.fillMaxWidth())
}