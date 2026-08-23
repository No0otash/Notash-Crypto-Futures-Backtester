package com.notash.cryptobacktester.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import kotlin.math.max

@Composable
fun ProfessionalTerminal() {
    var page by remember { mutableStateOf(TerminalPage.MARKET) }; var persian by remember { mutableStateOf(true) }
    val liveState = remember { com.notash.cryptobacktester.engine.LiveBacktestState() }
    var strategy by remember { mutableStateOf(com.notash.cryptobacktester.engine.StrategyPackage("ema-v1","EMA Trend","1.0","EMA20 > EMA50","EMA20 < EMA50","1% risk")) }
    Scaffold(bottomBar={TerminalNavigation(page,{page=it},persian)}) { padding -> Surface(Modifier.fillMaxSize().padding(padding)){ when(page){
        TerminalPage.MARKET->MarketPage(persian); TerminalPage.FUTURES->FuturesPage(persian); TerminalPage.BACKTEST->BacktestPage(persian); TerminalPage.LIVE_BACKTEST->LiveBacktestPage(persian,liveState,strategy); TerminalPage.STRATEGY->StrategyPage(persian,strategy){strategy=it}; TerminalPage.AI->AiPage(persian); TerminalPage.TRADES->TradesPage(persian); TerminalPage.SETTINGS->SettingsPage(persian){persian=it}
    }}}
}
@Composable private fun Page(title:String,content:@Composable ColumnScope.()->Unit){Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){Text("NOTASH",style=MaterialTheme.typography.titleLarge);Text(title,style=MaterialTheme.typography.headlineSmall);content()}}
@Composable private fun MarketPage(fa:Boolean)=Page(if(fa)"روند بازار" else "Market Trend"){var live by remember{mutableStateOf(true)};var tf by remember{mutableStateOf("5m")};val coins=listOf("BTC/USDT","ETH/USDT","BNB/USDT","SOL/USDT","XRP/USDT","DOGE/USDT","ADA/USDT","AVAX/USDT","LINK/USDT","LTC/USDT");var coin by remember{mutableStateOf(coins.first())};Card{Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Text(coin,style=MaterialTheme.typography.titleLarge);Row{coins.forEach{c->FilterChip(coin==c,{coin=c},label={Text(c.substringBefore('/'))})}};Row{FilterChip(live,{live=true},label={Text("LIVE")});FilterChip(!live,{live=false},label={Text(if(fa)"تاریخی" else "Historical")})};Row{listOf("1m","5m","15m","30m","1H","4H","1D").forEach{OutlinedButton({tf=it}){Text(it)}}};if(!live){Row{OutlinedTextField("2026-01-01",{},label={Text(if(fa)"از تاریخ" else "From")},modifier=Modifier.weight(1f));OutlinedTextField("2026-08-23",{},label={Text(if(fa)"تا تاریخ" else "To")},modifier=Modifier.weight(1f))};Button({}){Text(if(fa)"دریافت داده" else "Load data")}};Text("112,380   +2.38%   🟢 ${if(fa)"صعودی" else "Bullish"}");CandleChart();Text("EMA20 / EMA50 • Volume • ATR14 • TF $tf")}}}
@Composable private fun CandleChart(){Card{Canvas(Modifier.fillMaxWidth().height(220.dp).padding(8.dp)){val base=size.height*.7f;val step=max(size.width/18f,1f);for(i in 0..17){val x=i*step+step/2;val high=base-(i%6)*9;val low=high+42;val open=high+12;val close=if(i%3==0)high+34 else high+6;drawLine(color=Color.Gray,start=Offset(x,high),end=Offset(x,low),strokeWidth=3f);drawLine(color=Color.White,start=Offset(x,minOf(open,close)),end=Offset(x,maxOf(open,close)),strokeWidth=10f)}}}}
@Composable private fun FuturesPage(fa:Boolean)=Page(if(fa)"فیوچرز" else "Futures"){var amount by remember{mutableStateOf("100")};var lev by remember{mutableStateOf("3")};OutlinedTextField(amount,{amount=it.filter{c->c.isDigit()||c=='.'}},label={Text(if(fa)"مبلغ معامله (USDT)" else "Trade Amount")},modifier=Modifier.fillMaxWidth());OutlinedTextField(lev,{lev=it.filter(Char::isDigit)},label={Text(if(fa)"اهرم" else "Leverage")},modifier=Modifier.fillMaxWidth());Text("${if(fa)"ارزش پوزیشن" else "Position Value"}: ${"%.2f".format((amount.toDoubleOrNull()?:0.0)*(lev.toDoubleOrNull()?:1.0))} USDT");Row{Button({}){Text("LONG")};OutlinedButton({}){Text("SHORT")}}}
@Composable private fun BacktestPage(fa:Boolean)=Page(if(fa)"بک‌تست" else "Backtest"){Text(if(fa)"Historical Backtest با بازه قابل انتخاب و Strategy قابل تغییر." else "Historical backtest with editable range and strategy selection.")}
@Composable private fun LiveBacktestPage(fa:Boolean,state:com.notash.cryptobacktester.engine.LiveBacktestState,strategy:com.notash.cryptobacktester.engine.StrategyPackage)=Page(if(fa)"بک‌تست زنده" else "Live Backtest"){val running by state.running.collectAsState();val equity by state.equity.collectAsState();val pnl by state.pnl.collectAsState();val trades by state.trades.collectAsState();val win by state.winRate.collectAsState();Text("Strategy: ${strategy.name} v${strategy.version}");Text(if(running)"🔴 RUNNING" else "⏸ STOPPED");Row{Button({state.start()}){Text(if(fa)"شروع" else "Start")};OutlinedButton({state.stop()}){Text(if(fa)"توقف" else "Stop")};OutlinedButton({state.reset()}){Text(if(fa)"ریست" else "Reset")}};Text("Equity: %.2f USDT".format(equity));Text("PNL: %.2f USDT".format(pnl));Text("Trades: $trades   Win Rate: %.1f%%".format(win))}
@Composable private fun StrategyPage(fa:Boolean,current:com.notash.cryptobacktester.engine.StrategyPackage,onChange:(com.notash.cryptobacktester.engine.StrategyPackage)->Unit)=Page(if(fa)"آزمایشگاه استراتژی" else "Strategy Lab"){Text("${current.name} v${current.version}");Button({onChange(com.notash.cryptobacktester.engine.StrategyPackage("rsi-v1","RSI Reversal","1.0","RSI<30","RSI>70","1% risk"))}){Text(if(fa)"تعویض استراتژی" else "Switch Strategy")};Text(if(fa)"Import: JSON/ZIP → اعتبارسنجی → فعال‌سازی" else "Import: JSON/ZIP → validate → activate")}
@Composable private fun AiPage(fa:Boolean)=Page(if(fa)"تحلیل هوشمند" else "AI Analyst"){Text(if(fa)"تحلیل فارسی علت سود/زیان و پیشنهاد بهینه‌سازی Strategy." else "Trade diagnosis and strategy optimization.")}
@Composable private fun TradesPage(fa:Boolean)=Page(if(fa)"معاملات" else "Trades"){Text(if(fa)"معاملات Live و Historical." else "Live and Historical trades.")}
@Composable private fun SettingsPage(fa:Boolean,onLang:(Boolean)->Unit)=Page(if(fa)"تنظیمات" else "Settings"){Row{Text(if(fa)"زبان فارسی" else "Persian language");Spacer(Modifier.weight(1f));Switch(fa,onLang)}}
