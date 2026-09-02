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

private val VBg = Color(0xFF050A12)
private val VPanel = Color(0xFF0A1320)
private val VPanel2 = Color(0xFF101B2A)
private val VBorder = Color(0xFF1B2B3D)
private val VText = Color(0xFFF5F7FB)
private val VMuted = Color(0xFF7F8DA3)
private val VPurple = Color(0xFF8A45FF)
private val VPurple2 = Color(0xFFB23CFF)
private val VCyan = Color(0xFF26D9FF)
private val VGreen = Color(0xFF21D79B)
private val VRed = Color(0xFFFF536F)
private val VGold = Color(0xFFFFC857)
private data class VQuote(val symbol: String, val price: Double, val change: Double, val volume: Double)

@Composable
fun AlvexPremiumWorkspaceV2(themeMode: AppThemeMode, onThemeMode: (AppThemeMode) -> Unit) {
    val scheme = darkColorScheme(primary = VPurple2, secondary = VCyan, background = VBg, surface = VPanel, onBackground = VText, onSurface = VText)
    var page by rememberSaveable { mutableStateOf(0) }
    var market by rememberSaveable { mutableStateOf("BTCUSDT") }
    var timeframe by rememberSaveable { mutableStateOf("15min") }
    var fa by rememberSaveable { mutableStateOf(true) }
    var settings by rememberSaveable { mutableStateOf(false) }
    var quotes by remember { mutableStateOf<List<VQuote>>(emptyList()) }
    val vm = remember { BacktestViewModel() }
    val state by vm.state.collectAsState()
    val repo = remember { CoinExRepository() }
    val scope = rememberCoroutineScope()
    fun refresh() { scope.launch { quotes = withContext(Dispatchers.IO) { listOf("BTCUSDT","ETHUSDT","BNBUSDT","SOLUSDT","XRPUSDT","DOGEUSDT","ADAUSDT","AVAXUSDT","LINKUSDT","PEPEUSDT").mapNotNull { s -> runCatching { repo.loadLatestTicker(s) }.getOrNull()?.let { VQuote(s,it.last,it.changeRate*100.0,it.volume) } } } } }
    LaunchedEffect(Unit) { refresh() }
    MaterialTheme(colorScheme = scheme) {
        if (settings) { VSettings(fa, themeMode, onThemeMode, { fa = !fa }) { settings = false }; return@MaterialTheme }
        Scaffold(containerColor = VBg, topBar = { VHeader(page, fa, { page = 3 }, { settings = true }, { fa = !fa }) }, bottomBar = { VBottom(page, fa) { page = it } }) { padding ->
            Box(Modifier.fillMaxSize().padding(padding).background(VBg)) {
                when (page) {
                    0 -> VHome(fa, quotes, state.report, { market = it; page = 2 }, ::refresh, { page = it })
                    1 -> VMarkets(fa, quotes, { market = it; vm.setMarket(it) }, { page = 2 }, ::refresh)
                    2 -> VTerminal(fa, market, { market = it; vm.setMarket(it) }, timeframe, { timeframe = it }, vm, state)
                    else -> VIntel(fa, market, repo)
                }
            }
        }
    }
}

@Composable private fun VHeader(page: Int, fa: Boolean, onAi: () -> Unit, onSettings: () -> Unit, onLanguage: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(64.dp).background(VBg).padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onAi) { Icon(Icons.Outlined.Star, null, tint = VCyan) }
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) { Row(verticalAlignment = Alignment.CenterVertically) { VMark(23); Spacer(Modifier.width(6.dp)); Text("ALVEX", color = VText, fontWeight = FontWeight.Black, letterSpacing = 2.sp) }; Text(if (fa) listOf("خانه","بازار","معاملات و بک‌تست","هوش بازار")[page] else listOf("Home","Markets","Trading Terminal","Intelligence")[page], color = VMuted, fontSize = 8.sp) }
        TextButton(onClick = onLanguage) { Text(if (fa) "EN" else "FA", color = VCyan, fontSize = 9.sp) }
        IconButton(onClick = onSettings) { Icon(Icons.Outlined.Settings, null, tint = VText) }
    }
}

