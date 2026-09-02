package com.notash.cryptobacktester.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Path
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

private val AxBg = Color(0xFF050A12)
private val AxPanel = Color(0xFF0A1320)
private val AxPanel2 = Color(0xFF101B2A)
private val AxBorder = Color(0xFF1B2B3D)
private val AxText = Color(0xFFF5F7FB)
private val AxMuted = Color(0xFF7F8DA3)
private val AxPurple = Color(0xFF8A45FF)
private val AxPurple2 = Color(0xFFB23CFF)
private val AxCyan = Color(0xFF26D9FF)
private val AxGreen = Color(0xFF21D79B)
private val AxRed = Color(0xFFFF536F)
private val AxGold = Color(0xFFFFC857)

private data class AxQuote(val symbol: String, val price: Double, val change: Double, val volume: Double)

@Composable
fun AlvexPremiumWorkspace(themeMode: AppThemeMode, onThemeMode: (AppThemeMode) -> Unit) {
    val dark = themeMode == AppThemeMode.DARK
    val scheme = darkColorScheme(
        primary = AxPurple2,
        secondary = AxCyan,
        background = AxBg,
        surface = AxPanel,
        onBackground = AxText,
        onSurface = AxText
    )
    var page by rememberSaveable { mutableStateOf(0) }
    var market by rememberSaveable { mutableStateOf("BTCUSDT") }
    var timeframe by rememberSaveable { mutableStateOf("15min") }
    var settings by rememberSaveable { mutableStateOf(false) }
    var fa by rememberSaveable { mutableStateOf(true) }
    var quotes by remember { mutableStateOf<List<AxQuote>>(emptyList()) }
    var report by remember { mutableStateOf<BacktestReport?>(null) }
    val vm = remember { BacktestViewModel() }
    val state by vm.state.collectAsState()
    val repo = remember { CoinExRepository() }
    val scope = rememberCoroutineScope()

    fun refreshQuotes() {
        scope.launch {
            quotes = withContext(Dispatchers.IO) {
                listOf("BTCUSDT", "ETHUSDT", "BNBUSDT", "SOLUSDT", "XRPUSDT", "DOGEUSDT", "ADAUSDT", "AVAXUSDT", "LINKUSDT", "PEPEUSDT").mapNotNull { symbol ->
                    runCatching { repo.loadLatestTicker(symbol) }.getOrNull()?.let { AxQuote(symbol, it.last, it.changeRate * 100.0, it.volume) }
                }
            }
        }
    }

    LaunchedEffect(Unit) { refreshQuotes() }
    LaunchedEffect(state.report) { report = state.report }

    MaterialTheme(colorScheme = scheme) {
        if (settings) {
            AxSettings(fa, themeMode, onThemeMode, { fa = !fa }) { settings = false }
            return@MaterialTheme
        }
        Scaffold(
            containerColor = AxBg,
            topBar = {
                AxHeader(
                    title = when (page) { 0 -> if (fa) "خانه" else "Home"; 1 -> if (fa) "بازار" else "Markets"; 2 -> if (fa) "معاملات و بک‌تست" else "Trading Terminal"; else -> if (fa) "هوش بازار" else "Intelligence" },
                    fa = fa,
                    onAi = { page = 3 },
                    onSettings = { settings = true },
                    onLanguage = { fa = !fa }
                )
            },
            bottomBar = {
                AxBottomBar(page, fa) { selected -> page = selected }
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding).background(AxBg)) {
                when (page) {
                    0 -> AxHome(fa, quotes, report, { market = it; page = 2 }, ::refreshQuotes, { page = it })
                    1 -> AxMarkets(fa, quotes, market, { market = it; vm.setMarket(it) }, { page = 2 }, ::refreshQuotes)
                    2 -> AxTerminal(fa, market, { market = it; vm.setMarket(it) }, timeframe, { timeframe = it }, vm, state)
                    else -> AxIntelligenceHub(fa, market, repo)
                }
            }
        }
    }
}

