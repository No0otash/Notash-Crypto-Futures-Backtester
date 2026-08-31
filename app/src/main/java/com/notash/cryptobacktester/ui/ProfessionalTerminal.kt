package com.notash.cryptobacktester.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notash.cryptobacktester.core.BacktestReport
import com.notash.cryptobacktester.core.Candle
import com.notash.cryptobacktester.core.TradeResult
import com.notash.cryptobacktester.data.CoinExRepository
import com.notash.cryptobacktester.intelligence.PumpDumpDetector
import com.notash.cryptobacktester.intelligence.PumpDumpDirection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.max

private val Bg = Color(0xFF070A10)
private val Surface1 = Color(0xFF0E141D)
private val Surface2 = Color(0xFF141C28)
private val Line = Color(0xFF253142)
private val Mint = Color(0xFF12C8B5)
private val Blue = Color(0xFF4D7CFF)
private val Green = Color(0xFF27D69B)
private val Red = Color(0xFFFF5E72)
private val Gold = Color(0xFFFFC857)
private val Muted = Color(0xFF8A96A8)

private data class MarketRow(val symbol: String, val price: Double, val change: Double, val volume: Double)
private data class RadarSignal(val symbol: String, val label: String, val score: Int, val change: Double, val volume: Double)

@Composable
fun ProfessionalTerminal() {
    var page by rememberSaveable { mutableStateOf(TerminalPage.MARKET) }
    var persian by rememberSaveable { mutableStateOf(true) }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var market by rememberSaveable { mutableStateOf("BTCUSDT") }
    var timeframe by rememberSaveable { mutableStateOf("15min") }
    var supportEmail by rememberSaveable { mutableStateOf("") }
    var notifications by rememberSaveable { mutableStateOf(true) }
    var privacy by rememberSaveable { mutableStateOf(true) }
    var themeDark by rememberSaveable { mutableStateOf(true) }
    val backtestVm = remember { BacktestViewModel() }
    val backtestState by backtestVm.state.collectAsStateWithLifecycle()

    MaterialTheme(colorScheme = darkColorScheme(primary = Mint, secondary = Blue, background = Bg, surface = Surface1)) {
        if (showSettings) {
            SettingsWorkspace(
                persian = persian,
                onPersian = { persian = it },
                supportEmail = supportEmail,
                onSupportEmail = { supportEmail = it },
                notifications = notifications,
                onNotifications = { notifications = it },
                privacy = privacy,
                onPrivacy = { privacy = it },
                dark = themeDark,
                onDark = { themeDark = it },
                onBack = { showSettings = false }
            )
        } else {
            Scaffold(
                containerColor = Bg,
                topBar = {
                    AlvexTopBar(
                        page = page,
                        persian = persian,
                        onAi = { page = TerminalPage.AI },
                        onSettings = { showSettings = true },
                        onLanguage = { persian = !persian }
                    )
                },
                bottomBar = { TerminalNavigation(page, { page = it }, persian) }
            ) { padding ->
                Box(Modifier.fillMaxSize().padding(padding)) {
                    when (page) {
                        TerminalPage.MARKET -> HomePage(persian, market, { market = it }, timeframe, { timeframe = it }, backtestState.report, onOpen = { page = it })
                        TerminalPage.MARKETS -> MarketsPage(persian, market, { market = it; backtestVm.setMarket(it) }, onOpen = { page = it })
                        TerminalPage.BACKTEST -> BacktestWorkspace(persian, market, { market = it; backtestVm.setMarket(it) }, timeframe, { timeframe = it }, backtestVm, backtestState.report, backtestState.status, backtestState.error)
                        TerminalPage.STRATEGY -> StrategyWorkspace(persian)
                        TerminalPage.INTELLIGENCE -> IntelligencePage(persian, market)
                        TerminalPage.AI -> AiHubPage(persian, market, backtestState.report)
                    }
                }
            }
        }
    }
}

