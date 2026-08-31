package com.notash.cryptobacktester.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notash.cryptobacktester.core.BacktestReport
import com.notash.cryptobacktester.core.Candle
import com.notash.cryptobacktester.core.TradeResult
import com.notash.cryptobacktester.data.CoinExRepository
import com.notash.cryptobacktester.intelligence.PumpDumpDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

private val Bg = Color(0xFF070A10)
private val Panel = Color(0xFF0F1722)
private val Panel2 = Color(0xFF151F2C)
private val Line = Color(0xFF263343)
private val Mint = Color(0xFF12C8B5)
private val Blue = Color(0xFF4D7CFF)
private val Green = Color(0xFF2BD69A)
private val Red = Color(0xFFFF6074)
private val Gold = Color(0xFFFFC857)
private val Muted = Color(0xFF8995A8)

private data class Quote(val symbol: String, val price: Double, val change: Double, val volume: Double)
private data class Radar(val quote: Quote, val score: Int)

@Composable
fun ProfessionalTerminal() {
    var page by rememberSaveable { mutableStateOf(TerminalPage.MARKET) }
    var fa by rememberSaveable { mutableStateOf(true) }
    var settings by rememberSaveable { mutableStateOf(false) }
    var market by rememberSaveable { mutableStateOf("BTCUSDT") }
    var timeframe by rememberSaveable { mutableStateOf("15min") }
    var supportEmail by rememberSaveable { mutableStateOf("") }
    var notifications by rememberSaveable { mutableStateOf(true) }
    var privacy by rememberSaveable { mutableStateOf(true) }
    val vm = remember { BacktestViewModel() }
    val state by vm.state.collectAsStateWithLifecycle()

    MaterialTheme(colorScheme = darkColorScheme(primary = Mint, secondary = Blue, background = Bg, surface = Panel)) {
        if (settings) {
            SettingsPage(fa, { fa = it }, supportEmail, { supportEmail = it }, notifications, { notifications = it }, privacy, { privacy = it }) { settings = false }
        } else {
            Scaffold(containerColor = Bg, topBar = {
                AlvexHeader(fa, page, onAi = { page = TerminalPage.AI }, onSettings = { settings = true }, onLanguage = { fa = !fa })
            }, bottomBar = { TerminalNavigation(page, { page = it }, fa) }) { pad ->
                Box(Modifier.fillMaxSize().padding(pad)) {
                    when (page) {
                        TerminalPage.MARKET -> HomePage(fa, market, { market = it }, state.report, { page = it })
                        TerminalPage.MARKETS -> MarketsPage(fa, market, { market = it; vm.setMarket(it) }) { page = TerminalPage.BACKTEST }
                        TerminalPage.BACKTEST -> BacktestPage(fa, market, { market = it; vm.setMarket(it) }, timeframe, { timeframe = it }, vm, state)
                        TerminalPage.STRATEGY -> StrategyPage(fa)
                        TerminalPage.INTELLIGENCE -> IntelligencePage(fa, market)
                        TerminalPage.AI -> AiPage(fa, market, state.report)
                    }
                }
            }
        }
    }
}