@Composable private fun AxHeader(title: String, fa: Boolean, onAi: () -> Unit, onSettings: () -> Unit, onLanguage: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(66.dp).background(AxBg).padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onAi) { Icon(Icons.Outlined.Star, "AI", tint = AxCyan) }
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) { AxMark(24); Spacer(Modifier.width(7.dp)); Text("ALVEX", color = AxText, fontWeight = FontWeight.Black, letterSpacing = 2.sp) }
            Text(title, color = AxMuted, fontSize = 9.sp)
        }
        TextButton(onClick = onLanguage) { Text(if (fa) "EN" else "FA", color = AxCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
        IconButton(onClick = onSettings) { Icon(Icons.Outlined.Settings, "Settings", tint = AxText) }
    }
}

@Composable private fun AxBottomBar(selected: Int, fa: Boolean, onSelected: (Int) -> Unit) {
    NavigationBar(containerColor = AxPanel, tonalElevation = 0.dp) {
        val items = listOf(
            Triple(0, Icons.Outlined.Home, if (fa) "خانه" else "Home"),
            Triple(1, Icons.Outlined.List, if (fa) "بازار" else "Markets"),
            Triple(2, Icons.Outlined.Build, if (fa) "بک‌تست" else "Terminal"),
            Triple(3, Icons.Outlined.Info, if (fa) "هوش" else "Intel")
        )
        items.forEach { (index, icon, label) ->
            NavigationBarItem(selected == index, { onSelected(index) }, icon = { Icon(icon, label) }, label = { Text(label, fontSize = 9.sp) })
        }
    }
}

@Composable private fun AxHome(fa: Boolean, quotes: List<AxQuote>, report: BacktestReport?, openMarket: (String) -> Unit, refresh: () -> Unit, openPage: (Int) -> Unit) {
    val btc = quotes.firstOrNull { it.symbol == "BTCUSDT" }
    val movers = quotes.sortedByDescending { it.change }.take(4)
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(top = 8.dp, bottom = 22.dp)) {
        item {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = AxPanel)) {
                Box(Modifier.fillMaxWidth().height(150.dp).background(Brush.linearGradient(listOf(Color(0xFF15103A), AxPanel, Color(0xFF08141F))), RoundedCornerShape(26.dp))) {
                    Column(Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) { Text("MARKET OVERVIEW", color = AxMuted, fontSize = 9.sp, letterSpacing = 1.sp); Spacer(Modifier.weight(1f)); StatusPill("LIVE", AxGreen) }
                        Spacer(Modifier.height(8.dp)); Text("BTC Dominance", color = AxText, fontSize = 12.sp); Row(verticalAlignment = Alignment.Bottom) { Text("—", color = AxText, fontSize = 29.sp, fontWeight = FontWeight.Black); Spacer(Modifier.width(7.dp)); Text("Market feed", color = AxMuted, fontSize = 10.sp) }
                        Spacer(Modifier.height(8.dp)); MiniSparkline(movers.map { it.change }, Modifier.fillMaxWidth().height(42.dp))
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("BTC", btc?.let { money(it.price) } ?: "—", btc?.let { pct(it.change) } ?: "—", btc?.let { if (it.change >= 0) AxGreen else AxRed } ?: AxMuted, Modifier.weight(1f))
                StatCard("24H VOL", quotes.firstOrNull()?.let { compact(it.volume) } ?: "—", "LIVE", AxCyan, Modifier.weight(1f))
                StatCard("FEED", if (quotes.isEmpty()) "WAIT" else "LIVE", "CoinEx", if (quotes.isEmpty()) AxGold else AxGreen, Modifier.weight(1f))
            }
        }
        item { AxSection("TOP MOVERS", if (fa) "حرکت‌های مهم بازار" else "Live market movers") }
        items(movers) { quote -> AxMarketRow(quote) { openMarket(quote.symbol) } }
        item {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = AxPanel)) {
                Column(Modifier.padding(15.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) { Text(if (fa) "AI MARKET RADAR" else "AI MARKET RADAR", color = AxText, fontWeight = FontWeight.Black); Spacer(Modifier.weight(1f)); Text("VIEW AI", color = AxPurple2, fontSize = 9.sp, modifier = Modifier.clickable { openPage(3) }) }
                    Spacer(Modifier.height(10.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RadarTile("Pump / Dump", "Signal engine", AxPurple2) { openPage(3) }; RadarTile("Whale", "Provider layer", AxCyan) { openPage(3) }; RadarTile("Meme", "Risk scanner", AxGold) { openPage(3) }
                    }
                }
            }
        }
        item { AxSection(if (fa) "آخرین بک‌تست" else "LATEST BACKTEST", "Real report metrics") }
        item { ReportCard(report) { openPage(2) } }
        item { AxSection(if (fa) "دسترسی سریع" else "QUICK ACCESS") }
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { ActionTile(Icons.Outlined.List, "Markets") { openPage(1) }; ActionTile(Icons.Outlined.Build, "Terminal") { openPage(2) }; ActionTile(Icons.Outlined.Star, "AI Hub") { openPage(3) } } }
        item { OutlinedButton(onClick = refresh, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(15.dp)) { Icon(Icons.Outlined.Refresh, null); Spacer(Modifier.width(6.dp)); Text(if (fa) "به‌روزرسانی داده بازار" else "Refresh market data") } }
    }
}

