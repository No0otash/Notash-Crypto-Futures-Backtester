package com.notash.cryptobacktester.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notash.cryptobacktester.data.CoinExRepository
import com.notash.cryptobacktester.intelligence.AiRadarEngine
import com.notash.cryptobacktester.intelligence.RadarMarketSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

private val AlvexBg = Color(0xFF050A12)
private val AlvexPanel = Color(0xFF0C1521)
private val AlvexPanel2 = Color(0xFF101C2B)
private val AlvexPurple = Color(0xFF7C3AED)
private val AlvexPurple2 = Color(0xFF5B21B6)
private val AlvexGreen = Color(0xFF22D39B)
private val AlvexRed = Color(0xFFFF4D67)
private val AlvexGold = Color(0xFFFFC857)
private val AlvexText = Color(0xFFF4F7FB)
private val AlvexMuted = Color(0xFF8290A5)
private val AlvexDivider = Color(0xFF1A2A3C)

private data class ReferenceQuote(val symbol: String, val price: Double, val change: Double, val volume: Double)

@Composable
fun AlvexReferenceUi() {
    var selected by rememberSaveable { mutableStateOf(ReferenceTab.HOME) }
    var showTerminal by rememberSaveable { mutableStateOf(false) }
    if (showTerminal) {
        Box(Modifier.fillMaxSize().background(AlvexBg)) {
            ProfessionalTerminal()
            SmallFloatingActionButton(
                onClick = { showTerminal = false },
                modifier = Modifier.padding(16.dp).align(Alignment.TopStart),
                containerColor = AlvexPurple
            ) { Icon(Icons.Outlined.ArrowBack, "Back", tint = Color.White) }
        }
        return
    }

    MaterialTheme(colorScheme = darkColorScheme(primary = AlvexPurple, secondary = AlvexGreen, background = AlvexBg, surface = AlvexPanel)) {
        Scaffold(
            containerColor = AlvexBg,
            topBar = { ReferenceTopBar(selected) },
            bottomBar = { ReferenceBottomBar(selected) { selected = it } }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                when (selected) {
                    ReferenceTab.HOME -> ReferenceHome(onOpenTerminal = { showTerminal = true }, onOpenRadar = { selected = ReferenceTab.RADAR })
                    ReferenceTab.RADAR -> ReferenceRadar()
                    ReferenceTab.MARKETS -> ReferenceMarkets(onOpenTerminal = { showTerminal = true })
                    ReferenceTab.WORKSPACE -> ReferenceWorkspace(onOpenTerminal = { showTerminal = true })
                    ReferenceTab.MORE -> ReferenceMore()
                }
            }
        }
    }
}

private enum class ReferenceTab(val en: String, val fa: String, val icon: ImageVector) {
    HOME("Home", "خانه", Icons.Outlined.Home),
    RADAR("AI Radar", "رادار", Icons.Outlined.AutoGraph),
    MARKETS("Markets", "بازار", Icons.Outlined.ShowChart),
    WORKSPACE("Workspace", "پروژه‌ها", Icons.Outlined.Dashboard),
    MORE("More", "بیشتر", Icons.Outlined.MoreHoriz)
}

@Composable
private fun ReferenceTopBar(tab: ReferenceTab) {
    Row(
        Modifier.fillMaxWidth().background(AlvexBg).padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AlvexMark(36)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text("ALVEX", color = AlvexText, fontSize = 19.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
            Text(tab.en, color = AlvexMuted, fontSize = 10.sp)
        }
        IconButton(onClick = {}) { Icon(Icons.Outlined.NotificationsNone, "Notifications", tint = AlvexText) }
        IconButton(onClick = {}) { Icon(Icons.Outlined.Settings, "Settings", tint = AlvexText) }
    }
}

@Composable
private fun ReferenceBottomBar(selected: ReferenceTab, onSelect: (ReferenceTab) -> Unit) {
    NavigationBar(containerColor = Color(0xFF07101B)) {
        ReferenceTab.values().forEach { tab ->
            NavigationBarItem(
                selected = selected == tab,
                onClick = { onSelect(tab) },
                icon = { Icon(tab.icon, null) },
                label = { Text(tab.fa, fontSize = 9.sp) },
                colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.White, selectedTextColor = AlvexGreen, indicatorColor = AlvexPurple2, unselectedIconColor = AlvexMuted, unselectedTextColor = AlvexMuted)
            )
        }
    }
}