@Composable private fun VBottom(page: Int, fa: Boolean, onSelect: (Int) -> Unit) { NavigationBar(containerColor = VPanel, tonalElevation = 0.dp) { listOf(Triple(0,Icons.Outlined.Home,if(fa)"خانه" else "Home"),Triple(1,Icons.Outlined.List,if(fa)"بازار" else "Markets"),Triple(2,Icons.Outlined.Build,if(fa)"بک‌تست" else "Terminal"),Triple(3,Icons.Outlined.Info,if(fa)"هوش" else "Intel")).forEach { (i,icon,label) -> NavigationBarItem(page==i,{onSelect(i)},icon={Icon(icon,label)},label={Text(label,fontSize=8.sp)}) } } }

@Composable private fun VHome(fa:Boolean, quotes:List<VQuote>, report:BacktestReport?, openMarket:(String)->Unit, refresh:()->Unit, open:(Int)->Unit) {
    val btc=quotes.firstOrNull{it.symbol=="BTCUSDT"}; val movers=quotes.sortedByDescending{it.change}.take(4)
    LazyColumn(Modifier.fillMaxSize().padding(horizontal=12.dp),verticalArrangement=Arrangement.spacedBy(11.dp),contentPadding=PaddingValues(top=8.dp,bottom=22.dp)) {
        item { Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(26.dp),colors=CardDefaults.cardColors(containerColor=VPanel)) { Box(Modifier.fillMaxWidth().height(148.dp).background(Brush.linearGradient(listOf(Color(0xFF1B1245),VPanel,Color(0xFF071824))),RoundedCornerShape(26.dp))) { Column(Modifier.padding(17.dp)) { Row(verticalAlignment=Alignment.CenterVertically){Text("MARKET OVERVIEW",color=VMuted,fontSize=8.sp,letterSpacing=1.sp);Spacer(Modifier.weight(1f));VPill("LIVE",VGreen)};Spacer(Modifier.height(7.dp));Text("BTC Dominance",color=VText,fontSize=11.sp);Text("—",color=VText,fontSize=28.sp,fontWeight=FontWeight.Black);Text("Live market feed",color=VMuted,fontSize=9.sp);Spacer(Modifier.height(7.dp));VSpark(movers.map{it.change},Modifier.fillMaxWidth().height(35.dp)) } } } }
        item { Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(7.dp)){VStat("BTC",btc?.let{money(it.price)}?:"—",btc?.let{pct(it.change)}?:"—",if((btc?.change?:0.0)>=0)VGreen else VRed,Modifier.weight(1f));VStat("24H VOL",quotes.firstOrNull()?.let{compact(it.volume)}?:"—","LIVE",VCyan,Modifier.weight(1f));VStat("FEED",if(quotes.isEmpty())"WAIT" else "LIVE","CoinEx",if(quotes.isEmpty())VGold else VGreen,Modifier.weight(1f))} }
        item { VSection("TOP MOVERS",if(fa)"حرکت‌های مهم بازار" else "Live market movers") }
        items(movers){q->VRow(q){openMarket(q.symbol)}}
        item { Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(22.dp),colors=CardDefaults.cardColors(containerColor=VPanel)){Column(Modifier.padding(14.dp)){Row(verticalAlignment=Alignment.CenterVertically){Text("AI MARKET RADAR",color=VText,fontWeight=FontWeight.Black);Spacer(Modifier.weight(1f));Text("VIEW AI",color=VPurple2,fontSize=8.sp)};Spacer(Modifier.height(9.dp));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(7.dp)){VRadar("Pump / Dump","Signal engine",VPurple2,Modifier.weight(1f)){open(3)};VRadar("Whale","Provider layer",VCyan,Modifier.weight(1f)){open(3)};VRadar("Meme","Risk scanner",VGold,Modifier.weight(1f)){open(3)}}}} }
        item { VSection(if(fa)"آخرین بک‌تست" else "LATEST BACKTEST","Real report metrics") }
        item { VReport(report){open(2)} }
        item { Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(7.dp)){VAction(Icons.Outlined.List,"Markets",Modifier.weight(1f)){open(1)};VAction(Icons.Outlined.Build,"Terminal",Modifier.weight(1f)){open(2)};VAction(Icons.Outlined.Star,"AI Hub",Modifier.weight(1f)){open(3)}} }
        item { OutlinedButton(onClick=refresh,Modifier.fillMaxWidth(),shape=RoundedCornerShape(15.dp)){Icon(Icons.Outlined.Refresh,null);Spacer(Modifier.width(5.dp));Text(if(fa)"به‌روزرسانی داده بازار" else "Refresh market data")} }
    }
}