@Composable
private fun AlvexHeader(fa: Boolean, page: TerminalPage, onAi: () -> Unit, onSettings: () -> Unit, onLanguage: () -> Unit) {
    Box(Modifier.fillMaxWidth().background(Bg).height(68.dp)) {
        IconButton(onClick = onAi, modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp)) { Icon(Icons.Outlined.AutoAwesome, "AI", tint = Mint) }
        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("ALVEX", color = Color.White, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            Text(if (fa) page.titleFa else page.titleEn, color = Muted, fontSize = 10.sp)
        }
        Row(Modifier.align(Alignment.CenterEnd).padding(end = 6.dp)) {
            IconButton(onClick = onLanguage) { Text(if (fa) "EN" else "FA", color = Mint, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
            IconButton(onClick = onSettings) { Icon(Icons.Outlined.Settings, "Settings", tint = Color.White) }
        }
    }
}

@Composable
private fun HomePage(fa: Boolean, market: String, onMarket: (String) -> Unit, report: BacktestReport?, open: (TerminalPage) -> Unit) {
    val repo = remember { CoinExRepository() }
    val scope = rememberCoroutineScope()
    val symbols = remember { listOf("BTCUSDT", "ETHUSDT", "SOLUSDT", "XRPUSDT", "DOGEUSDT", "PEPEUSDT", "SUIUSDT", "PUMPUSDT") }
    var quotes by remember { mutableStateOf<List<Quote>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    fun refresh() { scope.launch { loading = true; quotes = withContext(Dispatchers.IO) { symbols.mapNotNull { s -> runCatching { repo.loadLatestTicker(s) }.getOrNull()?.let { Quote(s, it.last, it.changeRate * 100, it.volume) } } }; loading = false } }
    LaunchedEffect(Unit) { refresh() }
    val radar = quotes.map { Radar(it, ((abs(it.change) * 10) + if (it.volume > 10_000_000) 20 else 5).coerceIn(0.0, 99.0).toInt()) }.sortedByDescending { it.score }.take(5)

    LazyColumn(Modifier.fillMaxSize().background(Bg).padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
        item { HeroCard(fa, market, onMarket, quotes.firstOrNull { it.symbol == market }, loading, refresh) }
        item { TitleBlock(if (fa) "رادار هوشمند بازار" else "AI Market Radar", "Live CoinEx • explainable signals") }
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) { ActionCard("⚡", if (fa) "پامپ / دامپ" else "Pump / Dump", Mint) { open(TerminalPage.INTELLIGENCE) }; ActionCard("🐋", if (fa) "نهنگ" else "Whales", Blue) { open(TerminalPage.INTELLIGENCE) }; ActionCard("🧠", "AI Hub", Gold) { open(TerminalPage.AI) } } }
        item { RadarCard(fa, radar, onMarket) }
        item { TitleBlock(if (fa) "نرخ ارزهای منتخب" else "Market Pulse", "Live price • 24h change • volume") }
        items(quotes.take(6)) { QuoteCard(it) { onMarket(it.symbol); open(TerminalPage.MARKETS) } }
        item { TitleBlock(if (fa) "Curve و تشخیص معاملات ربات" else "Curve & Robot Diagnostics", "Touch the chart to inspect candle data") }
        item { CurveCard(fa, report) { open(TerminalPage.BACKTEST) } }
        item { TitleBlock(if (fa) "فضای حرفه‌ای ALVEX" else "ALVEX Workspace", "All modules are connected") }
        item { Workspace(fa, open) }
    }
}

@Composable private fun HeroCard(fa: Boolean, market: String, onMarket: (String) -> Unit, quote: Quote?, loading: Boolean, refresh: () -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = Panel)) {
        Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(if (fa) "مرکز فرمان بازار" else "Market Command Center", color = Muted, fontSize = 11.sp); Text("ALVEX", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black) }; IconButton({ refresh() }) { Icon(Icons.Outlined.Refresh, null, tint = Mint) } }
            Row(verticalAlignment = Alignment.CenterVertically) { OutlinedTextField(market, { onMarket(it.uppercase()) }, Modifier.weight(1f), label = { Text(if (fa) "نماد بازار" else "Market") }, singleLine = true); Spacer(Modifier.width(10.dp)); Column(horizontalAlignment = Alignment.End) { Text(if (loading) "…" else quote?.let { price(it.price) } ?: "—", color = Color.White, fontWeight = FontWeight.Black); Text(if (loading) "" else quote?.let { "%+.2f%%".format(it.change) } ?: "", color = if ((quote?.change ?: 0.0) >= 0) Green else Red, fontSize = 11.sp) } }
        }
    }
}

@Composable private fun ActionCard(icon: String, title: String, color: Color, onClick: () -> Unit) { Card(Modifier.weight(1f), onClick = onClick, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Panel2)) { Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(icon, fontSize = 23.sp); Spacer(Modifier.height(4.dp)); Text(title, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold) } } }

