package com.notash.cryptobacktester.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.notash.cryptobacktester.ai.TradeAiAnalyzer
import com.notash.cryptobacktester.core.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private val BG=Color(0xFF05070D); private val PANEL=Color(0xFF0B1020); private val PANEL2=Color(0xFF121A2D)
private val PURPLE=Color(0xFF8B5CF6); private val CYAN=Color(0xFF22D3EE); private val GREEN=Color(0xFF20E6A5)
private val RED=Color(0xFFFF5577); private val TEXT=Color(0xFFF5F7FF); private val MUTED=Color(0xFF8490A7)
private data class Nav(val id:Int,val en:String,val fa:String)
private val NAV=listOf(Nav(0,"Overview","نمای کلی"),Nav(1,"Chart","نمودار"),Nav(2,"Backtest","بک‌تست"),Nav(3,"Analysis","تحلیل"),Nav(4,"Trades","معاملات"),Nav(5,"Top 10","۱۰ ارز برتر"),Nav(6,"Settings","تنظیمات"))

@Composable fun HannahTerminal(){
    val vm:BacktestViewModel=viewModel(); val s by vm.state.collectAsState()
    var page by remember{mutableIntStateOf(0)}; var fa by remember{mutableStateOf(false)}
    var market by remember{mutableStateOf(s.market)}; var tf by remember{mutableStateOf(s.timeframe)}
    CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides if(fa)LayoutDirection.Rtl else LayoutDirection.Ltr){
        MaterialTheme(colorScheme=darkColorScheme(primary=PURPLE,secondary=CYAN,background=BG,surface=PANEL)){
            Scaffold(containerColor=BG,topBar={HannahTopBar(page,fa,{page=it},{fa=it})}){p->
                Column(Modifier.fillMaxSize().padding(p)){AnimatedContent(page,label="page"){when(it){
                    0->Home(s,fa);1->ChartScreen(s.report,fa);2->BacktestScreen(s,vm,market,tf,fa,{market=it},{tf=it});3->AnalysisScreen(s.report,fa)
                    4->TradesScreen(s.report,fa);5->TopMarketsScreen(s,vm,fa){market=it;page=2};else->SettingsScreen(fa)
                }}}
            }
        }
    }
}

@Composable private fun HannahTopBar(selected:Int,fa:Boolean,go:(Int)->Unit,setFa:(Boolean)->Unit){
    var menu by remember{mutableStateOf(false)}; var lang by remember{mutableStateOf(false)}
    Surface(color=PANEL){Column{
        Row(Modifier.fillMaxWidth().padding(horizontal=10.dp,vertical=7.dp),verticalAlignment=Alignment.CenterVertically){
            Text("☰",color=CYAN,fontSize=22.sp);Spacer(Modifier.width(8.dp));Column(Modifier.weight(1f)){Text("HANNAH",color=TEXT,fontSize=20.sp,fontWeight=FontWeight.Black);Text("FUTURES INTELLIGENCE",color=CYAN,fontSize=8.sp,fontWeight=FontWeight.Bold)}
            TextButton({lang=true}){Text(if(fa)"EN"else"FA",color=CYAN,fontWeight=FontWeight.Bold)};TextButton({menu=true}){Text(if(fa)"منو"else"MENU",color=TEXT,fontWeight=FontWeight.Bold)}
            DropdownMenu(lang,{lang=false}){DropdownMenuItem({Text("English")},{setFa(false);lang=false});DropdownMenuItem({Text("فارسی")},{setFa(true);lang=false})}
            DropdownMenu(menu,{menu=false}){NAV.forEach{n->DropdownMenuItem({Text(if(fa)n.fa else n.en)},{go(n.id);menu=false})}}
        }
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(2.dp)){NAV.take(5).forEach{n->TextButton({go(n.id)}){Text(if(fa)n.fa else n.en,color=if(selected==n.id)CYAN else MUTED,fontSize=9.sp,fontWeight=FontWeight.Bold)}}}
    }}
}

