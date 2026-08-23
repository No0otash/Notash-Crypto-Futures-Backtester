package com.notash.cryptobacktester.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.notash.cryptobacktester.core.BacktestReport
import com.notash.cryptobacktester.core.Side

private object H {
    val bg=Color(0xFF05070D); val panel=Color(0xFF0B1020); val panel2=Color(0xFF121A2D)
    val purple=Color(0xFF8B5CF6); val cyan=Color(0xFF22D3EE); val green=Color(0xFF20E6A5)
    val red=Color(0xFFFF5577); val amber=Color(0xFFFFB84D); val text=Color(0xFFF5F7FF); val muted=Color(0xFF8490A7)
}

@Composable
fun ProfessionalTerminal() {
    val vm: BacktestViewModel=viewModel(); val state by vm.state.collectAsState()
    var tab by remember{mutableIntStateOf(0)}; var market by remember{mutableStateOf(state.market)}; var tf by remember{mutableStateOf(state.timeframe)}
    MaterialTheme(colorScheme=androidx.compose.material3.darkColorScheme(primary=H.purple,secondary=H.cyan,background=H.bg,surface=H.panel)){
        Scaffold(containerColor=H.bg,bottomBar={Navigation(tab){tab=it}}){pad->
            Column(Modifier.fillMaxSize().padding(pad)){ Header(state); AnimatedContent(targetState=tab,label="nav"){when(it){
                0->Overview(state.report,state.market); 1->Charts(state.report); 2->Backtest(state,vm,market,tf,{market=it},{tf=it}); else->Trades(state.report)
            }}}
        }
    }
}

@Composable private fun Header(state:BacktestUiState){
    Row(Modifier.fillMaxWidth().padding(16.dp,14.dp,16.dp,4.dp),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){
        Column{Text("HANNAH",color=H.text,fontSize=25.sp,fontWeight=FontWeight.Black);Text("FUTURES INTELLIGENCE",color=H.cyan,fontSize=9.sp,fontWeight=FontWeight.Bold)}
        Surface(color=(if(state.isRunning)H.cyan else H.green).copy(alpha=.12f),shape=RoundedCornerShape(50)){Text(if(state.isRunning)"● RUNNING"else"● READY",color=if(state.isRunning)H.cyan else H.green,fontSize=10.sp,fontWeight=FontWeight.Bold,modifier=Modifier.padding(11.dp,7.dp))}
    }
    Text(state.status.uppercase(),Modifier.padding(0.dp,0.dp,16.dp,9.dp).fillMaxWidth(),color=H.muted,fontSize=9.sp,textAlign=androidx.compose.ui.text.style.TextAlign.End)
}

@Composable private fun Overview(report:BacktestReport?,market:String){
    LazyColumn(Modifier.fillMaxSize().padding(horizontal=16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
        item{Text("MARKET OVERVIEW",color=H.cyan,fontSize=10.sp,fontWeight=FontWeight.Bold);Text(market,color=H.text,fontSize=29.sp,fontWeight=FontWeight.Black);Text("CoinEx Futures  •  Crypto Native",color=H.muted,fontSize=11.sp)}
        item{Metrics(report)}
        item{CardBox{Title("EQUITY CURVE");Spacer(Modifier.height(9.dp));if(report?.equityCurve?.size?:0>1)EquityChart(report!!.equityCurve)else Empty("Run a backtest to generate the curve")}}
        item{CardBox{Title("STRATEGY CORE");Spacer(Modifier.height(5.dp));Line("TREND","HTF LWMA 20 / 50");Line("ENTRY","LTF LWMA + 0.5 ATR");Line("RISK","1.0% per trade");Line("EXIT","SL 1.5 ATR  •  TP 3 ATR");Line("COSTS","Maker + Taker + Slippage");Line("FUNDING","Historical funding enabled")}}
    }
}

@Composable private fun Metrics(r:BacktestReport?){Column(verticalArrangement=Arrangement.spacedBy(8.dp)){
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){Stat("EQUITY",r?.let{money(it.finalBalance)}?:"—",0,Modifier.weight(1f));Stat("NET PNL",r?.let{money(it.netPnl)}?:"—",if(r!=null&&r.netPnl<0)2 else 1,Modifier.weight(1f))}
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){Stat("ROI",r?.let{"%.2f%%".format(it.roiPercent)}?:"—",if(r!=null&&r.roiPercent<0)2 else 1,Modifier.weight(1f));Stat("MAX DD",r?.let{"%.2f%%".format(it.maxDrawdownPercent)}?:"—",2,Modifier.weight(1f))}
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){Stat("WIN RATE",r?.let{"%.1f%%".format(it.winRatePercent)}?:"—",1,Modifier.weight(1f));Stat("TRADES",r?.trades?.size?.toString()?:"—",0,Modifier.weight(1f))}
}}