@Composable private fun RadarCard(fa: Boolean, radar: List<Radar>, onMarket: (String) -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Panel)) {
        Column(Modifier.padding(13.dp)) {
            if (radar.isEmpty()) Text(if (fa) "در حال دریافت داده واقعی..." else "Loading live market data...", color = Muted)
            radar.forEach { r ->
                Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(if (r.quote.change >= 0) Green.copy(.12f) else Red.copy(.12f)), contentAlignment = Alignment.Center) { Text(if (r.quote.change >= 0) "↑" else "↓", color = if (r.quote.change >= 0) Green else Red, fontWeight = FontWeight.Black) }
                    Spacer(Modifier.width(9.dp)); Column(Modifier.weight(1f)) { Text(r.quote.symbol.removeSuffix("USDT"), color = Color.White, fontWeight = FontWeight.Bold); Text(if (r.quote.change >= 0) "PUMP WATCH" else "DUMP WATCH", color = Muted, fontSize = 9.sp) }
                    Text("${r.score}", color = Gold, fontWeight = FontWeight.Black); Spacer(Modifier.width(8.dp)); Text("%+.2f".format(r.quote.change), color = if (r.quote.change >= 0) Green else Red, fontSize = 11.sp)
                }
                HorizontalDivider(color = Line)
            }
        }
    }
}

@Composable private fun QuoteCard(q: Quote, onClick: () -> Unit) { Card(Modifier.fillMaxWidth(), onClick = onClick, shape = RoundedCornerShape(17.dp), colors = CardDefaults.cardColors(containerColor = Panel)) { Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(Panel2), contentAlignment = Alignment.Center) { Text(q.symbol.take(1), color = Mint, fontWeight = FontWeight.Black) }; Spacer(Modifier.width(9.dp)); Column(Modifier.weight(1f)) { Text(q.symbol.removeSuffix("USDT"), color = Color.White, fontWeight = FontWeight.Bold); Text("USDT • ${compact(q.volume)} vol", color = Muted, fontSize = 9.sp) }; Column(horizontalAlignment = Alignment.End) { Text(price(q.price), color = Color.White, fontWeight = FontWeight.SemiBold); Text("%+.2f%%".format(q.change), color = if (q.change >= 0) Green else Red, fontSize = 11.sp) } } } }

@Composable private fun CurveCard(fa: Boolean, report: BacktestReport?, open: () -> Unit) { Card(Modifier.fillMaxWidth(), onClick = open, shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Panel)) { Column(Modifier.padding(14.dp)) { Row(Modifier.fillMaxWidth()) { Text(if (fa) "Equity Curve" else "Equity Curve", color = Color.White, fontWeight = FontWeight.Bold, Modifier.weight(1f)); Text(if (report == null) "READY" else "REPORT", color = Mint, fontSize = 9.sp) }; Spacer(Modifier.height(8.dp)); EquityChart(report?.equityCurve ?: emptyList(), Modifier.fillMaxWidth().height(120.dp)); Spacer(Modifier.height(8.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { SmallMetric("ROI", report?.roiPercent?.let { "%.2f%%".format(it) } ?: "—"); SmallMetric("PnL", report?.netPnl?.let { "%.2f".format(it) } ?: "—"); SmallMetric("DD", report?.maxDrawdownPercent?.let { "%.2f%%".format(it) } ?: "—") } } } }

@Composable private fun Workspace(fa: Boolean, open: (TerminalPage) -> Unit) { Column(verticalArrangement = Arrangement.spacedBy(9.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) { Tile(Icons.Outlined.ShowChart, if (fa) "بازار" else "Markets") { open(TerminalPage.MARKETS) }; Tile(Icons.Outlined.Science, if (fa) "استراتژی" else "Strategy Lab") { open(TerminalPage.STRATEGY) } }; Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) { Tile(Icons.Outlined.AutoGraph, if (fa) "بک‌تست" else "Backtest") { open(TerminalPage.BACKTEST) }; Tile(Icons.Outlined.Psychology, "AI Hub") { open(TerminalPage.AI) } } } }
@Composable private fun Tile(icon: ImageVector, title: String, onClick: () -> Unit) { Card(Modifier.weight(1f), onClick = onClick, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Panel)) { Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = Mint); Spacer(Modifier.width(8.dp)); Text(title, color = Color.White, fontWeight = FontWeight.SemiBold) } } }

