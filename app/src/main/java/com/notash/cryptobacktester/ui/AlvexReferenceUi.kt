package com.notash.cryptobacktester.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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

private val Bg = Color(0xFF050A12)
private val Panel = Color(0xFF0C1521)
private val Panel2 = Color(0xFF101C2B)
private val Purple = Color(0xFF7C3AED)
private val PurpleDark = Color(0xFF24134F)
private val Green = Color(0xFF22D39B)
private val Red = Color(0xFFFF4D67)
private val Gold = Color(0xFFFFC857)
private val TextPrimary = Color(0xFFF4F7FB)
private val Muted = Color(0xFF8290A5)
private data class Q(val symbol:String,val price:Double,val change:Double,val volume:Double)

@Composable
fun AlvexReferenceUi() {
    var tab by rememberSaveable { mutableStateOf(RefTab.HOME) }
    var terminal by rememberSaveable { mutableStateOf(false) }
    if (terminal) {
        Box(Modifier.fillMaxSize().background(Bg)) {
            ProfessionalTerminal()
            SmallFloatingActionButton(onClick={terminal=false}, modifier=Modifier.padding(14.dp).align(Alignment.TopStart), containerColor=Purple) { Icon(Icons.Outlined.ArrowBack,"Back",tint=Color.White) }
        }
        return
    }
    MaterialTheme(colorScheme=darkColorScheme(primary=Purple,secondary=Green,background=Bg,surface=Panel)) {
        Scaffold(containerColor=Bg,topBar={TopBar(tab)},bottomBar={BottomBar(tab){tab=it}}){p->
            Box(Modifier.fillMaxSize().padding(p)) {
                when(tab){
                    RefTab.HOME -> Home({terminal=true},{tab=RefTab.RADAR})
                    RefTab.RADAR -> Radar()
                    RefTab.MARKETS -> Markets({terminal=true})
                    RefTab.WORKSPACE -> Workspace({terminal=true})
                    RefTab.MORE -> More()
                }
            }
        }
    }
}

private enum class RefTab(val en:String,val fa:String,val icon:ImageVector){
    HOME("Home","خانه",Icons.Outlined.Home), RADAR("AI Radar","رادار",Icons.Outlined.AutoGraph),
    MARKETS("Markets","بازار",Icons.Outlined.ShowChart), WORKSPACE("Workspace","پروژه‌ها",Icons.Outlined.Dashboard),
    MORE("More","بیشتر",Icons.Outlined.List)
}

@Composable private fun TopBar(tab:RefTab){
    Row(Modifier.fillMaxWidth().background(Bg).padding(14.dp),verticalAlignment=Alignment.CenterVertically){
        Mark(36); Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)){Text("ALVEX",color=TextPrimary,fontSize=19.sp,fontWeight=FontWeight.Black,letterSpacing=1.5.sp);Text(tab.en,color=Muted,fontSize=9.sp)}
        IconButton(onClick={}){Icon(Icons.Outlined.Star,"AI",tint=Green)}; IconButton(onClick={}){Icon(Icons.Outlined.Settings,"Settings",tint=TextPrimary)}
    }
}
@Composable private fun BottomBar(selected:RefTab,onSelect:(RefTab)->Unit){NavigationBar(containerColor=Color(0xFF07101B)){RefTab.values().forEach{t->NavigationBarItem(selected==t,{onSelect(t)},{Icon(t.icon,null)},{Text(t.fa,fontSize=9.sp)},colors=NavigationBarItemDefaults.colors(selectedIconColor=Color.White,selectedTextColor=Green,indicatorColor=PurpleDark,unselectedIconColor=Muted,unselectedTextColor=Muted))}}}