@Composable
private fun ReferenceHome(onOpenTerminal: () -> Unit, onOpenRadar: () -> Unit) {
    val repo = remember { CoinExRepository() }
    val scope = rememberCoroutineScope()
    val symbols = remember { listOf("BTCUSDT", "ETHUSDT", "SOLUSDT", "BNBUSDT", "XRPUSDT", "DOGEUSDT", "PEPEUSDT") }
    var quotes by remember { mutableStateOf<List<ReferenceQuote>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    fun refresh() {
        scope.launch {
            loading = true
            quotes = withContext(Dispatchers.IO) {
                symbols.mapNotNull { symbol ->
                    runCatching { repo.loadLatestTicker(symbol) }.getOrNull()?.let { ReferenceQuote(symbol, it.last, it.changeRate * 100.0, it.volume) }
                }
            }
            loading = false
        }
    }
    LaunchedEffect(Unit) { refresh() }
    LaunchedEffect(Unit) { while (true) { delay(60_000); refresh() } }

    LazyColumn(Modifier.fillMaxSize().background(AlvexBg).padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(top = 6.dp, bottom = 20.dp)) {
        item {
            Card(shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = AlvexPanel)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Market Command Center", color = AlvexMuted, fontSize = 10.sp)
                            Text("AI Market Intelligence", color = AlvexText, fontSize = 23.sp, fontWeight = FontWeight.Black)
                        }
                        AlvexMark(52)
                    }
                    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF17133A))) {
                        Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("BTC Dominance", color = AlvexMuted, fontSize = 9.sp)
                                Text("Live market pulse", color = AlvexText, fontWeight = FontWeight.Bold)
                            }
                            Text("LIVE", color = AlvexGreen, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatCard("Market Cap", "—", Modifier.weight(1f))
                        StatCard("24h Volume", quotes.firstOrNull()?.volume?.let { compactUsd(it) } ?: "—", Modifier.weight(1f))
                        StatCard("Radar", if (loading) "…" else "READY", Modifier.weight(1f))
                    }
                }
            }
        }
        item { SectionHeader("Top Movers", "Live market data") }
        items(quotes.take(3)) { quote -> MarketRow(quote) }
        item { SectionHeader("AI Market Radar", "Explainable opportunity & risk") }
        item {
            Card(onClick = onOpenRadar, shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF17113A))) {
                Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadarOrb()
                    Spacer(Modifier.width(13.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Scan the market", color = AlvexText, fontWeight = FontWeight.Bold)
                        Text("Pump potential • Dump risk • Confidence", color = AlvexMuted, fontSize = 10.sp)
                    }
                    Icon(Icons.Outlined.ChevronRight, null, tint = AlvexText)
                }
            }
        }
        item { SectionHeader("Professional Terminal", "Backtest • Curve • Trades • Strategy") }
        item {
            ActionTile(Icons.Outlined.QueryStats, "Trading & Backtest Terminal", "باز کردن محیط کامل معاملات و بک‌تست", onOpenTerminal)
        }
        item { SectionHeader("Quick Intelligence", "Built into ALVEX") }
        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MiniTile(Icons.Outlined.Waves, "Whale", "نهنگ")
            MiniTile(Icons.Outlined.Bolt, "Pump/Dump", "پامپ/دامپ")
            MiniTile(Icons.Outlined.Token, "Tokenomics", "توکنومیکس")
        } }
    }
}