@Composable private fun VMarkets(fa:Boolean,quotes:List<VQuote>,onMarket:(String)->Unit,open:()->Unit,refresh:()->Unit){var query by rememberSaveable{mutableStateOf("")};val list=quotes.filter{query.isBlank()||it.symbol.contains(query.uppercase())};LazyColumn(Modifier.fillMaxSize().padding(12.dp),verticalArrangement=Arrangement.spacedBy(8.dp),contentPadding=PaddingValues(top=8.dp,bottom=20.dp)){item{VSection(if(fa)"بازار" else "Markets","Crypto • Futures • Favorites")};item{OutlinedTextField(query,{query=it},Modifier.fillMaxWidth(),singleLine=true,placeholder={Text(if(fa)"جستجوی ارز" else "Search asset")},leadingIcon={Icon(Icons.Outlined.Search,null)},shape=RoundedCornerShape(16.dp))};item{Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf("Favorites","Crypto","Futures","Forex").forEach{FilterChip(it=="Crypto",{},label={Text(it,fontSize=8.sp)})}}};items(list){q->VRow(q){onMarket(q.symbol);open()}};item{OutlinedButton(onClick=refresh,Modifier.fillMaxWidth()){Icon(Icons.Outlined.Refresh,null);Spacer(Modifier.width(5.dp));Text("Refresh")}}}}

@Composable private fun VTerminal(fa:Boolean,market:String,onMarket:(String)->Unit,timeframe:String,onTimeframe:(String)->Unit,vm:BacktestViewModel,state:BacktestUiState){val repo=remember{CoinExRepository()};val scope=rememberCoroutineScope();var candles by remember{mutableStateOf<List<Candle>>(emptyList())};var selected by remember{mutableStateOf<Int?>(null)};fun load(){scope.launch{candles=runCatching{repo.loadKlines(market,timeframe,220)}.getOrDefault(emptyList())}};LaunchedEffect(market,timeframe){vm.setMarket(market);vm.setTimeframe(timeframe);load()};LazyColumn(Modifier.fillMaxSize().padding(12.dp),verticalArrangement=Arrangement.spacedBy(9.dp),contentPadding=PaddingValues(top=8.dp,bottom=24.dp)){item{Row(verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(market.replace("USDT"," / USDT"),color=VText,fontSize=20.sp,fontWeight=FontWeight.Black);Text("CoinEx • real OHLC",color=VMuted,fontSize=8.sp)};VPill(if(state.isRunning)"RUNNING" else "READY",if(state.isRunning)VGold else VGreen)}};item{VTimeframes(timeframe,onTimeframe)};item{VCandles(candles,state.report?.trades?:emptyList(),selected,{selected=it},Modifier.fillMaxWidth().height(325.dp))};item{selected?.let{i->candles.getOrNull(i)?.let{VOhlc(it)}}};item{VControls(fa,market,onMarket,vm,state)};item{VReport(state.report,null)};item{state.report?.let{VTrades(it.trades,fa)}}}}