@Composable private fun Home(s:BacktestUiState,fa:Boolean){LazyColumn(Modifier.fillMaxSize().padding(12.dp),verticalArrangement=Arrangement.spacedBy(9.dp)){
    item{Head(if(fa)"نمای کلی بازار"else"MARKET OVERVIEW",s.market,if(fa)"CoinEx Futures • موتور کریپتویی"else"CoinEx Futures • Crypto Native")};item{Metrics(s.report,fa)}
    item{Panel{Label(if(fa)"منحنی سرمایه"else"EQUITY CURVE");if((s.report?.equityCurve?.size?:0)>1)Equity(s.report!!.equityCurve)else Empty(if(fa)"بک‌تست را اجرا کنید"else"Run a backtest to generate the curve")}}
    item{Panel{Label(if(fa)"هسته استراتژی"else"STRATEGY CORE");Line("HTF","LWMA 20 / 50");Line("ENTRY","LTF LWMA + 0.5 ATR");Line("RISK","1.0%");Line("EXIT","SL 1.5 ATR • TP 3 ATR");Line("FUNDING",if(fa)"فعال"else"Enabled")}}
}}

@Composable private fun ChartScreen(r:BacktestReport?,fa:Boolean){LazyColumn(Modifier.fillMaxSize().padding(8.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
    item{Head(if(fa)"نمودار حرفه‌ای"else"PRO CHART","OHLC • ${r?.candles?.size?:0} candles",if(fa)"کندل واقعی + ورود/خروج لانگ و شورت"else"Real candles + Long/Short entries and exits")}
    item{Panel{if(r?.candles?.isNotEmpty()==true)CandleChart(r.candles,r.trades,fa)else Empty(if(fa)"بعد از بک‌تست کندل‌ها اینجا می‌آیند"else"Run a backtest to load real candles",330)}}
    item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly){Text("● LONG",color=GREEN,fontSize=10.sp);Text("● SHORT",color=RED,fontSize=10.sp);Text("○ EXIT",color=TEXT,fontSize=10.sp)}}
    item{Panel{Label(if(fa)"راهنما"else"READ THE CHART");Text(if(fa)"کندل سبز صعودی و قرمز نزولی است. دایره سبز/قرمز محل باز شدن لانگ/شورت و دایره توخالی محل بسته شدن معامله است. خط نقطه‌چین Entry تا Exit را وصل می‌کند."else"Green candles are bullish, red bearish. Green/red circles mark Long/Short entries; hollow circles mark exits and the dashed line connects Entry to Exit.",color=MUTED,fontSize=11.sp)}}
}}