@Composable private fun MarketsPage(fa: Boolean, market: String, onMarket: (String) -> Unit, open: () -> Unit) {
    val repo = remember { CoinExRepository() }; val scope = rememberCoroutineScope(); var query by rememberSaveable { mutableStateOf(market) }; var quote by remember { mutableStateOf<Quote?>(null) }; var loading by remember { mutableStateOf(false) }
    fun load() { scope.launch { loading = true; quote = runCatching { repo.loadLatestTicker(query.uppercase()) }.getOrNull()?.let { Quote(query.uppercase(), it.last, it.changeRate * 100, it.volume) }; loading = false } }
    LaunchedEffect(Unit) { load() }
    LazyColumn(Modifier.fillMaxSize().background(Bg).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { item { TitleBlock(if (fa) "بازارها" else "Markets", "Real-time CoinEx spot market") }; item { Row(verticalAlignment = Alignment.CenterVertically) { OutlinedTextField(query, { query = it.uppercase() }, Modifier.weight(1f), singleLine = true, label = { Text("Market") }); Spacer(Modifier.width(7.dp)); IconButton({ load() }) { Icon(Icons.Outlined.Refresh, null, tint = Mint) } } }; item { if (loading) Text("Loading…", color = Muted) else quote?.let { QuoteCard(it) { onMarket(it.symbol); open() } } }; item { Text(if (fa) "بازارهای سریع" else "Quick markets", color = Muted, fontSize = 11.sp) }; items(listOf("BTCUSDT", "ETHUSDT", "SOLUSDT", "DOGEUSDT", "PEPEUSDT")) { s -> OutlinedButton({ query = s; onMarket(s); load() }, Modifier.fillMaxWidth()) { Text(s) } } }
}

@Composable private fun BacktestPage(fa: Boolean, market: String, onMarket: (String) -> Unit, timeframe: String, onTimeframe: (String) -> Unit, vm: BacktestViewModel, state: BacktestUiState) {
    val repo = remember { CoinExRepository() }; val scope = rememberCoroutineScope(); var candles by remember { mutableStateOf<List<Candle>>(emptyList()) }; var selected by remember { mutableStateOf<Int?>(null) }
    fun load() { scope.launch { candles = runCatching { repo.loadKlines(market, timeframe, 240) }.getOrDefault(emptyList()) } }
    LaunchedEffect(market, timeframe) { vm.setMarket(market); vm.setTimeframe(timeframe); load() }
    LazyColumn(Modifier.fillMaxSize().background(Bg).padding(14.dp), verticalArrangement = Arrangement.spacedBy(11.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
        item { TitleBlock(if (fa) "Backtest Terminal" else "Backtest Terminal", "Real OHLC • touch chart • robot diagnostics") }
        item { Row(verticalAlignment = Alignment.CenterVertically) { OutlinedTextField(market, { onMarket(it.uppercase()) }, Modifier.weight(1f), singleLine = true, label = { Text("Market") }); Spacer(Modifier.width(7.dp)); IconButton({ load() }) { Icon(Icons.Outlined.Refresh, null, tint = Mint) } } }
        item { Timeframes(timeframe, onTimeframe) }
        item { CandleChart(candles, state.report?.trades ?: emptyList(), selected, { selected = it }, Modifier.fillMaxWidth().height(310.dp)) }
        item { selected?.let { candles.getOrNull(it)?.let { c -> CandleInfo(it, c) } } }
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) { Metric("ROI", state.report?.roiPercent?.let { "%.2f%%".format(it) } ?: "—", Green); Metric("PnL", state.report?.netPnl?.let { "%.2f".format(it) } ?: "—", if ((state.report?.netPnl ?: 0.0) >= 0) Green else Red); Metric("Win", state.report?.winRatePercent?.let { "%.1f%%".format(it) } ?: "—", Gold) } }
        item { Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(20.dp)) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(if (fa) "کنترل ربات و خطایابی" else "Robot control & diagnostics", color = Color.White, fontWeight = FontWeight.Bold); Text(state.status, color = Muted, fontSize = 11.sp); state.error?.let { Text(it, color = Red, fontSize = 10.sp) }; Button({ vm.runBacktest() }, Modifier.fillMaxWidth(), enabled = !state.isRunning) { Icon(Icons.Outlined.PlayArrow, null); Spacer(Modifier.width(6.dp)); Text(if (fa) "اجرای بک‌تست واقعی" else "Run real backtest") } } } }
        item { state.report?.let { Trades(it.trades, fa) } }
    }
}

