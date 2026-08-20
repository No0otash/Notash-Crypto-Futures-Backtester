package com.notash.cryptobacktester

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

data class Candle(
    val time: Long, val open: Double, val high: Double,
    val low: Double, val close: Double, val volume: Double
)

data class BacktestSettings(
    val initialBalance: Double = 1000.0,
    val riskPercent: Double = 1.0,
    val leverage: Double = 10.0,
    val fastLwma: Int = 20,
    val slowLwma: Int = 50,
    val atrPeriod: Int = 14,
    val entryOffsetAtr: Double = .5,
    val slAtr: Double = 1.5,
    val tpAtr: Double = 3.0,
    val makerFee: Double = .0002,
    val takerFee: Double = .0005,
    val slippage: Double = .0001
)

data class TradeResult(
    val side: String,
    val entry: Double,
    val exit: Double,
    val qty: Double,
    val pnl: Double,
    val funding: Double,
    val fees: Double,
    val reason: String
)

data class BacktestReport(
    val endingBalance: Double,
    val pnl: Double,
    val roi: Double,
    val trades: List<TradeResult>,
    val maxDrawdown: Double
)

object Indicators {
    fun lwma(values: List<Double>, period: Int): DoubleArray {
        val out = DoubleArray(values.size) { Double.NaN }
        val weight = period * (period + 1) / 2.0
        for (i in period - 1 until values.size) {
            var sum = 0.0
            for (j in 0 until period) sum += values[i - j] * (period - j)
            out[i] = sum / weight
        }
        return out
    }

    fun atr(c: List<Candle>, period: Int): DoubleArray {
        val tr = DoubleArray(c.size)
        for (i in c.indices) {
            tr[i] = if (i == 0) c[i].high - c[i].low
            else maxOf(c[i].high-c[i].low,
                abs(c[i].high-c[i-1].close),
                abs(c[i].low-c[i-1].close))
        }
        val out = DoubleArray(c.size) { Double.NaN }
        var sum = 0.0
        for (i in c.indices) {
            sum += tr[i]
            if (i >= period) sum -= tr[i-period]
            if (i >= period-1) out[i] = sum / period
        }
        return out
    }
}

