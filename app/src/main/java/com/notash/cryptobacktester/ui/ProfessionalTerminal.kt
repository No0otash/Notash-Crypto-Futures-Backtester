package com.notash.cryptobacktester.ui

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import com.notash.cryptobacktester.ai.TradeAiAnalyzer
import com.notash.cryptobacktester.core.BacktestReport
import com.notash.cryptobacktester.core.Candle
import com.notash.cryptobacktester.core.MarketTicker
import com.notash.cryptobacktester.core.Side
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private object H {
    val bg = Color(0xFF05070D); val panel = Color(0xFF0B1020); val panel2 = Color(0xFF121A2D)
    val purple = Color(0xFF8B5CF6); val cyan = Color(0xFF22D3EE); val green = Color(0xFF20E6A5)
    val red = Color(0xFFFF5577); val amber = Color(0xFFFFB84D); val text = Color(0xFFF5F7FF); val muted = Color(0xFF8490A7)
}

private data class Page(val id: Int, val en: String, val fa: String)
private val pages = listOf(
    Page(0, "Overview", "نمای کلی"), Page(1, "Chart", "نمودار"), Page(2, "Backtest", "بک‌تست"),
    Page(3, "Analysis", "تحلیل"), Page(4, "Trades", "معاملات"), Page(5, "Top 10", "۱۰ ارز برتر"), Page(6, "Settings", "تنظیمات")
)

@Composable
fun ProfessionalTerminal() {
    val vm: BacktestViewModel = viewModel(); val state by vm.state.collectAsState()
    var page by remember { mutableIntStateOf(0) }; var languageFa by remember { mutableStateOf(false) }
    var market by remember { mutableStateOf(state.market) }; var tf by remember { mutableStateOf(state.timeframe) }
    CompositionLocalProvider(LocalLayoutDirection provides if (languageFa) LayoutDirection.Rtl else LayoutDirection.Ltr) {
        MaterialTheme(colorScheme = androidx.compose.material3.darkColorScheme(primary = H.purple, secondary = H.cyan, background = H.bg, surface = H.panel)) {
            Scaffold(containerColor = H.bg, topBar = { TopMenu(page, languageFa, { id -> page = id }, { languageFa = it }) }) { pad ->
                Column(Modifier.fillMaxSize().padding(pad)) {
                    AnimatedContent(targetState = page, label = "page") { selected ->
                        when (selected) {
                            0 -> Overview(state.report, state.market, languageFa)
                            1 -> ChartPage(state.report, languageFa)
                            2 -> BacktestPage(state, vm, market, tf, languageFa, { market = it }, { tf = it })
                            3 -> AnalysisPage(state.report, languageFa)
                            4 -> TradesPage(state.report, languageFa)
                            5 -> MarketsPage(state, vm, languageFa) { market = it; page = 2 }
                            else -> SettingsPage(languageFa)
                        }
                    }
                }
            }
        }
    }
}

@Composable private fun TopMenu(selected: Int, fa: Boolean, go: (Int) -> Unit, setLanguageFa: (Boolean) -> Unit) {
    var menu by remember { mutableStateOf(false) }; var lang by remember { mutableStateOf(false) }
    Surface(color = H.panel) {
        Column {
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("☰", color = H.cyan, fontSize = 22.sp, modifier = Modifier.padding(4.dp))
                Spacer(Modifier.size(8.dp)); Column(Modifier.weight(1f)) { Text("HANNAH", color = H.text, fontSize = 20.sp, fontWeight = FontWeight.Black); Text("FUTURES INTELLIGENCE", color = H.cyan, fontSize = 8.sp, fontWeight = FontWeight.Bold) }
                TextButton(onClick = { lang = true }) { Text(if (fa) "EN" else "FA", color = H.cyan, fontWeight = FontWeight.Bold) }
                TextButton(onClick = { menu = true }) { Text(if (fa) "منو" else "MENU", color = H.text, fontWeight = FontWeight.Bold) }
                DropdownMenu(expanded = lang, onDismissRequest = { lang = false }) {
                    DropdownMenuItem(text = { Text("English") }, onClick = { setLanguageFa(false); lang = false })
                    DropdownMenuItem(text = { Text("فارسی") }, onClick = { setLanguageFa(true); lang = false })
                }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    pages.forEach { p -> DropdownMenuItem(text = { Text(if (fa) p.fa else p.en) }, onClick = { go(p.id); menu = false }) }
                }
            }
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                pages.take(5).forEach { p -> TextButton(onClick = { go(p.id) }) { Text(if (fa) p.fa else p.en, color = if (selected == p.id) H.cyan else H.muted, fontSize = 9.sp, fontWeight = FontWeight.Bold) } }
            }
        }
    }
}