@Composable private fun StrategyPage(fa: Boolean) { var name by rememberSaveable { mutableStateOf("Advanced Pullback") }; var entry by rememberSaveable { mutableStateOf("LWMA20 > LWMA50 + ATR") }; var exit by rememberSaveable { mutableStateOf("SL 1.5 ATR / TP 3 ATR") }; var risk by rememberSaveable { mutableStateOf("1") }; var saved by rememberSaveable { mutableStateOf(false) }; LazyColumn(Modifier.fillMaxSize().background(Bg).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { item { TitleBlock(if (fa) "مدیریت استراتژی" else "Strategy Lab", "Save configuration and feed it into Backtest") }; item { Field("Strategy name", name) { name = it } }; item { Field("Entry rules", entry) { entry = it } }; item { Field("Exit / SL / TP", exit) { exit = it } }; item { Field("Risk %", risk) { risk = it } }; item { Button({ saved = true }, Modifier.fillMaxWidth()) { Icon(Icons.Outlined.Save, null); Spacer(Modifier.width(6.dp)); Text(if (fa) "ذخیره و فعال‌سازی" else "Save & Activate") } }; item { if (saved) Text(if (fa) "Strategy فعال است." else "Strategy is active.", color = Green) }; item { OutlinedButton({ }, Modifier.fillMaxWidth()) { Icon(Icons.Outlined.UploadFile, null); Spacer(Modifier.width(6.dp)); Text(if (fa) "Import Robot / JSON / ZIP" else "Import Robot / JSON / ZIP") } } } }

@Composable private fun IntelligencePage(fa: Boolean, market: String) { val repo = remember { CoinExRepository() }; val scope = rememberCoroutineScope(); var text by remember { mutableStateOf(if (fa) "در حال تحلیل..." else "Analyzing...") }; var score by remember { mutableStateOf(0) }; fun analyze() { scope.launch { runCatching { repo.loadKlines(market, "15min", 120) }.onSuccess { candles -> val s = PumpDumpDetector().analyze(candles); score = s?.score?.toInt() ?: 0; text = if (s == null) (if (fa) "سیگنال قوی پامپ/دامپ دیده نشد" else "No strong pump/dump anomaly") else "${s.direction} • %+.2f • volume x%.1f • ${s.severity}".format(s.priceChangePercent, s.volumeRatio) }.onFailure { text = it.message ?: "Network error" } } }; LaunchedEffect(market) { analyze() }; LazyColumn(Modifier.fillMaxSize().background(Bg).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { item { TitleBlock(if (fa) "Coin Intelligence" else "Coin Intelligence", "Pump/Dump • Whale • Meme • Tokenomics") }; item { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Panel)) { Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(market, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black); Text("Signal score", color = Muted, fontSize = 10.sp); Text("$score / 100", color = if (score >= 70) Gold else Mint, fontSize = 32.sp, fontWeight = FontWeight.Black); Text(text, color = Color.White, fontSize = 12.sp); Button({ analyze() }, Modifier.fillMaxWidth()) { Icon(Icons.Outlined.Bolt, null); Spacer(Modifier.width(6.dp)); Text(if (fa) "تحلیل مجدد" else "Analyze again") } } } }; item { IntelTile(Icons.Outlined.Whatshot, if (fa) "Meme / Shitcoin Scanner" else "Meme / Shitcoin Scanner", "Risk + liquidity + volatility") }; item { IntelTile(Icons.Outlined.AccountBalanceWallet, if (fa) "Whale / Smart Money" else "Whale / Smart Money", "Provider-neutral on-chain layer") }; item { IntelTile(Icons.Outlined.Token, if (fa) "Tokenomics / Unlock" else "Tokenomics / Unlock", "Supply + allocation + vesting") }; item { IntelTile(Icons.Outlined.Groups, if (fa) "Team / Investors" else "Team / Investors", "Research + transparency") } } }

