package com.notash.cryptobacktester.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notash.cryptobacktester.ai.TradeAiAnalyzer
import com.notash.cryptobacktester.core.BacktestReport
import com.notash.cryptobacktester.core.Candle
import com.notash.cryptobacktester.core.Side
import com.notash.cryptobacktester.core.TradeResult
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private val BG = Color(0xFF05070D)
private val PANEL = Color(0xFF0B1020)
private val PANEL2 = Color(0xFF121A2D)
private val PURPLE = Color(0xFF8B5CF6)
private val CYAN = Color(0xFF22D3EE)
private val GREEN = Color(0xFF20E6A5)
private val RED = Color(0xFFFF5577)
private val TEXT = Color(0xFFF5F7FF)
private val MUTED = Color(0xFF8490A7)
private data class Nav(val id: Int, val en: String, val fa: String)
private val NAV = listOf(Nav(0,"Overview","نمای کلی"),Nav(1,"Chart","نمودار"),Nav(2,"Backtest","بک‌تست"),Nav(3,"Analysis","تحلیل"),Nav(4,"Trades","معاملات"),Nav(5,"Top 10","۱۰ ارز برتر"),Nav(6,"Settings","تنظیمات"))

@Composable
fun HannahTerminal() {
    val vm: BacktestViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val state by vm.state.collectAsState()
    var page by remember { mutableIntStateOf(0) }
    var fa by remember { mutableStateOf(false) }
    var market by remember { mutableStateOf(state.market) }
    var timeframe by remember { mutableStateOf(state.timeframe) }
    MaterialTheme(colorScheme = darkColorScheme(primary=PURPLE,secondary=CYAN,background=BG,surface=PANEL)) {
        Column(Modifier.fillMaxSize().background(BG)) {
            Header(page,fa,{page=it},{fa=!fa})
            when(page) {
                0 -> Overview(state,fa); 1 -> ChartPage(state.report,fa)
                2 -> BacktestPage(state,vm,market,timeframe,fa,{market=it},{timeframe=it})
                3 -> AnalysisPage(state.report,fa); 4 -> TradesPage(state.report,fa)
                5 -> TopMarketsPage(state,vm,fa){market=it;page=2}; else -> SettingsPage(fa)
            }
        }
    }
}

@Composable private fun Header(selected:Int,fa:Boolean,go:(Int)->Unit,toggle:()->Unit){Surface(color=PANEL){Column{Row(Modifier.fillMaxWidth().padding(10.dp),verticalAlignment=Alignment.CenterVertically){Text("HANNAH",color=TEXT,fontSize=20.sp,fontWeight=FontWeight.Black);Spacer(Modifier.width(10.dp));Text("FUTURES INTELLIGENCE",color=CYAN,fontSize=8.sp,fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f));TextButton(toggle){Text(if(fa)"EN"else"FA",color=CYAN,fontWeight=FontWeight.Bold)}};Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())){NAV.forEach{n->TextButton({go(n.id)}){Text(if(fa)n.fa else n.en,color=if(selected==n.id)CYAN else MUTED,fontSize=10.sp,fontWeight=FontWeight.Bold)}}}}}}

@Composable private fun Overview(s:BacktestUiState,fa:Boolean){ScrollColumn{Head(if(fa)"نمای کلی بازار"else"MARKET OVERVIEW",s.market,if(fa)"CoinEx Futures • موتور کریپتویی"else"CoinEx Futures • Crypto Native");Metrics(s.report,fa);Panel{Label(if(fa)"منحنی سرمایه"else"EQUITY CURVE");if((s.report?.equityCurve?.size?:0)>1)Equity(s.report!!.equityCurve)else Empty(if(fa)"بک‌تست را اجرا کنید"else"Run a backtest to generate the curve")};Panel{Label(if(fa)"هسته استراتژی"else"STRATEGY CORE");Line("HTF","LWMA 20 / 50");Line("ENTRY","LTF LWMA + 0.5 ATR");Line("RISK","1.0%");Line("EXIT","SL 1.5 ATR • TP 3 ATR");Line("FUNDING",if(fa)"فعال"else"Enabled")}}}

@Composable private fun ChartPage(r:BacktestReport?,fa:Boolean){ScrollColumn{Head(if(fa)"نمودار حرفه‌ای"else"PRO CHART","OHLC • ${r?.candles?.size?:0} candles",if(fa)"کندل واقعی + ورود/خروج لانگ و شورت"else"Real candles + Long/Short entries and exits");Panel{if(r?.candles?.isNotEmpty()==true)CandleChart(r.candles,r.trades,fa)else Empty(if(fa)"بعد از بک‌تست کندل‌ها اینجا می‌آیند"else"Run a backtest to load real candles",330)};Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly){Text("● LONG",color=GREEN,fontSize=10.sp);Text("● SHORT",color=RED,fontSize=10.sp);Text("○ EXIT",color=TEXT,fontSize=10.sp)};Panel{Label(if(fa)"راهنمای نمودار"else"READ THE CHART");Text(if(fa)"کندل سبز صعودی و قرمز نزولی است. دایره سبز/قرمز محل باز شدن لانگ/شورت و دایره توخالی محل بسته شدن معامله است. خط نقطه‌چین ورود تا خروج را وصل می‌کند."else"Green candles are bullish and red candles are bearish. Green/red circles mark Long/Short entries; hollow circles mark exits and the dashed line connects Entry to Exit.",color=MUTED,fontSize=11.sp)}}}

