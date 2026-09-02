package com.notash.cryptobacktester.ui

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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notash.cryptobacktester.core.BacktestReport
import com.notash.cryptobacktester.core.Candle
import com.notash.cryptobacktester.core.Side
import com.notash.cryptobacktester.core.TradeResult
import com.notash.cryptobacktester.data.CoinExRepository
import com.notash.cryptobacktester.intelligence.PumpDumpDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

private val AP_BG = Color(0xFF050912)
private val AP_PANEL = Color(0xFF0B1422)
private val AP_PANEL2 = Color(0xFF111D2D)
private val AP_TEXT = Color(0xFFF5F7FB)
private val AP_MUTED = Color(0xFF7F8DA3)
private val AP_PURPLE = Color(0xFF8A45FF)
private val AP_CYAN = Color(0xFF26D9FF)
private val AP_GREEN = Color(0xFF21D79B)
private val AP_RED = Color(0xFFFF536F)
private val AP_GOLD = Color(0xFFFFC857)
private data class APQuote(val symbol: String, val price: Double, val change: Double, val volume: Double)

@Composable
fun AlvexPremiumFinal2(themeMode: AppThemeMode, onTheme: (AppThemeMode) -> Unit) {
    var page by rememberSaveable { mutableStateOf(0) }
    var pair by rememberSaveable { mutableStateOf("BTCUSDT") }
    var timeframe by rememberSaveable { mutableStateOf("15min") }
    var fa by rememberSaveable { mutableStateOf(true) }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var quotes by remember { mutableStateOf<List<APQuote>>(emptyList()) }
    val vm = remember { BacktestViewModel() }
    val state by vm.state.collectAsState()
    val repo = remember { CoinExRepository() }
    val scope = rememberCoroutineScope()

    fun refreshQuotes() {
        scope.launch {
            quotes = withContext(Dispatchers.IO) {
                listOf("BTCUSDT", "ETHUSDT", "BNBUSDT", "SOLUSDT", "XRPUSDT", "DOGEUSDT", "ADAUSDT", "AVAXUSDT", "LINKUSDT", "PEPEUSDT").mapNotNull { symbol ->
                    runCatching { repo.loadLatestTicker(symbol) }.getOrNull()?.let { APQuote(symbol, it.last, it.changeRate * 100.0, it.volume) }
                }
            }
        }
    }

    LaunchedEffect(Unit) { refreshQuotes() }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = AP_PURPLE,
            secondary = AP_CYAN,
            background = AP_BG,
            surface = AP_PANEL,
            onBackground = AP_TEXT,
            onSurface = AP_TEXT
        )
    ) {
        if (showSettings) {
            APSettings(fa, themeMode, onTheme, { fa = !fa }) { showSettings = false }
            return@MaterialTheme
        }
        Scaffold(
            containerColor = AP_BG,
            topBar = { APHeader(page, fa, { page = 3 }, { showSettings = true }, { fa = !fa }) },
            bottomBar = { APBottomBar(page, fa) { page = it } }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                when (page) {
                    0 -> APHome(fa, quotes, state.report, { pair = it; page = 2 }, ::refreshQuotes, { page = 3 })
                    1 -> APMarkets(fa, quotes, { pair = it; vm.setMarket(it); page = 2 }, ::refreshQuotes)
                    2 -> APTerminal(fa, pair, { pair = it; vm.setMarket(it) }, timeframe, { timeframe = it }, vm, state)
                    else -> APIntelligence(fa, pair, repo)
                }
            }
        }
    }
}

