package com.notash.cryptobacktester.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notash.cryptobacktester.core.MarketTicker
import com.notash.cryptobacktester.data.CoinExRepository
import com.notash.cryptobacktester.market.MarketRadar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val homeBg = Color(0xFF05070D)
private val homePanel = Color(0xFF0B1020)
private val homeCyan = Color(0xFF22D3EE)
private val homeGreen = Color(0xFF20E6A5)
private val homeRed = Color(0xFFFF5577)
private val homeAmber = Color(0xFFFFB84D)
private val homeMuted = Color(0xFF8490A7)
private val homeText = Color(0xFFF5F7FF)

@Composable
fun HannahTerminal() {
    val drawer = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val repository = remember { CoinExRepository() }
    var fa by remember { mutableStateOf(true) }
    var page by remember { mutableStateOf("home") }
    var tickers by remember { mutableStateOf<List<MarketTicker>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var carouselPage by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            try { tickers = repository.loadAllFuturesMarkets(); error = null }
            catch (e: Exception) { error = e.message ?: if (fa) "خطا در دریافت بازار" else "Market data error" }
            finally { loading = false }
            delay(10_000)
        }
    }
    LaunchedEffect(tickers.size) {
        carouselPage = 0
        if (tickers.size > 4) {
            while (isActive) {
                delay(5_000)
                carouselPage = (carouselPage + 1) % ((tickers.size + 3) / 4)
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawer,
        drawerContent = {
            ModalDrawerSheet {
                Column(Modifier.fillMaxWidth().background(homePanel).padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(if (fa) "منوی اصلی" else "MAIN MENU", color = homeText, fontSize = 22.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(8.dp))
                    MenuItem(if (fa) "🏠 صفحه اصلی" else "🏠 Home") { page = "home"; scope.launch { drawer.close() }; Unit }
                    MenuItem(if (fa) "📊 ترمینال حرفه‌ای" else "📊 Professional Terminal") { page = "terminal"; scope.launch { drawer.close() }; Unit }
                    MenuItem(if (fa) "🤖 وارد کردن استراتژی / ربات" else "🤖 Import Strategy / Bot") { page = "strategy"; scope.launch { drawer.close() }; Unit }
                    MenuItem(if (fa) "🧠 ساخت استراتژی با AI" else "🧠 AI Strategy Builder") { page = "ai-builder"; scope.launch { drawer.close() }; Unit }
                    MenuItem(if (fa) "🪙 ۱۰ ارز منتخب" else "🪙 Top 10 Coins") { page = "coins"; scope.launch { drawer.close() }; Unit }
                    MenuItem(if (fa) "📈 تحلیل بازار AI" else "📈 AI Market Radar") { page = "radar"; scope.launch { drawer.close() }; Unit }
                    MenuItem(if (fa) "🚀 راهنمای اتصال به صرافی" else "🚀 Exchange Deployment Guide") { page = "deployment"; scope.launch { drawer.close() }; Unit }
                    Spacer(Modifier.height(8.dp))
                    Text(if (fa) "زبان" else "LANGUAGE", color = homeMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    MenuItem(if (fa) "English" else "فارسی") { fa = !fa; Unit }
                }
            }
        }
    ) {
        Column(Modifier.fillMaxSize().background(homeBg)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { scope.launch { drawer.open() }; Unit }) { Text("☰  ${if (fa) "منو" else "MENU"}", color = homeCyan, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
                Text("HANNAH", color = homeText, fontSize = 18.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
            }
            when (page) {
                "terminal" -> ProfessionalTerminal()
                "strategy" -> StrategyImportPage(fa = fa) { }
                "ai-builder" -> AiStrategyBuilderPage(fa = fa, onGenerate = { /* backend adapter plugs in here */ }, onUseForBacktest = { }, onSave = { })
                "coins" -> Top10Page(tickers, fa) { page = "terminal" }
                "radar" -> MarketRadarPage(tickers, fa)
                "deployment" -> DeploymentGuidePage(fa)
                else -> HomePage(tickers, loading, error, carouselPage, fa)
            }
        }
    }
}

@Composable private fun MenuItem(text: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) { Text(text, color = homeText, fontSize = 14.sp, modifier = Modifier.fillMaxWidth()) }
}

@Composable private fun DeploymentGuidePage(fa: Boolean) {
    var exchange by remember { mutableStateOf("CoinEx") }
    val steps = com.notash.cryptobacktester.exchange.ExchangeDeploymentGuide.forExchange(exchange)
    LazyColumn(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        item { Text(if (fa) "راهنمای بارگذاری ربات روی صرافی" else "EXCHANGE DEPLOYMENT GUIDE", color = homeCyan, fontSize = 24.sp, fontWeight = FontWeight.Black) }
        item { Text(if (fa) "این بخش فعلاً راهنمای امن و مرحله‌به‌مرحله است؛ کلیدها داخل APK ذخیره نمی‌شوند." else "This is a secure step-by-step guide; credentials are never stored in the APK.", color = homeMuted, fontSize = 10.sp) }
        item { Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf("CoinEx", "Binance", "Bybit", "Bitget").forEach { e -> FilterChip(selected = exchange == e, onClick = { exchange = e }, label = { Text(e) }) } } }
        items(steps) { step -> Card(colors = CardDefaults.cardColors(containerColor = homePanel)) { Column(Modifier.padding(14.dp)) { Text(step.title, color = homeText, fontWeight = FontWeight.Bold); Text(step.details, color = homeMuted, fontSize = 10.sp, modifier = Modifier.padding(top = 5.dp)); step.warning?.let { Text("⚠ $it", color = homeAmber, fontSize = 9.sp, modifier = Modifier.padding(top = 5.dp)) } } } }
    }
}

@Composable private fun HomePage(tickers: List<MarketTicker>, loading: Boolean, error: String?, carouselPage: Int, fa: Boolean) {
    val visible = if (tickers.isEmpty()) emptyList() else { val start = (carouselPage * 4) % tickers.size; tickers.drop(start).take(4).let { if (it.size < 4 && tickers.size >= 4) it + tickers.take(4 - it.size) else it } }
    val pumps = MarketRadar.pumpList(tickers, 5)
    val dumps = MarketRadar.dumpList(tickers, 5)
    val candidates = MarketRadar.rankGrowthCandidates(tickers, 5)
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Text(if (fa) "بازار لحظه‌ای فیوچرز" else "LIVE FUTURES MARKET", color = homeCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold); Text(if (fa) "رصد هوشمند بازار" else "INTELLIGENT MARKET RADAR", color = homeText, fontSize = 26.sp, fontWeight = FontWeight.Black); Text(if (fa) "به‌روزرسانی خودکار هر ۱۰ ثانیه • نمایش ۴ ارز و چرخش خودکار" else "Auto refresh every 10 seconds • 4 coins per view", color = homeMuted, fontSize = 10.sp) }
        item { Card(colors = CardDefaults.cardColors(containerColor = homePanel)) { Column(Modifier.padding(12.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(if (fa) "قیمت لحظه‌ای" else "LIVE PRICES", color = homeText, fontWeight = FontWeight.Bold); Text("${visible.size}/4", color = homeCyan, fontSize = 10.sp) }; if (loading && visible.isEmpty()) Text(if (fa) "در حال دریافت داده…" else "Loading market data…", color = homeMuted, modifier = Modifier.padding(top = 12.dp)); visible.forEach { TickerRow(it) } } } }
        item { MarketMovementCard(if (fa) "🚀 ارزهای پامپ‌شده" else "🚀 PUMPING", pumps, fa, homeGreen) }
        item { MarketMovementCard(if (fa) "🔻 ارزهای دامپ‌شده" else "🔻 DUMPING", dumps, fa, homeRed) }
        item { Card(colors = CardDefaults.cardColors(containerColor = homePanel)) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) { Text(if (fa) "🧠 AI — ارزهای با احتمال رشد بالا" else "🧠 AI — HIGH GROWTH CANDIDATES", color = homeCyan, fontWeight = FontWeight.Black); Text(if (fa) "تمام بازار رصد می‌شود؛ فقط گزینه‌های دارای امتیاز رشد بالا معرفی می‌شوند." else "The full futures universe is scanned; only high-score growth candidates are surfaced.", color = homeMuted, fontSize = 10.sp); candidates.forEach { c -> Text("${c.market}   ${c.score}/100   ${if (c.change24h >= 0) "+" else ""}${"%.2f".format(c.change24h)}%", color = homeGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp); Text(c.reasons.joinToString(" • "), color = homeMuted, fontSize = 9.sp); Text(if (fa) "ریسک: ${c.risk}" else "Risk: ${c.risk}", color = homeAmber, fontSize = 9.sp) }; if (candidates.isEmpty()) Text(if (fa) "فعلاً گزینه‌ای با امتیاز کافی پیدا نشد." else "No high-score candidate right now.", color = homeMuted, fontSize = 10.sp); Text(if (fa) "⚠️ امتیاز قدرت سیگنال است، نه تضمین سود." else "⚠️ Score indicates signal strength, not guaranteed profit.", color = homeAmber, fontSize = 9.sp) } } }
        error?.let { item { Text(it, color = homeRed, fontSize = 10.sp) } }
    }
}

