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

private val XBG=Color(0xFF050912)
private val XP=Color(0xFF0B1422)
private val XP2=Color(0xFF111D2D)
private val XT=Color(0xFFF5F7FB)
private val XM=Color(0xFF7F8DA3)
private val XV=Color(0xFF8A45FF)
private val XC=Color(0xFF26D9FF)
private val XG=Color(0xFF21D79B)
private val XR=Color(0xFFFF536F)
private val XY=Color(0xFFFFC857)
private data class XQuote(val symbol:String,val price:Double,val change:Double,val volume:Double)

@Composable
fun AlvexPremiumFinal(themeMode:AppThemeMode,onTheme:(AppThemeMode)->Unit){
    var page by rememberSaveable{mutableStateOf(0)}
    var pair by rememberSaveable{mutableStateOf("BTCUSDT")}
    var tf by rememberSaveable{mutableStateOf("15min")}
    var fa by rememberSaveable{mutableStateOf(true)}
    var settings by rememberSaveable{mutableStateOf(false)}
    var quotes by remember{mutableStateOf<List<XQuote>>(emptyList())}
    val repo=remember{CoinExRepository()};val vm=remember{BacktestViewModel()};val state by vm.state.collectAsState();val scope=rememberCoroutineScope()
    fun refresh(){scope.launch{quotes=withContext(Dispatchers.IO){listOf("BTCUSDT","ETHUSDT","BNBUSDT","SOLUSDT","XRPUSDT","DOGEUSDT","ADAUSDT","AVAXUSDT","LINKUSDT","PEPEUSDT").mapNotNull{s->runCatching{repo.loadLatestTicker(s)}.getOrNull()?.let{XQuote(s,it.last,it.changeRate*100.0,it.volume)}}}}}
    LaunchedEffect(Unit){refresh()}
    MaterialTheme(colorScheme=darkColorScheme(primary=XV,secondary=XC,background=XBG,surface=XP,onBackground=XT,onSurface=XT)){
        if(settings){XSettings(fa,themeMode,onTheme,{fa=!fa}){settings=false};return@MaterialTheme}
        Scaffold(containerColor=XBG,topBar={XHeader(page,fa,{page=3},{settings=true},{fa=!fa})},bottomBar={XNav(page,fa){page=it}}){pad->
            Box(Modifier.fillMaxSize().padding(pad).background(XBG)){
                when(page){
                    0->XHome(fa,quotes,state.report,{pair=it;page=2},::refresh,{page=3})
                    1->XMarkets(fa,quotes,{pair=it;page=2;vm.setMarket(it)},::refresh)
                    2->XTerminal(fa,pair,{pair=it;vm.setMarket(it)},tf,{tf=it},vm,state)
                    else->XIntel(fa,pair,repo)
                }
            }
        }
    }
}

@Composable private fun XHeader(page:Int,fa:Boolean,ai:()->Unit,settings:()->Unit,lang:()->Unit){
    Row(Modifier.fillMaxWidth().height(64.dp).background(XBG).padding(horizontal=8.dp),verticalAlignment=Alignment.CenterVertically){
        IconButton(onClick=ai){Icon(Icons.Outlined.Star,null,tint=XC)}
        Column(Modifier.weight(1f),horizontalAlignment=Alignment.CenterHorizontally){
            Row(verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(23.dp).clip(RoundedCornerShape(6.dp)).background(Brush.linearGradient(listOf(XV,XC))),contentAlignment=Alignment.Center){Text("A",color=Color.White,fontWeight=FontWeight.Black)};Spacer(Modifier.width(6.dp));Text("ALVEX",color=XT,fontWeight=FontWeight.Black,letterSpacing=2.sp)}
            Text(if(fa)listOf("خانه","بازار","معاملات و بک‌تست","هوش بازار")[page]else listOf("Home","Markets","Trading Terminal","Intelligence")[page],color=XM,fontSize=8.sp)
        }
        TextButton(onClick=lang){Text(if(fa)"EN"else"FA",color=XC,fontSize=9.sp)}
        IconButton(onClick=settings){Icon(Icons.Outlined.Settings,null,tint=XT)}
    }
}