@Composable private fun APHeader(page: Int, fa: Boolean, ai: () -> Unit, settings: () -> Unit, language: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(64.dp).background(AP_BG).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = ai) { Icon(Icons.Outlined.Star, null, tint = AP_CYAN) }
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(23.dp).clip(RoundedCornerShape(6.dp)).background(Brush.linearGradient(listOf(AP_PURPLE, AP_CYAN))), contentAlignment = Alignment.Center) { Text("A", color = Color.White, fontWeight = FontWeight.Black) }
                Spacer(Modifier.width(6.dp)); Text("ALVEX", color = AP_TEXT, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            }
            Text(if (fa) listOf("خانه", "بازار", "معاملات و بک‌تست", "هوش بازار")[page] else listOf("Home", "Markets", "Trading Terminal", "Intelligence")[page], color = AP_MUTED, fontSize = 8.sp)
        }
        TextButton(onClick = language) { Text(if (fa) "EN" else "FA", color = AP_CYAN, fontSize = 9.sp) }
        IconButton(onClick = settings) { Icon(Icons.Outlined.Settings, null, tint = AP_TEXT) }
    }
}

@Composable private fun APBottomBar(page: Int, fa: Boolean, select: (Int) -> Unit) {
    NavigationBar(containerColor = AP_PANEL, tonalElevation = 0.dp) {
        val entries = listOf(Triple(0, Icons.Outlined.Home, if (fa) "خانه" else "Home"), Triple(1, Icons.Outlined.List, if (fa) "بازار" else "Markets"), Triple(2, Icons.Outlined.Build, if (fa) "بک‌تست" else "Terminal"), Triple(3, Icons.Outlined.Info, if (fa) "هوش" else "Intel"))
        entries.forEach { (index, icon, label) -> NavigationBarItem(page == index, { select(index) }, icon = { Icon(icon, label) }, label = { Text(label, fontSize = 8.sp) }) }
    }
}

@Composable private fun APHome(fa: Boolean, quotes: List<APQuote>, report: BacktestReport?, openPair: (String) -> Unit, refresh: () -> Unit, intel: () -> Unit) {
    val btc = quotes.firstOrNull { it.symbol == "BTCUSDT" }
    val movers = quotes.sortedByDescending { it.change }.take(4)
    LazyColumn(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 20.dp)) {
        item {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = AP_PANEL)) {
                Box(Modifier.fillMaxWidth().height(155.dp).background(Brush.linearGradient(listOf(Color(0xFF1E104A), AP_PANEL, Color(0xFF071A25))), RoundedCornerShape(26.dp))) {
                    Column(Modifier.padding(17.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) { Text("MARKET OVERVIEW", color = AP_MUTED, fontSize = 8.sp, letterSpacing = 1.sp); Spacer(Modifier.weight(1f)); APStatus("LIVE", AP_GREEN) }
                        Spacer(Modifier.height(7.dp)); Text("BTC / USDT", color = AP_TEXT, fontSize = 12.sp); Text(btc?.let { apMoney(it.price) } ?: "—", color = AP_TEXT, fontSize = 29.sp, fontWeight = FontWeight.Black); Text(btc?.let { apPct(it.change) } ?: "Waiting for market data", color = if ((btc?.change ?: 0.0) >= 0) AP_GREEN else AP_RED, fontSize = 10.sp)
                        Spacer(Modifier.height(7.dp)); APSpark(movers.map { it.change }, Modifier.fillMaxWidth().height(32.dp))
                    }
                }
            }
        }
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) { APStat("BTC", btc?.let { apMoney(it.price) } ?: "—", btc?.let { apPct(it.change) } ?: "—", if ((btc?.change ?: 0.0) >= 0) AP_GREEN else AP_RED, Modifier.weight(1f)); APStat("24H VOL", quotes.firstOrNull()?.let { apCompact(it.volume) } ?: "—", "CoinEx", AP_CYAN, Modifier.weight(1f)); APStat("RADAR", if (quotes.isEmpty()) "WAIT" else "READY", "AI", AP_PURPLE, Modifier.weight(1f)) } }
        item { APSection("TOP MOVERS", if (fa) "حرکت‌های مهم بازار" else "Live market movers") }
        items(movers) { q -> APMarketRow(q) { openPair(q.symbol) } }
        item { APSection("AI MARKET RADAR", "Pump/Dump • Whale • Meme • Coin Intelligence") }
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) { APRadar("PUMP / DUMP", AP_PURPLE, Modifier.weight(1f), intel); APRadar("WHALE", AP_CYAN, Modifier.weight(1f), intel); APRadar("MEME", AP_GOLD, Modifier.weight(1f), intel) } }
        item { APSection(if (fa) "آخرین بک‌تست" else "LATEST BACKTEST", "Real report metrics") }
        item { APReport(report) }
        item { OutlinedButton(onClick = refresh, Modifier.fillMaxWidth(), shape = RoundedCornerShape(15.dp)) { Icon(Icons.Outlined.Refresh, null); Spacer(Modifier.width(5.dp)); Text(if (fa) "به‌روزرسانی داده بازار" else "Refresh market data") } }
    }
}