@Composable private fun VControls(fa:Boolean,market:String,onMarket:(String)->Unit,vm:BacktestViewModel,state:BacktestUiState){var amount by rememberSaveable{mutableStateOf("1000")};var risk by rememberSaveable{mutableStateOf(state.riskPercent.toString())};var lev by rememberSaveable{mutableStateOf(state.leverage.toString())};Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(21.dp),colors=CardDefaults.cardColors(containerColor=VPanel)){Column(Modifier.padding(13.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Text(if(fa)"تنظیمات بک‌تست" else "BACKTEST CONFIG",color=VText,fontWeight=FontWeight.Black);OutlinedTextField(market,{onMarket(it.uppercase())},Modifier.fillMaxWidth(),singleLine=true,label={Text("Pair")});Row(horizontalArrangement=Arrangement.spacedBy(7.dp)){OutlinedTextField(amount,{amount=it},Modifier.weight(1f),singleLine=true,label={Text("Amount")});OutlinedTextField(lev,{lev=it},Modifier.weight(1f),singleLine=true,label={Text("Leverage")})};OutlinedTextField(risk,{risk=it},Modifier.fillMaxWidth(),singleLine=true,label={Text("Risk %")});Button(onClick={vm.setRiskPercent(risk.toDoubleOrNull()?:1.0);vm.setLeverage(lev.toDoubleOrNull()?:3.0);vm.runBacktest()},Modifier.fillMaxWidth(),enabled=!state.isRunning,shape=RoundedCornerShape(14.dp)){Icon(Icons.Outlined.PlayArrow,null);Spacer(Modifier.width(5.dp));Text(if(fa)"اجرای بک‌تست واقعی" else "Run real backtest")};Text(state.error?:state.status,color=if(state.error!=null)VRed else VMuted,fontSize=8.sp)}}}

@Composable private fun VIntel(fa:Boolean,market:String,repo:CoinExRepository){var candles by remember{mutableStateOf<List<Candle>>(emptyList())};var text by remember{mutableStateOf(if(fa)"در انتظار داده واقعی..." else "Waiting for live data...")};var score by remember{mutableStateOf(0)};LaunchedEffect(market){candles=runCatching{repo.loadKlines(market,"15min",120)}.getOrDefault(emptyList());val s=PumpDumpDetector().analyze(candles);if(s!=null){score=s.score.toInt();text="${s.direction} • ${"%.2f".format(s.priceChangePercent)}% • volume x${"%.1f".format(s.volumeRatio)}"}};LazyColumn(Modifier.fillMaxSize().padding(12.dp),verticalArrangement=Arrangement.spacedBy(10.dp),contentPadding=PaddingValues(top=8.dp,bottom=20.dp)){item{VSection(if(fa)"هوش بازار" else "INTELLIGENCE HUB","AI Radar • Whale • Pump/Dump • Meme • Coin Intelligence")};item{Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(25.dp),colors=CardDefaults.cardColors(containerColor=VPanel)){Column(Modifier.padding(16.dp),horizontalAlignment=Alignment.CenterHorizontally){VRadarGauge(score);Text(market,color=VText,fontSize=22.sp,fontWeight=FontWeight.Black);Text(text,color=VMuted,fontSize=9.sp);Text(if(fa)"فقط داده قابل‌اندازه‌گیری؛ پیش‌بینی قطعی نیست." else "Measurable data only; no guaranteed prediction.",color=VMuted,fontSize=8.sp)}}};item{VIntelTile(Icons.Outlined.Warning,"PUMP / DUMP RADAR","Price acceleration • volume anomaly • score",VPurple2)};item{VIntelTile(Icons.Outlined.AccountBox,"WHALE INTELLIGENCE","Provider-neutral smart-money layer",VCyan)};item{VIntelTile(Icons.Outlined.Info,"MEME / SHITCOIN SCANNER","Liquidity • volatility • concentration • risk",VGold)};item{VIntelTile(Icons.Outlined.List,"COIN INTELLIGENCE","Tokenomics • project • team • roadmap • on-chain",VGreen)};item{VIntelTile(Icons.Outlined.Star,"AI TRADE ANALYST","Backtest diagnostics and optimization context",VPurple2)}}}