@Composable private fun AxMarkets(fa: Boolean, quotes: List<AxQuote>, market: String, onMarket: (String) -> Unit, open: () -> Unit, refresh: () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    val filtered = quotes.filter { query.isBlank() || it.symbol.contains(query.uppercase()) }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(top = 8.dp, bottom = 22.dp)) {
        item { AxSection(if (fa) "بازار" else "Markets", "Crypto • Futures • Favorites") }
        item { OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text(if (fa) "جستجوی ارز" else "Search asset") }, leadingIcon = { Icon(Icons.Outlined.Search, null) }, shape = RoundedCornerShape(16.dp)) }
        item { Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) { listOf("Favorites", "Crypto", "Futures", "Forex").forEachIndexed { i, label -> FilterChip(i == 1, {}, label = { Text(label, fontSize = 9.sp) }) } } }
        item { Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text("Name", color = AxMuted, fontSize = 9.sp); Text("Price / 24h", color = AxMuted, fontSize = 9.sp) } }
        items(filtered) { quote -> AxMarketRow(quote) { onMarket(quote.symbol); open() } }
        item { OutlinedButton(onClick = refresh, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Outlined.Refresh, null); Spacer(Modifier.width(5.dp)); Text("Refresh") } }
    }
}

@Composable private fun AxTerminal(fa: Boolean, market: String, onMarket: (String) -> Unit, timeframe: String, onTimeframe: (String) -> Unit, vm: BacktestViewModel, state: BacktestUiState) {
    val repo = remember { CoinExRepository() }
    val scope = rememberCoroutineScope()
    var candles by remember { mutableStateOf<List<Candle>>(emptyList()) }
    var selected by remember { mutableStateOf<Int?>(null) }
    var loading by remember { mutableStateOf(false) }
    fun load() { scope.launch { loading = true; candles = runCatching { repo.loadKlines(market, timeframe, 220) }.getOrDefault(emptyList()); loading = false } }
    LaunchedEffect(market, timeframe) { vm.setMarket(market); vm.setTimeframe(timeframe); load() }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(market.replace("USDT", " / USDT"), color = AxText, fontSize = 20.sp, fontWeight = FontWeight.Black); Text("CoinEx • ${if (loading) "loading" else "live data"}", color = AxMuted, fontSize = 9.sp) }; StatusPill(if (state.isRunning) "RUNNING" else "READY", if (state.isRunning) AxGold else AxGreen) }
        }
        item { TimeframeStrip(timeframe, onTimeframe) }
        item { ProfessionalCandleChart(candles, state.report?.trades ?: emptyList(), selected, { selected = it }, Modifier.fillMaxWidth().height(330.dp)) }
        item { selected?.let { i -> candles.getOrNull(i)?.let { AxOhlc(it) } } }
        item { TerminalControls(fa, market, onMarket, vm, state) }
        item { ReportCard(state.report, null) }
        item { state.report?.let { AxTradeDiagnostics(it.trades, fa) } }
    }
}