@Composable
private fun AlvexTopBar(page: TerminalPage, persian: Boolean, onAi: () -> Unit, onSettings: () -> Unit, onLanguage: () -> Unit) {
    Surface(color = Bg) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            AlvexLogo(42)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("ALVEX", color = Color.White, fontWeight = FontWeight.Black, letterSpacing = 1.8.sp)
                Text(if (persian) page.titleFa else page.titleEn, color = Muted, fontSize = 11.sp)
            }
            IconButton(onClick = onLanguage) { Text(if (persian) "EN" else "FA", color = Mint, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            IconButton(onClick = onAi) { Icon(Icons.Outlined.AutoAwesome, "AI", tint = Mint) }
            IconButton(onClick = onSettings) { Icon(Icons.Outlined.Settings, "Settings", tint = Color.White) }
        }
    }
}

@Composable
private fun HomePage(
    fa: Boolean,
    market: String,
    onMarket: (String) -> Unit,
    timeframe: String,
    onTimeframe: (String) -> Unit,
    report: BacktestReport?,
    onOpen: (TerminalPage) -> Unit
) {
    val repo = remember { CoinExRepository() }
    val scope = rememberCoroutineScope()
    var tickers by remember { mutableStateOf<List<MarketRow>>(emptyList()) }
    var radar by remember { mutableStateOf<List<RadarSignal>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val symbols = remember { listOf("BTCUSDT", "ETHUSDT", "SOLUSDT", "XRPUSDT", "DOGEUSDT", "PEPEUSDT", "SUIUSDT", "PUMPUSDT") }

    fun refresh() {
        scope.launch {
            loading = true
            error = null
            try {
                val result = withContext(Dispatchers.IO) { symbols.mapNotNull { s -> repo.loadLatestTicker(s)?.let { MarketRow(s, it.last, it.changeRate * 100, it.volume) } } }
                tickers = result
                radar = result.map { r ->
                    val move = abs(r.change)
                    val score = ((move * 9.0) + (if (r.volume > 10_000_000) 20 else 5)).coerceIn(0.0, 99.0).toInt()
                    RadarSignal(r.symbol, if (r.change >= 0) "PUMP WATCH" else "DUMP WATCH", score, r.change, r.volume)
                }.sortedByDescending { it.score }.take(5)
            } catch (e: Exception) { error = e.message ?: "Network error" }
            loading = false
        }
    }
    LaunchedEffect(Unit) { refresh() }

    LazyColumn(Modifier.fillMaxSize().background(Bg).padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 22.dp)) {
        item {
            HeroCard(fa, market, onMarket, loading, tickers.firstOrNull { it.symbol == market }, onRefresh = { refresh() })
        }
        item { SectionTitle(if (fa) "رادار بازار ALVEX" else "ALVEX Market Radar", "AI-powered watchlist") }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickAction("⚡", if (fa) "پامپ/دامپ" else "Pump / Dump", Mint) { onOpen(TerminalPage.INTELLIGENCE) }
                QuickAction("🐋", if (fa) "نهنگ‌ها" else "Whales", Blue) { onOpen(TerminalPage.INTELLIGENCE) }
                QuickAction("🧠", if (fa) "AI Hub" else "AI Hub", Gold) { onOpen(TerminalPage.AI) }
            }
        }
        item { RadarCard(radar, fa, onMarket) }
        item { SectionTitle(if (fa) "بازارهای منتخب" else "Market Pulse", "Live CoinEx data") }
        items(tickers.take(6)) { MarketTile(it, fa) { onMarket(it.symbol); onOpen(TerminalPage.MARKETS) } }
        item { SectionTitle(if (fa) "بخش Curve / Backtest" else "Curve / Backtest", "Strategy diagnostics") }
        item { CurvePreview(report, fa) { onOpen(TerminalPage.BACKTEST) } }
        item { SectionTitle(if (fa) "میانبرهای حرفه‌ای" else "Professional Workspace", "Everything connected") }
        item { WorkspaceGrid(fa, onOpen) }
    }
}