@Composable private fun VCandles(candles:List<Candle>,trades:List<TradeResult>,selected:Int?,onSelect:(Int)->Unit,modifier:Modifier){if(candles.isEmpty()){Box(modifier.clip(RoundedCornerShape(21.dp)).background(VPanel2),contentAlignment=Alignment.Center){Text("Loading real CoinEx candles…",color=VMuted)};return};Canvas(modifier.clip(RoundedCornerShape(21.dp)).background(VPanel2).pointerInput(candles){detectTapGestures{p->onSelect(((p.x/size.width)*candles.size).toInt().coerceIn(0,candles.lastIndex))}}){val min=candles.minOf{it.low};val max=candles.maxOf{it.high};val range=(max-min).takeIf{it>0}?:1.0;val step=size.width/candles.size;val body=(step*.62f).coerceAtLeast(2f);fun y(v:Double)=size.height-((v-min)/range*size.height).toFloat();for(i in 0..3){val gy=size.height*i/3f;drawLine(VBorder,Offset(0f,gy),Offset(size.width,gy),1f)};candles.forEachIndexed{i,c->val x=i*step+step/2;val col=if(c.close>=c.open)VGreen else VRed;drawLine(col,Offset(x,y(c.high)),Offset(x,y(c.low)),1.3f);drawRect(col,Offset(x-body/2,y(maxOf(c.open,c.close))),androidx.compose.ui.geometry.Size(body,maxOf(2f,abs(y(c.open)-y(c.close)))))};trades.forEach{t->val ei=candles.indices.minByOrNull{abs(candles[it].timestamp-t.entryTime)}?:return@forEach;val x=ei*step+step/2;val col=if(t.side==Side.LONG)VGreen else VRed;drawCircle(col,5f,Offset(x,y(t.entryPrice)));val ex=candles.indices.minByOrNull{abs(candles[it].timestamp-t.exitTime)};if(ex!=null){val xx=ex*step+step/2;drawCircle(VGold,4f,Offset(xx,y(t.exitPrice)));drawLine(col,Offset(x,y(t.entryPrice)),Offset(xx,y(t.exitPrice)),1f)}};selected?.let{i->val x=i*step+step/2;drawLine(VText,Offset(x,0f),Offset(x,size.height),1f)}}}

@Composable private fun VTrades(trades:List<TradeResult>,fa:Boolean){Column(verticalArrangement=Arrangement.spacedBy(6.dp)){Text(if(fa)"تشخیص معامله‌به‌معامله" else "TRADE-BY-TRADE DIAGNOSTICS",color=VText,fontWeight=FontWeight.Black);trades.takeLast(30).asReversed().forEach{t->Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(15.dp),colors=CardDefaults.cardColors(containerColor=VPanel)){Row(Modifier.padding(10.dp),verticalAlignment=Alignment.CenterVertically){VPill(if(t.side==Side.LONG)"LONG" else "SHORT",if(t.side==Side.LONG)VGreen else VRed);Spacer(Modifier.width(8.dp));Column(Modifier.weight(1f)){Text("${money(t.entryPrice)} → ${money(t.exitPrice)}",color=VText,fontSize=9.sp);Text("SL ${money(t.stopLoss)} • TP ${money(t.takeProfit)} • ${t.exitReason}",color=VMute dFix(),fontSize=8.sp)};Text("%+.3f".format(t.netPnl),color=if(t.netPnl>=0)VGreen else VRed,fontWeight=FontWeight.Black,fontSize=10.sp)}}}}}