@Composable private fun TerminalControls(fa: Boolean, market: String, onMarket: (String) -> Unit, vm: BacktestViewModel, state: BacktestUiState) {
    var amount by rememberSaveable { mutableStateOf("1000") }
    var risk by rememberSaveable { mutableStateOf(state.riskPercent.toString()) }
    var leverage by rememberSaveable { mutableStateOf(state.leverage.toString()) }
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = AxPanel)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Text(if (fa) "تنظیمات بک‌تست" else "BACKTEST CONFIG", color = AxText, fontWeight = FontWeight.Black); Spacer(Modifier.weight(1f)); Text("COINEX", color = AxCyan, fontSize = 9.sp) }
            OutlinedTextField(market, { onMarket(it.uppercase()) }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Pair") })
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Box(Modifier.weight(1f)) { OutlinedTextField(amount, { amount = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Amount") }) }; Box(Modifier.weight(1f)) { OutlinedTextField(leverage, { leverage = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Leverage") }) } }
            OutlinedTextField(risk, { risk = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Risk %") })
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { vm.setRiskPercent(risk.toDoubleOrNull() ?: 1.0); vm.setLeverage(leverage.toDoubleOrNull() ?: 3.0); vm.runBacktest() }, Modifier.weight(1f), enabled = !state.isRunning, shape = RoundedCornerShape(14.dp)) { Icon(Icons.Outlined.PlayArrow, null); Spacer(Modifier.width(5.dp)); Text(if (fa) "اجرای بک‌تست" else "Run Backtest") }
                OutlinedButton(onClick = { }, Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) { Text("Import Robot") }
            }
            Text(if (state.error != null) state.error else state.status, color = if (state.error != null) AxRed else AxMuted, fontSize = 9.sp)
        }
    }
}

@Composable private fun AxIntelligenceHub(fa: Boolean, market: String, repo: CoinExRepository) {
    val scope = rememberCoroutineScope()
    var candles by remember { mutableStateOf<List<Candle>>(emptyList()) }
    var signalText by remember { mutableStateOf(if (fa) "در انتظار داده واقعی..." else "Waiting for live data...") }
    var score by remember { mutableStateOf(0) }
    LaunchedEffect(market) { candles = runCatching { repo.loadKlines(market, "15min", 120) }.getOrDefault(emptyList()); val signal = PumpDumpDetector().analyze(candles); if (signal != null) { score = signal.score.toInt(); signalText = "${signal.direction} • ${"%.2f".format(signal.priceChangePercent)}% • volume x${"%.1f".format(signal.volumeRatio)}" } }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(11.dp), contentPadding = PaddingValues(top = 8.dp, bottom = 22.dp)) {
        item { AxSection(if (fa) "هوش بازار" else "INTELLIGENCE HUB", "AI Radar • Whale • Pump/Dump • Meme • Coin Intelligence") }
        item {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = AxPanel)) {
                Column(Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    AxRadar(score)
                    Spacer(Modifier.height(10.dp)); Text(market, color = AxText, fontSize = 22.sp, fontWeight = FontWeight.Black); Text(signalText, color = AxMuted, fontSize = 10.sp)
                    Spacer(Modifier.height(10.dp)); Text(if (fa) "امتیاز فقط بر اساس داده قابل‌دسترسی محاسبه می‌شود؛ پیش‌بینی قطعی نیست." else "Score uses available measurable data only; it is not a guaranteed prediction.", color = AxMuted, fontSize = 9.sp)
                }
            }
        }
        item { IntelTile(Icons.Outlined.Warning, "PUMP / DUMP RADAR", "Price acceleration • volume anomaly • score", AxPurple2) }
        item { IntelTile(Icons.Outlined.AccountBox, "WHALE INTELLIGENCE", "Provider-neutral smart-money layer; unknown stays unknown", AxCyan) }
        item { IntelTile(Icons.Outlined.Info, "MEME / SHITCOIN SCANNER", "Liquidity • volatility • concentration • risk", AxGold) }
        item { IntelTile(Icons.Outlined.List, "COIN INTELLIGENCE", "Tokenomics • project • team • roadmap • on-chain", AxGreen) }
        item { IntelTile(Icons.Outlined.Star, "AI TRADE ANALYST", "Backtest diagnostics and optimization context", AxPurple2) }
    }
}