@Composable private fun AiPage(fa: Boolean, market: String, report: BacktestReport?) { var prompt by rememberSaveable { mutableStateOf("") }; var answer by rememberSaveable { mutableStateOf(if (fa) "ALVEX AI آماده تحلیل Coin، Strategy و معاملات شماست." else "ALVEX AI is ready for coin, strategy and trade analysis.") }; LazyColumn(Modifier.fillMaxSize().background(Bg).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { item { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(25.dp), colors = CardDefaults.cardColors(containerColor = Panel)) { Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { AlvexLogo(48); Spacer(Modifier.width(9.dp)); Column { Text("ALVEX AI", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Black); Text("Explainable Intelligence Hub", color = Muted, fontSize = 10.sp) } }; Text(answer, color = Color.White, fontSize = 13.sp); report?.let { Text("Backtest: ROI ${"%.2f".format(it.roiPercent)}% • Win ${"%.1f".format(it.winRatePercent)}% • DD ${"%.2f".format(it.maxDrawdownPercent)}%", color = Mint, fontSize = 10.sp) } } } }; item { Field(if (fa) "Coin / Strategy / Trade" else "Coin / Strategy / Trade", prompt) { prompt = it }) }; item { Button({ answer = if (prompt.isBlank()) "${if (fa) "برای تحلیل" else "For analysis"} $market ${if (fa) "یک ورودی وارد کنید." else "enter a prompt."}" else "ALVEX AI: $prompt — trend, volatility, volume, pump/dump risk and strategy diagnostics are requested. Unknown data remains Unknown; no guaranteed prediction is presented." }, Modifier.fillMaxWidth()) { Icon(Icons.Outlined.AutoAwesome, null); Spacer(Modifier.width(6.dp)); Text(if (fa) "تحلیل با AI" else "Analyze with AI") } }; item { Text(if (fa) "این لایه برای اتصال Provider واقعی AI طراحی شده و در نبود Provider داده جعلی تولید نمی‌کند." else "The AI layer is provider-neutral. Without a live provider it does not fabricate model output or certainty.", color = Muted, fontSize = 10.sp) } } }