@Composable private fun CandleChart(candles:List<Candle>,trades:List<TradeResult>,fa:Boolean){var count by remember(candles.size){mutableIntStateOf(min(75,candles.size))};var start by remember(candles.size){mutableIntStateOf(max(0,candles.size-count))};val from=start.coerceIn(0,max(0,candles.size-1));val shown=candles.subList(from,min(candles.size,from+count.coerceAtLeast(5)))
    Column{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(if(fa)"کندل‌ها"else"CANDLES",color=MUTED,fontSize=9.sp);Row{TextButton({count=min(candles.size,count+15);start=max(0,candles.size-count)}){Text("+",color=CYAN)};TextButton({count=max(25,count-15);start=max(0,candles.size-count)}){Text("−",color=CYAN)}}}
    Box(Modifier.fillMaxWidth().height(315.dp).clip(RoundedCornerShape(12.dp)).background(PANEL2).pointerInput(candles.size,count){detectDragGestures{_,d->start=(start+(-d.x/12f).toInt()).coerceIn(0,max(0,candles.size-count))}}){Canvas(Modifier.fillMaxSize().padding(7.dp)){
        if(shown.size<2)return@Canvas;val low=shown.minOf{it.low};val high=shown.maxOf{it.high};val range=(high-low).takeIf{it>0}?:1.0;val gap=size.width/shown.size;val body=max(2f,gap*.55f);fun y(v:Double)=size.height-((v-low)/range).toFloat()*size.height
        shown.forEachIndexed{i,c->{val x=i*gap+gap/2;val col=if(c.close>=c.open)GREEN else RED;drawLine(col,Offset(x,y(c.high)),Offset(x,y(c.low)),1.5f);drawRect(col,Offset(x-body/2,y(max(c.open,c.close))),Size(body,max(2f,y(min(c.open,c.close))-y(max(c.open,c.close)))))}
        trades.forEach{t->{val ei=near(shown,t.entryTime);val xi=near(shown,t.exitTime);if(ei>=0){val x=ei*gap+gap/2;val y0=y(t.entryPrice);val col=if(t.side==Side.LONG)GREEN else RED;drawCircle(col,6f,Offset(x,y0));drawCircle(PANEL2,3f,Offset(x,y0));if(xi>=0){val xx=xi*gap+gap/2;val yy=y(t.exitPrice);drawLine(col.copy(alpha=.7f),Offset(x,y0),Offset(xx,yy),2f,pathEffect=PathEffect.dashPathEffect(floatArrayOf(7f,6f)));drawCircle(col,7f,Offset(xx,yy),style=Stroke(2f))}}}
    }};Text(if(fa)"Drag = جابه‌جایی • +/− = زوم"else"Drag = pan • +/− = zoom",color=MUTED,fontSize=9.sp)}
}
private fun near(c:List<Candle>,t:Long):Int{var b=-1;var d=Long.MAX_VALUE;c.forEachIndexed{i,x->{val n=abs(x.timestamp-t);if(n<d){d=n;b=i}}};return b}

@Composable private fun BacktestScreen(s:BacktestUiState,vm:BacktestViewModel,market:String,tf:String,fa:Boolean,setMarket:(String)->Unit,setTf:(String)->Unit){LazyColumn(Modifier.fillMaxSize().padding(12.dp),verticalArrangement=Arrangement.spacedBy(9.dp)){
    item{Head(if(fa)"آزمایش بک‌تست"else"BACKTEST LAB",market,if(fa)"داده تاریخی واقعی CoinEx"else"Real CoinEx historical data")};item{Panel{Label(if(fa)"بازار و تایم‌فریم"else"MARKET & TIMEFRAME");OutlinedTextField(market,setMarket,Modifier.fillMaxWidth(),singleLine=true,label={Text(if(fa)"نماد"else"Symbol")});Spacer(Modifier.height(6.dp));Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(4.dp)){listOf("5m","15m","30m","1h","4h","1d").forEach{x->FilterChip(tf==x,{setTf(x);vm.setTimeframe(x)},label={Text(x)})}}}};item{Panel{Label(if(fa)"پارامترها"else"ENGINE PARAMETERS");Line("INITIAL","1,000 USDT");Line("RISK","1.00%");Line("LEVERAGE","3x");Line("LWMA","20 / 50");Line("ATR","14");Line("SL / TP","1.5 / 3.0 ATR");Line("FUNDING","ON")}};item{Button({vm.setMarket(market);vm.runBacktest()},enabled=!s.isRunning,modifier=Modifier.fillMaxWidth().height(55.dp),shape=RoundedCornerShape(15.dp),colors=ButtonDefaults.buttonColors(containerColor=PURPLE)){if(s.isRunning)CircularProgressIndicator(Modifier.size(20.dp),color=TEXT,strokeWidth=2.dp)else Text(if(fa)"اجرای بک‌تست"else"RUN BACKTEST",fontWeight=FontWeight.Black)}};s.error?.let{item{Panel{Label("ERROR");Text(it,color=RED,fontSize=11.sp)}}};s.report?.let{item{Result(it,fa)}}
}}

@Composable private fun AnalysisScreen(r:BacktestReport?,fa:Boolean){val a=r?.let{TradeAiAnalyzer.analyze(it)};LazyColumn(Modifier.fillMaxSize().padding(12.dp),verticalArrangement=Arrangement.spacedBy(9.dp)){
    item{Head(if(fa)"تحلیل استراتژی"else"AI STRATEGY ANALYSIS","Offline-first",if(fa)"موتور تحلیل نسخه‌های قبلی بازگردانده شده"else"Restored previous diagnosis engine")};if(a==null)item{Panel{Text(if(fa)"ابتدا بک‌تست اجرا کنید."else"Run a backtest first.",color=MUTED)}};a?.let{x->item{Panel{Label(if(fa)"خلاصه"else"SUMMARY");Text(x.strategy.summary,color=TEXT,fontSize=12.sp)}};item{Bullets(if(fa)"نقاط قوت"else"STRENGTHS",x.strategy.strengths,GREEN)};item{Bullets(if(fa)"نقاط ضعف"else"WEAKNESSES",x.strategy.weaknesses,RED)};item{Bullets(if(fa)"پیشنهادها"else"RECOMMENDATIONS",x.strategy.recommendations,CYAN)};item{Label(if(fa)"تشخیص معاملات"else"TRADE DIAGNOSIS")};items(x.trades){d->Panel{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("#${d.index} ${d.outcome}",color=if(d.outcome=="WIN")GREEN else RED,fontWeight=FontWeight.Bold);Text(d.severity,color=MUTED,fontSize=9.sp)};Text(d.primaryCause,color=TEXT,fontSize=11.sp,modifier=Modifier.padding(top=5.dp));d.evidence.forEach{Text("• $it",color=MUTED,fontSize=10.sp)};Text(d.recommendation,color=CYAN,fontSize=10.sp)}}}}
}}
@Composable private fun Bullets(t:String,x:List<String>,c:Color){Panel{Label(t);x.forEach{Text("• $it",color=c,fontSize=11.sp,modifier=Modifier.padding(top=5.dp))}}}