@Composable private fun CandleChart(candles:List<Candle>,trades:List<TradeResult>,fa:Boolean){var visible by remember(candles.size){mutableIntStateOf(min(80,candles.size))};var start by remember(candles.size){mutableIntStateOf(max(0,candles.size-visible))};val count=visible.coerceIn(5,max(5,candles.size));val first=start.coerceIn(0,max(0,candles.size-count));val shown=candles.subList(first,min(candles.size,first+count));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Text(if(fa)"کندل‌ها"else"CANDLES",color=MUTED,fontSize=9.sp);Row{TextButton({visible=min(candles.size,visible+15);start=max(0,candles.size-visible)}){Text("+",color=CYAN)};TextButton({visible=max(5,visible-15);start=max(0,candles.size-visible)}){Text("−",color=CYAN)}}};Box(Modifier.fillMaxWidth().height(315.dp).clip(MaterialTheme.shapes.medium).background(PANEL2).pointerInput(candles.size,visible){detectDragGestures{_,drag->start=(start-(drag.x/12f).toInt()).coerceIn(0,max(0,candles.size-count))}}){Canvas(Modifier.fillMaxSize().padding(7.dp)){if(shown.size<2)return@Canvas;val low=shown.minOf{it.low};val high=shown.maxOf{it.high};val range=(high-low).takeIf{it>0.0}?:1.0;val gap=size.width/shown.size;val body=max(2f,gap*.55f);fun y(v:Double)=size.height-((v-low)/range).toFloat()*size.height;shown.forEachIndexed{i,c->{val x=i*gap+gap/2f;val color=if(c.close>=c.open)GREEN else RED;drawLine(color,Offset(x,y(c.high)),Offset(x,y(c.low)),1.5f);drawRect(color,Offset(x-body/2f,y(max(c.open,c.close))),Size(body,max(2f,y(min(c.open,c.close))-y(max(c.open,c.close)))))};trades.forEach{t->{val ei=nearestIndex(shown,t.entryTime);val xi=nearestIndex(shown,t.exitTime);if(ei>=0){val color=if(t.side==Side.LONG)GREEN else RED;val x=ei*gap+gap/2f;val yy=y(t.entryPrice);drawCircle(color,6f,Offset(x,yy));drawCircle(PANEL2,3f,Offset(x,yy));if(xi>=0){val xx=xi*gap+gap/2f;val y2=y(t.exitPrice);drawLine(color.copy(alpha=.75f),Offset(x,yy),Offset(xx,y2),2f,pathEffect=PathEffect.dashPathEffect(floatArrayOf(7f,6f)));drawCircle(color,7f,Offset(xx,y2),style=Stroke(2f))}}}}}}};Text(if(fa)"Drag = جابه‌جایی • +/− = زوم"else"Drag = pan • +/− = zoom",color=MUTED,fontSize=9.sp)}
private fun nearestIndex(candles:List<Candle>,time:Long):Int{var best=-1;var distance=Long.MAX_VALUE;candles.forEachIndexed{i,c->{val d=abs(c.timestamp-time);if(d<distance){distance=d;best=i}}};return best}

@Composable private fun BacktestPage(s:BacktestUiState,vm:BacktestViewModel,market:String,tf:String,fa:Boolean,setMarket:(String)->Unit,setTf:(String)->Unit){ScrollColumn{Head(if(fa)"آزمایش بک‌تست"else"BACKTEST LAB",market,if(fa)"داده تاریخی واقعی CoinEx"else"Real CoinEx historical data");Panel{Label(if(fa)"بازار و تایم‌فریم"else"MARKET & TIMEFRAME");OutlinedTextField(value=market,onValueChange=setMarket,modifier=Modifier.fillMaxWidth(),singleLine=true,label={Text(if(fa)"نماد"else"Symbol")});Spacer(Modifier.height(8.dp));Row(Modifier.horizontalScroll(rememberScrollState())){listOf("5m","15m","30m","1h","4h","1d").forEach{x->TextButton({setTf(x);vm.setTimeframe(x)}){Text(x,color=if(tf==x)CYAN else MUTED,fontWeight=FontWeight.Bold)}}}};Panel{Label(if(fa)"پارامترها"else"ENGINE PARAMETERS");Line("INITIAL","1,000 USDT");Line("RISK","1.00%");Line("LEVERAGE","3x");Line("LWMA","20 / 50");Line("ATR","14");Line("SL / TP","1.5 / 3.0 ATR");Line("FUNDING","ON")};Button({vm.setMarket(market);vm.runBacktest()},enabled=!s.isRunning,modifier=Modifier.fillMaxWidth().height(55.dp)){if(s.isRunning)CircularProgressIndicator(Modifier.size(20.dp),color=TEXT,strokeWidth=2.dp)else Text(if(fa)"اجرای بک‌تست"else"RUN BACKTEST",fontWeight=FontWeight.Black)};Text(s.status,color=MUTED,fontSize=10.sp);s.error?.let{Panel{Label("ERROR");Text(it,color=RED,fontSize=11.sp)}};s.report?.let{Result(it,fa)}}}