class CryptoBacktestEngine {
    /*
     * Crypto-native engine:
     * - No MetaTrader/MQL5 dependency.
     * - USDT notional and quantity.
     * - Limit-entry simulation.
     * - Maker fee on filled limit entry.
     * - Taker fee on SL/TP exit (configurable).
     * - Funding can be supplied as historical events.
     *
     * HTF candles must be aligned to LTF timestamps by the data adapter.
     */
    fun run(
        candles: List<Candle>,
        htfCandles: List<Candle>,
        fundingEvents: Map<Long, Double>,
        s: BacktestSettings
    ): BacktestReport {
        if (candles.size < maxOf(s.slowLwma, s.atrPeriod) + 5 ||
            htfCandles.size < s.slowLwma + 5) {
            return BacktestReport(s.initialBalance, 0.0, 0.0, emptyList(), 0.0)
        }

        var balance = s.initialBalance
        var peak = balance
        var maxDd = 0.0
        var position: TradeState? = null
        var pending: Pending? = null
        val results = mutableListOf<TradeResult>()

        val ltfClose = candles.map { it.close }
        val htfClose = htfCandles.map { it.close }
        val lwmaLtf = Indicators.lwma(ltfClose, s.fastLwma)
        val atr = Indicators.atr(candles, s.atrPeriod)
        val lwmaFastH = Indicators.lwma(htfClose, s.fastLwma)
        val lwmaSlowH = Indicators.lwma(htfClose, s.slowLwma)

        fun htfIndex(time: Long): Int {
            var lo = 0; var hi = htfCandles.lastIndex; var ans = -1
            while (lo <= hi) {
                val m = (lo + hi) ushr 1
                if (htfCandles[m].time <= time) { ans = m; lo = m + 1 }
                else hi = m - 1
            }
            return ans
        }

        for (i in 1 until candles.size) {
            val c = candles[i]

            // Existing position: conservative same-candle rule = SL first.
            position?.let { p ->
                var exit: Double? = null
                var reason = ""
                if (p.side == "LONG") {
                    if (c.low <= p.sl) { exit = p.sl; reason = "SL" }
                    else if (c.high >= p.tp) { exit = p.tp; reason = "TP" }
                } else {
                    if (c.high >= p.sl) { exit = p.sl; reason = "SL" }
                    else if (c.low <= p.tp) { exit = p.tp; reason = "TP" }
                }
                val funding = fundingEvents.entries
                    .filter { it.key > p.openTime && it.key <= c.time }
                    .sumOf { rate ->
                        val sign = if (p.side == "LONG") -1.0 else 1.0
                        sign * p.qty * c.close * rate.value
                    }
                balance += funding
                if (exit != null) {
                    val gross = if (p.side == "LONG")
                        (exit!! - p.entry) * p.qty
                    else (p.entry - exit!!) * p.qty
                    val fee = exit!! * p.qty * s.takerFee + p.entry * p.qty * s.makerFee
                    val net = gross + funding - fee
                    balance += gross - fee
                    results += TradeResult(p.side,p.entry,exit!!,p.qty,net,funding,fee,reason)
                    position = null
                    peak = max(peak,balance)
                    maxDd = max(maxDd,(peak-balance)/peak*100.0)
                }
            }

            if (position == null && pending == null && i > s.slowLwma) {
                val hi = htfIndex(c.time)
                if (hi >= s.slowLwma && !lwmaLtf[i].isNaN() && !atr[i].isNaN()) {
                    val bull = lwmaFastH[hi] > lwmaSlowH[hi]
                    val bear = lwmaFastH[hi] < lwmaSlowH[hi]
                    if (bull) pending = Pending("LONG", lwmaLtf[i] - atr[i]*s.entryOffsetAtr, atr[i], c.time)
                    if (bear) pending = Pending("SHORT", lwmaLtf[i] + atr[i]*s.entryOffsetAtr, atr[i], c.time)
                }
            }

            pending?.let { q ->
                val touched = if (q.side == "LONG") c.low <= q.price else c.high >= q.price
                if (touched) {
                    val slDist = q.atr*s.slAtr
                    val riskCash = balance*s.riskPercent/100.0
                    val qty = if (slDist > 0) riskCash/slDist else 0.0
                    val maxNotionalQty = if (q.price > 0) balance*s.leverage/q.price else 0.0
                    val finalQty = min(qty,maxNotionalQty)
                    val sl = if(q.side=="LONG") q.price-slDist else q.price+slDist
                    val tp = if(q.side=="LONG") q.price+q.atr*s.tpAtr else q.price-q.atr*s.tpAtr
                    position = TradeState(q.side,q.price,sl,tp,finalQty,c.time)
                    pending = null
                }
            }
        }

        return BacktestReport(balance,balance-s.initialBalance,
            (balance/s.initialBalance-1)*100.0,results,maxDd)
    }
}

data class Pending(val side:String,val price:Double,val atr:Double,val created:Long)
data class TradeState(val side:String,val entry:Double,val sl:Double,val tp:Double,val qty:Double,val openTime:Long)