@Composable private fun Home(openTerminal:()->Unit,openRadar:()->Unit){
    val repo=remember{CoinExRepository()}; val scope=rememberCoroutineScope(); val symbols=remember{listOf("BTCUSDT","ETHUSDT","SOLUSDT","BNBUSDT","XRPUSDT","DOGEUSDT","PEPEUSDT")}; var quotes by remember{mutableStateOf(emptyList<Q>())}; var loading by remember{mutableStateOf(true)}
    fun load(){scope.launch{loading=true;quotes=withContext(Dispatchers.IO){symbols.mapNotNull{s->runCatching{repo.loadLatestTicker(s)}.getOrNull()?.let{Q(s,it.last,it.changeRate*100,it.volume)}}};loading=false}}
    LaunchedEffect(Unit){load()};LaunchedEffect(Unit){while(true){delay(60000);load()}}
    LazyColumn(Modifier.fillMaxSize().background(Bg).padding(horizontal=14.dp),verticalArrangement=Arrangement.spacedBy(12.dp),contentPadding=PaddingValues(top=5.dp,bottom=20.dp)){
        item{Card(shape=RoundedCornerShape(26.dp),colors=CardDefaults.cardColors(Panel)){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Row(verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text("Market Command Center",color=Muted,fontSize=10.sp);Text("AI Market Intelligence",color=TextPrimary,fontSize=23.sp,fontWeight=FontWeight.Black)};Mark(52)};Card(shape=RoundedCornerShape(18.dp),colors=CardDefaults.cardColors(PurpleDark)){Row(Modifier.padding(13.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text("BTC Dominance",color=Muted,fontSize=9.sp);Text("Live market pulse",color=TextPrimary,fontWeight=FontWeight.Bold)};Text("LIVE",color=Green,fontWeight=FontWeight.Black,fontSize=10.sp)}};Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Stat("24h Volume",quotes.firstOrNull()?.volume?.let(::compact)?:"—",Modifier.weight(1f));Stat("Assets",quotes.size.toString(),Modifier.weight(1f));Stat("Radar",if(loading)"…"else"READY",Modifier.weight(1f))}}}}
        item{Header("Top Movers","Live market data")};items(quotes.take(3)){MarketRow(it)}
        item{Header("AI Market Radar","Explainable opportunity & risk")};item{Action(Icons.Outlined.AutoGraph,"AI Market Radar","Pump potential • Dump risk • Confidence",openRadar)}
        item{Header("Professional Terminal","Backtest • Curve • Trades • Strategy")};item{Action(Icons.Outlined.Build,"Trading & Backtest Terminal","Chart, entries/exits, equity curve and diagnostics",openTerminal)}
        item{Header("Quick Intelligence","Built into ALVEX")};item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Mini(Icons.Outlined.Token,"Tokenomics");Mini(Icons.Outlined.Bolt,"Pump/Dump");Mini(Icons.Outlined.Star,"AI Hub")}}
    }
}

@Composable private fun Radar(){
    val repo=remember{CoinExRepository()};val engine=remember{AiRadarEngine()};val scope=rememberCoroutineScope();var signals by remember{mutableStateOf(emptyList<com.notash.cryptobacktester.intelligence.RadarSignal>())};var status by remember{mutableStateOf("Scanning public market data…")};val symbols=remember{listOf("BTCUSDT","ETHUSDT","SOLUSDT","BNBUSDT","XRPUSDT","DOGEUSDT","PEPEUSDT")}
    fun scan(){scope.launch{status="Scanning…";val data=withContext(Dispatchers.IO){symbols.mapNotNull{s->runCatching{repo.loadLatestTicker(s)}.getOrNull()?.let{t->val open=if(abs(t.changeRate)<.999)t.last/(1+t.changeRate) else t.last;RadarMarketSnapshot("CoinEx",s,t.last,open,t.last,t.last,t.volume,t.volume,timestampMs=t.timestamp)}}};signals=engine.score(data);status=if(signals.isEmpty())"No market data available" else "LIVE • ${signals.size} assets scored"}}
    LaunchedEffect(Unit){scan()};LaunchedEffect(Unit){while(true){delay(60000);scan()}}
    LazyColumn(Modifier.fillMaxSize().background(Bg).padding(14.dp),verticalArrangement=Arrangement.spacedBy(12.dp),contentPadding=PaddingValues(bottom=20.dp)){
        item{Card(shape=RoundedCornerShape(26.dp),colors=CardDefaults.cardColors(Panel)){Column(Modifier.padding(16.dp)){Row(verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text("AI Market Radar",color=TextPrimary,fontSize=23.sp,fontWeight=FontWeight.Black);Text(status,color=Muted,fontSize=9.sp)};IconButton(onClick={scan()}){Icon(Icons.Outlined.Refresh,null,tint=Green)}};RadarOrb();Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Badge("PUMP",signals.maxOfOrNull{it.pumpPotential}?.toString()?:"—",Green);Badge("DUMP",signals.maxOfOrNull{it.dumpRisk}?.toString()?:"—",Red);Badge("CONF",signals.maxOfOrNull{it.confidence}?.toString()?:"—",Gold)}}}}
        item{Header("Ranked Signals","Real market data • explainable scoring")};items(signals.take(10)){SignalRow(it)}
    }
}