@Composable private fun ProfessionalCandleChart(candles: List<Candle>, trades: List<TradeResult>, selected: Int?, onSelect: (Int) -> Unit, modifier: Modifier) {
    if (candles.isEmpty()) { Box(modifier.clip(RoundedCornerShape(22.dp)).background(AxPanel2), contentAlignment = Alignment.Center) { Text("Loading real CoinEx candles…", color = AxMuted) }; return }
    Canvas(modifier.clip(RoundedCornerShape(22.dp)).background(AxPanel2).pointerInput(candles) { detectTapGestures { p -> onSelect(((p.x / size.width) * candles.size).toInt().coerceIn(0, candles.lastIndex)) } }) {
        val min = candles.minOf { it.low }; val max = candles.maxOf { it.high }; val range = (max - min).takeIf { it > 0 } ?: 1.0; val step = size.width / candles.size; val body = (step * .62f).coerceAtLeast(2f)
        fun y(v: Double) = size.height - ((v - min) / range * size.height).toFloat()
        for (i in 0..3) { val gy = size.height * i / 3f; drawLine(AxBorder, Offset(0f, gy), Offset(size.width, gy), 1f) }
        candles.forEachIndexed { i, c -> val x = i * step + step / 2; val up = c.close >= c.open; val col = if (up) AxGreen else AxRed; drawLine(col, Offset(x, y(c.high)), Offset(x, y(c.low)), 1.2f); drawRect(col, Offset(x - body / 2, y(maxOf(c.open, c.close))), androidx.compose.ui.geometry.Size(body, maxOf(2f, abs(y(c.open) - y(c.close))))) }
        trades.forEach { t -> val ei = candles.indices.minByOrNull { abs(candles[it].timestamp - t.entryTime) }; val xi = ei ?: return@forEach; val x = xi * step + step / 2; val entryColor = if (t.side == Side.LONG) AxGreen else AxRed; drawCircle(entryColor, 5f, Offset(x, y(t.entryPrice))); val ex = candles.indices.minByOrNull { abs(candles[it].timestamp - t.exitTime) }; if (ex != null) { val xx = ex * step + step / 2; drawCircle(AxGold, 4f, Offset(xx, y(t.exitPrice))); drawLine(entryColor, Offset(x, y(t.entryPrice)), Offset(xx, y(t.exitPrice)), 1f) } }
        selected?.let { i -> val x = i * step + step / 2; drawLine(AxText, Offset(x, 0f), Offset(x, size.height), 1f) }
    }
}