@Composable private fun XNav(page:Int,fa:Boolean,go:(Int)->Unit){NavigationBar(containerColor=XP,tonalElevation=0.dp){listOf(Triple(0,Icons.Outlined.Home,if(fa)"خانه"else"Home"),Triple(1,Icons.Outlined.List,if(fa)"بازار"else"Markets"),Triple(2,Icons.Outlined.Build,if(fa)"بک‌تست"else"Terminal"),Triple(3,Icons.Outlined.Info,if(fa)"هوش"else"Intel")).forEach{(i,ic,t)->NavigationBarItem(page==i,{go(i)},icon={Icon(ic,t)},label={Text(t,fontSize=8.sp)})}}}

@Composable private fun XHome(fa:Boolean,quotes:List<XQuote>,report:BacktestReport?,open:(String)->Unit,refresh:()->Unit,intel:()->Unit){
    val btc=quotes.firstOrNull{it.symbol=="BTCUSDT"};val movers=quotes.sortedByDescending{it.change}.take(4)
    LazyColumn(Modifier.fillMaxSize().padding(12.dp),verticalArrangement=Arrangement.spacedBy(10.dp),contentPadding=PaddingValues(bottom=20.dp)){
        item{Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(26.dp),colors=CardDefaults.cardColors(containerColor=XP)){Box(Modifier.fillMaxWidth().height(155.dp).background(Brush.linearGradient(listOf(Color(0xFF1E104A),XP,Color(0xFF071A25))),RoundedCornerShape(26.dp))){Column(Modifier.padding(17.dp)){Row(verticalAlignment=Alignment.CenterVertically){Text("MARKET OVERVIEW",color=XM,fontSize=8.sp,letterSpacing=1.sp);Spacer(Modifier.weight(1f));Pill("LIVE",XG)};Spacer(Modifier.height(7.dp));Text("BTC / USDT",color=XT,fontSize=12.sp);Text(btc?.let{money(it.price)}?:"—",color=XT,fontSize=29.sp,fontWeight=FontWeight.Black);Text(btc?.let{pct(it.change)}?:"Waiting for market data",color=if((btc?.change?:0.0)>=0)XG else XR,fontSize=10.sp);Spacer(Modifier.height(7.dp));Spark(movers.map{it.change},Modifier.fillMaxWidth().height(32.dp))}}}}
        item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(7.dp)){Stat("BTC",btc?.let{money(it.price)}?:"—",btc?.let{pct(it.change)}?:"—",if((btc?.change?:0.0)>=0)XG else XR,Modifier.weight(1f));Stat("24H VOL",quotes.firstOrNull()?.let{compact(it.volume)}?:"—","CoinEx",XC,Modifier.weight(1f));Stat("RADAR",if(quotes.isEmpty())"WAIT"else"READY","AI",XV,Modifier.weight(1f))}}
        item{Section("TOP MOVERS",if(fa)"حرکت‌های مهم بازار"else"Live market movers")};items(movers){q->Market(q){open(q.symbol)}}
        item{Section("AI MARKET RADAR","Pump/Dump • Whale • Meme • Coin Intelligence")}
        item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(7.dp)){Radar("PUMP / DUMP",XV,Modifier.weight(1f),intel);Radar("WHALE",XC,Modifier.weight(1f),intel);Radar("MEME",XY,Modifier.weight(1f),intel)}}
        item{Section(if(fa)"آخرین بک‌تست"else"LATEST BACKTEST","Real report metrics")};item{Report(report)}
        item{OutlinedButton(onClick=refresh,Modifier.fillMaxWidth(),shape=RoundedCornerShape(15.dp)){Icon(Icons.Outlined.Refresh,null);Spacer(Modifier.width(5.dp));Text(if(fa)"به‌روزرسانی داده بازار"else"Refresh market data")}}
    }
}