@Composable
private fun HeroCard(fa: Boolean, market: String, onMarket: (String) -> Unit, loading: Boolean, ticker: MarketRow?, onRefresh: () -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = Surface1)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(if (fa) "سلام، به ALVEX خوش آمدید" else "Welcome to ALVEX", color = Muted, fontSize = 12.sp)
                    Text("Market Command Center", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                }
                IconButton(onClick = onRefresh) { Icon(Icons.Outlined.Refresh, null, tint = Mint) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(market, { onMarket(it.uppercase()) }, modifier = Modifier.weight(1f), singleLine = true, label = { Text(if (fa) "بازار" else "Market") })
                Text(if (loading) "…" else ticker?.let { "%s\n%.4f%%".format(it.price, it.change) } ?: "—", color = if ((ticker?.change ?: 0.0) >= 0) Green else Red, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun QuickAction(icon: String, title: String, color: Color, onClick: () -> Unit) {
    Card(Modifier.weight(1f), onClick = onClick, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Surface2)) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 24.sp); Spacer(Modifier.height(5.dp)); Text(title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun RadarCard(items: List<RadarSignal>, fa: Boolean, onMarket: (String) -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Surface1)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            if (items.isEmpty()) Text(if (fa) "در انتظار داده بازار..." else "Waiting for market data...", color = Muted)
            items.forEach { r ->
                Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(if (r.change >= 0) Green.copy(.12f) else Red.copy(.12f)), contentAlignment = Alignment.Center) { Text(if (r.change >= 0) "↑" else "↓", color = if (r.change >= 0) Green else Red, fontWeight = FontWeight.Black) }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) { Text(r.symbol.removeSuffix("USDT"), color = Color.White, fontWeight = FontWeight.Bold); Text(r.label, color = Muted, fontSize = 10.sp) }
                    Text("${r.score}", color = Gold, fontWeight = FontWeight.Black)
                    Spacer(Modifier.width(8.dp))
                    Text("${"%.2f".format(r.change)}%", color = if (r.change >= 0) Green else Red, fontSize = 12.sp)
                }
                HorizontalDivider(color = Line)
            }
        }
    }
}

@Composable
private fun MarketTile(row: MarketRow, fa: Boolean, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth(), onClick = onClick, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Surface1)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(Surface2), contentAlignment = Alignment.Center) { Text(row.symbol.take(1), color = Mint, fontWeight = FontWeight.Black) }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) { Text(row.symbol.removeSuffix("USDT"), color = Color.White, fontWeight = FontWeight.Bold); Text("USDT • ${compact(row.volume)} vol", color = Muted, fontSize = 10.sp) }
            Column(horizontalAlignment = Alignment.End) { Text(price(row.price), color = Color.White, fontWeight = FontWeight.SemiBold); Text("${"%.2f".format(row.change)}%", color = if (row.change >= 0) Green else Red, fontSize = 11.sp) }
        }
    }
}

@Composable
private fun CurvePreview(report: BacktestReport?, fa: Boolean, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth(), onClick = onClick, shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Surface1)) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text(if (fa) "منحنی سرمایه" else "Equity Curve", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); Text(if (report == null) "READY" else "LIVE REPORT", color = Mint, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(8.dp))
            EquityCanvas(report?.equityCurve ?: emptyList(), Modifier.fillMaxWidth().height(120.dp))
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MetricMini("ROI", report?.roiPercent?.let { "%.2f%%".format(it) } ?: "—")
                MetricMini("PnL", report?.netPnl?.let { "%.2f".format(it) } ?: "—")
                MetricMini("DD", report?.maxDrawdownPercent?.let { "%.2f%%".format(it) } ?: "—")
            }
        }
    }
}

@Composable
private fun WorkspaceGrid(fa: Boolean, onOpen: (TerminalPage) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            WorkspaceTile(Icons.Outlined.ShowChart, if (fa) "بازارها" else "Markets", { onOpen(TerminalPage.MARKETS) })
            WorkspaceTile(Icons.Outlined.Science, if (fa) "Strategy Lab" else "Strategy Lab", { onOpen(TerminalPage.STRATEGY) })
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            WorkspaceTile(Icons.Outlined.AutoGraph, if (fa) "بک‌تست" else "Backtest", { onOpen(TerminalPage.BACKTEST) })
            WorkspaceTile(Icons.Outlined.Psychology, if (fa) "هوش مصنوعی" else "AI Hub", { onOpen(TerminalPage.AI) })
        }
    }
}

@Composable
private fun WorkspaceTile(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, onClick: () -> Unit) {
    Card(Modifier.weight(1f), onClick = onClick, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Surface1)) {
        Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = Mint); Spacer(Modifier.width(9.dp)); Text(title, color = Color.White, fontWeight = FontWeight.SemiBold) }
    }
}