@Composable private fun SettingsPage(fa: Boolean, onFa: (Boolean) -> Unit, email: String, onEmail: (String) -> Unit, notifications: Boolean, onNotifications: (Boolean) -> Unit, privacy: Boolean, onPrivacy: (Boolean) -> Unit, onBack: () -> Unit) { val context = LocalContext.current; Column(Modifier.fillMaxSize().background(Bg)) { Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) { IconButton(onBack) { Icon(Icons.Outlined.ArrowBack, null, tint = Color.White) }; Column(Modifier.weight(1f)) { Text("ALVEX", color = Color.White, fontWeight = FontWeight.Black); Text(if (fa) "تنظیمات" else "Settings", color = Muted, fontSize = 10.sp) } } ; LazyColumn(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { item { SettingsGroup(Icons.Outlined.Person, if (fa) "پروفایل" else "Profile") { Text(if (fa) "پروفایل و ترجیحات فضای کاری" else "Profile and workspace preferences", color = Muted, fontSize = 11.sp) } }; item { SwitchRow(Icons.Outlined.Security, if (fa) "امنیت و Privacy" else "Security & Privacy", privacy, onPrivacy) }; item { SwitchRow(Icons.Outlined.Notifications, if (fa) "اعلان‌ها" else "Notifications", notifications, onNotifications) }; item { SettingsGroup(Icons.Outlined.Language, if (fa) "زبان" else "Language") { Row(verticalAlignment = Alignment.CenterVertically) { Text(if (fa) "فارسی" else "English", color = Color.White, Modifier.weight(1f)); Switch(fa, onFa) } } }; item { SettingsGroup(Icons.Outlined.SupportAgent, if (fa) "پشتیبانی ایمیلی" else "Email Support") { Field("Support email", email, onEmail); Button({ val address = email.trim(); if (address.isNotBlank()) { runCatching { context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$address"))) } } }, Modifier.fillMaxWidth(), enabled = email.isNotBlank()) { Icon(Icons.Outlined.Email, null); Spacer(Modifier.width(6.dp)); Text(if (fa) "ارسال ایمیل" else "Open email") } } }; item { SettingsGroup(Icons.Outlined.Share, if (fa) "اشتراک‌گذاری" else "Share") { OutlinedButton({ val i = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, "ALVEX — Crypto Intelligence & Backtesting") }; context.startActivity(Intent.createChooser(i, "Share ALVEX")) }, Modifier.fillMaxWidth()) { Text(if (fa) "اشتراک‌گذاری ALVEX" else "Share ALVEX") } } } } } }

@Composable private fun SettingsGroup(icon: ImageVector, title: String, content: @Composable ColumnScope.() -> Unit) { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Panel)) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = Mint); Spacer(Modifier.width(8.dp)); Text(title, color = Color.White, fontWeight = FontWeight.Bold) }; content() } } }
@Composable private fun SwitchRow(icon: ImageVector, title: String, value: Boolean, onChange: (Boolean) -> Unit) { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Panel)) { Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = Mint); Spacer(Modifier.width(9.dp)); Text(title, color = Color.White, Modifier.weight(1f)); Switch(value, onChange) } } }
@Composable private fun Field(label: String, value: String, onValue: (String) -> Unit) { OutlinedTextField(value, onValue, Modifier.fillMaxWidth(), label = { Text(label) }, singleLine = true, shape = RoundedCornerShape(14.dp)) }
@Composable private fun TitleBlock(title: String, subtitle: String) { Column { Text(title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black); Text(subtitle, color = Muted, fontSize = 9.sp) } }
@Composable private fun Timeframes(selected: String, onSelect: (String) -> Unit) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) { listOf("1min", "5min", "15min", "1hour", "4hour", "1day").forEach { FilterChip(selected == it, { onSelect(it) }, label = { Text(it, fontSize = 9.sp) }) } } }
@Composable private fun Metric(label: String, value: String, color: Color) { Card(Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(15.dp)) { Column(Modifier.padding(9.dp)) { Text(label, color = Muted, fontSize = 8.sp); Text(value, color = color, fontWeight = FontWeight.Black) } } }
@Composable private fun SmallMetric(label: String, value: String) { Column { Text(label, color = Muted, fontSize = 8.sp); Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp) } }
@Composable private fun IntelTile(icon: ImageVector, title: String, subtitle: String) { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Panel)) { Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = Mint); Spacer(Modifier.width(10.dp)); Column { Text(title, color = Color.White, fontWeight = FontWeight.Bold); Text(subtitle, color = Muted, fontSize = 10.sp) } } } }