@Composable private fun APMarkets(fa: Boolean, quotes: List<APQuote>, open: (String) -> Unit, refresh: () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    val list = quotes.filter { query.isBlank() || it.symbol.contains(query.uppercase()) }
    LazyColumn(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { APSection(if (fa) "بازار" else "MARKETS", "Crypto • Futures • Favorites") }
        item { OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text(if (fa) "جستجوی ارز" else "Search asset") }, leadingIcon = { Icon(Icons.Outlined.Search, null) }, shape = RoundedCornerShape(16.dp)) }
        item { Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf("Favorites", "Crypto", "Futures", "Forex").forEach { FilterChip(it == "Crypto", {}, label = { Text(it, fontSize = 8.sp) }) } } }
        items(list) { q -> APMarketRow(q) { open(q.symbol) } }
        item { OutlinedButton(onClick = refresh, Modifier.fillMaxWidth()) { Icon(Icons.Outlined.Refresh, null); Spacer(Modifier.width(5.dp)); Text("Refresh") } }
    }
}

@Composable private fun APTerminal(fa: Boolean, pair: String, onPair: (String) -> Unit, tf: String, onTf: (String) -> Unit, vm: BacktestViewModel, state: BacktestUiState) {
    val repo = remember { CoinExRepository() }; val scope = rememberCoroutineScope(); var candles by remember { mutableStateOf<List<Candle>>(emptyList()) }; var selected by remember { mutableStateOf<Int?>(null) }
    fun load() { scope.launch { candles = runCatching { repo.loadKlines(pair, tf, 220) }.getOrDefault(emptyList()) } }
    LaunchedEffect(pair, tf) { vm.setMarket(pair); vm.setTimeframe(tf); load() }
    LazyColumn(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(9.dp), contentPadding = PaddingValues(bottom = 22.dp)) {
        item { Row(verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(pair.replace("USDT", " / USDT"), color = AP_TEXT, fontSize = 20.sp, fontWeight = FontWeight.Black); Text("CoinEx • real OHLC", color = AP_MUTED, fontSize = 8.sp) }; APStatus(if (state.isRunning) "RUNNING" else "READY", if (state.isRunning) AP_GOLD else AP_GREEN) } }
        item { Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { listOf("1min", "5min", "15min", "1h", "4h", "1D").forEach { v -> FilterChip(tf == v, { onTf(v) }, label = { Text(v, fontSize = 7.sp) }) } } }
        item { APChart(candles, state.report?.trades ?: emptyList(), selected, { selected = it }, Modifier.fillMaxWidth().height(325.dp)) }
        item { selected?.let { i -> candles.getOrNull(i)?.let { APOhlc(it) } } }
        item { APControls(fa, pair, onPair, vm, state) }
        item { APReport(state.report) }
        item { state.report?.let { APTrades(it.trades, fa) } }
    }
}