@Composable private fun VReport(report:BacktestReport?,onOpen:(()->Unit)?){Card(onClick=onOpen?:{},Modifier.fillMaxWidth(),shape=RoundedCornerShape(21.dp),colors=CardDefaults.cardColors(containerColor=VPanel)){Column(Modifier.padding(13.dp)){Row(verticalAlignment=Alignment.CenterVertically){Text("AI BACKTEST ANALYST",color=VText,fontWeight=FontWeight.Black);Spacer(Modifier.weight(1f));Text(if(report==null)"READY" else "REPORT",color=VGreen,fontSize=8.sp)};Spacer(Modifier.height(9.dp));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)){VMini("ROI",report?.let{"%.2f%%".format(it.roiPercent)}?:"—");VMini("WIN",report?.let{"%.1f%%".format(it.winRatePercent)}?:"—");VMini("PF",report?.let{if(it.profitFactor.isInfinite())"∞" else "%.2f".format(it.profitFactor)}?:"—");VMini("DD",report?.let{"%.2f%%".format(it.maxDrawdownPercent)}?:"—")};Spacer(Modifier.height(7.dp));VEquity(report?.equityCurve?:emptyList(),Modifier.fillMaxWidth().height(65.dp))}}}

@Composable private fun VSettings(fa:Boolean,themeMode:AppThemeMode,onThemeMode:(AppThemeMode)->Unit,toggleLanguage:()->Unit,onBack:()->Unit){Column(Modifier.fillMaxSize().background(VBg)){Row(Modifier.fillMaxWidth().padding(8.dp),verticalAlignment=Alignment.CenterVertically){IconButton(onClick=onBack){Icon(Icons.Outlined.ArrowBack,null,tint=VText)};Column{Text("ALVEX",color=VText,fontWeight=FontWeight.Black);Text(if(fa)"تنظیمات" else "Settings",color=VMute dFix(),fontSize=8.sp)}};LazyColumn(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){item{VSetting(Icons.Outlined.Person,"Account & Security","Session • password • API-key policy")};item{VSetting(Icons.Outlined.Notifications,"Notifications","Pump/Dump • Whale • AI alerts")};item{VSetting(Icons.Outlined.Language,"Language",if(fa)"فارسی / English" else "English / فارسی",toggleLanguage)};item{Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text("Theme",color=VText,fontWeight=FontWeight.Bold);Text(if(themeMode==AppThemeMode.DARK)"Dark" else "Light",color=VMute dFix(),fontSize=8.sp)};Switch(checked=themeMode==AppThemeMode.LIGHT,onCheckedChange={onThemeMode(if(it)AppThemeMode.LIGHT else AppThemeMode.DARK)})}};item{VSetting(Icons.Outlined.Share,"Share & Reports","CSV • JSON • AI report")};item{VSetting(Icons.Outlined.Info,"About ALVEX","Professional crypto intelligence workspace")}}}}

@Composable private fun VSetting(icon:androidx.compose.ui.graphics.vector.ImageVector,title:String,subtitle:String,onClick:(()->Unit)?=null){Card(onClick=onClick?:{},Modifier.fillMaxWidth(),shape=RoundedCornerShape(17.dp),colors=CardDefaults.cardColors(containerColor=VPanel)){Row(Modifier.padding(13.dp),verticalAlignment=Alignment.CenterVertically){Icon(icon,null,tint=VCyan);Spacer(Modifier.width(9.dp));Column(Modifier.weight(1f)){Text(title,color=VText,fontWeight=FontWeight.Bold);Text(subtitle,color=VMute dFix(),fontSize=8.sp)};Icon(Icons.Outlined.Info,null,tint=VMute dFix(),modifier=Modifier.size(15.dp))}}}