@Composable private fun TradesScreen(r:BacktestReport?,fa:Boolean){val ts=r?.trades.orEmpty().reversed();LazyColumn(Modifier.fillMaxSize().padding(12.dp),verticalArrangement=Arrangement.spacedBy(7.dp)){item{Head(if(fa)"معاملات"else"TRADE TAPE","${ts.size}",if(fa)"پوزیشن‌های اجراشده"else"Executed positions")};if(ts.isEmpty())item{Panel{Text(if(fa)"معامله‌ای نیست."else"No trades yet.",color=MUTED)}};items(ts){t->Panel{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(if(t.side==Side.LONG)"LONG"else"SHORT",color=if(t.side==Side.LONG)GREEN else RED,fontWeight=FontWeight.Black);Text(money(t.netPnl),color=if(t.netPnl>=0)GREEN else RED,fontWeight=FontWeight.Bold)};Text("${price(t.entryPrice)} → ${price(t.exitPrice)}",color=TEXT,fontSize=12.sp);Text("Qty ${price(t.quantity)} • Fee ${money(t.fees)} • Funding ${money(t.funding)}",color=MUTED,fontSize=9.sp)}}}}

@Composable private fun TopMarketsScreen(s:BacktestUiState,vm:BacktestViewModel,fa:Boolean,select:(String)->Unit){LazyColumn(Modifier.fillMaxSize().padding(12.dp),verticalArrangement=Arrangement.spacedBy(7.dp)){item{Head(if(fa)"۱۰ ارز برتر"else"TOP 10 FUTURES","CoinEx","24h value ranking")};item{Button({vm.loadTopMarkets()},enabled=!s.isLoadingMarkets,modifier=Modifier.fillMaxWidth()){Text(if(s.isLoadingMarkets)"Loading…"else if(fa)"به‌روزرسانی"else"REFRESH TOP 10")}};if(s.topMarkets.isEmpty()&&!s.isLoadingMarkets)item{Panel{Text(if(fa)"داده‌ای دریافت نشد؛ اتصال اینترنت و API را بررسی کنید."else"No market data returned.",color=MUTED)}};items(s.topMarkets){m->Panel{Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(m.market,color=TEXT,fontWeight=FontWeight.Bold);Text("24h value ${money(m.value24h)}",color=MUTED,fontSize=9.sp)};Column(horizontalAlignment=Alignment.End){Text(price(m.last),color=TEXT,fontSize=11.sp);Text("${if(m.change24h>=0)"+"else"}${"%.2f".format(m.change24h)}%",color=if(m.change24h>=0)GREEN else RED,fontWeight=FontWeight.Bold,fontSize=10.sp)};TextButton({select(m.market)}){Text(if(fa)"انتخاب"else"SELECT",color=CYAN,fontSize=9.sp)}}}}}

@Composable private fun SettingsScreen(fa:Boolean){LazyColumn(Modifier.fillMaxSize().padding(12.dp),verticalArrangement=Arrangement.spacedBy(9.dp)){item{Head(if(fa)"تنظیمات"else"SETTINGS","HANNAH",if(fa)"ترمینال حرفه‌ای بک‌تست فیوچرز"else"Professional futures backtesting terminal")};item{Panel{Label("HANNAH");Line("EXCHANGE","CoinEx Futures");Line("DATA",if(fa)"بدون محدودیت مصنوعی"else"No artificial size cap");Line("LANGUAGE","English / فارسی");Line("CHART","OHLC + trade markers");Line("ANALYSIS","AI Strategy Diagnosis")}}}}

@Composable private fun Metrics(r:BacktestReport?,fa:Boolean){Column(verticalArrangement=Arrangement.spacedBy(6.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)){Stat(if(fa)"سرمایه"else"EQUITY",r?.let{money(it.finalBalance)}?:"—",0,Modifier.weight(1f));Stat(if(fa)"سود"else"PNL",r?.let{money(it.netPnl)}?:"—",if(r!=null&&r.netPnl<0)2 else 1,Modifier.weight(1f))};Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)){Stat("ROI",r?.let{"%.2f%%".format(it.roiPercent)}?:"—",if(r!=null&&r.roiPercent<0)2 else 1,Modifier.weight(1f));Stat(if(fa)"افت"else"MAX DD",r?.let{"%.2f%%".format(it.maxDrawdownPercent)}?:"—",2,Modifier.weight(1f))}}
}
@Composable private fun Stat(a:String,b:String,t:Int,m:Modifier){Card(m,colors=CardDefaults.cardColors(containerColor=PANEL),shape=RoundedCornerShape(14.dp)){Column(Modifier.padding(11.dp)){Text(a,color=MUTED,fontSize=8.sp,fontWeight=FontWeight.Bold);Text(b,color=if(t==1)GREEN else if(t==2)RED else TEXT,fontSize=16.sp,fontWeight=FontWeight.Bold)}}}
@Composable private fun Result(r:BacktestReport,fa:Boolean){Panel{Label(if(fa)"نتیجه"else"RESULT");Line("FINAL",money(r.finalBalance));Line("PNL",money(r.netPnl));Line("WIN RATE","%.1f%%".format(r.winRatePercent));Line("MAX DD","%.2f%%".format(r.maxDrawdownPercent));Line("PROFIT FACTOR",if(r.profitFactor.isInfinite())"∞"else"%.2f".format(r.profitFactor));Line("FEES",money(r.totalFees));Line("FUNDING",money(r.totalFunding))}}
@Composable private fun Equity(v:List<Double>){Canvas(Modifier.fillMaxWidth().height(170.dp).clip(RoundedCornerShape(11.dp)).background(PANEL2).padding(7.dp)){if(v.size<2)return@Canvas;val lo=v.minOrNull()?:return@Canvas;val hi=v.maxOrNull()?:return@Canvas;val range=(hi-lo).takeIf{it>0}?:1.0;val p=androidx.compose.ui.graphics.Path();v.forEachIndexed{i,x->{val xx=i.toFloat()/v.lastIndex*size.width;val yy=size.height-((x-lo)/range).toFloat()*size.height;if(i==0)p.moveTo(xx,yy)else p.lineTo(xx,yy)};drawPath(p,CYAN,style=Stroke(3f,cap=StrokeCap.Round))}}
@Composable private fun Panel(content:@Composable ColumnScope.()->Unit){Card(colors=CardDefaults.cardColors(containerColor=PANEL),shape=RoundedCornerShape(16.dp)){Column(Modifier.fillMaxWidth().padding(13.dp),content=content)}}
@Composable private fun Label(t:String){Text(t,color=MUTED,fontSize=9.sp,fontWeight=FontWeight.Bold,letterSpacing=1.sp)}
@Composable private fun Line(a:String,b:String){Row(Modifier.fillMaxWidth().padding(vertical=4.dp),horizontalArrangement=Arrangement.SpaceBetween){Text(a,color=MUTED,fontSize=10.sp);Text(b,color=TEXT,fontSize=10.sp,fontWeight=FontWeight.SemiBold)}}
@Composable private fun Head(t:String,m:String,s:String){Column(Modifier.padding(vertical=3.dp)){Text(t,color=CYAN,fontSize=9.sp,fontWeight=FontWeight.Bold);Text(m,color=TEXT,fontSize=25.sp,fontWeight=FontWeight.Black);Text(s,color=MUTED,fontSize=10.sp)}}
@Composable private fun Empty(t:String,h:Int=170){Box(Modifier.fillMaxWidth().height(h.dp).clip(RoundedCornerShape(11.dp)).background(PANEL2),contentAlignment=Alignment.Center){Text(t,color=MUTED,fontSize=10.sp,textAlign=TextAlign.Center,modifier=Modifier.padding(18.dp))}}
private fun money(v:Double)=if(v.isFinite())"%,.2f".format(v)else"∞";private fun price(v:Double)=if(v.isFinite())"%,.4f".format(v)else"∞"