@Composable private fun Markets(openTerminal:()->Unit){val repo=remember{CoinExRepository()};val scope=rememberCoroutineScope();var qs by remember{mutableStateOf(emptyList<Q>())};fun load(){scope.launch{qs=withContext(Dispatchers.IO){listOf("BTCUSDT","ETHUSDT","BNBUSDT","SOLUSDT","XRPUSDT","DOGEUSDT","ADAUSDT","AVAXUSDT","LINKUSDT").mapNotNull{s->runCatching{repo.loadLatestTicker(s)}.getOrNull()?.let{Q(s,it.last,it.changeRate*100,it.volume)}}}}};LaunchedEffect(Unit){load()};Column(Modifier.fillMaxSize().background(Bg)){Row(Modifier.fillMaxWidth().padding(14.dp),verticalAlignment=Alignment.CenterVertically){Text("Markets",color=TextPrimary,fontSize=23.sp,fontWeight=FontWeight.Black,modifier=Modifier.weight(1f));IconButton({load()}){Icon(Icons.Outlined.Refresh,null,tint=Green)}};LazyColumn(Modifier.padding(horizontal=14.dp),verticalArrangement=Arrangement.spacedBy(7.dp)){items(qs){MarketRow(it)};item{Action(Icons.Outlined.ShowChart,"Open Trading Terminal","Candles • indicators • entries/exits • backtest",openTerminal)}}}}