@Composable private fun APControls(fa: Boolean, pair: String, onPair: (String) -> Unit, vm: BacktestViewModel, state: BacktestUiState) {
    var amount by rememberSaveable { mutableStateOf("1000") }; var risk by rememberSaveable { mutableStateOf(state.riskPercent.toString()) }; var lev by rememberSaveable { mutableStateOf(state.leverage.toString()) }
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = AP_PANEL)) { Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(if (fa) "تنظیمات بک‌تست" else "BACKTEST CONFIG", color = AP_TEXT, fontWeight = FontWeight.Black)
        OutlinedTextField(pair, { onPair(it.uppercase()) }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Pair") })
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) { OutlinedTextField(amount, { amount = it }, Modifier.weight(1f), singleLine = true, label = { Text("Amount") }); OutlinedTextField(lev, { lev = it }, Modifier.weight(1f), singleLine = true, label = { Text("Leverage") }) }
        OutlinedTextField(risk, { risk = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Risk %") })
        Button(onClick = { vm.setRiskPercent(risk.toDoubleOrNull() ?: 1.0); vm.setLeverage(lev.toDoubleOrNull() ?: 3.0); vm.runBacktest() }, Modifier.fillMaxWidth(), enabled = !state.isRunning, shape = RoundedCornerShape(14.dp)) { Icon(Icons.Outlined.PlayArrow, null); Spacer(Modifier.width(5.dp)); Text(if (fa) "اجرای بک‌تست واقعی" else "Run real backtest") }
        Text(state.error ?: state.status, color = if (state.error != null) AP_RED else AP_MUTED, fontSize = 8.sp)
    } }
}

@Composable private fun APIntelligence(fa: Boolean, pair: String, repo: CoinExRepository) {
    var score by remember { mutableStateOf(0) }; var detail by remember { mutableStateOf(if (fa) "در انتظار داده واقعی..." else "Waiting for live data...") }
    LaunchedEffect(pair) { val cs = runCatching { repo.loadKlines(pair, "15min", 120) }.getOrDefault(emptyList()); val s = PumpDumpDetector().analyze(cs); if (s != null) { score = s.score.toInt(); detail = "${s.direction} • ${"%.2f".format(s.priceChangePercent)}% • volume x${"%.1f".format(s.volumeRatio)}" } }
    LazyColumn(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { APSection(if (fa) "هوش بازار" else "INTELLIGENCE HUB", "AI Radar • Whale • Pump/Dump • Meme • Coin Intelligence") }
        item { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(25.dp), colors = CardDefaults.cardColors(containerColor = AP_PANEL)) { Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) { APGauge(score); Text(pair, color = AP_TEXT, fontSize = 22.sp, fontWeight = FontWeight.Black); Text(detail, color = AP_MUTED, fontSize = 9.sp); Text(if (fa) "داده قابل‌اندازه‌گیری؛ پیش‌بینی قطعی نیست." else "Measurable data only; no guaranteed prediction.", color = AP_MUTED, fontSize = 8.sp) } } }
        item { APIntel("PUMP / DUMP RADAR", "Price acceleration • volume anomaly • score", AP_PURPLE) }
        item { APIntel("WHALE INTELLIGENCE", "Provider-neutral smart-money layer", AP_CYAN) }
        item { APIntel("MEME / SHITCOIN SCANNER", "Liquidity • volatility • concentration • risk", AP_GOLD) }
        item { APIntel("COIN INTELLIGENCE", "Tokenomics • project • team • roadmap • on-chain", AP_GREEN) }
        item { APIntel("AI TRADE ANALYST", "Backtest diagnostics and optimization context", AP_PURPLE) }
    }
}

