package com.notash.cryptobacktester.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
            try {
                val fresh = repository.loadAllFuturesMarkets()
                tickers = fresh
                error = null
                loading = false
            } catch (e: Exception) {
                error = e.message ?: "خطا در دریافت بازار"
                loading = false
            }
            delay(10_000)
        }
    }

    LaunchedEffect(tickers.size) {
        while (tickers.size > 4) {
            delay(5_000)
            carouselPage = (carouselPage + 1) % ((tickers.size + 3) / 4)
        }
    }

    val openMenu: () -> Unit = { scope.launch { drawer.open() } }
    MaterialTheme(colorScheme = androidx.compose.material3.darkColorScheme(background = homeBg, surface = homePanel, primary = homeCyan)) {
        ModalNavigationDrawer(
            drawerState = drawer,
            drawerContent = {
                ModalDrawerSheet {
                    Column(Modifier.fillMaxWidth().background(homePanel).padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(if (fa) "منوی اصلی" else "MAIN MENU", color = homeText, fontSize = 22.sp, fontWeight = FontWeight.Black)
                        Spacer(Modifier.height(6.dp))
                        MenuItem(if (fa) "🏠 صفحه اصلی" else "🏠 Home") { page = "home"; scope.launch { drawer.close() } }
                        MenuItem(if (fa) "📊 ترمینال حرفه‌ای" else "📊 Professional Terminal") { page = "terminal"; scope.launch { drawer.close() } }
                        MenuItem(if (fa) "🤖 وارد کردن استراتژی / ربات" else "🤖 Import Strategy / Bot") { page = "strategy"; scope.launch { drawer.close() } }
                        MenuItem(if (fa) "🪙 ۱۰ ارز منتخب" else "🪙 Top 10 Coins") { page = "coins"; scope.launch { drawer.close() } }
                        MenuItem(if (fa) "📈 تحلیل بازار AI" else "📈 AI Market Radar") { page = "radar"; scope.launch { drawer.close() } }
                        Spacer(Modifier.height(8.dp))
                        Text(if (fa) "زبان" else "LANGUAGE", color = homeMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        MenuItem(if (fa) "English" else "فارسی") { fa = !fa }
                    }
                }
            }
        ) {
            Column(Modifier.fillMaxSize().background(homeBg)) {
                if (page != "terminal") {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = openMenu) { Text("☰  ${if (fa) "منو" else "MENU"}", color = homeCyan, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
                        Text("HANNAH", color = homeText, fontSize = 18.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                    }
                }
                when (page) {
                    "terminal" -> ProfessionalTerminal()
                    "strategy" -> StrategyImportPage(fa = fa) { }
                    "coins" -> Top10Page(tickers, fa) { page = "terminal" }
                    "radar" -> MarketRadarPage(tickers, fa)
                    else -> HomePage(tickers, loading, error, carouselPage, fa) { openMenu() }
                }
            }
        }
    }
}

@Composable
private fun MenuItem(text: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text(text, color = homeText, fontSize = 14.sp) }
    }
}

@Composable
private fun HomePage(tickers: List<MarketTicker>, loading: Boolean, error: String?, carouselPage: Int, fa: Boolean, openMenu: () -> Unit) {
    val visible = if (tickers.isEmpty()) emptyList() else tickers.drop((carouselPage * 4) % tickers.size).take(4).let { first -> if (first.size < 4 && tickers.size >= 4) (first + tickers.take(4 - first.size)) else first }
    val pumps = MarketRadar.pumpList(tickers, 5)
    val dumps = MarketRadar.dumpList(tickers, 5)
    val candidates = MarketRadar.rankGrowthCandidates(tickers, 5)
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text(if (fa) "بازار لحظه‌ای فیوچرز" else "LIVE FUTURES MARKET", color = homeCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(if (fa) "رصد هوشمند بازار" else "INTELLIGENT MARKET RADAR", color = homeText, fontSize = 26.sp, fontWeight = FontWeight.Black)
            Text(if (fa) "به‌روزرسانی خودکار هر ۱۰ ثانیه" else "Auto refresh every 10 seconds", color = homeMuted, fontSize = 10.sp)
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = homePanel)) {
                Column(Modifier.padding(12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (fa) "قیمت لحظه‌ای" else "LIVE PRICES", color = homeText, fontWeight = FontWeight.Bold)
                        Text(if (fa) "۴ ارز" else "4 COINS", color = homeCyan, fontSize = 10.sp)
                    }
                    if (loading && visible.isEmpty()) Text(if (fa) "در حال دریافت داده…" else "Loading market data…", color = homeMuted, modifier = Modifier.padding(top = 12.dp))
                    visible.forEach { ticker -> TickerRow(ticker, fa) }
                }
            }
        }
        item { MarketMovementCard(if (fa) "🚀 ارزهای پامپ‌شده" else "🚀 PUMPING", pumps, fa, homeGreen) }
        item { MarketMovementCard(if (fa) "🔻 ارزهای دامپ‌شده" else "🔻 DUMPING", dumps, fa, homeRed) }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = homePanel)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text(if (fa) "🧠 AI — ارزهای با احتمال رشد بالا" else "🧠 AI — HIGH GROWTH CANDIDATES", color = homeCyan, fontWeight = FontWeight.Black)
                    Text(if (fa) "AI تمام ارزهای دارای داده را بررسی می‌کند؛ فقط گزینه‌های دارای امتیاز رشد بالا نمایش داده می‌شوند." else "AI scans the available futures universe and shows only high-scoring growth candidates.", color = homeMuted, fontSize = 10.sp)
                    candidates.forEach { c ->
                        Text("${c.market}   ${c.score}/100   +${"%.2f".format(c.change24h)}%", color = homeGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(c.reasons.joinToString(" • "), color = homeMuted, fontSize = 9.sp)
                    }
                    if (candidates.isEmpty()) Text(if (fa) "فعلاً گزینه‌ای با امتیاز کافی پیدا نشد." else "No high-score candidate right now.", color = homeMuted, fontSize = 10.sp)
                    Text(if (fa) "⚠️ این امتیاز احتمال/قدرت سیگنال است، نه تضمین رشد." else "⚠️ Score indicates signal strength, not a guarantee of profit.", color = homeAmber, fontSize = 9.sp)
                }
            }
        }
        error?.let { message -> item { Text(message, color = homeRed, fontSize = 10.sp) } }
    }
}