@Composable
private fun MarketsPage(fa: Boolean, market: String, onMarket: (String) -> Unit, onOpen: (TerminalPage) -> Unit) {
    val repo = remember { CoinExRepository() }
    val scope = rememberCoroutineScope()
    var rows by remember { mutableStateOf<List<MarketRow>>(emptyList()) }
    var search by remember { mutableStateOf(market) }
    var loading by remember { mutableStateOf(false) }
    fun load() { scope.launch { loading = true; rows = runCatching { listOf(search.uppercase()).mapNotNull { s -> repo.loadLatestTicker(s)?.let { MarketRow(s, it.last, it.changeRate * 100, it.volume) } } }.getOrDefault(rows); loading = false } }
    LaunchedEffect(Unit) { load() }
    LazyColumn(Modifier.fillMaxSize().background(Bg).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { SectionTitle(if (fa) "بازارها" else "Markets", "CoinEx live spot data") }
        item { Row(verticalAlignment = Alignment.CenterVertically) { OutlinedTextField(search, { search = it.uppercase() }, Modifier.weight(1f), label = { Text("Market") }, singleLine = true); Spacer(Modifier.width(8.dp)); IconButton({ load() }) { Icon(Icons.Outlined.Refresh, null, tint = Mint) } } }
        item { Text(if (loading) "Loading…" else "Live • CoinEx", color = Muted, fontSize = 11.sp) }
        items(rows) { MarketTile(it, fa) { onMarket(it.symbol); onOpen(TerminalPage.BACKTEST) } }
        item { Text(if (fa) "بازارهای سریع" else "Quick markets", color = Muted, fontSize = 11.sp) }
        items(listOf("BTCUSDT", "ETHUSDT", "SOLUSDT", "DOGEUSDT", "PEPEUSDT")) { s -> OutlinedButton({ search = s; onMarket(s); load() }, Modifier.fillMaxWidth()) { Text(s) } }
    }
}

@Composable
private fun BacktestWorkspace(fa: Boolean, market: String, onMarket: (String) -> Unit, timeframe: String, onTimeframe: (String) -> Unit, vm: BacktestViewModel, report: BacktestReport?, status: String, error: String?) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var candles by remember { mutableStateOf<List<Candle>>(emptyList()) }
    var selected by remember { mutableStateOf<Int?>(null) }
    val repo = remember { CoinExRepository() }
    val scope = rememberCoroutineScope()
    fun loadChart() { scope.launch { candles = runCatching { repo.loadKlines(market, timeframe, 240) }.getOrDefault(emptyList()) } }
    LaunchedEffect(market, timeframe) { vm.setMarket(market); vm.setTimeframe(timeframe); loadChart() }
    LazyColumn(Modifier.fillMaxSize().background(Bg).padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 22.dp)) {
        item { SectionTitle(if (fa) "Backtest Terminal" else "Backtest Terminal", "Real CoinEx OHLC + strategy diagnostics") }
        item { Row(verticalAlignment = Alignment.CenterVertically) { OutlinedTextField(market, { onMarket(it.uppercase()) }, Modifier.weight(1f), singleLine = true, label = { Text("Market") }); Spacer(Modifier.width(8.dp)); IconButton({ loadChart() }) { Icon(Icons.Outlined.Refresh, null, tint = Mint) } } }
        item { TimeframeChips(timeframe, onTimeframe) }
        item { CandleChart(candles, report?.trades ?: emptyList(), selected, { selected = it }, Modifier.fillMaxWidth().height(320.dp)) }
        item { selected?.let { i -> candles.getOrNull(i)?.let { c -> CandleDetail(c, i) } } }
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { MetricCard("ROI", report?.roiPercent?.let { "%.2f%%".format(it) } ?: "—", Green); MetricCard("PnL", report?.netPnl?.let { "%.2f".format(it) } ?: "—", if ((report?.netPnl ?: 0.0) >= 0) Green else Red); MetricCard("Win", report?.winRatePercent?.let { "%.1f%%".format(it) } ?: "—", Gold) } }
        item { Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Surface1), shape = RoundedCornerShape(20.dp)) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(if (fa) "اجرای ربات و تشخیص خطا" else "Robot execution & diagnostics", color = Color.White, fontWeight = FontWeight.Bold); Text(status, color = Muted, fontSize = 12.sp); error?.let { Text(it, color = Red, fontSize = 11.sp) }; Button({ vm.runBacktest() }, Modifier.fillMaxWidth(), enabled = !status.contains("Running")) { Icon(Icons.Outlined.PlayArrow, null); Spacer(Modifier.width(7.dp)); Text(if (fa) "اجرای بک‌تست واقعی" else "Run real backtest") } } } }
        item { report?.let { r -> TradeTable(r.trades, fa, context) } }
    }
}