@Composable private fun Charts(r:BacktestReport?){LazyColumn(Modifier.fillMaxSize().padding(horizontal=16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
    item{Text("CHARTS",color=H.cyan,fontSize=10.sp,fontWeight=FontWeight.Bold);Text("Performance Trend",color=H.text,fontSize=28.sp,fontWeight=FontWeight.Black);Text("Equity and trade distribution",color=H.muted,fontSize=11.sp)}
    item{CardBox{Title("EQUITY / BALANCE");Spacer(Modifier.height(9.dp));if(r?.equityCurve?.size?:0>1)EquityChart(r!!.equityCurve,true)else Empty("No equity history yet",250)}}
    item{CardBox{Title("TRADE PNL");Spacer(Modifier.height(9.dp));if(r?.trades?.isNotEmpty()==true)TradeBars(r.trades.map{it.netPnl})else Empty("No trades yet",140)}}
}}

@Composable private fun Backtest(s:BacktestUiState,vm:BacktestViewModel,market:String,tf:String,setMarket:(String)->Unit,setTf:(String)->Unit){LazyColumn(Modifier.fillMaxSize().padding(horizontal=16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
    item{Text("BACKTEST LAB",color=H.cyan,fontSize=10.sp,fontWeight=FontWeight.Bold);Text("Build a test",color=H.text,fontSize=28.sp,fontWeight=FontWeight.Black);Text("Real CoinEx historical data • no artificial input-size cap",color=H.muted,fontSize=11.sp)}
    item{CardBox{Title("MARKET");Spacer(Modifier.height(8.dp));OutlinedTextField(value=market,onValueChange=setMarket,modifier=Modifier.fillMaxWidth(),singleLine=true,label={Text("Symbol")});Spacer(Modifier.height(8.dp));Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf("5m","15m","30m","1h","4h","1d").forEach{v->FilterChip(selected=tf==v,onClick={setTf(v);vm.setTimeframe(v)},label={Text(v)})}}}}
    item{CardBox{Title("ENGINE PARAMETERS");Line("INITIAL BALANCE","1,000 USDT");Line("RISK / TRADE","1.00%");Line("LEVERAGE","3x");Line("LWMA","20 / 50");Line("ATR","14");Line("SL / TP","1.5 / 3.0 ATR");Line("FUNDING","ON")}}
    item{Button(onClick={vm.setMarket(market);vm.runBacktest()},enabled=!s.isRunning,modifier=Modifier.fillMaxWidth().height(58.dp),shape=RoundedCornerShape(16.dp),colors=ButtonDefaults.buttonColors(containerColor=H.purple)){if(s.isRunning)CircularProgressIndicator(Modifier.size(22.dp),color=H.text,strokeWidth=2.dp)else Text("RUN BACKTEST",fontWeight=FontWeight.Black,letterSpacing=1.sp)}}
    s.error?.let{e->item{CardBox{Title("ENGINE ERROR");Text(e,color=H.red,fontSize=12.sp)}}};s.report?.let{r->item{Result(r)}}
}}

@Composable private fun Trades(r:BacktestReport?){val trades=r?.trades.orEmpty().reversed();LazyColumn(Modifier.fillMaxSize().padding(horizontal=16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
    item{Text("TRADE TAPE",color=H.cyan,fontSize=10.sp,fontWeight=FontWeight.Bold);Text("Executed positions",color=H.text,fontSize=28.sp,fontWeight=FontWeight.Black)}
    if(trades.isEmpty())item{CardBox{Text("No trades yet. Run a backtest first.",color=H.muted,fontSize=12.sp)}}
    items(trades){t->CardBox{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(if(t.side==Side.LONG)"LONG"else"SHORT",color=if(t.side==Side.LONG)H.green else H.red,fontWeight=FontWeight.Black);Text(money(t.netPnl)+" USDT",color=if(t.netPnl>=0)H.green else H.red,fontWeight=FontWeight.Bold)};Spacer(Modifier.height(6.dp));Text("${price(t.entryPrice)}  →  ${price(t.exitPrice)}",color=H.text,fontSize=13.sp);Text("Qty ${price(t.quantity)}  •  Fees ${money(t.fees)}  •  Funding ${money(t.funding)}",color=H.muted,fontSize=10.sp)}}
}}

@Composable private fun Result(r:BacktestReport){CardBox{Title("LATEST RESULT");Line("FINAL EQUITY",money(r.finalBalance));Line("NET PNL",money(r.netPnl));Line("WIN RATE","%.1f%%".format(r.winRatePercent));Line("MAX DD","%.2f%%".format(r.maxDrawdownPercent));Line("PROFIT FACTOR",if(r.profitFactor.isInfinite())"∞"else"%.2f".format(r.profitFactor));Line("FEES",money(r.totalFees));Line("FUNDING",money(r.totalFunding))}}

@Composable private fun CardBox(content:@Composable androidx.compose.foundation.layout.ColumnScope.()->Unit){Card(colors=CardDefaults.cardColors(containerColor=H.panel),shape=RoundedCornerShape(18.dp)){Column(Modifier.fillMaxWidth().padding(15.dp),content=content)}}
@Composable private fun Title(t:String){Text(t,color=H.muted,fontSize=10.sp,fontWeight=FontWeight.Bold,letterSpacing=1.2.sp)}
@Composable private fun Line(a:String,b:String){Row(Modifier.fillMaxWidth().padding(vertical=5.dp),horizontalArrangement=Arrangement.SpaceBetween){Text(a,color=H.muted,fontSize=11.sp);Text(b,color=H.text,fontSize=11.sp,fontWeight=FontWeight.SemiBold)}}
@Composable private fun Stat(title:String,value:String,tone:Int,modifier:Modifier){val c=when(tone){1->H.green;2->H.red;else->H.text};Card(modifier=modifier,colors=CardDefaults.cardColors(containerColor=H.panel),shape=RoundedCornerShape(16.dp)){Column(Modifier.padding(14.dp)){Text(title,color=H.muted,fontSize=9.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(4.dp));Text(value,color=c,fontSize=18.sp,fontWeight=FontWeight.Bold)}}}

@Composable private fun EquityChart(values:List<Double>,large:Boolean=false){val reveal by animateFloatAsState(1f,label="chart");Canvas(Modifier.fillMaxWidth().height(if(large)250.dp else 185.dp).clip(RoundedCornerShape(14.dp)).background(H.panel2).padding(8.dp)){if(values.size<2)return@Canvas;val min=values.minOrNull()?:return@Canvas;val max=values.maxOrNull()?:return@Canvas;val range=(max-min).takeIf{it>0}?:1.0;val p=Path();values.forEachIndexed{i,v->val x=i.toFloat()/values.lastIndex.coerceAtLeast(1)*size.width;val y=size.height-((v-min)/range).toFloat()*size.height;if(i==0)p.moveTo(x,y)else p.lineTo(x,y)};drawLine(H.cyan.copy(alpha=.15f),Offset(0f,size.height/2),Offset(size.width,size.height/2),1f);drawPath(p,H.cyan.copy(alpha=reveal),style=Stroke(4f,cap=StrokeCap.Round))}}
@Composable private fun TradeBars(values:List<Double>){Canvas(Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(14.dp)).background(H.panel2)){if(values.isEmpty())return@Canvas;val maxAbs=values.maxOf{ kotlin.math.abs(it)}.takeIf{it>0}?:1.0;val w=size.width/values.size;val zero=size.height/2;drawLine(H.cyan.copy(alpha=.15f),Offset(0f,zero),Offset(size.width,zero),1f);values.forEachIndexed{i,v->val h=(kotlin.math.abs(v)/maxAbs).toFloat()*size.height*.42f;drawRect(if(v>=0)H.green else H.red,Offset(i*w+w*.12f,if(v>=0)zero-h else zero),androidx.compose.ui.geometry.Size(w*.76f,h))}}}
@Composable private fun Empty(text:String,height:Int=185){Box(Modifier.fillMaxWidth().height(height.dp).clip(RoundedCornerShape(14.dp)).background(H.panel2),contentAlignment=Alignment.Center){Text(text,color=H.muted,fontSize=11.sp)}}
@Composable private fun Navigation(selected:Int,select:(Int)->Unit){Surface(color=H.panel){Row(Modifier.fillMaxWidth().padding(7.dp),horizontalArrangement=Arrangement.SpaceEvenly){listOf("OVERVIEW","CHART","BACKTEST","TRADES").forEachIndexed{i,t->TextButton(onClick={select(i)}){Text(t,color=if(selected==i)H.cyan else H.muted,fontSize=9.sp,fontWeight=FontWeight.Bold)}}}}}
private fun money(v:Double)=if(v.isFinite())"%,.2f".format(v)else"∞";private fun price(v:Double)=if(v.isFinite())"%,.4f".format(v)else"∞"