@Composable private fun Overview(r: BacktestReport?, market: String, fa: Boolean) {
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { HeaderText(if (fa) "نمای کلی بازار" else "MARKET OVERVIEW", market, if (fa) "فیوچرز CoinEx • موتور کریپتویی" else "CoinEx Futures • Crypto Native") }
        item { Metrics(r, fa) }
        item { CardBox { Title(if (fa) "منحنی سرمایه" else "EQUITY CURVE"); Spacer(Modifier.height(8.dp)); if ((r?.equityCurve?.size ?: 0) > 1) EquityChart(r!!.equityCurve) else Empty(if (fa) "برای ساخت نمودار بک‌تست را اجرا کنید" else "Run a backtest to generate the curve") } }
        item { CardBox { Title(if (fa) "هسته استراتژی" else "STRATEGY CORE"); Line(if (fa) "روند" else "TREND", "HTF LWMA 20 / 50"); Line(if (fa) "ورود" else "ENTRY", "LTF LWMA + 0.5 ATR"); Line(if (fa) "ریسک" else "RISK", "1.0% / trade"); Line(if (fa) "خروج" else "EXIT", "SL 1.5 ATR • TP 3 ATR"); Line(if (fa) "هزینه" else "COSTS", "Maker + Taker + Slippage"); Line(if (fa) "فاندینگ" else "FUNDING", if (fa) "فعال" else "Historical enabled") } }
    }
}

@Composable private fun ChartPage(r: BacktestReport?, fa: Boolean) {
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { HeaderText(if (fa) "نمودار حرفه‌ای" else "PRO CHART", "OHLC • ${r?.candles?.size ?: 0} candles", if (fa) "کندل‌های واقعی + نقاط ورود و خروج" else "Real candles + Long/Short entry and exit markers") }
        item { CardBox { if (r?.candles?.isNotEmpty() == true) CandlestickChart(r.candles, r.trades, fa) else Empty(if (fa) "بعد از اجرای بک‌تست کندل‌ها نمایش داده می‌شوند" else "Run a backtest to load real candles", 320) } }
        item { Legend(fa) }
        item { CardBox { Title(if (fa) "نحوه خواندن" else "READ THE CHART"); Text(if (fa) "سبز=صعود، قرمز=نزول. دایره سبز محل باز شدن لانگ و دایره قرمز محل باز شدن شورت است. خروج با دایره توخالی و خط اتصال Entry → Exit مشخص می‌شود." else "Green candles are bullish and red candles bearish. Green/red markers show Long/Short entries; hollow markers show exits and a dashed line connects Entry → Exit.", color = H.muted, fontSize = 11.sp) } }
    }
}