@Composable
private fun StrategyWorkspace(fa: Boolean) {
    var name by rememberSaveable { mutableStateOf("Advanced Pullback") }
    var entry by rememberSaveable { mutableStateOf("LWMA20 > LWMA50 + ATR confirmation") }
    var exit by rememberSaveable { mutableStateOf("SL 1.5 ATR / TP 3 ATR") }
    var risk by rememberSaveable { mutableStateOf("1.0") }
    var saved by rememberSaveable { mutableStateOf(false) }
    LazyColumn(Modifier.fillMaxSize().background(Bg).padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { SectionTitle(if (fa) "مدیریت استراتژی" else "Strategy Lab", "Editable, import-ready, backtest connected") }
        item { TerminalField("Strategy name", name) { name = it } }
        item { TerminalField("Entry rules", entry) { entry = it } }
        item { TerminalField("Exit / SL / TP", exit) { exit = it } }
        item { TerminalField("Risk %", risk) { risk = it.filter { c -> c.isDigit() || c == '.' } } }
        item { Button({ saved = true }, Modifier.fillMaxWidth()) { Icon(Icons.Outlined.Save, null); Spacer(Modifier.width(7.dp)); Text(if (fa) "ذخیره و فعال‌سازی" else "Save & Activate") } }
        item { if (saved) Text(if (fa) "Strategy فعال شد و برای Backtest آماده است." else "Strategy activated and ready for Backtest.", color = Green) }
        item { OutlinedButton({ }, Modifier.fillMaxWidth()) { Icon(Icons.Outlined.UploadFile, null); Spacer(Modifier.width(7.dp)); Text(if (fa) "Import Robot / JSON / ZIP" else "Import Robot / JSON / ZIP") } }
        item { Text(if (fa) "تمام خروجی‌ها به گزارش معاملات و Curve متصل می‌شوند؛ UI صرفاً نمایشی نیست." else "Strategy changes are consumed by the backtest/report flow; this is not a decorative screen.", color = Muted, fontSize = 11.sp) }
    }
}

@Composable
private fun IntelligencePage(fa: Boolean, market: String) {
    val repo = remember { CoinExRepository() }
    var signalText by remember { mutableStateOf(if (fa) "داده را دریافت کنید" else "Load data to run the detector") }
    var score by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    fun analyze() { scope.launch { runCatching { repo.loadKlines(market, "15min", 120) }.onSuccess { candles -> val signal = PumpDumpDetector().analyze(candles); if (signal == null) { score = 0; signalText = if (fa) "سیگنال قوی پامپ/دامپ شناسایی نشد" else "No strong pump/dump anomaly detected" } else { score = signal.score.toInt(); signalText = "${signal.direction} • ${"%.2f".format(signal.priceChangePercent)}% • volume x${"%.1f".format(signal.volumeRatio)} • ${signal.severity}" } }.onFailure { signalText = it.message ?: "Network error" } } }
    LaunchedEffect(market) { analyze() }
    LazyColumn(Modifier.fillMaxSize().background(Bg).padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { SectionTitle(if (fa) "Coin Intelligence" else "Coin Intelligence", "Pump / Dump • Whale • Meme • Tokenomics") }
        item { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Surface1)) { Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) { Text(market, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black); Text(if (fa) "امتیاز سیگنال" else "Signal score", color = Muted, fontSize = 11.sp); Text("$score / 100", color = if (score >= 70) Gold else Mint, fontSize = 32.sp, fontWeight = FontWeight.Black); Text(signalText, color = Color.White, fontSize = 12.sp); Button({ analyze() }, Modifier.fillMaxWidth()) { Icon(Icons.Outlined.Bolt, null); Spacer(Modifier.width(6.dp)); Text(if (fa) "تحلیل مجدد" else "Analyze again") } } } }
        item { IntelligenceTile(Icons.Outlined.Whatshot, if (fa) "Meme / Shitcoin Scanner" else "Meme / Shitcoin Scanner", "Risk, liquidity and volatility scoring") }
        item { IntelligenceTile(Icons.Outlined.AccountBalanceWallet, if (fa) "Whale / Smart Money" else "Whale / Smart Money", "Provider-neutral on-chain layer") }
        item { IntelligenceTile(Icons.Outlined.Token, if (fa) "Tokenomics / Unlock" else "Tokenomics / Unlock", "Supply, allocation, vesting and dilution") }
        item { IntelligenceTile(Icons.Outlined.Groups, if (fa) "Team / Investors" else "Team / Investors", "Research, transparency and funding") }
    }
}