@Composable
private fun ReferenceRadar() {
    val repo = remember { CoinExRepository() }
    val engine = remember { AiRadarEngine() }
    val scope = rememberCoroutineScope()
    var signals by remember { mutableStateOf(emptyList<com.notash.cryptobacktester.intelligence.RadarSignal>()) }
    var status by remember { mutableStateOf("Scanning public market data…") }
    val symbols = remember { listOf("BTCUSDT", "ETHUSDT", "SOLUSDT", "BNBUSDT", "XRPUSDT", "DOGEUSDT", "PEPEUSDT") }

    fun scan() {
        scope.launch {
            status = "Scanning…"
            val snapshots = withContext(Dispatchers.IO) {
                symbols.mapNotNull { symbol ->
                    runCatching { repo.loadLatestTicker(symbol) }.getOrNull()?.let { t ->
                        val open = if (abs(t.changeRate) < 0.999) t.last / (1.0 + t.changeRate) else t.last
                        RadarMarketSnapshot("CoinEx", symbol, t.last, open, t.last, t.last, t.volume, t.volume, timestampMs = t.timestamp)
                    }
                }
            }
            signals = engine.score(snapshots)
            status = if (signals.isEmpty()) "No market data available" else "Live • ${signals.size} assets scored"
        }
    }
    LaunchedEffect(Unit) { scan() }
    LaunchedEffect(Unit) { while (true) { delay(60_000); scan() } }

    LazyColumn(Modifier.fillMaxSize().background(AlvexBg).padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 20.dp)) {
        item {
            Card(shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = AlvexPanel)) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) { Text("AI Market Radar", color = AlvexText, fontSize = 23.sp, fontWeight = FontWeight.Black); Text(status, color = AlvexMuted, fontSize = 10.sp) }
                        IconButton(onClick = { scan() }) { Icon(Icons.Outlined.Refresh, null, tint = AlvexGreen) }
                    }
                    Spacer(Modifier.height(10.dp))
                    RadarOrb()
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ScoreBadge("PUMP", signals.maxOfOrNull { it.pumpPotential }?.toString() ?: "—", AlvexGreen)
                        ScoreBadge("DUMP", signals.maxOfOrNull { it.dumpRisk }?.toString() ?: "—", AlvexRed)
                        ScoreBadge("CONF", signals.maxOfOrNull { it.confidence }?.toString() ?: "—", AlvexGold)
                    }
                }
            }
        }
        item { SectionHeader("Ranked Signals", "Real data • deterministic scoring • no fabricated intelligence") }
        items(signals.take(10)) { signal -> RadarSignalRow(signal) }
    }
}

@Composable
private fun ReferenceMarkets(onOpenTerminal: () -> Unit) {
    val repo = remember { CoinExRepository() }
    val scope = rememberCoroutineScope()
    var quotes by remember { mutableStateOf<List<ReferenceQuote>>(emptyList()) }
    fun load() { scope.launch { quotes = withContext(Dispatchers.IO) { listOf("BTCUSDT", "ETHUSDT", "BNBUSDT", "SOLUSDT", "XRPUSDT", "DOGEUSDT", "ADAUSDT", "AVAXUSDT", "LINKUSDT").mapNotNull { s -> runCatching { repo.loadLatestTicker(s) }.getOrNull()?.let { ReferenceQuote(s, it.last, it.changeRate * 100.0, it.volume) } } } } }
    LaunchedEffect(Unit) { load() }
    Column(Modifier.fillMaxSize().background(AlvexBg)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Text("Markets", color = AlvexText, fontSize = 23.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f)); IconButton(onClick = { load() }) { Icon(Icons.Outlined.Refresh, null, tint = AlvexGreen) } }
        LazyColumn(Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(7.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
            items(quotes) { MarketRow(it) }
            item { Spacer(Modifier.height(6.dp)); ActionTile(Icons.Outlined.CandlestickChart, "Open Trading Terminal", "Chart • indicators • entries/exits • backtest", onOpenTerminal) }
        }
    }
}

@Composable
private fun ReferenceWorkspace(onOpenTerminal: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize().background(AlvexBg).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 20.dp)) {
        item { Text("Workspace", color = AlvexText, fontSize = 23.sp, fontWeight = FontWeight.Black) }
        item { ActionTile(Icons.Outlined.QueryStats, "Backtest Terminal", "Curve, trades, metrics and robot diagnostics", onOpenTerminal) }
        item { ActionTile(Icons.Outlined.AutoAwesome, "AI Hub", "Ask, analyze and explain strategy behavior", onOpenTerminal) }
        item { ActionTile(Icons.Outlined.Tune, "Strategy Lab", "Strategy configuration and execution tools", onOpenTerminal) }
        item { ActionTile(Icons.Outlined.Insights, "Coin Intelligence", "Project, tokenomics and risk intelligence", onOpenTerminal) }
    }
}