@Composable private fun XMarkets(fa:Boolean,quotes:List<XQuote>,open:(String)->Unit,refresh:()->Unit){var q by rememberSaveable{mutableStateOf("")};val list=quotes.filter{q.isBlank()||it.symbol.contains(q.uppercase())};LazyColumn(Modifier.fillMaxSize().padding(12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){item{Section(if(fa)"بازار"else"MARKETS","Crypto • Futures • Favorites")};item{OutlinedTextField(q,{q=it},Modifier.fillMaxWidth(),singleLine=true,placeholder={Text(if(fa)"جستجوی ارز"else"Search asset")},leadingIcon={Icon(Icons.Outlined.Search,null)},shape=RoundedCornerShape(16.dp))};item{Row(horizontalArrangement=Arrangement.spacedBy(5.dp)){listOf("Favorites","Crypto","Futures","Forex").forEach{FilterChip(it=="Crypto",{},label={Text(it,fontSize=8.sp)})}}};items(list){x->Market(x){open(x.symbol)}};item{OutlinedButton(onClick=refresh,Modifier.fillMaxWidth()){Text("Refresh")}}}}

@Composable private fun XTerminal(fa:Boolean,pair:String,onPair:(String)->Unit,tf:String,onTf:(String)->Unit,vm:BacktestViewModel,state:BacktestUiState){val repo=remember{CoinExRepository()};val scope=rememberCoroutineScope();var candles by remember{mutableStateOf<List<Candle>>(emptyList())};var selected by remember{mutableStateOf<Int?>(null)};fun load(){scope.launch{candles=runCatching{repo.loadKlines(pair,tf,220)}.getOrDefault(emptyList())}};LaunchedEffect(pair,tf){vm.setMarket(pair);vm.setTimeframe(tf);load()};LazyColumn(Modifier.fillMaxSize().padding(12.dp),verticalArrangement=Arrangement.spacedBy(9.dp),contentPadding=PaddingValues(bottom=20.dp)){item{Row(verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(pair.replace("USDT"," / USDT"),color=XT,fontSize=20.sp,fontWeight=FontWeight.Black);Text("CoinEx • real OHLC",color=XM,fontSize=8.sp)};Pill(if(state.isRunning)"RUNNING"else"READY",if(state.isRunning)XY else XG)}};item{Row(horizontalArrangement=Arrangement.spacedBy(4.dp)){listOf("1min","5min","15min","1h","4h","1D").forEach{v->FilterChip(tf==v,{onTf(v)},label={Text(v,fontSize=7.sp)})}}};item{Chart(candles,state.report?.trades?:emptyList(),selected,{selected=it},Modifier.fillMaxWidth().height(325.dp))};item{selected?.let{i->candles.getOrNull(i)?.let{Ohlc(it)}}};item{Controls(fa,pair,onPair,vm,state)};item{Report(state.report)};item{state.report?.let{Trades(it.trades,fa)}}}}

@Composable private fun Controls(fa:Boolean,pair:String,onPair:(String)->Unit,vm:BacktestViewModel,state:BacktestUiState){var amount by rememberSaveable{mutableStateOf("1000")};var risk by rememberSaveable{mutableStateOf(state.riskPercent.toString())};var lev by rememberSaveable{mutableStateOf(state.leverage.toString())};Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(20.dp),colors=CardDefaults.cardColors(containerColor=XP)){Column(Modifier.padding(13.dp),verticalArrangement=Arrangement.spacedBy(7.dp)){Text(if(fa)"تنظیمات بک‌تست"else"BACKTEST CONFIG",color=XT,fontWeight=FontWeight.Black);OutlinedTextField(pair,{onPair(it.uppercase())},Modifier.fillMaxWidth(),singleLine=true,label={Text("Pair")});Row(horizontalArrangement=Arrangement.spacedBy(7.dp)){OutlinedTextField(amount,{amount=it},Modifier.weight(1f),singleLine=true,label={Text("Amount")});OutlinedTextField(lev,{lev=it},Modifier.weight(1f),singleLine=true,label={Text("Leverage")})};OutlinedTextField(risk,{risk=it},Modifier.fillMaxWidth(),singleLine=true,label={Text("Risk %")});Button(onClick={vm.setRiskPercent(risk.toDoubleOrNull()?:1.0);vm.setLeverage(lev.toDoubleOrNull()?:3.0);vm.runBacktest()},Modifier.fillMaxWidth(),enabled=!state.isRunning,shape=RoundedCornerShape(14.dp)){Icon(Icons.Outlined.PlayArrow,null);Spacer(Modifier.width(5.dp));Text(if(fa)"اجرای بک‌تست واقعی"else"Run real backtest")};Text(state.error?:state.status,color=if(state.error!=null)XR else XM,fontSize=8.sp)}}}