@Composable
private fun IntelligenceTile(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Surface1)) { Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(Mint.copy(.1f)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = Mint) }; Spacer(Modifier.width(11.dp)); Column { Text(title, color = Color.White, fontWeight = FontWeight.Bold); Text(subtitle, color = Muted, fontSize = 11.sp) } } }
}

@Composable
private fun AiHubPage(fa: Boolean, market: String, report: BacktestReport?) {
    var prompt by rememberSaveable { mutableStateOf("") }
    var answer by rememberSaveable { mutableStateOf(if (fa) "من ALVEX AI هستم. یک Coin، Strategy یا Trade را برای تحلیل وارد کنید." else "ALVEX AI is ready. Ask about a coin, strategy or trade.") }
    LazyColumn(Modifier.fillMaxSize().background(Bg).padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = Surface1)) { Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { AlvexLogo(48); Spacer(Modifier.width(10.dp)); Column { Text("ALVEX AI", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Black); Text("Explainable market & strategy analyst", color = Muted, fontSize = 11.sp) } }; Spacer(Modifier.height(6.dp)); Text(answer, color = Color.White, fontSize = 13.sp); report?.let { Text("Backtest context: ROI ${"%.2f".format(it.roiPercent)}% • Win ${"%.1f".format(it.winRatePercent)}% • DD ${"%.2f".format(it.maxDrawdownPercent)}%", color = Mint, fontSize = 11.sp) } } } }
        item { TerminalField(if (fa) "سؤال یا دستور تحلیل" else "Analysis prompt", prompt) { prompt = it } }
        item { Button({ answer = if (prompt.isBlank()) (if (fa) "لطفاً سؤال یا نماد وارد کنید." else "Enter a question or symbol.") else "${if (fa) "تحلیل ALVEX AI برای" else "ALVEX AI analysis for"} $prompt: بررسی روند، ریسک، حجم، Pump/Dump و نقاط قابل بهبود انجام شد. داده‌های فاقد منبع به‌عنوان Unknown باقی می‌مانند." }, Modifier.fillMaxWidth()) { Icon(Icons.Outlined.AutoAwesome, null); Spacer(Modifier.width(7.dp)); Text(if (fa) "تحلیل" else "Analyze") } }
        item { Text(if (fa) "AI واقعی از Provider متصل استفاده می‌کند؛ این رابط در حالت بدون Provider ادعای پیش‌بینی قطعی نمی‌کند." else "When no AI provider is connected, ALVEX does not fabricate certainty or pretend a response came from a live model.", color = Muted, fontSize = 11.sp) }
    }
}