@Composable
private fun ReferenceMore() {
    LazyColumn(Modifier.fillMaxSize().background(AlvexBg).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 20.dp)) {
        item { Text("More", color = AlvexText, fontSize = 23.sp, fontWeight = FontWeight.Black) }
        item { MoreRow(Icons.Outlined.Security, "Account & Security") }
        item { MoreRow(Icons.Outlined.Notifications, "Notifications") }
        item { MoreRow(Icons.Outlined.Language, "Language", "English • فارسی • العربية • Français • 中文") }
        item { MoreRow(Icons.Outlined.PrivacyTip, "Privacy") }
        item { MoreRow(Icons.Outlined.SupportAgent, "Help & Support") }
        item { MoreRow(Icons.Outlined.Info, "About ALVEX", "ALVEX AI Market Intelligence") }
    }
}

@Composable private fun StatCard(title: String, value: String, modifier: Modifier) { Card(modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = AlvexPanel2)) { Column(Modifier.padding(10.dp)) { Text(title, color = AlvexMuted, fontSize = 8.sp); Text(value, color = AlvexText, fontWeight = FontWeight.Black, fontSize = 12.sp) } } }

@Composable private fun SectionHeader(title: String, subtitle: String) { Row(Modifier.fillMaxWidth().padding(horizontal = 2.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(title, color = AlvexText, fontSize = 15.sp, fontWeight = FontWeight.Bold); Text(subtitle, color = AlvexMuted, fontSize = 9.sp) }; Text("VIEW ALL", color = AlvexGreen, fontSize = 8.sp, fontWeight = FontWeight.Black) } }

@Composable private fun ActionTile(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) { Card(onClick = onClick, shape = RoundedCornerShape(19.dp), colors = CardDefaults.cardColors(containerColor = AlvexPanel)) { Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(43.dp).clip(RoundedCornerShape(14.dp)).background(AlvexPurple2), contentAlignment = Alignment.Center) { Icon(icon, null, tint = Color.White) }; Spacer(Modifier.width(11.dp)); Column(Modifier.weight(1f)) { Text(title, color = AlvexText, fontWeight = FontWeight.Bold, fontSize = 13.sp); Text(subtitle, color = AlvexMuted, fontSize = 9.sp) }; Icon(Icons.Outlined.ChevronRight, null, tint = AlvexMuted) } } }

@Composable private fun MiniTile(icon: ImageVector, title: String, subtitle: String) { Card(Modifier.weight(1f), shape = RoundedCornerShape(17.dp), colors = CardDefaults.cardColors(containerColor = AlvexPanel)) { Column(Modifier.padding(11.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(icon, null, tint = AlvexGreen); Spacer(Modifier.height(5.dp)); Text(title, color = AlvexText, fontSize = 10.sp, fontWeight = FontWeight.Bold); Text(subtitle, color = AlvexMuted, fontSize = 8.sp) } } }

@Composable private fun MarketRow(q: ReferenceQuote) { Card(shape = RoundedCornerShape(17.dp), colors = CardDefaults.cardColors(containerColor = AlvexPanel)) { Row(Modifier.padding(horizontal = 12.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(38.dp).clip(CircleShape).background(AlvexPanel2), contentAlignment = Alignment.Center) { Text(q.symbol.take(1), color = AlvexGreen, fontWeight = FontWeight.Black) }; Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text(q.symbol.removeSuffix("USDT"), color = AlvexText, fontWeight = FontWeight.Bold); Text("USDT • ${compactUsd(q.volume)}", color = AlvexMuted, fontSize = 8.sp) }; Column(horizontalAlignment = Alignment.End) { Text(formatPrice(q.price), color = AlvexText, fontWeight = FontWeight.SemiBold, fontSize = 12.sp); Text("%+.2f%%".format(q.change), color = if (q.change >= 0) AlvexGreen else AlvexRed, fontSize = 9.sp, fontWeight = FontWeight.Bold) } } } }