@Composable private fun CandlestickChart(candles: List<Candle>, trades: List<com.notash.cryptobacktester.core.TradeResult>, fa: Boolean) {
    var visible by remember(candles.size) { mutableIntStateOf(min(80, candles.size)) }; var start by remember(candles.size) { mutableIntStateOf(max(0, candles.size - visible)) }
    val shown = candles.subList(start.coerceIn(0, max(0, candles.size - 1)), min(candles.size, start + visible.coerceAtLeast(5)))
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(if (fa) "کندل‌ها" else "CANDLES", color = H.muted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { TextButton(onClick = { visible = min(candles.size, visible + 15); start = max(0, candles.size - visible) }) { Text("+", color = H.cyan) }; TextButton(onClick = { visible = max(25, visible - 15); start = max(0, candles.size - visible) }) { Text("−", color = H.cyan) } }
        }
        Box(Modifier.fillMaxWidth().height(320.dp).clip(RoundedCornerShape(12.dp)).background(H.panel2).pointerInput(candles.size, visible) {
            detectDragGestures { _, drag -> val step = (-drag.x / 12f).toInt(); start = (start + step).coerceIn(0, max(0, candles.size - visible)) }
        }) {
            Canvas(Modifier.fillMaxSize().padding(8.dp)) {
                if (shown.size < 2) return@Canvas
                val lo = shown.minOf { it.low }; val hi = shown.maxOf { it.high }; val range = (hi - lo).takeIf { it > 0 } ?: 1.0
                val gap = size.width / shown.size; val body = max(2f, gap * .55f)
                fun y(price: Double) = size.height - ((price - lo) / range).toFloat() * size.height
                shown.forEachIndexed { i, c ->
                    val x = i * gap + gap / 2; val up = c.close >= c.open; val color = if (up) H.green else H.red
                    drawLine(color, Offset(x, y(c.high)), Offset(x, y(c.low)), strokeWidth = 1.5f)
                    val top = y(max(c.open, c.close)); val bottom = y(min(c.open, c.close)); drawRect(color, Offset(x - body / 2, top), Size(body, max(2f, bottom - top)))
                }
                trades.forEach { t ->
                    val ei = nearestIndex(shown, t.entryTime); val xi = nearestIndex(shown, t.exitTime)
                    if (ei >= 0) {
                        val ex = ei * gap + gap / 2; val ey = y(t.entryPrice); val ec = if (t.side == Side.LONG) H.green else H.red
                        drawCircle(ec, 6f, Offset(ex, ey)); drawCircle(H.panel2, 3f, Offset(ex, ey))
                        if (xi >= 0) { val xx = xi * gap + gap / 2; val xy = y(t.exitPrice); drawLine(ec.copy(alpha = .65f), Offset(ex, ey), Offset(xx, xy), 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))); drawCircle(ec, 7f, Offset(xx, xy), style = Stroke(2f)) }
                    }
                }
            }
        }
        Text(if (fa) "برای جابه‌جایی نمودار بکشید • برای زوم +/−" else "Drag to pan • use +/− to zoom", color = H.muted, fontSize = 9.sp, modifier = Modifier.padding(top = 5.dp))
    }
}

private fun nearestIndex(c: List<Candle>, time: Long): Int { if (c.isEmpty()) return -1; var best = 0; var d = Long.MAX_VALUE; c.forEachIndexed { i, x -> val n = abs(x.timestamp - time); if (n < d) { d = n; best = i } }; return best }

@Composable private fun BacktestPage(s: BacktestUiState, vm: BacktestViewModel, market: String, tf: String, fa: Boolean, setMarket: (String) -> Unit, setTf: (String) -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { HeaderText(if (fa) "آزمایش بک‌تست" else "BACKTEST LAB", market, if (fa) "داده تاریخی واقعی CoinEx" else "Real CoinEx historical data") }
        item { CardBox { Title(if (fa) "بازار" else "MARKET"); OutlinedTextField(market, setMarket, Modifier.fillMaxWidth(), singleLine = true, label = { Text(if (fa) "نماد مثل BTCUSDT" else "Symbol e.g. BTCUSDT") }); Spacer(Modifier.height(7.dp)); Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(5.dp)) { listOf("5m","15m","30m","1h","4h","1d").forEach { x -> FilterChip(tf == x, { setTf(x); vm.setTimeframe(x) }, label = { Text(x) }) } } } }
        item { CardBox { Title(if (fa) "پارامترهای موتور" else "ENGINE PARAMETERS"); Line(if (fa) "سرمایه اولیه" else "INITIAL", "1,000 USDT"); Line(if (fa) "ریسک معامله" else "RISK", "1.00%"); Line(if (fa) "اهرم" else "LEVERAGE", "3x"); Line("LWMA", "20 / 50"); Line("ATR", "14"); Line(if (fa) "حد ضرر / سود" else "SL / TP", "1.5 / 3.0 ATR"); Line("FUNDING", "ON") } }
        item { Button(onClick = { vm.setMarket(market); vm.runBacktest() }, enabled = !s.isRunning, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = H.purple)) { if (s.isRunning) CircularProgressIndicator(Modifier.size(22.dp), color = H.text, strokeWidth = 2.dp) else Text(if (fa) "اجرای بک‌تست" else "RUN BACKTEST", fontWeight = FontWeight.Black) } }
        s.error?.let { e -> item { CardBox { Title(if (fa) "خطای موتور" else "ENGINE ERROR"); Text(e, color = H.red, fontSize = 11.sp) } } }
        s.report?.let { r -> item { Result(r, fa) } }
    }
}