@Composable private fun APChart(cs: List<Candle>, trades: List<TradeResult>, selected: Int?, pick: (Int) -> Unit, modifier: Modifier) { if (cs.isEmpty()) { Box(modifier.clip(RoundedCornerShape(20.dp)).background(AP_PANEL2), contentAlignment = Alignment.Center) { Text("Loading real CoinEx candles…", color = AP_MUTED) }; return }; Canvas(modifier.clip(RoundedCornerShape(20.dp)).background(AP_PANEL2).pointerInput(cs) { detectTapGestures { p -> pick(((p.x / size.width) * cs.size).toInt().coerceIn(0, cs.lastIndex)) } }) { val lo = cs.minOf { it.low }; val hi = cs.maxOf { it.high }; val range = (hi - lo).takeIf { it > 0 } ?: 1.0; val step = size.width / cs.size; val body = (step * .58f).coerceAtLeast(2f); fun y(v: Double) = size.height - ((v - lo) / range * size.height).toFloat(); for (i in 0..3) drawLine(Color(0xFF1B2B3D), Offset(0f, size.height * i / 3f), Offset(size.width, size.height * i / 3f), 1f); cs.forEachIndexed { i, c -> val x = i * step + step / 2; val col = if (c.close >= c.open) AP_GREEN else AP_RED; drawLine(col, Offset(x, y(c.high)), Offset(x, y(c.low)), 1.2f); drawRect(col, Offset(x - body / 2, y(maxOf(c.open, c.close))), androidx.compose.ui.geometry.Size(body, maxOf(2f, abs(y(c.open) - y(c.close))))) }; trades.forEach { t -> val ei = cs.indices.minByOrNull { abs(cs[it].timestamp - t.entryTime) } ?: return@forEach; val x = ei * step + step / 2; val col = if (t.side == Side.LONG) AP_GREEN else AP_RED; drawCircle(col, 5f, Offset(x, y(t.entryPrice))); val ex = cs.indices.minByOrNull { abs(cs[it].timestamp - t.exitTime) }; if (ex != null) { val xx = ex * step + step / 2; drawCircle(AP_GOLD, 4f, Offset(xx, y(t.exitPrice))); drawLine(col, Offset(x, y(t.entryPrice)), Offset(xx, y(t.exitPrice)), 1f) } }; selected?.let { val x = it * step + step / 2; drawLine(AP_TEXT, Offset(x, 0f), Offset(x, size.height), 1f) } } }

@Composable private fun APTrades(ts: List<TradeResult>, fa: Boolean) { Column(verticalArrangement = Arrangement.spacedBy(6.dp)) { Text(if (fa) "معاملات و خطایابی ربات" else "TRADE DIAGNOSTICS", color = AP_TEXT, fontWeight = FontWeight.Black); ts.takeLast(25).asReversed().forEach { t -> Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(15.dp), colors = CardDefaults.cardColors(containerColor = AP_PANEL)) { Column(Modifier.padding(10.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { APStatus(t.side.name, if (t.side == Side.LONG) AP_GREEN else AP_RED); Spacer(Modifier.width(7.dp)); Text("${apMoney(t.entryPrice)} → ${apMoney(t.exitPrice)}", color = AP_TEXT, fontSize = 9.sp); Spacer(Modifier.weight(1f)); Text("%+.3f".format(t.netPnl), color = if (t.netPnl >= 0) AP_GREEN else AP_RED, fontWeight = FontWeight.Black, fontSize = 9.sp) }; Text("SL ${apMoney(t.stopLoss)} • TP ${apMoney(t.takeProfit)} • ${t.exitReason}", color = AP_MUTED, fontSize = 8.sp); Text("Qty ${"%.5f".format(t.quantity)} • Fee ${"%.4f".format(t.fees)} • Funding ${"%.4f".format(t.funding)}", color = AP_MUTED, fontSize = 7.sp) } } } } }

@Composable private fun APReport(r: BacktestReport?) { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = AP_PANEL)) { Column(Modifier.padding(13.dp)) { Text("AI BACKTEST ANALYST", color = AP_TEXT, fontWeight = FontWeight.Black); Spacer(Modifier.height(7.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) { APMetric("ROI", r?.let { "%.2f%%".format(it.roiPercent) } ?: "—"); APMetric("WIN", r?.let { "%.1f%%".format(it.winRatePercent) } ?: "—"); APMetric("PF", r?.let { if (it.profitFactor.isInfinite()) "∞" else "%.2f".format(it.profitFactor) } ?: "—"); APMetric("DD", r?.let { "%.2f%%".format(it.maxDrawdownPercent) } ?: "—") } }
} }