@Composable private fun AnalysisPage(r:BacktestReport?,fa:Boolean){ScrollColumn{Head(if(fa)"تحلیل استراتژی"else"STRATEGY ANALYSIS","Offline-first",if(fa)"تشخیص ساختاری معاملات"else"Deterministic trade diagnosis");if(r==null)Panel{Text(if(fa)"ابتدا بک‌تست اجرا کنید."else"Run a backtest first.",color=MUTED)};r?.let{val a=TradeAiAnalyzer.analyze(it);Panel{Label(if(fa)"خلاصه"else"SUMMARY");Text(a.strategy.summary,color=TEXT,fontSize=12.sp)};BulletPanel(if(fa)"نقاط قوت"else"STRENGTHS",a.strategy.strengths,GREEN);BulletPanel(if(fa)"نقاط ضعف"else"WEAKNESSES",a.strategy.weaknesses,RED);BulletPanel(if(fa)"پیشنهادها"else"RECOMMENDATIONS",a.strategy.recommendations,CYAN)}}}}

@Composable private fun TradesPage(r:BacktestReport?,fa:Boolean){ScrollColumn{val trades=r?.trades.orEmpty().reversed();Head(if(fa)"معاملات"else"TRADE TAPE",trades.size.toString(),if(fa)"پوزیشن‌های اجراشده"else"Executed positions");if(trades.isEmpty())Panel{Text(if(fa)"هنوز معامله‌ای نیست."else"No trades yet.",color=MUTED)};trades.forEach{t->Panel{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(if(t.side==Side.LONG)"LONG"else"SHORT",color=if(t.side==Side.LONG)GREEN else RED,fontWeight=FontWeight.Black);Text(money(t.netPnl),color=if(t.netPnl>=0)GREEN else RED,fontWeight=FontWeight.Bold)};Text("${price(t.entryPrice)} → ${price(t.exitPrice)}",color=TEXT,fontSize=12.sp);Text("Qty ${price(t.quantity)} • Fee ${money(t.fees)} • Funding ${money(t.funding)}",color=MUTED,fontSize=9.sp)}}}}

@Composable private fun TopMarketsPage(s:BacktestUiState,vm:BacktestViewModel,fa:Boolean,select:(String)->Unit){ScrollColumn{Head(if(fa)"۱۰ ارز برتر"else"TOP 10 FUTURES","CoinEx","24h value ranking");Button({vm.loadTopMarkets()},enabled=!s.isLoadingMarkets,modifier=Modifier.fillMaxWidth()){Text(if(s.isLoadingMarkets)"Loading…"else if(fa)"به‌روزرسانی"else"REFRESH TOP 10")};s.topMarkets.forEach{m->Panel{Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(m.market,color=TEXT,fontWeight=FontWeight.Bold);Text("24h value ${money(m.value24h)}",color=MUTED,fontSize=9.sp)};Column(horizontalAlignment=Alignment.End){Text(price(m.last),color=TEXT,fontSize=11.sp);Text("${if(m.change24h>=0)"+"else""}${"%.2f".format(m.change24h)}%",color=if(m.change24h>=0)GREEN else RED,fontWeight=FontWeight.Bold)};TextButton({select(m.market)}){Text(if(fa)"انتخاب"else"SELECT",color=CYAN,fontSize=9.sp)}}}}};if(s.topMarkets.isEmpty()&&!s.isLoadingMarkets)Panel{Text(if(fa)"داده‌ای دریافت نشد؛ اتصال اینترنت و API را بررسی کنید."else"No market data returned.",color=MUTED)}}}

@Composable private fun SettingsPage(fa:Boolean){ScrollColumn{Head(if(fa)"تنظیمات"else"SETTINGS","HANNAH",if(fa)"ترمینال حرفه‌ای بک‌تست فیوچرز"else"Professional futures backtesting terminal");Panel{Label("HANNAH");Line("EXCHANGE","CoinEx Futures");Line("DATA",if(fa)"داده واقعی"else"Real historical data");Line("LANGUAGE","English / فارسی");Line("CHART","OHLC + trade markers");Line("ANALYSIS","Offline trade diagnosis")}}}