@Composable private fun AnalysisPage(r: BacktestReport?, fa: Boolean) {
    val context = LocalContext.current
    val analysis = r?.let { TradeAiAnalyzer.analyze(it) }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { HeaderText(if (fa) "تحلیل هوشمند استراتژی" else "AI STRATEGY ANALYSIS", "Bot Diagnosis", if (fa) "تشخیص علت معاملات و پیشنهاد اصلاح ربات" else "Trade causes, bot weaknesses and improvement plan") }
        if (analysis == null) item { CardBox { Text(if (fa) "ابتدا یک بک‌تست اجرا کنید تا تحلیل تولید شود." else "Run a backtest first to generate analysis.", color = H.muted, fontSize = 12.sp) } }
        analysis?.let { a ->
            item { CardBox { Title(if (fa) "امتیاز سلامت ربات" else "BOT HEALTH SCORE"); Text("${a.strategy.healthScore}/100", color = if (a.strategy.healthScore >= 70) H.green else H.amber, fontSize = 30.sp, fontWeight = FontWeight.Black); Spacer(Modifier.height(6.dp)); Text(a.strategy.summary, color = H.text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) } }
            item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) { Button(onClick = { r?.let { TradeAnalysisExporterBridge.shareCsv(context, it, a) } }, modifier = Modifier.weight(1f)) { Text(if (fa) "خروجی CSV" else "EXPORT CSV") }; Button(onClick = { r?.let { TradeAnalysisExporterBridge.shareJson(context, it, a) } }, modifier = Modifier.weight(1f)) { Text(if (fa) "خروجی JSON" else "EXPORT JSON") } } }
            item { AnalysisList(if (fa) "نقاط قوت" else "STRENGTHS", a.strategy.strengths, H.green) }
            item { AnalysisList(if (fa) "مشکلات ربات" else "BOT WEAKNESSES", a.strategy.weaknesses, H.red) }
            item { AnalysisList(if (fa) "پیشنهادهای قابل تست" else "TESTABLE IMPROVEMENTS", a.strategy.recommendations, H.cyan) }
            item { CardBox { Title(if (fa) "تحلیل تک‌تک معاملات" else "TRADE-BY-TRADE DIAGNOSIS"); a.trades.forEach { d ->
                val color = if (d.outcome == "سود") H.green else H.red
                Text("#${d.index} • ${d.outcome} • ${d.severity}", color = color, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(top = 9.dp))
                Text(if (fa) "علت: ${d.primaryCause}" else "Cause: ${d.primaryCause}", color = H.text, fontSize = 10.sp, modifier = Modifier.padding(top = 3.dp))
                d.evidence.forEach { e -> Text("• $e", color = H.muted, fontSize = 9.sp) }
                Text(if (fa) "بهبود: ${d.recommendation}" else "Improve: ${d.recommendation}", color = H.cyan, fontSize = 10.sp, modifier = Modifier.padding(top = 3.dp))
            } } }
        }
    }
}

@Composable private fun AnalysisList(title: String, items: List<String>, color: Color) { CardBox { Title(title); if (items.isEmpty()) Text("—", color = H.muted); items.forEach { Text("• $it", color = color, fontSize = 11.sp, modifier = Modifier.padding(top = 6.dp)) } } }

@Composable private fun TradesPage(r: BacktestReport?, fa: Boolean) { val trades = r?.trades.orEmpty().reversed(); LazyColumn(Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { item { HeaderText(if (fa) "معاملات" else "TRADE TAPE", "${trades.size}", if (fa) "تمام پوزیشن‌های اجراشده" else "All executed positions") }; if (trades.isEmpty()) item { CardBox { Text(if (fa) "هنوز معامله‌ای وجود ندارد." else "No trades yet. Run a backtest first.", color = H.muted) } }; items(trades) { t -> CardBox { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(if (t.side == Side.LONG) "LONG" else "SHORT", color = if (t.side == Side.LONG) H.green else H.red, fontWeight = FontWeight.Black); Text(money(t.netPnl), color = if (t.netPnl >= 0) H.green else H.red, fontWeight = FontWeight.Bold) }; Text("${price(t.entryPrice)} → ${price(t.exitPrice)}", color = H.text, fontSize = 13.sp); Text("Qty ${price(t.quantity)} • Fee ${money(t.fees)} • Funding ${money(t.funding)}", color = H.muted, fontSize = 10.sp) } } } }