@Composable private fun APSettings(fa: Boolean, mode: AppThemeMode, onMode: (AppThemeMode) -> Unit, language: () -> Unit, back: () -> Unit) { Column(Modifier.fillMaxSize().background(AP_BG)) { Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = back) { Icon(Icons.Outlined.ArrowBack, null, tint = AP_TEXT) }; Column { Text("ALVEX", color = AP_TEXT, fontWeight = FontWeight.Black); Text(if (fa) "تنظیمات" else "Settings", color = AP_MUTED, fontSize = 8.sp) } }; LazyColumn(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { item { APSetting("Account & Security", "Session • password • API-key policy", Icons.Outlined.Person) }; item { APSetting("Notifications", "Pump/Dump • Whale • AI alerts", Icons.Outlined.Notifications) }; item { APSetting("Language", if (fa) "فارسی / English" else "English / فارسی", Icons.Outlined.Language, language) }; item { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("Theme", color = AP_TEXT, fontWeight = FontWeight.Bold); Text(if (mode == AppThemeMode.DARK) "Dark" else "Light", color = AP_MUTED, fontSize = 8.sp) }; Switch(checked = mode == AppThemeMode.LIGHT, onCheckedChange = { onMode(if (it) AppThemeMode.LIGHT else AppThemeMode.DARK) }) } }; item { APSetting("Share & Reports", "CSV • JSON • AI report • Android share", Icons.Outlined.Share) }; item { APSetting("About ALVEX", "Professional crypto intelligence workspace", Icons.Outlined.Info) } } } }