@Composable private fun Metrics(r:BacktestReport?,fa:Boolean){Column(verticalArrangement=Arrangement.spacedBy(6.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)){Stat(if(fa)"سرمایه"else"EQUITY",r?.let{money(it.finalBalance)}?:"—",Modifier.weight(1f));Stat(if(fa)"سود"else"PNL",r?.let{money(it.netPnl)}?:"—",Modifier.weight(1f))};Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)){Stat("ROI",r?.let{"%.2f%%".format(it.roiPercent)}?:"—",Modifier.weight(1f));Stat(if(fa)"افت"else"MAX DD",r?.let{"%.2f%%".format(it.maxDrawdownPercent)}?:"—",Modifier.weight(1f))}}}
@Composable private fun Stat(label:String,value:String,modifier:Modifier){Card(modifier,colors=CardDefaults.cardColors(containerColor=PANEL)){Column(Modifier.padding(11.dp)){Text(label,color=MUTED,fontSize=8.sp,fontWeight=FontWeight.Bold);Text(value,color=TEXT,fontSize=16.sp,fontWeight=FontWeight.Bold)}}}
@Composable private fun Result(r:BacktestReport,fa:Boolean){Panel{Label(if(fa)"نتیجه"else"RESULT");Line("FINAL",money(r.finalBalance));Line("PNL",money(r.netPnl));Line("WIN RATE","%.1f%%".format(r.winRatePercent));Line("MAX DD","%.2f%%".format(r.maxDrawdownPercent));Line("PROFIT FACTOR",if(r.profitFactor.isInfinite())"∞"else"%.2f".format(r.profitFactor));Line("FEES",money(r.totalFees));Line("FUNDING",money(r.totalFunding))}}
@Composable private fun Equity(values:List<Double>){Canvas(Modifier.fillMaxWidth().height(170.dp).clip(MaterialTheme.shapes.medium).background(PANEL2).padding(7.dp)){if(values.size<2)return@Canvas;val low=values.minOrNull()?:return@Canvas;val high=values.maxOrNull()?:return@Canvas;val range=(high-low).takeIf{it>0}?:1.0;val path=androidx.compose.ui.graphics.Path();values.forEachIndexed{i,v->{val x=i.toFloat()/values.lastIndex*size.width;val y=size.height-((v-low)/range).toFloat()*size.height;if(i==0)path.moveTo(x,y)else path.lineTo(x,y)}};drawPath(path,CYAN,style=Stroke(width=3f,cap=StrokeCap.Round))}}
@Composable private fun BulletPanel(title:String,items:List<String>,color:Color){Panel{Label(title);items.forEach{Text("• $it",color=color,fontSize=11.sp,modifier=Modifier.padding(top=5.dp))}}}
@Composable private fun Panel(content:@Composable ColumnScope.()->Unit){Card(Modifier.fillMaxWidth(),colors=CardDefaults.cardColors(containerColor=PANEL)){Column(Modifier.padding(13.dp),content=content)}}
@Composable private fun Label(text:String){Text(text,color=MUTED,fontSize=9.sp,fontWeight=FontWeight.Bold,letterSpacing=1.sp)}
@Composable private fun Line(a:String,b:String){Row(Modifier.fillMaxWidth().padding(vertical=4.dp),horizontalArrangement=Arrangement.SpaceBetween){Text(a,color=MUTED,fontSize=10.sp);Text(b,color=TEXT,fontSize=10.sp,fontWeight=FontWeight.SemiBold)}}
@Composable private fun Head(title:String,main:String,subtitle:String){Column(Modifier.padding(vertical=3.dp)){Text(title,color=CYAN,fontSize=9.sp,fontWeight=FontWeight.Bold);Text(main,color=TEXT,fontSize=25.sp,fontWeight=FontWeight.Black);Text(subtitle,color=MUTED,fontSize=10.sp)}}
@Composable private fun Empty(text:String,height:Int=170){Box(Modifier.fillMaxWidth().height(height.dp).clip(MaterialTheme.shapes.medium).background(PANEL2),contentAlignment=Alignment.Center){Text(text,color=MUTED,fontSize=10.sp,textAlign=TextAlign.Center,modifier=Modifier.padding(18.dp))}}
@Composable private fun ScrollColumn(content:@Composable ColumnScope.()->Unit){Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp),verticalArrangement=Arrangement.spacedBy(9.dp),content=content)}
private fun money(v:Double)=if(v.isFinite())"%,.2f".format(v)else"∞"
private fun price(v:Double)=if(v.isFinite())"%,.4f".format(v)else"∞"