@Composable private fun MarketsPage(s: BacktestUiState, vm: BacktestViewModel, fa: Boolean, select: (String) -> Unit) { LazyColumn(Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { item { HeaderText(if (fa) "۱۰ ارز برتر" else "TOP 10 FUTURES", "CoinEx", if (fa) "بر اساس ارزش معاملات ۲۴ ساعته" else "Ranked by 24h futures value") }; item { Button(onClick = { vm.loadTopMarkets() }, modifier = Modifier.fillMaxWidth(), enabled = !s.isLoadingMarkets) { Text(if (s.isLoadingMarkets) (if (fa) "در حال دریافت…" else "Loading…") else (if (fa) "به‌روزرسانی" else "REFRESH TOP 10")) } }; if (s.topMarkets.isEmpty() && !s.isLoadingMarkets) item { CardBox { Text(if (fa) "داده بازار دریافت نشد. اینترنت و API را بررسی کنید." else "No market data returned. Check network/API response.", color = H.muted) } }; items(s.topMarkets) { ticker -> MarketCard(ticker, fa) { select(ticker.market) } } } }

@Composable private fun MarketCard(t: MarketTicker, fa: Boolean, select: () -> Unit) { Card(colors = CardDefaults.cardColors(containerColor = H.panel), shape = RoundedCornerShape(15.dp), modifier = Modifier.fillMaxWidth()) { Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(t.market, color = H.text, fontWeight = FontWeight.Bold); Text("24h value ${money(t.value24h)}", color = H.muted, fontSize = 9.sp) }; Column(horizontalAlignment = Alignment.End) { Text(price(t.last), color = H.text, fontSize = 12.sp); Text("${if (t.change24h >= 0) "+" else ""}${"%.2f".format(t.change24h)}%", color = if (t.change24h >= 0) H.green else H.red, fontWeight = FontWeight.Bold, fontSize = 11.sp) }; TextButton(onClick = select) { Text(if (fa) "انتخاب" else "SELECT", color = H.cyan, fontSize = 10.sp) } } } }

@Composable private fun SettingsPage(fa: Boolean) { LazyColumn(Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { item { HeaderText(if (fa) "تنظیمات" else "SETTINGS", "HANNAH", if (fa) "ترمینال بک‌تست فیوچرز" else "Futures backtesting terminal") }; item { CardBox { Title(if (fa) "سیستم" else "SYSTEM"); Line(if (fa) "برند" else "BRAND", "HANNAH"); Line(if (fa) "اکسچنج" else "EXCHANGE", "CoinEx Futures"); Line(if (fa) "داده" else "DATA", if (fa) "بدون محدودیت مصنوعی حجم" else "No artificial file-size cap"); Line(if (fa) "زبان" else "LANGUAGE", if (fa) "فارسی / English" else "English / فارسی") } }; item { CardBox { Title(if (fa) "قابلیت‌ها" else "FEATURES"); Text(if (fa) "کندل واقعی • مارکر لانگ/شورت • تحلیل استراتژی • Top 10 • فاندینگ • کارمزد • Drawdown" else "Real OHLC candles • Long/Short markers • Strategy analysis • Top 10 • Funding • Fees • Drawdown", color = H.muted, fontSize = 11.sp) } } } }

@Composable private fun Result(r: BacktestReport, fa: Boolean) { CardBox { Title(if (fa) "نتیجه آخر" else "LATEST RESULT"); Line(if (fa) "سرمایه نهایی" else "FINAL EQUITY", money(r.finalBalance)); Line(if (fa) "سود خالص" else "NET PNL", money(r.netPnl)); Line(if (fa) "نرخ برد" else "WIN RATE", "%.1f%%".format(r.winRatePercent)); Line(if (fa) "افت سرمایه" else "MAX DD", "%.2f%%".format(r.maxDrawdownPercent)); Line("PROFIT FACTOR", if (r.profitFactor.isInfinite()) "∞" else "%.2f".format(r.profitFactor)); Line(if (fa) "کارمزد" else "FEES", money(r.totalFees)); Line(if (fa) "فاندینگ" else "FUNDING", money(r.totalFunding)) } }

@Composable private fun Metrics(r: BacktestReport?, fa: Boolean) { Column(verticalArrangement = Arrangement.spacedBy(7.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) { Stat(if (fa) "سرمایه" else "EQUITY", r?.let { money(it.finalBalance) } ?: "—", 0, Modifier.weight(1f)); Stat(if (fa) "سود خالص" else "NET PNL", r?.let { money(it.netPnl) } ?: "—", if (r != null && r.netPnl < 0) 2 else 1, Modifier.weight(1f)) }; Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) { Stat("ROI", r?.let { "%.2f%%".format(it.roiPercent) } ?: "—", if (r != null && r.roiPercent < 0) 2 else 1, Modifier.weight(1f)); Stat(if (fa) "افت سرمایه" else "MAX DD", r?.let { "%.2f%%".format(it.maxDrawdownPercent) } ?: "—", 2, Modifier.weight(1f)) }; Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) { Stat(if (fa) "نرخ برد" else "WIN RATE", r?.let { "%.1f%%".format(it.winRatePercent) } ?: "—", 1, Modifier.weight(1f)); Stat(if (fa) "معاملات" else "TRADES", r?.trades?.size?.toString() ?: "—", 0, Modifier.weight(1f)) } } }