@Composable private fun APSetting(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, click: (() -> Unit)? = null) { Card(onClick = click ?: {}, Modifier.fillMaxWidth(), shape = RoundedCornerShape(17.dp), colors = CardDefaults.cardColors(containerColor = AP_PANEL)) { Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = AP_CYAN); Spacer(Modifier.width(9.dp)); Column { Text(title, color = AP_TEXT, fontWeight = FontWeight.Bold); Text(subtitle, color = AP_MUTED, fontSize = 8.sp) } } } }
@Composable private fun APSection(title: String, subtitle: String = "") { Column { Text(title, color = AP_TEXT, fontSize = 15.sp, fontWeight = FontWeight.Black); if (subtitle.isNotBlank()) Text(subtitle, color = AP_MUTED, fontSize = 8.sp) } }
@Composable private fun APStatus(text: String, color: Color) { Box(Modifier.clip(RoundedCornerShape(30.dp)).background(color.copy(.12f)).padding(horizontal = 8.dp, vertical = 4.dp)) { Text(text, color = color, fontSize = 7.sp, fontWeight = FontWeight.Black) } }
@Composable private fun APStat(title: String, value: String, sub: String, color: Color, modifier: Modifier) { Card(modifier, shape = RoundedCornerShape(15.dp), colors = CardDefaults.cardColors(containerColor = AP_PANEL)) { Column(Modifier.padding(9.dp)) { Text(title, color = AP_MUTED, fontSize = 7.sp); Text(value, color = AP_TEXT, fontSize = 10.sp, fontWeight = FontWeight.Black); Text(sub, color = color, fontSize = 7.sp) } } }
@Composable private fun APMarketRow(q: APQuote, on: () -> Unit) { Card(onClick = on, Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = AP_PANEL)) { Row(Modifier.padding(11.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(39.dp).clip(RoundedCornerShape(12.dp)).background(Brush.linearGradient(listOf(AP_PURPLE.copy(.2f), AP_CYAN.copy(.1f)))), contentAlignment = Alignment.Center) { Text(q.symbol.take(1), color = AP_TEXT, fontWeight = FontWeight.Black) }; Spacer(Modifier.width(9.dp)); Column(Modifier.weight(1f)) { Text(q.symbol.removeSuffix("USDT"), color = AP_TEXT, fontWeight = FontWeight.Bold); Text("/ USDT • ${apCompact(q.volume)} vol", color = AP_MUTED, fontSize = 7.sp) }; Column(horizontalAlignment = Alignment.End) { Text(apMoney(q.price), color = AP_TEXT, fontSize = 9.sp, fontWeight = FontWeight.Bold); Text(apPct(q.change), color = if (q.change >= 0) AP_GREEN else AP_RED, fontSize = 9.sp) } } } }
@Composable private fun APRadar(title: String, color: Color, modifier: Modifier, on: () -> Unit) { Card(onClick = on, modifier = modifier, shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = AP_PANEL2)) { Column(Modifier.padding(9.dp)) { APStatus("•", color); Spacer(Modifier.height(4.dp)); Text(title, color = AP_TEXT, fontSize = 8.sp, fontWeight = FontWeight.Bold) } } }
@Composable private fun APIntel(title: String, subtitle: String, color: Color) { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = AP_PANEL)) { Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(39.dp).clip(RoundedCornerShape(11.dp)).background(color.copy(.12f)), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Info, null, tint = color) }; Spacer(Modifier.width(9.dp)); Column { Text(title, color = AP_TEXT, fontSize = 10.sp, fontWeight = FontWeight.Black); Text(subtitle, color = AP_MUTED, fontSize = 7.sp) } } } }
@Composable private fun APMetric(label: String, value: String) { Column(Modifier.width(70.dp)) { Text(label, color = AP_MUTED, fontSize = 7.sp); Text(value, color = AP_TEXT, fontSize = 9.sp, fontWeight = FontWeight.Black) } }
@Composable private fun APOhlc(c: Candle) { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = AP_PANEL)) { Row(Modifier.fillMaxWidth().padding(9.dp), horizontalArrangement = Arrangement.SpaceEvenly) { Text("O ${apMoney(c.open)}", color = AP_TEXT, fontSize = 8.sp); Text("H ${apMoney(c.high)}", color = AP_GREEN, fontSize = 8.sp); Text("L ${apMoney(c.low)}", color = AP_RED, fontSize = 8.sp); Text("C ${apMoney(c.close)}", color = AP_TEXT, fontSize = 8.sp); Text("V ${apCompact(c.volume)}", color = AP_MUTED, fontSize = 8.sp) } } }
@Composable private fun APGauge(score: Int) { Canvas(Modifier.size(125.dp)) { for (i in 1..3) drawCircle(Color(0xFF1B2B3D), size.minDimension * .34f * i / 3f, center, style = androidx.compose.ui.graphics.drawscope.Stroke(1f)); drawCircle(if (score >= 70) AP_RED else AP_PURPLE, size.minDimension * .18f, center); drawCircle(AP_CYAN, size.minDimension * .34f, center, style = androidx.compose.ui.graphics.drawscope.Stroke(2f)) } }
@Composable private fun APSpark(values: List<Double>, modifier: Modifier) { Canvas(modifier) { if (values.size < 2) return@Canvas; val lo = values.minOrNull() ?: 0.0; val hi = values.maxOrNull() ?: 1.0; val range = (hi - lo).takeIf { it > 0 } ?: 1.0; var last = Offset(0f, size.height); values.forEachIndexed { i, v -> val p = Offset(size.width * i / values.lastIndex.toFloat(), size.height - ((v - lo) / range * size.height).toFloat()); if (i > 0) drawLine(AP_CYAN, last, p, 2f); last = p } } }
private fun apMoney(v: Double) = if (v >= 1000) "%,.2f".format(v) else if (v >= 1) "%.4f".format(v) else "%.8f".format(v)
private fun apPct(v: Double) = "%+.2f%%".format(v)
private fun apCompact(v: Double) = when { v >= 1e9 -> "%.1fB".format(v / 1e9); v >= 1e6 -> "%.1fM".format(v / 1e6); v >= 1e3 -> "%.1fK".format(v / 1e3); else -> "%.0f".format(v) }
