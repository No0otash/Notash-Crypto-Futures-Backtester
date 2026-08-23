package com.notash.cryptobacktester.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
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
    Card { Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("BTC/USDT-PERP  112,380", style = MaterialTheme.typography.titleLarge)
        Text("+2.38%   🟢 ${if (fa) "روند صعودی" else "Bullish trend"}")
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf("1m","5m","15m","1H","4H","1D").forEach { OutlinedButton({}) { Text(it) } } }
        CandleChart()
        Text(if (fa) "EMA20 / EMA50 • حجم معاملات • ATR14" else "EMA20 / EMA50 • Volume • ATR14")
    } }
    Card { Column(Modifier.padding(12.dp)) { Text(if (fa) "خلاصه بازار" else "Market Summary"); Text(if (fa) "مومنتوم مثبت است؛ تأیید روند در تایم‌فریم بالاتر توصیه می‌شود." else "Positive momentum; higher-timeframe confirmation is recommended.") } }
}

@Composable private fun CandleChart() { Card { Canvas(Modifier.fillMaxWidth().height(220.dp).padding(8.dp)) { val base=size.height*.7f; val step=max(size.width/16f,1f); for(i in 0..15){ val x=i*step+step/2; val high=base-(i%5)*9; val low=high+42; val open=high+12; val close=if(i%3==0)high+34 else high+6; drawLine(androidx.compose.ui.graphics.Color.Gray,x,high,x,low,3f); val top=minOf(open,close); val bottom=maxOf(open,close); drawLine(androidx.compose.ui.graphics.Color.White,x,top,x,bottom,10f) } } } }

@Composable private fun FuturesPage(fa: Boolean) = Page(if (fa) "فیوچرز" else "Futures") {
    var amount by remember { mutableStateOf("100") }; var leverage by remember { mutableStateOf("3") }
    OutlinedTextField(amount, { amount=it.filter(Char::isDigit) }, label={Text(if(fa) "مبلغ معامله (USDT)" else "Trade Amount (USDT)")}, modifier=Modifier.fillMaxWidth())
    OutlinedTextField(leverage, { leverage=it.filter(Char::isDigit) }, label={Text(if(fa) "اهرم" else "Leverage")}, modifier=Modifier.fillMaxWidth())
    Text("${if(fa) "ارزش پوزیشن" else "Position Value"}: ${((amount.toDoubleOrNull()?:0.0)*(leverage.toDoubleOrNull()?:1.0))} USDT")
    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) { Button({}) { Text("LONG") }; OutlinedButton({}) { Text("SHORT") } }
    Card { Column(Modifier.padding(12.dp)) { Text(if(fa) "جزئیات پوزیشن" else "Position Details"); listOf("Entry / قیمت ورود","SL / حد ضرر","TP / حد سود","Margin / مارجین","Fee / کارمزد","Funding / فاندینگ","Liquidation / لیکوئیدیشن","PNL / سود و زیان","ROI").forEach { Text(it) } } }
}

@Composable private fun BacktestPage(fa:Boolean)=Page(if(fa)"بک‌تست" else "Backtest") { Text(if(fa)"تنظیمات بک‌تست و اجرای استراتژی در این بخش قرار می‌گیرد." else "Backtest configuration and strategy execution.") }
@Composable private fun AiPage(fa:Boolean)=Page(if(fa)"تحلیل هوشمند" else "AI Analyst") { Text(if(fa)"تحلیل علت سود و زیان معاملات، الگوهای موفقیت/شکست و پیشنهاد بهینه‌سازی Strategy." else "Trade diagnosis, success/failure patterns and strategy optimization.") }
@Composable private fun TradesPage(fa:Boolean)=Page(if(fa)"معاملات" else "Trades") { Text(if(fa)"لیست معاملات و جزئیات هر معامله." else "Trade list and detailed positions.") }
@Composable private fun SettingsPage(fa:Boolean,onLang:(Boolean)->Unit)=Page(if(fa)"تنظیمات" else "Settings") { Row { Text(if(fa)"زبان فارسی" else "Persian language"); Spacer(Modifier.weight(1f)); Switch(fa,onLang) } }
