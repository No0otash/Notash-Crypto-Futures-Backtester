package com.notash.cryptobacktester.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.max

@Composable
fun ProfessionalTerminal() {
    var page by remember { mutableStateOf(TerminalPage.MARKET) }
    var persian by remember { mutableStateOf(true) }
    Scaffold(bottomBar = { TerminalNavigation(page, { page = it }, persian) }) { padding ->
        Surface(Modifier.fillMaxSize().padding(padding)) {
            when (page) {
                TerminalPage.MARKET -> MarketPage(persian)
                TerminalPage.FUTURES -> FuturesPage(persian)
                TerminalPage.BACKTEST -> BacktestPage(persian)
                TerminalPage.AI -> AiPage(persian)
                TerminalPage.TRADES -> TradesPage(persian)
                TerminalPage.SETTINGS -> SettingsPage(persian) { persian = it }
            }
        }
    }
}

@Composable private fun Page(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("NOTASH", style = MaterialTheme.typography.titleLarge)
        Text(title, style = MaterialTheme.typography.headlineSmall)
        content()
    }
}

@Composable private fun MarketPage(fa: Boolean) = Page(if (fa) "روند بازار" else "Market Trend") {
    var live by remember { mutableStateOf(true) }
    var tf by remember { mutableStateOf("5m") }
    Card { Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("BTC/USDT-PERP", style = MaterialTheme.typography.titleLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(live, { live = true }, label = { Text("LIVE") })
            FilterChip(!live, { live = false }, label = { Text(if (fa) "تاریخی" else "Historical") })
        }
        Text(if (live) "● ${if (fa) "داده زنده" else "Live data"}" else "● ${if (fa) "داده تاریخی قابل ویرایش" else "Editable historical data"}")
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { listOf("1m","5m","15m","30m","1H","4H","1D").forEach { OutlinedButton({ tf = it }) { Text(it) } } }
        if (!live) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { OutlinedTextField("2026-01-01", {}, label={Text(if(fa)"از تاریخ" else "From")}, modifier=Modifier.weight(1f)); OutlinedTextField("2026-08-23", {}, label={Text(if(fa)"تا تاریخ" else "To")}, modifier=Modifier.weight(1f)) }
            Button({}) { Text(if(fa) "دریافت داده" else "Load data") }
        }
        Text("112,380   +2.38%   🟢 ${if (fa) "صعودی" else "Bullish"}")
        CandleChart()
        Text("EMA20 / EMA50 • Volume • ATR14 • TF $tf")
    } }
}

@Composable private fun CandleChart() { Card { Canvas(Modifier.fillMaxWidth().height(220.dp).padding(8.dp)) { val base=size.height*.7f; val step=max(size.width/18f,1f); for(i in 0..17){ val x=i*step+step/2; val high=base-(i%6)*9; val low=high+42; val open=high+12; val close=if(i%3==0)high+34 else high+6; drawLine(Color.Gray,x,high,x,low,3f); drawLine(Color.White,x,minOf(open,close),x,maxOf(open,close),10f) } } } }

@Composable private fun FuturesPage(fa: Boolean) = Page(if (fa) "فیوچرز" else "Futures") {
    var amount by remember { mutableStateOf("100") }; var leverage by remember { mutableStateOf("3") }
    OutlinedTextField(amount, { amount=it.filter { c -> c.isDigit() || c=='.' } }, label={Text(if(fa) "مبلغ معامله (USDT)" else "Trade Amount (USDT)")}, modifier=Modifier.fillMaxWidth())
    OutlinedTextField(leverage, { leverage=it.filter(Char::isDigit) }, label={Text(if(fa) "اهرم" else "Leverage")}, modifier=Modifier.fillMaxWidth())
    val value=(amount.toDoubleOrNull()?:0.0)*(leverage.toDoubleOrNull()?:1.0)
    Text("${if(fa) "ارزش پوزیشن" else "Position Value"}: ${"%.2f".format(value)} USDT")
    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) { Button({}) { Text("LONG") }; OutlinedButton({}) { Text("SHORT") } }
    Text(if(fa) "قیمت ورود، SL، TP، مارجین، کارمزد، فاندینگ، لیکوئیدیشن و PNL در جزئیات پوزیشن نمایش داده می‌شوند." else "Entry, SL, TP, margin, fees, funding, liquidation and PNL are shown in position details.")
}

@Composable private fun BacktestPage(fa:Boolean)=Page(if(fa)"بک‌تست" else "Backtest") { Text(if(fa)"Historical Backtest با بازه زمانی قابل انتخاب و تنظیمات Strategy." else "Historical backtest with editable date range and strategy settings.") }
@Composable private fun AiPage(fa:Boolean)=Page(if(fa)"تحلیل هوشمند" else "AI Analyst") { Text(if(fa)"تحلیل فارسی علت سود و زیان، الگوهای موفقیت/شکست و پیشنهاد بهینه‌سازی Strategy." else "Trade diagnosis, success/failure patterns and strategy optimization.") }
@Composable private fun TradesPage(fa:Boolean)=Page(if(fa)"معاملات" else "Trades") { Text(if(fa)"معاملات Live Backtest و Historical در این بخش قابل بررسی هستند." else "Live Backtest and Historical trades are reviewed here.") }
@Composable private fun SettingsPage(fa:Boolean,onLang:(Boolean)->Unit)=Page(if(fa)"تنظیمات" else "Settings") { Row { Text(if(fa)"زبان فارسی" else "Persian language"); Spacer(Modifier.weight(1f)); Switch(fa,onLang) } }