@Composable private fun Workspace(openTerminal:()->Unit){LazyColumn(Modifier.fillMaxSize().background(Bg).padding(14.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){item{Text("Workspace",color=TextPrimary,fontSize=23.sp,fontWeight=FontWeight.Black)};item{Action(Icons.Outlined.Build,"Backtest Terminal","Curve, trades, metrics and robot diagnostics",openTerminal)};item{Action(Icons.Outlined.AutoAwesome,"AI Hub","AI analysis and strategy explanation",openTerminal)};item{Action(Icons.Outlined.List,"Strategy Lab","Strategy configuration and execution",openTerminal)};item{Action(Icons.Outlined.AutoGraph,"Coin Intelligence","Project, tokenomics and risk intelligence",openTerminal)}}}
@Composable private fun More(){LazyColumn(Modifier.fillMaxSize().background(Bg).padding(14.dp),verticalArrangement=Arrangement.spacedBy(9.dp)){item{Text("More",color=TextPrimary,fontSize=23.sp,fontWeight=FontWeight.Black)};item{MoreRow(Icons.Outlined.Security,"Account & Security")};item{MoreRow(Icons.Outlined.Notifications,"Notifications")};item{MoreRow(Icons.Outlined.Language,"Language","English • فارسی • العربية • Français • 中文")};item{MoreRow(Icons.Outlined.Security,"Privacy")};item{MoreRow(Icons.Outlined.SupportAgent,"Help & Support")};item{MoreRow(Icons.Outlined.Info,"About ALVEX","AI Market Intelligence")}}

@Composable private fun Header(title:String,sub:String){Column(Modifier.fillMaxWidth()){Text(title,color=TextPrimary,fontSize=14.sp,fontWeight=FontWeight.Bold);Text(sub,color=Muted,fontSize=8.sp)}}
@Composable private fun Stat(t:String,v:String,m:Modifier)=Card(m,shape=RoundedCornerShape(15.dp),colors=CardDefaults.cardColors(Panel2)){Column(Modifier.padding(10.dp)){Text(t,color=Muted,fontSize=8.sp);Text(v,color=TextPrimary,fontWeight=FontWeight.Black,fontSize=12.sp)}}
@Composable private fun Action(icon:ImageVector,title:String,sub:String,onClick:()->Unit)=Card(onClick=onClick,shape=RoundedCornerShape(19.dp),colors=CardDefaults.cardColors(Panel)){Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(43.dp).clip(RoundedCornerShape(14.dp)).background(PurpleDark),contentAlignment=Alignment.Center){Icon(icon,null,tint=Color.White)};Spacer(Modifier.width(11.dp));Column(Modifier.weight(1f)){Text(title,color=TextPrimary,fontWeight=FontWeight.Bold,fontSize=12.sp);Text(sub,color=Muted,fontSize=8.sp)};Icon(Icons.Outlined.Info,null,tint=Muted)}}
@Composable private fun Mini(icon:ImageVector,title:String)=Card(Modifier.weight(1f),shape=RoundedCornerShape(16.dp),colors=CardDefaults.cardColors(Panel)){Column(Modifier.padding(10.dp),horizontalAlignment=Alignment.CenterHorizontally){Icon(icon,null,tint=Green);Text(title,color=TextPrimary,fontSize=8.sp,fontWeight=FontWeight.Bold)}}
@Composable private fun MarketRow(q:Q)=Card(shape=RoundedCornerShape(17.dp),colors=CardDefaults.cardColors(Panel)){Row(Modifier.padding(12.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(38.dp).clip(CircleShape).background(Panel2),contentAlignment=Alignment.Center){Text(q.symbol.take(1),color=Green,fontWeight=FontWeight.Black)};Spacer(Modifier.width(10.dp));Column(Modifier.weight(1f)){Text(q.symbol.removeSuffix("USDT"),color=TextPrimary,fontWeight=FontWeight.Bold);Text("USDT • ${compact(q.volume)}",color=Muted,fontSize=8.sp)};Column(horizontalAlignment=Alignment.End){Text(price(q.price),color=TextPrimary,fontSize=11.sp,fontWeight=FontWeight.SemiBold);Text("%+.2f%%".format(q.change),color=if(q.change>=0)Green else Red,fontSize=9.sp)}}}
@Composable private fun SignalRow(s:com.notash.cryptobacktester.intelligence.RadarSignal){val bull=s.pumpPotential>=s.dumpRisk;Card(shape=RoundedCornerShape(18.dp),colors=CardDefaults.cardColors(Panel)){Column(Modifier.padding(13.dp)){Row(verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(if(bull)Green.copy(.12f)else Red.copy(.12f)),contentAlignment=Alignment.Center){Text(if(bull)"↑"else"↓",color=if(bull)Green else Red,fontSize=20.sp,fontWeight=FontWeight.Black)};Spacer(Modifier.width(10.dp));Column(Modifier.weight(1f)){Text(s.symbol.removeSuffix("USDT"),color=TextPrimary,fontWeight=FontWeight.Black);Text(s.exchanges.joinToString(" • "),color=Muted,fontSize=8.sp)};Text((if(bull)s.pumpPotential else s.dumpRisk).toString(),color=if(bull)Green else Red,fontSize=18.sp,fontWeight=FontWeight.Black)};Text(s.reasons.joinToString(" • "),color=Muted,fontSize=8.sp,modifier=Modifier.padding(top=7.dp))}}}
@Composable private fun Badge(t:String,v:String,c:Color)=Card(Modifier.weight(1f),shape=RoundedCornerShape(14.dp),colors=CardDefaults.cardColors(c.copy(.10f))){Column(Modifier.padding(9.dp)){Text(t,color=Muted,fontSize=8.sp);Text(v,color=c,fontSize=18.sp,fontWeight=FontWeight.Black)}}
@Composable private fun RadarOrb(){Box(Modifier.fillMaxWidth().height(72.dp),contentAlignment=Alignment.Center){Canvas(Modifier.size(70.dp)){drawCircle(Purple.copy(.15f),34f);drawCircle(Purple.copy(.22f),25f);drawCircle(Green.copy(.2f),15f);drawCircle(Green,5f);drawLine(Green.copy(.5f),Offset(0f,35f),Offset(70f,35f),1f);drawLine(Green.copy(.5f),Offset(35f,0f),Offset(35f,70f),1f)}}}
@Composable private fun MoreRow(icon:ImageVector,t:String,sub:String?=null)=Card(shape=RoundedCornerShape(17.dp),colors=CardDefaults.cardColors(Panel)){Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically){Icon(icon,null,tint=TextPrimary);Spacer(Modifier.width(11.dp));Column(Modifier.weight(1f)){Text(t,color=TextPrimary,fontWeight=FontWeight.SemiBold,fontSize=11.sp);sub?.let{Text(it,color=Muted,fontSize=8.sp)}};Icon(Icons.Outlined.Info,null,tint=Muted)}}
@Composable private fun Mark(s:Int){Box(Modifier.size(s.dp).clip(RoundedCornerShape((s/4).dp)).background(Brush.linearGradient(listOf(Purple,Color(0xFFB65CFF),Green))),contentAlignment=Alignment.Center){Text("A",color=Color.White,fontSize=(s*.48f).sp,fontWeight=FontWeight.Black)}}
private fun compact(v:Double)=when{v>=1e9->"%.1fB".format(v/1e9);v>=1e6->"%.1fM".format(v/1e6);v>=1e3->"%.1fK".format(v/1e3);else->"%.0f".format(v)}
private fun price(v:Double)=if(v>=1000)"%,.2f".format(v) else "%.6f".format(v)