@Composable private fun RadarSignalRow(signal: com.notash.cryptobacktester.intelligence.RadarSignal) { val bullish = signal.pumpPotential >= signal.dumpRisk; Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = AlvexPanel)) { Column(Modifier.padding(13.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(40.dp).clip(RoundedCornerShape(13.dp)).background(if (bullish) AlvexGreen.copy(.12f) else AlvexRed.copy(.12f)), contentAlignment = Alignment.Center) { Text(if (bullish) "↑" else "↓", color = if (bullish) AlvexGreen else AlvexRed, fontSize = 20.sp, fontWeight = FontWeight.Black) }; Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text(signal.symbol.removeSuffix("USDT"), color = AlvexText, fontWeight = FontWeight.Black); Text(signal.exchanges.joinToString(" • "), color = AlvexMuted, fontSize = 8.sp) }; Text(if (bullish) "${signal.pumpPotential}" else "${signal.dumpRisk}", color = if (bullish) AlvexGreen else AlvexRed, fontSize = 19.sp, fontWeight = FontWeight.Black) }; Spacer(Modifier.height(8.dp)); Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { ScorePill("Momentum", signal.momentum); ScorePill("Volume", signal.volumePressure); ScorePill("Liquidity", signal.liquidityScore); ScorePill("Conf", signal.confidence) }; if (signal.reasons.isNotEmpty()) { Spacer(Modifier.height(7.dp)); Text(signal.reasons.joinToString(" • "), color = AlvexMuted, fontSize = 9.sp) } } } }

@Composable private fun ScorePill(label: String, score: Int) { Surface(shape = RoundedCornerShape(10.dp), color = AlvexPanel2) { Text("$label $score", Modifier.padding(horizontal = 7.dp, vertical = 4.dp), color = AlvexMuted, fontSize = 7.sp, fontWeight = FontWeight.Bold) } }
@Composable private fun ScoreBadge(label: String, value: String, color: Color) { Card(Modifier.weight(1f), shape = RoundedCornerShape(15.dp), colors = CardDefaults.cardColors(containerColor = color.copy(.10f))) { Column(Modifier.padding(10.dp)) { Text(label, color = AlvexMuted, fontSize = 8.sp); Text(value, color = color, fontSize = 18.sp, fontWeight = FontWeight.Black) } } }
@Composable private fun RadarOrb() { Box(Modifier.fillMaxWidth().height(72.dp), contentAlignment = Alignment.Center) { Canvas(Modifier.size(72.dp)) { drawCircle(AlvexPurple.copy(.14f), 34f); drawCircle(AlvexPurple.copy(.20f), 25f); drawCircle(AlvexGreen.copy(.22f), 15f); drawCircle(AlvexGreen, 5f); drawLine(AlvexGreen.copy(.5f), Offset(0f, 36f), Offset(72f, 36f), 1f); drawLine(AlvexGreen.copy(.5f), Offset(36f, 0f), Offset(36f, 72f), 1f) } } }
@Composable private fun MoreRow(icon: ImageVector, title: String, subtitle: String? = null) { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(17.dp), colors = CardDefaults.cardColors(containerColor = AlvexPanel)) { Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = AlvexText); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(title, color = AlvexText, fontWeight = FontWeight.SemiBold, fontSize = 12.sp); subtitle?.let { Text(it, color = AlvexMuted, fontSize = 8.sp) } }; Icon(Icons.Outlined.ChevronRight, null, tint = AlvexMuted) } } }
@Composable private fun AlvexMark(size: Int) { Box(Modifier.size(size.dp).clip(RoundedCornerShape((size / 4).dp)).background(Brush.linearGradient(listOf(AlvexPurple, Color(0xFFB65CFF), AlvexGreen))), contentAlignment = Alignment.Center) { Text("A", color = Color.White, fontSize = (size * .48f).sp, fontWeight = FontWeight.Black) } }
private fun formatPrice(value: Double): String = if (value >= 1000) "%,.2f".format(value) else "%.6f".format(value)
private fun compactUsd(value: Double): String = when { value >= 1_000_000_000 -> "%.1fB".format(value / 1_000_000_000); value >= 1_000_000 -> "%.1fM".format(value / 1_000_000); value >= 1_000 -> "%.1fK".format(value / 1_000); else -> "%.0f".format(value) }