@Composable
private fun TickerRow(t: MarketTicker, fa: Boolean) {
    Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(t.market, color = homeText, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End) {
            Text("${"%.6f".format(t.last)}", color = homeText, fontSize = 11.sp)
            Text("${if (t.change24h >= 0) "+" else ""}${"%.2f".format(t.change24h)}%", color = if (t.change24h >= 0) homeGreen else homeRed, fontWeight = FontWeight.Bold, fontSize = 10.sp)
        }
    }
}

@Composable
private fun MarketMovementCard(title: String, markets: List<MarketTicker>, fa: Boolean, accent: Color) {
    Card(colors = CardDefaults.cardColors(containerColor = homePanel)) {
        Column(Modifier.padding(14.dp)) {
            Text(title, color = accent, fontWeight = FontWeight.Black)
            if (markets.isEmpty()) Text(if (fa) "حرکت غیرعادی فعلاً شناسایی نشد." else "No unusual move detected.", color = homeMuted, fontSize = 10.sp)
            markets.forEach { t -> Text("${t.market}   ${if (t.change24h >= 0) "+" else ""}${"%.2f".format(t.change24h)}%", color = accent, fontSize = 11.sp, modifier = Modifier.padding(top = 6.dp)) }
        }
    }
}

@Composable
private fun MarketRadarPage(tickers: List<MarketTicker>, fa: Boolean) {
    val candidates = MarketRadar.rankGrowthCandidates(tickers, 10)
    LazyColumn(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        item {
            Text(if (fa) "تحلیل هوش مصنوعی بازار" else "AI MARKET RADAR", color = homeCyan, fontSize = 24.sp, fontWeight = FontWeight.Black)
            Text(if (fa) "رصد کل بازار، سپس معرفی فقط گزینه‌های با امتیاز رشد بالا" else "Scan the market universe, then surface only high-score growth candidates.", color = homeMuted, fontSize = 10.sp)
        }
        items(candidates) { c ->
            Card(colors = CardDefaults.cardColors(containerColor = homePanel)) {
                Column(Modifier.padding(14.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(c.market, color = homeText, fontWeight = FontWeight.Black)
                        Text("${c.score}/100", color = if (c.score >= 75) homeGreen else homeAmber, fontWeight = FontWeight.Bold)
                    }
                    Text(if (fa) "تغییر ۲۴ساعته: ${"%.2f".format(c.change24h)}%" else "24h change: ${"%.2f".format(c.change24h)}%", color = homeMuted, fontSize = 10.sp)
                    Text(c.reasons.joinToString(" • "), color = homeCyan, fontSize = 10.sp, modifier = Modifier.padding(top = 5.dp))
                    Text(if (fa) "ریسک: ${c.risk}" else "Risk: ${c.risk}", color = homeAmber, fontSize = 9.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}

@Composable
private fun Top10Page(tickers: List<MarketTicker>, fa: Boolean, select: (String) -> Unit) {
    val markets = tickers.take(10)
    LazyColumn(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Text(if (fa) "۱۰ ارز منتخب" else "TOP 10 FUTURES", color = homeCyan, fontSize = 23.sp, fontWeight = FontWeight.Black) }
        items(markets) { t ->
            Button(onClick = { select(t.market) }, modifier = Modifier.fillMaxWidth()) {
                Text("${t.market}   ${"%.2f".format(t.change24h)}%")
            }
        }
    }
}