@Composable private fun TickerRow(t: MarketTicker) { Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) { Text(t.market, color = homeText, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); Column(horizontalAlignment = Alignment.End) { Text("${"%.6f".format(t.last)}", color = homeText, fontSize = 11.sp); Text("${if (t.change24h >= 0) "+" else ""}${"%.2f".format(t.change24h)}%", color = if (t.change24h >= 0) homeGreen else homeRed, fontWeight = FontWeight.Bold, fontSize = 10.sp) } } }
@Composable private fun MarketMovementCard(title: String, markets: List<MarketTicker>, fa: Boolean, accent: Color) { Card(colors = CardDefaults.cardColors(containerColor = homePanel)) { Column(Modifier.padding(14.dp)) { Text(title, color = accent, fontWeight = FontWeight.Black); if (markets.isEmpty()) Text(if (fa) "حرکت غیرعادی فعلاً شناسایی نشد." else "No unusual move detected.", color = homeMuted, fontSize = 10.sp); markets.forEach { t -> Text("${t.market}   ${if (t.change24h >= 0) "+" else ""}${"%.2f".format(t.change24h)}%", color = accent, fontSize = 11.sp, modifier = Modifier.padding(top = 6.dp)) } } } }
@Composable private fun MarketRadarPage(tickers: List<MarketTicker>, fa: Boolean) { val candidates = MarketRadar.rankGrowthCandidates(tickers, 10); LazyColumn(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) { item { Text(if (fa) "تحلیل هوش مصنوعی بازار" else "AI MARKET RADAR", color = homeCyan, fontSize = 24.sp, fontWeight = FontWeight.Black); Text(if (fa) "رصد کل بازار، سپس معرفی فقط گزینه‌های با امتیاز رشد بالا" else "Scan the market universe, then surface only high-score growth candidates.", color = homeMuted, fontSize = 10.sp) }; items(candidates) { c -> Card(colors = CardDefaults.cardColors(containerColor = homePanel)) { Column(Modifier.padding(14.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(c.market, color = homeText, fontWeight = FontWeight.Black); Text("${c.score}/100", color = if (c.score >= 75) homeGreen else homeAmber, fontWeight = FontWeight.Bold) }; Text(if (fa) "تغییر ۲۴ساعته: ${"%.2f".format(c.change24h)}%" else "24h change: ${"%.2f".format(c.change24h)}%", color = homeMuted, fontSize = 10.sp); Text(c.reasons.joinToString(" • "), color = homeCyan, fontSize = 10.sp, modifier = Modifier.padding(top = 5.dp)); Text(if (fa) "ریسک: ${c.risk}" else "Risk: ${c.risk}", color = homeAmber, fontSize = 9.sp, modifier = Modifier.padding(top = 4.dp)) } } } } }
@Composable private fun Top10Page(tickers: List<MarketTicker>, fa: Boolean, select: (String) -> Unit) { val markets = tickers.take(10); LazyColumn(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { item { Text(if (fa) "۱۰ ارز منتخب" else "TOP 10 FUTURES", color = homeCyan, fontSize = 23.sp, fontWeight = FontWeight.Black) }; items(markets) { t -> Button(onClick = { select(t.market) }, modifier = Modifier.fillMaxWidth()) { Text("${t.market}   ${"%.2f".format(t.change24h)}%") } } } }