@Composable private fun Stat(title: String, value: String, tone: Int, modifier: Modifier) { val c = when (tone) { 1 -> H.green; 2 -> H.red; else -> H.text }; Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = H.panel), shape = RoundedCornerShape(15.dp)) { Column(Modifier.padding(12.dp)) { Text(title, color = H.muted, fontSize = 8.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(3.dp)); Text(value, color = c, fontSize = 17.sp, fontWeight = FontWeight.Bold) } } }

@Composable private fun EquityChart(values: List<Double>) { Canvas(Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(12.dp)).background(H.panel2).padding(7.dp)) { if (values.size < 2) return@Canvas; val minV = values.minOrNull() ?: return@Canvas; val maxV = values.maxOrNull() ?: return@Canvas; val range = (maxV - minV).takeIf { it > 0 } ?: 1.0; val p = androidx.compose.ui.graphics.Path(); values.forEachIndexed { i, v -> val x = i.toFloat() / (values.lastIndex.coerceAtLeast(1)) * size.width; val y = size.height - ((v - minV) / range).toFloat() * size.height; if (i == 0) p.moveTo(x, y) else p.lineTo(x, y) }; drawPath(p, H.cyan, style = Stroke(3f, cap = StrokeCap.Round)) } }

@Composable private fun Legend(fa: Boolean) { Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.SpaceEvenly) { Text("● LONG", color = H.green, fontSize = 10.sp); Text("● SHORT", color = H.red, fontSize = 10.sp); Text("○ EXIT", color = H.text, fontSize = 10.sp); Text(if (fa) "قرمز/سبز = کندل" else "RED/GREEN = CANDLE", color = H.muted, fontSize = 9.sp) } }
@Composable private fun CardBox(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) { Card(colors = CardDefaults.cardColors(containerColor = H.panel), shape = RoundedCornerShape(17.dp)) { Column(Modifier.fillMaxWidth().padding(14.dp), content = content) } }
@Composable private fun Title(t: String) { Text(t, color = H.muted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp) }
@Composable private fun Line(a: String, b: String) { Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(a, color = H.muted, fontSize = 10.sp); Text(b, color = H.text, fontSize = 10.sp, fontWeight = FontWeight.SemiBold) } }
@Composable private fun HeaderText(title: String, main: String, sub: String) { Column(Modifier.padding(vertical = 5.dp)) { Text(title, color = H.cyan, fontSize = 9.sp, fontWeight = FontWeight.Bold); Text(main, color = H.text, fontSize = 25.sp, fontWeight = FontWeight.Black); Text(sub, color = H.muted, fontSize = 10.sp) } }
@Composable private fun Empty(text: String, height: Int = 180) { Box(Modifier.fillMaxWidth().height(height.dp).clip(RoundedCornerShape(12.dp)).background(H.panel2), contentAlignment = Alignment.Center) { Text(text, color = H.muted, fontSize = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(20.dp)) } }
private fun money(v: Double) = if (v.isFinite()) "%,.2f".format(v) else "∞"
private fun price(v: Double) = if (v.isFinite()) "%,.4f".format(v) else "∞"

private object TradeAnalysisExporterBridge {
    fun shareCsv(context: Context, report: BacktestReport, analysis: TradeAiAnalyzer.Analysis) = com.notash.cryptobacktester.ui.AnalysisExport.shareCsv(context, report, analysis)
    fun shareJson(context: Context, report: BacktestReport, analysis: TradeAiAnalyzer.Analysis) = com.notash.cryptobacktester.ui.AnalysisExport.shareJson(context, report, analysis)
}