@Composable private fun AxTradeDiagnostics(trades: List<TradeResult>, fa: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(if (fa) "تشخیص معامله‌به‌معامله" else "TRADE-BY-TRADE DIAGNOSTICS", color = AxText, fontWeight = FontWeight.Black)
        trades.takeLast(30).asReversed().forEachIndexed { i, t ->
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = AxPanel)) {
                Row(Modifier.padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(if (t.side == Side.LONG) AxGreen.copy(.12f) else AxRed.copy(.12f)), contentAlignment = Alignment.Center) { Text(if (t.side == Side.LONG) "L" else "S", color = if (t.side == Side.LONG) AxGreen else AxRed, fontWeight = FontWeight.Black) }
                    Spacer(Modifier.width(9.dp)); Column(Modifier.weight(1f)) { Text("${money(t.entryPrice)} → ${money(t.exitPrice)}", color = AxText, fontSize = 10.sp); Text("Qty ${"%.5f".format(t.quantity)} • Fee ${"%.4f".format(t.fees)} • Funding ${"%.4f".format(t.funding)}", color = AxMuted, fontSize = 8.sp) }; Text("%+.3f".format(t.netPnl), color = if (t.netPnl >= 0) AxGreen else AxRed, fontWeight = FontWeight.Black, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable private fun AxOhlc(c: Candle) { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(15.dp), colors = CardDefaults.cardColors(containerColor = AxPanel)) { Row(Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text("O ${money(c.open)}", color = AxText, fontSize = 9.sp); Text("H ${money(c.high)}", color = AxGreen, fontSize = 9.sp); Text("L ${money(c.low)}", color = AxRed, fontSize = 9.sp); Text("C ${money(c.close)}", color = AxText, fontSize = 9.sp); Text("V ${compact(c.volume)}", color = AxMuted, fontSize = 9.sp) } } }

@Composable private fun ReportCard(report: BacktestReport?, onOpen: (() -> Unit)?) { Card(onClick = onOpen ?: {}, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = AxPanel)) { Column(Modifier.padding(14.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Text("AI BACKTEST ANALYST", color = AxText, fontWeight = FontWeight.Black); Spacer(Modifier.weight(1f)); Text(if (report == null) "READY" else "REPORT", color = AxGreen, fontSize = 8.sp) }; Spacer(Modifier.height(10.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) { MiniMetric("ROI", report?.let { "%.2f%%".format(it.roiPercent) } ?: "—"); MiniMetric("WIN", report?.let { "%.1f%%".format(it.winRatePercent) } ?: "—"); MiniMetric("PF", report?.let { if (it.profitFactor.isInfinite()) "∞" else "%.2f".format(it.profitFactor) } ?: "—"); MiniMetric("DD", report?.let { "%.2f%%".format(it.maxDrawdownPercent) } ?: "—") }; Spacer(Modifier.height(8.dp)); EquityMini(report?.equityCurve ?: emptyList(), Modifier.fillMaxWidth().height(82.dp)) } } }

@Composable private fun AxSettings(fa: Boolean, themeMode: AppThemeMode, onThemeMode: (AppThemeMode) -> Unit, toggleLanguage: () -> Unit, onBack: () -> Unit) { Column(Modifier.fillMaxSize().background(AxBg)) { Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, null, tint = AxText) }; Column { Text("ALVEX", color = AxText, fontWeight = FontWeight.Black); Text(if (fa) "تنظیمات" else "Settings", color = AxMuted, fontSize = 9.sp) } }; LazyColumn(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(9.dp), contentPadding = PaddingValues(bottom = 24.dp)) { item { SettingsTile(Icons.Outlined.Person, if (fa) "Account & Security" else "Account & Security", "Session, password, secure API-key policy") }; item { SettingsTile(Icons.Outlined.Notifications, if (fa) "Notifications" else "Notifications", "Pump/Dump • Whale • AI alerts") }; item { SettingsTile(Icons.Outlined.Language, if (fa) "Language" else "Language", if (fa) "فارسی / English" else "English / فارسی") { toggleLanguage() } }; item { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("Theme", color = AxText, fontWeight = FontWeight.Bold); Text(if (themeMode == AppThemeMode.DARK) "Dark" else "Light", color = AxMuted, fontSize = 9.sp) }; Switch(themeMode == AppThemeMode.LIGHT) { onThemeMode(if (it) AppThemeMode.LIGHT else AppThemeMode.DARK) } } }; item { SettingsTile(Icons.Outlined.Share, if (fa) "Share & Reports" else "Share & Reports", "CSV • JSON • AI report • Android share") }; item { SettingsTile(Icons.Outlined.Info, "About ALVEX", "Professional crypto intelligence workspace") } } } }