@Composable private fun VSection(title:String,subtitle:String=""){Column{Text(title,color=VText,fontSize=15.sp,fontWeight=FontWeight.Black);if(subtitle.isNotBlank())Text(subtitle,color=VMute dFix(),fontSize=8.sp)}}
@Composable private fun VPill(text:String,color:Color){Box(Modifier.clip(RoundedCornerShape(30.dp)).background(color.copy(.12f)).padding(horizontal=8.dp,vertical=4.dp)){Text(text,color=color,fontSize=7.sp,fontWeight=FontWeight.Black)}}
@Composable private fun VStat(title:String,value:String,sub:String,color:Color,modifier:Modifier){Card(modifier,shape=RoundedCornerShape(16.dp),colors=CardDefaults.cardColors(containerColor=VPanel)){Column(Modifier.padding(10.dp)){Text(title,color=VMute dFix(),fontSize=7.sp);Text(value,color=VText,fontSize=11.sp,fontWeight=FontWeight.Black);Text(sub,color=color,fontSize=7.sp,fontWeight=FontWeight.Bold)}}}
@Composable private fun VRow(q:VQuote,onClick:()->Unit){Card(onClick=onClick,Modifier.fillMaxWidth(),shape=RoundedCornerShape(16.dp),colors=CardDefaults.cardColors(containerColor=VPanel)){Row(Modifier.padding(11.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(39.dp).clip(RoundedCornerShape(12.dp)).background(Brush.linearGradient(listOf(VPurple.copy(.22f),VCyan.copy(.12f)))),contentAlignment=Alignment.Center){Text(q.symbol.take(1),color=VText,fontWeight=FontWeight.Black)};Spacer(Modifier.width(9.dp));Column(Modifier.weight(1f)){Text(q.symbol.removeSuffix("USDT"),color=VText,fontWeight=FontWeight.Bold);Text("/ USDT • ${compact(q.volume)} vol",color=VMute dFix(),fontSize=7.sp)};Column(horizontalAlignment=Alignment.End){Text(money(q.price),color=VText,fontSize=9.sp,fontWeight=FontWeight.Bold);Text(pct(q.change),color=if(q.change>=0)VGreen else VRed,fontSize=9.sp)}}}}
@Composable private fun VRadar(title:String,subtitle:String,color:Color,modifier:Modifier,onClick:()->Unit){Card(onClick=onClick,modifier=modifier,shape=RoundedCornerShape(15.dp),colors=CardDefaults.cardColors(containerColor=VPanel2)){Column(Modifier.padding(9.dp)){Box(Modifier.size(27.dp).clip(RoundedCornerShape(8.dp)).background(color.copy(.12f)),contentAlignment=Alignment.Center){Text("•",color=color,fontSize=17.sp)};Spacer(Modifier.height(5.dp));Text(title,color=VText,fontSize=8.sp,fontWeight=FontWeight.Bold);Text(subtitle,color=VMute dFix(),fontSize=6.sp)}}}
@Composable private fun VAction(icon:androidx.compose.ui.graphics.vector.ImageVector,title:String,modifier:Modifier,onClick:()->Unit){Card(onClick=onClick,modifier=modifier,shape=RoundedCornerShape(15.dp),colors=CardDefaults.cardColors(containerColor=VPanel)){Column(Modifier.padding(10.dp),horizontalAlignment=Alignment.CenterHorizontally){Icon(icon,null,tint=VPurple2);Spacer(Modifier.height(4.dp));Text(title,color=VText,fontSize=8.sp,fontWeight=FontWeight.Bold)}}}
@Composable private fun VIntelTile(icon:androidx.compose.ui.graphics.vector.ImageVector,title:String,subtitle:String,color:Color){Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(18.dp),colors=CardDefaults.cardColors(containerColor=VPanel)){Row(Modifier.padding(13.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(39.dp).clip(RoundedCornerShape(11.dp)).background(color.copy(.12f)),contentAlignment=Alignment.Center){Icon(icon,null,tint=color)};Spacer(Modifier.width(9.dp));Column(Modifier.weight(1f)){Text(title,color=VText,fontWeight=FontWeight.Black,fontSize=10.sp);Text(subtitle,color=VMute dFix(),fontSize=7.sp)};Icon(Icons.Outlined.Info,null,tint=VMute dFix(),modifier=Modifier.size(15.dp))}}}
@Composable private fun VTimeframes(selected:String,onSelect:(String)->Unit){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(4.dp)){listOf("1min","5min","15min","1h","4h","1D").forEach{v->FilterChip(selected==v,{onSelect(v)},label={Text(v,fontSize=7.sp)})}}}
@Composable private fun VOhlc(c:Candle){Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(14.dp),colors=CardDefaults.cardColors(containerColor=VPanel)){Row(Modifier.fillMaxWidth().padding(9.dp),horizontalArrangement=Arrangement.SpaceEvenly){Text("O ${money(c.open)}",color=VText,fontSize=8.sp);Text("H ${money(c.high)}",color=VGreen,fontSize=8.sp);Text("L ${money(c.low)}",color=VRed,fontSize=8.sp);Text("C ${money(c.close)}",color=VText,fontSize=8.sp);Text("V ${compact(c.volume)}",color=VMute dFix(),fontSize=8.sp)}}}
@Composable private fun VMini(label:String,value:String){Column(Modifier.weight(1f)){Text(label,color=VMute dFix(),fontSize=7.sp);Text(value,color=VText,fontSize=9.sp,fontWeight=FontWeight.Black)}}
@Composable private fun VEquity(values:List<Double>,modifier:Modifier){Canvas(modifier){if(values.size<2)return@Canvas;val min=values.minOrNull()?:0.0;val max=values.maxOrNull()?:1.0;val r=(max-min).takeIf{it>0}?:1.0;for(i in 1 until values.size){val x1=size.width*(i-1)/values.lastIndex;val x2=size.width*i/values.lastIndex;val y1=size.height-((values[i-1]-min)/r*size.height).toFloat();val y2=size.height-((values[i]-min)/r*size.height).toFloat();drawLine(VPurple2,Offset(x1,y1),Offset(x2,y2),2f)}}}
@Composable private fun VSpark(values:List<Double>,modifier:Modifier){Canvas(modifier){if(values.size<2)return@Canvas;val min=values.minOrNull()?:0.0;val max=values.maxOrNull()?:1.0;val r=(max-min).takeIf{it>0}?:1.0;val p=Path();values.forEachIndexed{i,v->val x=size.width*i/values.lastIndex;val y=size.height-((v-min)/r*size.height).toFloat();if(i==0)p.moveTo(x,y)else p.lineTo(x,y)};drawPath(p,VCyan,style=androidx.compose.ui.graphics.drawscope.Stroke(2f))}}
@Composable private fun VRadarGauge(score:Int){Canvas(Modifier.size(130.dp)){for(i in 1..3)drawCircle(VBorder,size.minDimension*.34f*i/3f,center,style=androidx.compose.ui.graphics.drawscope.Stroke(1f));drawCircle(if(score>=70)VRed else VPurple2,size.minDimension*.18f,center);drawCircle(VCyan,size.minDimension*.34f,center,style=androidx.compose.ui.graphics.drawscope.Stroke(2f))}}
@Composable private fun VMark(size:Int){Box(Modifier.size(size.dp).clip(RoundedCornerShape((size/4).dp)).background(Brush.linearGradient(listOf(VPurple,VCyan))),contentAlignment=Alignment.Center){Text("A",color=Color.White,fontSize=(size/1.9).sp,fontWeight=FontWeight.Black)}}
@Composable private fun VMuteFix():Color=VMute
private fun money(v:Double)=if(v>=1000)"%,.2f".format(v)else if(v>=1)"%.4f".format(v)else"%.8f".format(v)
private fun pct(v:Double)="%+.2f%%".format(v)
private fun compact(v:Double)=when{v>=1e9->"%.1fB".format(v/1e9);v>=1e6->"%.1fM".format(v/1e6);v>=1e3->"%.1fK".format(v/1e3);else->"%.0f".format(v)}