@Composable private fun Chart(cs:List<Candle>,ts:List<TradeResult>,sel:Int?,pick:(Int)->Unit,m:Modifier){if(cs.isEmpty()){Box(m.clip(RoundedCornerShape(20.dp)).background(XP2),contentAlignment=Alignment.Center){Text("Loading real CoinEx candles…",color=XM)};return};Canvas(m.clip(RoundedCornerShape(20.dp)).background(XP2).pointerInput(cs){detectTapGestures{p->pick(((p.x/size.width)*cs.size).toInt().coerceIn(0,cs.lastIndex))}}){val lo=cs.minOf{it.low};val hi=cs.maxOf{it.high};val range=(hi-lo).takeIf{it>0}?:1.0;val step=size.width/cs.size;val body=(step*.58f).coerceAtLeast(2f);fun y(v:Double)=size.height-((v-lo)/range*size.height).toFloat();for(i in 0..3)drawLine(Color(0xFF1B2B3D),Offset(0f,size.height*i/3f),Offset(size.width,size.height*i/3f),1f);cs.forEachIndexed{i,c->val x=i*step+step/2;val col=if(c.close>=c.open)XG else XR;drawLine(col,Offset(x,y(c.high)),Offset(x,y(c.low)),1.2f);drawRect(col,Offset(x-body/2,y(maxOf(c.open,c.close))),androidx.compose.ui.geometry.Size(body,maxOf(2f,abs(y(c.open)-y(c.close)))))};ts.forEach{t->val ei=cs.indices.minByOrNull{abs(cs[it].timestamp-t.entryTime)}?:return@forEach;val x=ei*step+step/2;val col=if(t.side==Side.LONG)XG else XR;drawCircle(col,5f,Offset(x,y(t.entryPrice)));val ex=cs.indices.minByOrNull{abs(cs[it].timestamp-t.exitTime)};if(ex!=null){val xx=ex*step+step/2;drawCircle(XY,4f,Offset(xx,y(t.exitPrice)));drawLine(col,Offset(x,y(t.entryPrice)),Offset(xx,y(t.exitPrice)),1f)}};sel?.let{i->drawLine(XT,Offset(i*step+step/2,0f),Offset(i*step+step/2,size.height),1f)}}}

@Composable private fun Trades(ts:List<TradeResult>,fa:Boolean){Column(verticalArrangement=Arrangement.spacedBy(6.dp)){Text(if(fa)"معاملات و خطایابی ربات"else"TRADE DIAGNOSTICS",color=XT,fontWeight=FontWeight.Black);ts.takeLast(25).asReversed().forEach{t->Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(15.dp),colors=CardDefaults.cardColors(containerColor=XP)){Column(Modifier.padding(10.dp)){Row(verticalAlignment=Alignment.CenterVertically){Pill(t.side.name,if(t.side==Side.LONG)XG else XR);Spacer(Modifier.width(7.dp));Text("${money(t.entryPrice)} → ${money(t.exitPrice)}",color=XT,fontSize=9.sp);Spacer(Modifier.weight(1f));Text("%+.3f".format(t.netPnl),color=if(t.netPnl>=0)XG else XR,fontWeight=FontWeight.Black,fontSize=9.sp)};Text("SL ${money(t.stopLoss)} • TP ${money(t.takeProfit)} • ${t.exitReason}",color=XM,fontSize=8.sp);Text("Qty ${"%.5f".format(t.quantity)} • Fee ${"%.4f".format(t.fees)} • Funding ${"%.4f".format(t.funding)}",color=XM,fontSize=7.sp)}}}}}