@Composable private fun CandleChart(candles: List<Candle>, trades: List<TradeResult>, selected: Int?, onSelect: (Int) -> Unit, modifier: Modifier) { if (candles.isEmpty()) { Box(modifier.background(Panel2, RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) { Text("Loading CoinEx candles…", color = Muted) }; return }; Canvas(modifier.background(Panel2, RoundedCornerShape(18.dp)).pointerInput(candles) { detectTapGestures { p -> onSelect(((p.x / size.width) * candles.size).toInt().coerceIn(0, candles.lastIndex)) } }) { val minP = candles.minOf { it.low }; val maxP = candles.maxOf { it.high }; val range = (maxP - minP).takeIf { it > 0 } ?: 1.0; val step = size.width / candles.size; val body = (step * .58f).coerceAtLeast(2f); candles.forEachIndexed { i, c -> fun y(v: Double) = size.height - ((v - minP) / range * size.height).toFloat(); val x = i * step + step / 2; val up = c.close >= c.open; val top = y(maxOf(c.open, c.close)); val bottom = y(minOf(c.open, c.close)); drawLine(if (up) Green else Red, Offset(x, y(c.high)), Offset(x, y(c.low)), 1.5f); drawRect(if (up) Green else Red, Offset(x - body / 2, top), androidx.compose.ui.geometry.Size(body, maxOf(2f, bottom - top))); if (selected == i) drawLine(Color.White, Offset(x, 0f), Offset(x, size.height), 1f) }; trades.forEach { t -> val i = candles.indices.minByOrNull { abs(candles[it].timestamp - t.entryTime) } ?: return@forEach; val x = i * step + step / 2; val y = size.height - ((t.entryPrice - minP) / range * size.height).toFloat(); drawCircle(if (t.netPnl >= 0) Green else Red, 5f, Offset(x, y)) } } }
@Composable private fun CandleInfo(i: Int, c: Candle) { Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(15.dp)) { Row(Modifier.padding(10.dp), Modifier.fillMaxWidth(), Arrangement.SpaceBetween) { Text("#$i", color = Mint, fontWeight = FontWeight.Bold); Text("O ${price(c.open)}", color = Color.White, fontSize = 10.sp); Text("H ${price(c.high)}", color = Color.White, fontSize = 10.sp); Text("L ${price(c.low)}", color = Color.White, fontSize = 10.sp); Text("C ${price(c.close)}", color = if (c.close >= c.open) Green else Red, fontSize = 10.sp) } } }
@Composable private fun EquityChart(values: List<Double>, modifier: Modifier) { Canvas(modifier.background(Panel2, RoundedCornerShape(14.dp))) { if (values.size < 2) return@Canvas; val minV = values.minOrNull() ?: 0.0; val maxV = values.maxOrNull() ?: 1.0; val range = (maxV - minV).takeIf { it > 0 } ?: 1.0; val step = size.width / (values.size - 1); for (i in 1 until values.size) { val y1 = size.height - ((values[i - 1] - minV) / range * size.height).toFloat(); val y2 = size.height - ((values[i] - minV) / range * size.height).toFloat(); drawLine(Mint, Offset((i - 1) * step, y1), Offset(i * step, y2), 3f) } } }
@Composable private fun Trades(trades: List<TradeResult>, fa: Boolean) { Column(verticalArrangement = Arrangement.spacedBy(6.dp)) { Text(if (fa) "معاملات و نقاط ورود/خروج" else "Trades & robot entry/exit diagnostics", color = Color.White, fontWeight = FontWeight.Bold); trades.takeLast(25).asReversed().forEachIndexed { index, t -> Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(14.dp)) { Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) { Text("#${trades.size - index}", color = Muted, fontSize = 9.sp, Modifier.width(30.dp)); Text(t.side.name, color = if (t.side.name == "LONG") Green else Red, fontWeight = FontWeight.Bold, Modifier.width(55.dp)); Column(Modifier.weight(1f)) { Text("${price(t.entryPrice)} → ${price(t.exitPrice)}", color = Color.White, fontSize = 10.sp); Text("Qty ${"%.5f".format(t.quantity)} • Fee ${"%.4f".format(t.fees)} • Funding ${"%.4f".format(t.funding)}", color = Muted, fontSize = 8.sp) }; Text("%+.3f".format(t.netPnl), color = if (t.netPnl >= 0) Green else Red, fontWeight = FontWeight.Black, fontSize = 11.sp) } } } } }

@Composable fun AlvexLogo(size: Int) { Box(Modifier.size(size.dp).clip(RoundedCornerShape((size / 4).dp)).background(Brush.linearGradient(listOf(Mint, Blue))), contentAlignment = Alignment.Center) { Text("A", color = Color.White, fontSize = (size / 2.2).sp, fontWeight = FontWeight.Black) } }
private fun price(v: Double): String = if (v >= 1000) "%,.2f".format(v) else if (v >= 1) "%.4f".format(v) else "%.8f".format(v)
private fun compact(v: Double): String = when { v >= 1e9 -> "%.1fB".format(v / 1e9); v >= 1e6 -> "%.1fM".format(v / 1e6); v >= 1e3 -> "%.1fK".format(v / 1e3); else -> "%.0f".format(v) }