@Composable private fun SettingsTile(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, onClick: (() -> Unit)? = null) { Card(onClick = onClick ?: {}, Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = AxPanel)) { Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = AxCyan); Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text(title, color = AxText, fontWeight = FontWeight.Bold); Text(subtitle, color = AxMuted, fontSize = 9.sp) }; Icon(Icons.Outlined.Info, null, tint = AxMuted, modifier = Modifier.size(16.dp)) } } }

@Composable private fun AxSection(title: String, subtitle: String = "") { Column { Text(title, color = AxText, fontSize = 15.sp, fontWeight = FontWeight.Black, letterSpacing = .4.sp); if (subtitle.isNotBlank()) Text(subtitle, color = AxMuted, fontSize = 9.sp) } }
@Composable private fun StatusPill(text: String, color: Color) { Box(Modifier.clip(RoundedCornerShape(30.dp)).background(color.copy(.12f)).padding(horizontal = 9.dp, vertical = 5.dp)) { Text(text, color = color, fontSize = 8.sp, fontWeight = FontWeight.Black) } }
@Composable private fun StatCard(title: String, value: String, sub: String, color: Color, modifier: Modifier) { Card(modifier, shape = RoundedCornerShape(17.dp), colors = CardDefaults.cardColors(containerColor = AxPanel)) { Column(Modifier.padding(11.dp)) { Text(title, color = AxMuted, fontSize = 8.sp); Text(value, color = AxText, fontSize = 12.sp, fontWeight = FontWeight.Black); Text(sub, color = color, fontSize = 8.sp, fontWeight = FontWeight.Bold) } } }
@Composable private fun AxMarketRow(q: AxQuote, onClick: () -> Unit) { Card(onClick = onClick, Modifier.fillMaxWidth(), shape = RoundedCornerShape(17.dp), colors = CardDefaults.cardColors(containerColor = AxPanel)) { Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(40.dp).clip(RoundedCornerShape(13.dp)).background(Brush.linearGradient(listOf(AxPurple.copy(.22f), AxCyan.copy(.12f)))), contentAlignment = Alignment.Center) { Text(q.symbol.take(1), color = AxText, fontWeight = FontWeight.Black) }; Spacer(Modifier.width(9.dp)); Column(Modifier.weight(1f)) { Text(q.symbol.removeSuffix("USDT"), color = AxText, fontWeight = FontWeight.Bold); Text("/ USDT • ${compact(q.volume)} vol", color = AxMuted, fontSize = 8.sp) }; Column(horizontalAlignment = Alignment.End) { Text(money(q.price), color = AxText, fontSize = 10.sp, fontWeight = FontWeight.Bold); Text(pct(q.change), color = if (q.change >= 0) AxGreen else AxRed, fontSize = 10.sp) } } } }
@Composable private fun RadarTile(title: String, subtitle: String, color: Color, onClick: () -> Unit) { Card(onClick = onClick, Modifier.weight(1f), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = AxPanel2)) { Column(Modifier.padding(10.dp)) { Box(Modifier.size(30.dp).clip(RoundedCornerShape(9.dp)).background(color.copy(.13f)), contentAlignment = Alignment.Center) { Text("•", color = color, fontSize = 20.sp) }; Spacer(Modifier.height(6.dp)); Text(title, color = AxText, fontSize = 9.sp, fontWeight = FontWeight.Bold); Text(subtitle, color = AxMuted, fontSize = 7.sp) } } }
@Composable private fun ActionTile(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, onClick: () -> Unit) { Card(onClick = onClick, Modifier.weight(1f), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = AxPanel)) { Column(Modifier.padding(11.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(icon, null, tint = AxPurple2); Spacer(Modifier.height(5.dp)); Text(title, color = AxText, fontSize = 9.sp, fontWeight = FontWeight.Bold) } } }
@Composable private fun IntelTile(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, color: Color) { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(19.dp), colors = CardDefaults.cardColors(containerColor = AxPanel)) { Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(color.copy(.12f)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = color) }; Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text(title, color = AxText, fontWeight = FontWeight.Black, fontSize = 11.sp); Text(subtitle, color = AxMuted, fontSize = 8.sp) }; Icon(Icons.Outlined.Info, null, tint = AxMuted, modifier = Modifier.size(16.dp)) } } }
@Composable private fun TimeframeStrip(selected: String, onSelect: (String) -> Unit) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) { listOf("1min", "5min", "15min", "1h", "4h", "1D", "More").forEach { value -> FilterChip(selected == value, { onSelect(value) }, label = { Text(value, fontSize = 8.sp) }) } } }
@Composable private fun MiniSparkline(values: List<Double>, modifier: Modifier) { Canvas(modifier) { if (values.size < 2) return@Canvas; val min = values.minOrNull() ?: 0.0; val max = values.maxOrNull() ?: 1.0; val r = (max - min).takeIf { it > 0 } ?: 1.0; val path = Path(); values.forEachIndexed { i, v -> val x = size.width * i / (values.lastIndex.toFloat()); val y = size.height - ((v - min) / r * size.height).toFloat(); if (i == 0) path.moveTo(x, y) else path.lineTo(x, y) }; drawPath(path, AxCyan, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)) } }
@Composable private fun EquityMini(values: List<Double>, modifier: Modifier) { Canvas(modifier) { if (values.size < 2) return@Canvas; val min = values.minOrNull() ?: 0.0; val max = values.maxOrNull() ?: 1.0; val r = (max - min).takeIf { it > 0 } ?: 1.0; for (i in 1 until values.size) { val x1 = size.width * (i - 1) / (values.lastIndex.toFloat()); val x2 = size.width * i / (values.lastIndex.toFloat()); val y1 = size.height - ((values[i - 1] - min) / r * size.height).toFloat(); val y2 = size.height - ((values[i] - min) / r * size.height).toFloat(); drawLine(AxPurple2, Offset(x1, y1), Offset(x2, y2), 2.2f) } } }
@Composable private fun AxRadar(score: Int) { Canvas(Modifier.size(150.dp)) { val c = center; val r = size.minDimension * .34f; for (i in 1..3) drawCircle(AxBorder, r * i / 3f, center, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)); drawCircle(if (score >= 70) AxRed else AxPurple2, r * .55f, c); drawCircle(AxCyan, r, c, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)) } }
@Composable private fun MiniMetric(label: String, value: String) { Column(Modifier.weight(1f)) { Text(label, color = AxMuted, fontSize = 7.sp); Text(value, color = AxText, fontSize = 10.sp, fontWeight = FontWeight.Black) } }
@Composable private fun AxMark(size: Int) { Box(Modifier.size(size.dp).clip(RoundedCornerShape((size / 4).dp)).background(Brush.linearGradient(listOf(AxPurple, AxCyan))), contentAlignment = Alignment.Center) { Text("A", color = Color.White, fontSize = (size / 1.9).sp, fontWeight = FontWeight.Black) } }
private fun money(v: Double) = if (v >= 1000) "%,.2f".format(v) else if (v >= 1) "%.4f".format(v) else "%.8f".format(v)
private fun pct(v: Double) = "%+.2f%%".format(v)
private fun compact(v: Double) = when { v >= 1e9 -> "%.1fB".format(v / 1e9); v >= 1e6 -> "%.1fM".format(v / 1e6); v >= 1e3 -> "%.1fK".format(v / 1e3); else -> "%.0f".format(v) }