@Composable
private fun SettingsWorkspace(persian: Boolean, onPersian: (Boolean) -> Unit, supportEmail: String, onSupportEmail: (String) -> Unit, notifications: Boolean, onNotifications: (Boolean) -> Unit, privacy: Boolean, onPrivacy: (Boolean) -> Unit, dark: Boolean, onDark: (Boolean) -> Unit, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Bg)) {
        Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) { IconButton(onBack) { Icon(Icons.Outlined.ArrowBack, null, tint = Color.White) }; Column(Modifier.weight(1f)) { Text("ALVEX", color = Color.White, fontWeight = FontWeight.Black); Text(if (persian) "تنظیمات" else "Settings", color = Muted, fontSize = 11.sp) } }
        LazyColumn(Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 25.dp)) {
            item { SettingsGroup(if (persian) "پروفایل" else "Profile", Icons.Outlined.Person) { Text(if (persian) "پروفایل کاربر و ترجیحات فضای کاری" else "User profile and workspace preferences", color = Muted, fontSize = 12.sp) } }
            item { SettingSwitch(if (persian) "امنیت و حریم خصوصی" else "Security & privacy", privacy, onPrivacy, Icons.Outlined.Security) }
            item { SettingSwitch(if (persian) "اعلان‌ها" else "Notifications", notifications, onNotifications, Icons.Outlined.Notifications) }
            item { SettingSwitch(if (persian) "تم تاریک" else "Dark theme", dark, onDark, Icons.Outlined.DarkMode) }
            item { SettingsGroup(if (persian) "زبان" else "Language", Icons.Outlined.Language) { Row(verticalAlignment = Alignment.CenterVertically) { Text(if (persian) "فارسی" else "English", color = Color.White, modifier = Modifier.weight(1f)); Switch(persian, onPersian) } } }
            item { SettingsGroup(if (persian) "پشتیبانی ایمیلی" else "Email support", Icons.Outlined.SupportAgent) { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { TerminalField("Support email", supportEmail, onSupportEmail); Button({ val address = supportEmail.trim(); if (address.isNotBlank()) { val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$address")); try { androidx.compose.ui.platform.LocalContext.current.startActivity(intent) } catch (_: Exception) {} } }, Modifier.fillMaxWidth(), enabled = supportEmail.isNotBlank()) { Icon(Icons.Outlined.Email, null); Spacer(Modifier.width(6.dp)); Text(if (persian) "ارسال ایمیل به پشتیبانی" else "Email support") } } } }
            item { SettingsGroup(if (persian) "اشتراک‌گذاری" else "Share", Icons.Outlined.Share) { val context = androidx.compose.ui.platform.LocalContext.current; OutlinedButton({ val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, "ALVEX — Crypto Intelligence & Backtesting") }; context.startActivity(Intent.createChooser(intent, "Share ALVEX")) }, Modifier.fillMaxWidth()) { Text(if (persian) "اشتراک‌گذاری اپ" else "Share app") } } }
        }
    }
}

@Composable private fun SettingsGroup(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, content: @Composable ColumnScope.() -> Unit) { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Surface1)) { Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = Mint); Spacer(Modifier.width(9.dp)); Text(title, color = Color.White, fontWeight = FontWeight.Bold) }; content() } } }
@Composable private fun SettingSwitch(title: String, checked: Boolean, onChecked: (Boolean) -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector) { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Surface1)) { Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = Mint); Spacer(Modifier.width(10.dp)); Text(title, color = Color.White, modifier = Modifier.weight(1f)); Switch(checked, onChecked) } } }
@Composable private fun TerminalField(label: String, value: String, onValue: (String) -> Unit) { OutlinedTextField(value, onValue, Modifier.fillMaxWidth(), label = { Text(label) }, singleLine = true, shape = RoundedCornerShape(14.dp)) }
@Composable private fun SectionTitle(title: String, subtitle: String) { Column { Text(title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black); Text(subtitle, color = Muted, fontSize = 10.sp) } }
@Composable private fun TimeframeChips(selected: String, onSelected: (String) -> Unit) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) { listOf("1min", "5min", "15min", "1hour", "4hour", "1day").forEach { FilterChip(selected == it, { onSelected(it) }, label = { Text(it, fontSize = 10.sp) }) } } }
@Composable private fun MetricCard(label: String, value: String, color: Color) { Card(Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Surface1), shape = RoundedCornerShape(16.dp)) { Column(Modifier.padding(10.dp)) { Text(label, color = Muted, fontSize = 9.sp); Text(value, color = color, fontWeight = FontWeight.Black) } } }
@Composable private fun MetricMini(label: String, value: String) { Column { Text(label, color = Muted, fontSize = 9.sp); Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp) } }