@Composable private fun Report(r:BacktestReport?){Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(20.dp),colors=CardDefaults.cardColors(containerColor=XP)){Column(Modifier.padding(13.dp)){Text("AI BACKTEST ANALYST",color=XT,fontWeight=FontWeight.Black);Spacer(Modifier.height(7.dp));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(4.dp)){Metric("ROI",r?.let{"%.2f%%".format(it.roiPercent)}?:"—");Metric("WIN",r?.let{"%.1f%%".format(it.winRatePercent)}?:"—");Metric("PF",r?.let{if(it.profitFactor.isInfinite())"∞" else "%.2f".format(it.profitFactor)}?:"—");Metric("DD",r?.let{"%.2f%%".format(it.maxDrawdownPercent)}?:"—")}}}}

@Composable private fun XSettings(fa:Boolean,mode:AppThemeMode,onMode:(AppThemeMode)->Unit,language:()->Unit,back:()->Unit){Column(Modifier.fillMaxSize().background(XBG)){Row(Modifier.fillMaxWidth().padding(8.dp),verticalAlignment=Alignment.CenterVertically){IconButton(onClick=back){Icon(Icons.Outlined.ArrowBack,null,tint=XT)};Column{Text("ALVEX",color=XT,fontWeight=FontWeight.Black);Text(if(fa)"تنظیمات"else"Settings",color=XM,fontSize=8.sp)}};LazyColumn(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){item{Setting("Account & Security","Session • password • API-key policy",Icons.Outlined.Person)};item{Setting("Notifications","Pump/Dump • Whale • AI alerts",Icons.Outlined.Notifications)};item{Setting("Language",if(fa)"فارسی / English"else"English / فارسی",Icons.Outlined.Language,language)};item{Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text("Theme",color=XT,fontWeight=FontWeight.Bold);Text(if(mode==AppThemeMode.DARK)"Dark"else"Light",color=XM,fontSize=8.sp)};Switch(checked=mode==AppThemeMode.LIGHT,onCheckedChange={onMode(if(it)AppThemeMode.LIGHT else AppThemeMode.DARK)})}};item{Setting("Share & Reports","CSV • JSON • AI report • Android share",Icons.Outlined.Share)};item{Setting("About ALVEX","Professional crypto intelligence workspace",Icons.Outlined.Info)}}}}
@Composable private fun Setting(t:String,s:String,ic:androidx.compose.ui.graphics.vector.ImageVector,on:(()->Unit)?=null){Card(onClick=on?:{},Modifier.fillMaxWidth(),shape=RoundedCornerShape(17.dp),colors=CardDefaults.cardColors(containerColor=XP)){Row(Modifier.padding(13.dp),verticalAlignment=Alignment.CenterVertically){Icon(ic,null,tint=XC);Spacer(Modifier.width(9.dp));Column{Text(t,color=XT,fontWeight=FontWeight.Bold);Text(s,color=XM,fontSize=8.sp)}}}}
@Composable private fun Section(t:String,s:String=""){Column{Text(t,color=XT,fontSize=15.sp,fontWeight=FontWeight.Black);if(s.isNotBlank())Text(s,color=XM,fontSize=8.sp)}}
@Composable private fun Pill(t:String,c:Color){Box(Modifier.clip(RoundedCornerShape(30.dp)).background(c.copy(.12f)).padding(horizontal=8.dp,vertical=4.dp)){Text(t,color=c,fontSize=7.sp,fontWeight=FontWeight.Black)}}
@Composable private fun Stat(t:String,v:String,s:String,c:Color,m:Modifier){Card(m,shape=RoundedCornerShape(15.dp),colors=CardDefaults.cardColors(containerColor=XP)){Column(Modifier.padding(9.dp)){Text(t,color=XM,fontSize=7.sp);Text(v,color=XT,fontSize=10.sp,fontWeight=FontWeight.Black);Text(s,color=c,fontSize=7.sp)}}}
@Composable private fun Market(q:XQuote,on:()->Unit){Card(onClick=on,Modifier.fillMaxWidth(),shape=RoundedCornerShape(16.dp),colors=CardDefaults.cardColors(containerColor=XP)){Row(Modifier.padding(11.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(39.dp).clip(RoundedCornerShape(12.dp)).background(Brush.linearGradient(listOf(XV.copy(.2f),XC.copy(.1f)))),contentAlignment=Alignment.Center){Text(q.symbol.take(1),color=XT,fontWeight=FontWeight.Black)};Spacer(Modifier.width(9.dp));Column(Modifier.weight(1f)){Text(q.symbol.removeSuffix("USDT"),color=XT,fontWeight=FontWeight.Bold);Text("/ USDT • ${compact(q.volume)} vol",color=XM,fontSize=7.sp)};Column(horizontalAlignment=Alignment.End){Text(money(q.price),color=XT,fontSize=9.sp,fontWeight=FontWeight.Bold);Text(pct(q.change),color=if(q.change>=0)XG else XR,fontSize=9.sp)}}}}
@Composable private fun Radar(t:String,c:Color,m:Modifier,on:()->Unit){Card(onClick=on,modifier=m,shape=RoundedCornerShape(14.dp),colors=CardDefaults.cardColors(containerColor=XP2)){Column(Modifier.padding(9.dp)){Pill("•",c);Spacer(Modifier.height(4.dp));Text(t,color=XT,fontSize=8.sp,fontWeight=FontWeight.Bold)}}}
@Composable private fun Metric(t:String,v:String){Column(Modifier.width(70.dp)){Text(t,color=XM,fontSize=7.sp);Text(v,color=XT,fontSize=9.sp,fontWeight=FontWeight.Black)}}
@Composable private fun Ohlc(c:Candle){Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(14.dp),colors=CardDefaults.cardColors(containerColor=XP)){Row(Modifier.fillMaxWidth().padding(9.dp),horizontalArrangement=Arrangement.SpaceEvenly){Text("O ${money(c.open)}",color=XT,fontSize=8.sp);Text("H ${money(c.high)}",color=XG,fontSize=8.sp);Text("L ${money(c.low)}",color=XR,fontSize=8.sp);Text("C ${money(c.close)}",color=XT,fontSize=8.sp);Text("V ${compact(c.volume)}",color=XM,fontSize=8.sp)}}}
@Composable private fun Gauge(score:Int){Canvas(Modifier.size(125.dp)){for(i in 1..3)drawCircle(Color(0xFF1B2B3D),size.minDimension*.34f*i/3f,center,style=androidx.compose.ui.graphics.drawscope.Stroke(1f));drawCircle(if(score>=70)XR else XV,size.minDimension*.18f,center);drawCircle(XC,size.minDimension*.34f,center,style=androidx.compose.ui.graphics.drawscope.Stroke(2f))}}
@Composable private fun Spark(v:List<Double>,m:Modifier){Canvas(m){if(v.size<2)return@Canvas;val lo=v.minOrNull()?:0.0;val hi=v.maxOrNull()?:1.0;val range=(hi-lo).takeIf{it>0}?:1.0;var last=Offset(0f,size.height);v.forEachIndexed{i,x->val p=Offset(size.width*i/(v.lastIndex.toFloat()),size.height-((x-lo)/range*size.height).toFloat());if(i>0)drawLine(XC,last,p,2f);last=p}}}