@Composable
fun App() {
    var market by remember { mutableStateOf("BTCUSDT") }
    var initial by remember { mutableStateOf("1000") }
    var risk by remember { mutableStateOf("1") }
    var leverage by remember { mutableStateOf("10") }
    var ltf by remember { mutableStateOf("15min") }
    var htf by remember { mutableStateOf("1hour") }
    var status by remember { mutableStateOf("READY") }
    var report by remember { mutableStateOf<BacktestReport?>(null) }

    val bg = Color(0xFF070A12)
    val panel = Color(0xFF101728)
    val purple = Color(0xFF805BFF)
    val cyan = Color(0xFF00D9FF)
    val green = Color(0xFF20E6A5)
    val red = Color(0xFFFF5C7A)

    MaterialTheme(colorScheme = darkColorScheme(
        primary=purple, secondary=cyan, background=bg, surface=panel
    )) {
        Box(Modifier.fillMaxSize().background(bg)) {
            LazyColumn(
                Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement=Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("NOTASH // CRYPTO LAB", color=cyan, fontSize=12.sp, fontWeight=FontWeight.Bold)
                    Text("Futures Backtester", color=Color.White, fontSize=30.sp, fontWeight=FontWeight.Bold)
                    Text("Multi-Exchange • Crypto Native", color=Color.Gray)
                }
                item {
                    Card(colors=CardDefaults.cardColors(containerColor=panel)) {
                        Column(Modifier.padding(16.dp), verticalArrangement=Arrangement.spacedBy(9.dp)) {
                            Text("MARKET CONFIG", color=cyan, fontWeight=FontWeight.Bold)
                            OutlinedTextField(market,{market=it},label={Text("Market")},singleLine=true)
                            OutlinedTextField(ltf,{ltf=it},label={Text("LTF")},singleLine=true)
                            OutlinedTextField(htf,{htf=it},label={Text("HTF")},singleLine=true)
                            OutlinedTextField(initial,{initial=it},label={Text("Initial Balance USDT")},singleLine=true)
                            OutlinedTextField(risk,{risk=it},label={Text("Risk %")},singleLine=true)
                            OutlinedTextField(leverage,{leverage=it},label={Text("Leverage")},singleLine=true)
                            Button(
                                onClick={status="BACKTEST ENGINE READY — DATA ADAPTER PENDING"},
                                modifier=Modifier.fillMaxWidth()
                            ){Text("🚀 RUN BACKTEST")}
                        }
                    }
                }
                item {
                    Card(colors=CardDefaults.cardColors(containerColor=panel)) {
                        Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                            Text("STRATEGY",color=cyan,fontWeight=FontWeight.Bold)
                            Text("HTF LWMA 20/50  •  LTF LWMA 20  •  ATR 14")
                            Text("Limit Entry 0.5 ATR  •  SL 1.5 ATR  •  TP 3 ATR")
                            Text("Risk-based quantity  •  Leverage cap  •  Funding-aware")
                        }
                    }
                }
                item {
                    Card(colors=CardDefaults.cardColors(containerColor=panel)) {
                        Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                            Text("PERFORMANCE",color=cyan,fontWeight=FontWeight.Bold)
                            Text(status,color=Color.LightGray)
                            report?.let { r ->
                                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
                                    Metric("PNL", "%.2f".format(r.pnl), if(r.pnl>=0)green else red)
                                    Metric("ROI", "%.2f%%".format(r.roi), if(r.roi>=0)green else red)
                                    Metric("TRADES", "${r.trades.size}", Color.White)
                                    Metric("MAX DD", "%.2f%%".format(r.maxDrawdown), red)
                                }
                            }
                        }
                    }
                }
                item {
                    Card(colors=CardDefaults.cardColors(containerColor=panel)) {
                        Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(7.dp)) {
                            Text("EXCHANGE ADAPTERS",color=cyan,fontWeight=FontWeight.Bold)
                            Text("● CoinEx Futures     READY")
                            Text("○ Binance Futures    PLANNED")
                            Text("○ Bybit Futures      PLANNED")
                            Text("○ Bitget Futures     PLANNED")
                        }
                    }
                }
                item {
                    Text(
                        "Crypto-native: no MetaTrader, no MQL5, no broker lot/tick-value assumptions.",
                        color=Color.Gray, fontSize=12.sp
                    )
                }
            }
        }
    }
}

@Composable fun Metric(label:String,value:String,color:Color){
    Column(horizontalAlignment=Alignment.CenterHorizontally) {
        Text(label,color=Color.Gray,fontSize=10.sp)
        Text(value,color=color,fontSize=16.sp,fontWeight=FontWeight.Bold)
    }
}