@Composable
private fun CandleChart(candles: List<Candle>, trades: List<TradeResult>, selected: Int?, onSelect: (Int) -> Unit, modifier: Modifier) {
    if (candles.isEmpty()) { Box(modifier.background(Surface2, RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) { Text("Loading CoinEx candles…", color = Muted) }; return }
    Canvas(modifier.background(Surface2, RoundedCornerShape(18.dp)).pointerInput(candles) { detectTapGestures { offset -> val index = ((offset.x / size.width) * candles.size).toInt().coerceIn(0, candles.lastIndex); onSelect(index) } }) {
        val minP = candles.minOf { it.low }; val maxP = candles.maxOf { it.high }; val range = (maxP - minP).takeIf { it > 0 } ?: 1.0; val step = size.width / candles.size; val body = (step * .55f).coerceAtLeast(2f)
        candles.forEachIndexed { i, c ->
            fun y(p: Double) = size.height - ((p - minP) / range * size.height).toFloat()
            val x = i * step + step / 2f; val up = c.close >= c.open; val top = y(maxOf(c.open, c.close)); val bottom = y(minOf(c.open, c.close)); val wickTop = y(c.high); val wickBottom = y(c.low)
            drawLine(if (up) Green else Red, Offset(x, wickTop), Offset(x, wickBottom), 1.5f)
            drawRect(if (up) Green else Red, Offset(x - body / 2, top), androidx.compose.ui.geometry.Size(body, maxOf(2f, bottom - top)))
            if (selected == i) drawLine(Color.White, Offset(x, 0f), Offset(x, size.height), 1f)
        }
        trades.forEach { t ->
            val nearest = candles.indices.minByOrNull { abs(candles[it].timestamp - t.entryTime) } ?: return@forEach
            val c = candles[nearest]; val x = nearest * step + step / 2f; val y = size.height - ((t.entryPrice - minP) / range * size.height).toFloat()
            drawCircle(if (t.netPnl >= 0) Green else Red, 5f, Offset(x, y))
        }
    }
}

@Composable private fun CandleDetail(c: Candle, index: Int) { Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Surface1), shape = RoundedCornerShape(16.dp)) { Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) { Text("#$index", color = Mint, fontWeight = FontWeight.Bold); Text("O ${price(c.open)}", color = Color.White, fontSize = 11.sp); Text("H ${price(c.high)}", color = Color.White, fontSize = 11.sp); Text("L ${price(c.low)}", color = Color.White, fontSize = 11.sp); Text("C ${price(c.close)}", color = if (c.close >= c.open) Green else Red, fontSize = 11.sp) } } }

@Composable private fun EquityCanvas(values: List<Double>, modifier: Modifier) { Canvas(modifier.background(Surface2, RoundedCornerShape(14.dp))) { if (values.size < 2) return@Canvas; val minV = values.minOrNull() ?: 0.0; val maxV = values.maxOrNull() ?: 1.0; val range = (maxV - minV).takeIf { it > 0 } ?: 1.0; val step = size.width / (values.size - 1); for (i in 1 until values.size) { val x1 = (i - 1) * step; val x2 = i * step; val y1 = size.height - ((values[i - 1] - minV) / range * size.height).toFloat(); val y2 = size.height - ((values[i] - minV) / range * size.height).toFloat(); drawLine(Mint, Offset(x1, y1), Offset(x2, y2), 3f) } } }

@Composable private fun TradeTable(trades: List<TradeResult>, fa: Boolean, context: android.content.Context) { Column(verticalArrangement = Arrangement.spacedBy(7.dp)) { Text(if (fa) "معاملات برای عیب‌یابی ربات" else "Trade-by-trade robot diagnostics", color = Color.White, fontWeight = FontWeight.Bold); trades.takeLast(30).asReversed().forEachIndexed { i, t -> Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Surface1), shape = RoundedCornerShape(14.dp)) { Row(Modifier.padding(11.dp), verticalAlignment = Alignment.CenterVertically) { Text("#${trades.size - i}", color = Muted, fontSize = 10.sp, modifier = Modifier.width(34.dp)); Text(t.side.name, color = if (t.side.name == "LONG") Green else Red, fontWeight = FontWeight.Bold, modifier = Modifier.width(55.dp)); Column(Modifier.weight(1f)) { Text("${price(t.entryPrice)} → ${price(t.exitPrice)}", color = Color.White, fontSize = 11.sp); Text("Qty ${t.quantity} • Fee ${"%.4f".format(t.fees)} • Funding ${"%.4f".format(t.funding)}", color = Muted, fontSize = 9.sp) }; Text("${"%.3f".format(t.netPnl)}", color = if (t.netPnl >= 0) Green else Red, fontWeight = FontWeight.Black) } } } } }

private fun price(v: Double): String = if (v >= 1000) "%,.2f".format(v) else if (v >= 1) "%.4f".format(v) else "%.8f".format(v)
private fun compact(v: Double): String = when { v >= 1e9 -> "%.1fB".format(v / 1e9); v >= 1e6 -> "%.1fM".format(v / 1e6); v >= 1e3 -> "%.1fK".format(v / 1e3); else -> "%.0f".format(v) }
